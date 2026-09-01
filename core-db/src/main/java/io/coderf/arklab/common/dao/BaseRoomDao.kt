package io.coderf.arklab.common.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import io.coderf.arklab.common.bean.BaseDaoBean
import io.coderf.arklab.common.utils.log.LogUtil
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Room DAO 通用基类：封装 CRUD、动态条件查询、分页及 LiveData / Rx 多种返回类型。
 *
 * ## 快速接入
 * ```kotlin
 * abstract class YourDao : YourDaoRawQueryBridge() {
 *     override fun getTableName() = "YourEntity"  // 与 @Entity(tableName) 一致
 * }
 * ```
 * ```java
 * public abstract class PersonDao extends PersonDaoRawQueryBridge {
 *     @Override public String getTableName() { return "Person"; }
 * }
 * ```
 * ## RawQuery 观察实体（二选一）
 * 1. **推荐**：[@RoomObservedEntity][io.coderf.arklab.common.annotation.RoomObservedEntity] +
 *    `extends XxxDaoRawQueryBridge`（KSP 自动生成 observedEntities 正确的方法）；
 * 2. **兼容老项目**：`extends BaseRoomDao<T>()` 并手动 override 底部 `do*` 方法
 *    （含 [doCount]、[doExecute]）。
 * ## 上层配合
 * - 业务仓库继承 [io.coderf.arklab.common.repository.RoomRepositoryImpl]；
 * - 分页列表可配合 [io.coderf.arklab.common.datasource.RxRoomPagingSource]。
 *
 * @Dao
 * @RoomObservedEntity(YourEntity::class)  // 推荐：KSP 生成 XxxDaoRawQueryBridge
 * @Dao
 * @RoomObservedEntity(Person.class)
 * @param T 表实体类型
 * @see RoomSqlHelper
 * @see io.coderf.arklab.common.annotation.RoomObservedEntity
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
abstract class BaseRoomDao<T : Any> {

    /**
     * 当前 DAO 操作的表名。
     * 必须与 `@Entity(tableName = "...")` 一致；未指定 tableName 时一般为类名。
     * 仅允许字母、数字、下划线（与 [RoomSqlHelper.requireIdentifier] 一致）。
     */
    abstract fun getTableName(): String

    // ==================== Room 标准 CRUD（Rx Completable） ====================

    /** 插入单条；冲突策略 ABORT */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(obj: T): Completable

    /** 批量插入 */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(list: List<T>): Completable

    /** 批量插入（同步，返回 Room 生成实现） */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insertOnly(list: List<T>)

    /**
     * 插入或替换单条（冲突时 REPLACE）。
     * 与 [insert] 并存，默认插入仍为 ABORT。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    abstract fun upsert(obj: T): Completable

    /** 批量插入或替换（冲突时 REPLACE） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    abstract fun upsert(list: List<T>): Completable

    /** 批量插入或替换（同步） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    abstract fun upsertOnly(list: List<T>)

    /** 按主键删除单条 */
    @Delete
    @Transaction
    abstract fun delete(obj: T): Completable

    /** 按主键删除单条（同步，影响行数） */
    @Delete
    @Transaction
    abstract fun deleteOnly(obj: T): Int?

    /** 按主键批量删除（同步） */
    @Delete
    @Transaction
    abstract fun deleteOnly(list: List<T>): Int?

    /** 按主键更新单条 */
    @Update
    @Transaction
    abstract fun update(obj: T): Completable

    /** 按主键批量更新 */
    @Update
    @Transaction
    abstract fun update(obj: List<T>): Completable

    /** 批量更新（同步，影响行数） */
    @Update
    @Transaction
    abstract fun updateOnly(obj: List<T>): Int?

    // ==================== 动态删除（返回影响行数，推荐） ====================

    /**
     * 删除全表，返回影响行数（同步）。
     * 新代码优先使用本方法或 [deleteAllCount]；老 [deleteAll] Flowable 已标记废弃。
     */
    fun deleteAllRows(): Int {
        val query = RoomSqlHelper.delete(getTableName())
        logSql(query)
        return doExecute(query)
    }

    /** 删除全表，返回影响行数（Single） */
    fun deleteAllCount(): Single<Int> = Single.fromCallable { deleteAllRows() }

    /**
     * 按单列等值删除，返回影响行数（同步）。
     */
    fun deleteByParamsRows(column: String, value: Any): Int {
        RoomSqlHelper.requireIdentifier(column)
        val query = RoomSqlHelper.delete(getTableName(), " WHERE $column = ?", arrayOf(value))
        logSql(query)
        return doExecute(query)
    }

    /** 按单列等值删除，返回影响行数（Single） */
    fun deleteByParamsCount(column: String, value: Any): Single<Int> =
        Single.fromCallable { deleteByParamsRows(column, value) }

    /**
     * 按多列等值 / IN 条件 AND 删除，返回影响行数（同步）。
     * value 为 Collection 时生成 `IN (...)`。
     */
    fun deleteByParamsRows(params: Map<String, Any>): Int {
        val (where, args) = RoomSqlHelper.buildWhereClause(params)
        val query = RoomSqlHelper.delete(getTableName(), where, args)
        logSql(query)
        return doExecute(query)
    }

    /** 按多条件删除，返回影响行数（Single） */
    fun deleteByParamsCount(params: Map<String, Any>): Single<Int> =
        Single.fromCallable { deleteByParamsRows(params) }

    // ==================== 动态删除（Flowable，兼容老签名，已废弃） ====================

    /**
     * 删除全表数据。
     * 返回 [Flowable] 以兼容老项目；内部执行 DELETE 语句。
     *
     * @deprecated 请改用 [deleteAllRows] / [deleteAllCount]，语义为影响行数，避免 DELETE 走查询式 Flowable。
     */
    @Deprecated(
        message = "Use deleteAllRows() or deleteAllCount() instead",
        replaceWith = ReplaceWith("deleteAllCount()")
    )
    fun deleteAll(): Flowable<List<T>> {
        val query = RoomSqlHelper.delete(getTableName())
        logSql(query)
        return doQueryFlowable(query)
    }

    /**
     * 按单列等值删除。
     *
     * @param params 列名（会做标识符校验）
     * @param value 列值（占位符绑定）
     * @deprecated 请改用 [deleteByParamsRows] / [deleteByParamsCount]
     */
    @Deprecated(
        message = "Use deleteByParamsRows(column, value) or deleteByParamsCount(column, value)",
        replaceWith = ReplaceWith("deleteByParamsCount(params, value)")
    )
    fun deleteByParams(params: String, value: String): Flowable<List<T>> {
        RoomSqlHelper.requireIdentifier(params)
        val query = RoomSqlHelper.delete(getTableName(), " WHERE $params = ?", arrayOf(value))
        logSql(query)
        return doQueryFlowable(query)
    }

    /**
     * 按多列等值 AND 删除。
     *
     * @param params 列名 -> 值
     * @deprecated 请改用 [deleteByParamsRows] / [deleteByParamsCount]
     */
    @Deprecated(
        message = "Use deleteByParamsRows(params) or deleteByParamsCount(params)",
        replaceWith = ReplaceWith("deleteByParamsCount(params)")
    )
    fun deleteByParams(params: Map<String, Any>): Flowable<List<T>> {
        val (where, args) = RoomSqlHelper.buildWhereClause(params)
        val query = RoomSqlHelper.delete(getTableName(), where, args)
        logSql(query)
        return doQueryFlowable(query)
    }

    // ==================== count / exists ====================

    /** 全表行数（同步） */
    fun countAll(): Long {
        val query = RoomSqlHelper.count(getTableName())
        logSql(query)
        return doCount(query)
    }

    /** 全表行数（Single） */
    fun countAllSingle(): Single<Long> = Single.fromCallable { countAll() }

    /**
     * 按条件统计行数（同步）。
     * value 为 Collection 时支持 `IN`。
     */
    fun count(params: Map<String, Any>): Long {
        val (where, args) = RoomSqlHelper.buildWhereClause(params)
        val query = RoomSqlHelper.count(getTableName(), where, args)
        logSql(query)
        return doCount(query)
    }

    /** 按条件统计行数（Single） */
    fun countSingle(params: Map<String, Any>): Single<Long> =
        Single.fromCallable { count(params) }

    /** 全表是否有数据 */
    fun existsAny(): Boolean = countAll() > 0

    /** 按条件是否存在记录 */
    fun exists(params: Map<String, Any>): Boolean = count(params) > 0

    /** 按主键列是否存在 */
    fun existsById(primaryKey: String, id: Any): Boolean {
        RoomSqlHelper.requireIdentifier(primaryKey)
        return exists(mapOf(primaryKey to id))
    }

    // ==================== 动态查询（Rx） ====================

    /** 查询全表 */
    fun findAll(): Flowable<List<T>> {
        val query = RoomSqlHelper.query(getTableName())
        logSql(query)
        return doQueryFlowable(query)
    }

    /** 按列 `id` 查询单条（Long 主键） */
    fun findInfoById(id: Long): Single<T> {
        val query = RoomSqlHelper.selectByColumn(getTableName(), "id", id)
        logSql(query)
        return doFind(query)
    }

    /** 按列 `id` 查询单条（String 主键） */
    fun findInfoById(id: String): Single<T> {
        val query = RoomSqlHelper.selectByColumn(getTableName(), "id", id)
        logSql(query)
        return doFind(query)
    }

    /**
     * 按指定主键列查询单条。
     *
     * @param primaryKey 主键列名（非占位符，会校验后拼接）
     * @param id 主键值
     */
    fun findInfoById(primaryKey: String, id: Long): Single<T> {
        RoomSqlHelper.requireIdentifier(primaryKey)
        val query = RoomSqlHelper.selectByColumn(getTableName(), primaryKey, id)
        logSql(query)
        return doFind(query)
    }

    /** 按指定主键列查询单条（String 值） */
    fun findInfoById(primaryKey: String, id: String): Single<T> {
        RoomSqlHelper.requireIdentifier(primaryKey)
        val query = RoomSqlHelper.selectByColumn(getTableName(), primaryKey, id)
        logSql(query)
        return doFind(query)
    }

    /**
     * 按列 IN 查询列表（同步）。
     *
     * @param column 列名
     * @param values 值集合；空集合返回空列表
     */
    fun findByIn(column: String, values: Collection<*>): List<T> {
        if (values.isEmpty()) return emptyList()
        RoomSqlHelper.requireIdentifier(column)
        val (where, args) = RoomSqlHelper.buildWhereClause(mapOf(column to (values as Any)))
        val query = RoomSqlHelper.query(getTableName(), where, args)
        logSql(query)
        return doQueryList(query)
    }

    /**
     * 按列 IN 查询列表（Flowable）。
     */
    fun findByInFlowable(column: String, values: Collection<*>): Flowable<List<T>> {
        if (values.isEmpty()) return Flowable.just(emptyList())
        RoomSqlHelper.requireIdentifier(column)
        val (where, args) = RoomSqlHelper.buildWhereClause(mapOf(column to (values as Any)))
        val query = RoomSqlHelper.query(getTableName(), where, args)
        logSql(query)
        return doQueryFlowable(query)
    }

    /**
     * 分页查询；排序字段不强制 ASC/DESC（与历史行为一致）。
     *
     * @param params 等值条件（value 可为 Collection → IN）
     * @param orderBy 排序列，可为 null
     * @param limit 每页条数，默认 10
     * @param offset 偏移，默认 0
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        val query = buildPagedSelectQueryPlainOrder(params, null, null, orderBy, limit, offset)
        logSql(query)
        return doQueryFlowable(query)
    }

    /**
     * 同步分页：降序 + 可选多列 LIKE 关键字。
     * 供 [RxRoomPagingSource] 等同步加载使用。
     */
    fun findPageList(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = doQueryList(
        buildPagedSelectQuery(params, keywordsKey, keywords, orderBy, descending = true, limit, offset)
            .also { logSql(it) }
    )

    /** 同步分页：降序，无关键字 */
    fun findPageList(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = findPageList(params, null, null, orderBy, limit, offset)

    /** 同步分页：升序 */
    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = doQueryList(
        buildPagedSelectQuery(params, null, null, orderBy, descending = false, limit, offset)
            .also { logSql(it) }
    )

    /** 同步分页：升序 + 关键字 */
    fun findPageListAsc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = doQueryList(
        buildPagedSelectQuery(params, keywordsKey, keywords, orderBy, descending = false, limit, offset)
            .also { logSql(it) }
    )

    /** 分页查询 Flowable，降序 */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> = doQueryFlowable(
        buildPagedSelectQuery(params, null, null, orderBy, descending = true, limit, offset).also { logSql(it) }
    )

    /** 分页查询 Flowable，降序 + 关键字 */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> = doQueryFlowable(
        buildPagedSelectQuery(params, keywordsKey, keywords, orderBy, descending = true, limit, offset)
            .also { logSql(it) }
    )

    /** 分页查询 Flowable，升序 */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> = doQueryFlowable(
        buildPagedSelectQuery(params, null, null, orderBy, descending = false, limit, offset).also { logSql(it) }
    )

    // ==================== LiveData（页面观察，无 Repository Loading） ====================

    @Deprecated(
        message = "Use deleteAllRows() / deleteAllCount(); LiveData after DELETE is not recommended",
        replaceWith = ReplaceWith("deleteAllRows()")
    )
    fun deleteAllLiveData(): LiveData<List<T>> =
        doFindListLiveData(RoomSqlHelper.delete(getTableName()))

    @Deprecated(
        message = "Use deleteByParamsRows / deleteByParamsCount",
        replaceWith = ReplaceWith("deleteByParamsRows(params, value)")
    )
    fun deleteByParamsLiveData(params: String, value: String): LiveData<List<T>> {
        RoomSqlHelper.requireIdentifier(params)
        return doFindListLiveData(
            RoomSqlHelper.delete(getTableName(), " WHERE $params = ?", arrayOf(value))
        )
    }

    @Deprecated(
        message = "Use deleteByParamsRows / deleteByParamsCount",
        replaceWith = ReplaceWith("deleteByParamsRows(params)")
    )
    fun deleteByParamsLiveData(params: Map<String, Any>): LiveData<List<T>> {
        val (where, args) = RoomSqlHelper.buildWhereClause(params)
        return doFindListLiveData(RoomSqlHelper.delete(getTableName(), where, args))
    }

    fun findAllLiveData(): LiveData<List<T>> =
        doFindListLiveData(RoomSqlHelper.query(getTableName()))

    fun findInfoByIdLiveData(id: Long): LiveData<T> =
        doFindLiveData(RoomSqlHelper.selectByColumn(getTableName(), "id", id))

    fun findInfoByIdLiveData(id: String): LiveData<T> =
        doFindLiveData(RoomSqlHelper.selectByColumn(getTableName(), "id", id))

    fun doQueryByLimitLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> =
        doFindListLiveData(buildPagedSelectQueryPlainOrder(params, null, null, orderBy, limit, offset))

    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> =
        doFindListLiveData(buildPagedSelectQuery(params, null, null, orderBy, descending = true, limit, offset))

    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> =
        doFindListLiveData(
            buildPagedSelectQuery(params, keywordsKey, keywords, orderBy, descending = true, limit, offset)
        )

    fun doQueryByOrderAscLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> =
        doFindListLiveData(buildPagedSelectQuery(params, null, null, orderBy, descending = false, limit, offset))

    // ==================== 查询构建（子类可复用） ====================

    /**
     * 构建带 ASC/DESC 的分页 SELECT。
     * @param descending true 为 DESC
     */
    protected fun buildPagedSelectQuery(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        descending: Boolean,
        limit: Int,
        offset: Int
    ): SupportSQLiteQuery {
        val (where, whereArgs) = RoomSqlHelper.buildWhereClause(params)
        val (kwClause, kwArgs) = RoomSqlHelper.buildKeywordClause(keywordsKey, keywords)
        val fullWhere = RoomSqlHelper.appendKeywordToWhere(where, kwClause)
        val order = RoomSqlHelper.buildOrderClause(orderBy, descending)
        val allArgs = arrayOf(*whereArgs, *kwArgs)
        return RoomSqlHelper.query(getTableName(), fullWhere, allArgs, order, limit, offset)
    }

    /** 构建分页 SELECT，ORDER BY 不附加 ASC/DESC */
    protected fun buildPagedSelectQueryPlainOrder(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int,
        offset: Int
    ): SupportSQLiteQuery {
        val (where, whereArgs) = RoomSqlHelper.buildWhereClause(params)
        val (kwClause, kwArgs) = RoomSqlHelper.buildKeywordClause(keywordsKey, keywords)
        val fullWhere = RoomSqlHelper.appendKeywordToWhere(where, kwClause)
        val order = RoomSqlHelper.buildOrderClausePlain(orderBy)
        val allArgs = arrayOf(*whereArgs, *kwArgs)
        return RoomSqlHelper.query(getTableName(), fullWhere, allArgs, order, limit, offset)
    }

    private fun logSql(query: SupportSQLiteQuery) {
        LogUtil.logger("RoomDao", "sql:${query.sql}")
    }

    // ==================== RawQuery 执行入口（子类或 KSP Bridge 实现） ====================

    /**
     * 执行 RawQuery，返回单条 LiveData。
     * 未使用 [RoomObservedEntity] 时子类必须 override 并设置正确的 observedEntities。
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindLiveData(query: SupportSQLiteQuery): LiveData<T>

    /** 执行 RawQuery，返回列表 LiveData */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindListLiveData(query: SupportSQLiteQuery): LiveData<List<T>>

    /** 执行 RawQuery，返回单条 Single */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFind(query: SupportSQLiteQuery): Single<T>

    /** 执行 RawQuery，返回列表 Single */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindList(query: SupportSQLiteQuery): Single<List<T>>

    /** 执行 RawQuery，返回列表 Flowable（可观察表变更） */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryFlowable(query: SupportSQLiteQuery): Flowable<List<T>>

    /** 执行 RawQuery，同步返回列表 */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryList(query: SupportSQLiteQuery): List<T>

    /**
     * 执行 `SELECT COUNT(*)` 类 RawQuery，返回行数。
     * KSP Bridge 或子类需 override 并绑定正确 observedEntities。
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doCount(query: SupportSQLiteQuery): Long

    /**
     * 执行 DELETE / 写操作类 RawQuery，返回影响行数。
     * KSP Bridge 或子类需 override 并绑定正确 observedEntities。
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doExecute(query: SupportSQLiteQuery): Int
}
