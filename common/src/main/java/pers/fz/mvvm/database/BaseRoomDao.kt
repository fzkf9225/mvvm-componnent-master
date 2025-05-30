package pers.fz.mvvm.database

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.bean.BaseDaoBean
import pers.fz.mvvm.util.log.LogUtil

/**
 * created by fz on 2024/11/1 16:28
 * describe:
 */
abstract class BaseRoomDao<T : Any> {

    /**
     * 获取表名一般来说是 实体类名名字，可以用反射实现但是不建议
     */
    abstract fun getTableName(): String;

    /**
     * 添加单个对象
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(obj: T): Completable

    /**
     * 添加数组对象数据
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(vararg objs: T): Completable

    /**
     * 添加对象集合
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insert(list: List<T>): Completable

    /**
     * 添加数组对象数据
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insertOnly(vararg objs: T)
    /**
     * 添加数组对象数据
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Transaction
    abstract fun insertOnly(list: List<T>)

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    @Transaction
    abstract fun delete(obj: T): Completable

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    @Transaction
    abstract fun deleteOnly(obj: T): Int?

    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    @Transaction
    abstract fun deleteOnly(vararg obj: T): Int?
    /**
     * 根据对象中的主键删除（主键是自动增长的，无需手动赋值）
     */
    @Delete
    @Transaction
    abstract fun deleteOnly(list: List<T>): Int?
    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    @Update
    @Transaction
    abstract fun update(vararg obj: T): Completable

    /**
     * 根据对象中的主键更新（主键是自动增长的，无需手动赋值）
     */
    @Update
    @Transaction
    abstract fun updateOnly(vararg obj: T): Int?

    fun deleteAll(): Flowable<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()}")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    /**
     * [params] 列名
     * [value] 列的值
     */
    fun deleteByParams(params: String, value: String): Flowable<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $params='${value}'")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    /**
     * 根据多个参数删除数据
     */
    fun deleteByParams(params: Map<String, Any>): Flowable<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $conditions")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    fun findAll(): Flowable<List<T>> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()}")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    fun findInfoById(id: Long): Single<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFind(query)
    }

    fun findInfoById(id: String): Single<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFind(query)
    }

    fun findInfoById(primaryKey: String, id: Long): Single<T> {
        val query =
            SimpleSQLiteQuery(
                "select * from ${getTableName()} where ? = ?",
                arrayOf<Any>(primaryKey, id)
            )
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFind(query)
    }

    fun findInfoById(primaryKey: String, id: String): Single<T> {
        val query =
            SimpleSQLiteQuery(
                "select * from ${getTableName()} where ? = ?",
                arrayOf<Any>(primaryKey, id)
            )
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFind(query)
    }

    /**
     * 分页查询，支持传入多个字段，但必须要按照顺序传入
     * key = value，key = value 的形式，一一对应（可以使用 stringbuilder 去构造一下，这里就不演示了）
     */
    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY $orderBy "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order limit $limit offset $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageList(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        var conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        if (!keywordsKey.isNullOrEmpty()) {
            conditions =
                if (conditions.isBlank()) ("$conditions WHERE " + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                }) else ("$conditions and (" + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                } + ")")
        }
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy DESC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryList(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageList(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy DESC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryList(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY $orderBy ASC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryList(query)
    }

    /**
     * 降序分页查询
     */
    fun findPageListAsc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> {
        var conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        if (!keywordsKey.isNullOrEmpty()) {
            conditions =
                if (conditions.isBlank()) ("$conditions WHERE " + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                }) else ("$conditions and (" + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                } + ")")
        }
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY $orderBy ASC"
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryList(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy desc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        var conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        if (!keywordsKey.isNullOrEmpty()) {
            conditions =
                if (conditions.isBlank()) ("$conditions WHERE " + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                }) else ("$conditions and (" + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                } + ")")
        }
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy DESC "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy asc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions  $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doQueryFlowable(query)
    }

    fun deleteAllLiveData(): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()}")
        return doFindListLiveData(query)
    }

    /**
     * [params] 列名
     * [value] 列的值
     */
    fun deleteByParamsLiveData(params: String, value: String): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $params='${value}'")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    /**
     * 根据多个参数删除数据
     */
    fun deleteByParamsLiveData(params: Map<String, Any>): LiveData<List<T>> {
        val conditions = params.entries.joinToString(" and ") { entry ->
            if (entry.value is String) {
                "${entry.key}='${entry.value}'"
            } else {
                "${entry.key}=${entry.value}"
            }
        }
        val query = SimpleSQLiteQuery("delete from ${getTableName()} where $conditions")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    fun findAllLiveData(): LiveData<List<T>> {
        val query = SimpleSQLiteQuery("select * from ${getTableName()}")
        return doFindListLiveData(query)
    }

    fun findInfoByIdLiveData(id: Long): LiveData<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFindLiveData(query)
    }

    fun findInfoByIdLiveData(id: String): LiveData<T> {
        val query =
            SimpleSQLiteQuery("select * from ${getTableName()} where id = ?", arrayOf<Any>(id))
        return doFindLiveData(query)
    }

    /**
     * 分页查询，支持传入多个字段，但必须要按照顺序传入
     * key = value，key = value 的形式，一一对应（可以使用 stringbuilder 去构造一下，这里就不演示了）
     */
    fun doQueryByLimitLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else " ORDER BY $orderBy "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order limit $limit offset $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy desc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        var conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        if (!keywordsKey.isNullOrEmpty()) {
            conditions =
                if (conditions.isBlank()) ("$conditions WHERE " + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                }) else ("$conditions and (" + keywordsKey.joinToString(" or ") { entry ->
                    "$entry like '%${keywords ?: ""}%'"
                } + ")")
        }
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy DESC "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions $order  LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    /**
     * 降序分页查询
     */
    fun doQueryByOrderAscLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 10
    ): LiveData<List<T>> {
        val conditions =
            if (params.isEmpty()) "" else (" WHERE " + params.entries.joinToString(" and ") { entry ->
                if (entry.value is String) {
                    "${entry.key}='${entry.value}'"
                } else {
                    "${entry.key}=${entry.value}"
                }
            })
        val order = if (orderBy.isNullOrEmpty()) "" else "ORDER BY $orderBy asc "
        val query =
            SimpleSQLiteQuery("SELECT * FROM ${getTableName()} $conditions  $order LIMIT $limit OFFSET $offset")
        LogUtil.show(ApiRetrofit.TAG, "sql:${query.sql}")
        return doFindListLiveData(query)
    }

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindLiveData(query: SupportSQLiteQuery): LiveData<T>

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindListLiveData(query: SupportSQLiteQuery): LiveData<List<T>>

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFind(query: SupportSQLiteQuery): Single<T>
    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doFindList(query: SupportSQLiteQuery): Single<List<T>>
    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryFlowable(query: SupportSQLiteQuery): Flowable<List<T>>

    /**
     * 占位方法如果你需要调用这个方法最好是重写一下BaseDaoBean修改为你的实体表，不写observedEntities会无法编译
     */
    @RawQuery(observedEntities = [BaseDaoBean::class])
    protected abstract fun doQueryList(query: SupportSQLiteQuery): List<T>

}