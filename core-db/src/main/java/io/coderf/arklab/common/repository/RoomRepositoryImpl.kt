package io.coderf.arklab.common.repository

import androidx.lifecycle.LiveData
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Room 本地数据仓库基类，在 [BaseRoomDao] 之上封装线程调度、超时与请求 UI。
 *
 * ## 设计说明
 * - **RxJava3** 为主：Completable / Single / Flowable 与老项目一致；
 * - **LiveData** 直接透传 Dao，不加 Loading（由页面 observe 处理）；
 * - **Loading** 仅通过 [getRequestUi]（[io.coderf.arklab.core.request.RequestUi]），
 *   由 [io.coderf.arklab.common.base.BaseViewModel] 注入，对齐 [RepositoryImpl]；
 * - **Flow / 协程**：见 [RoomRepositoryFlowExt]、[RoomRepositoryCoroutineExt]。
 * - **日志**：`Config.enableDebug(true)` 后由 [io.coderf.arklab.core.db.RoomLog] 打印操作/耗时/结果，
 *   数据库 Builder 再套 [io.coderf.arklab.core.db.RoomLog.attachTo] 可打印全部 SQL。
 *
 * ## 用法示例
 * ```kotlin
 * class PersonRepository(dao: PersonDao) :
 *     RoomRepositoryImpl<Person, PersonDao>(dao)
 *
 * // ViewModel 中
 * repository.insert(person, showLoading = true)
 *     .subscribe({ }, { getRequestUi()?.showError(io.coderf.arklab.core.request.AppError.from(it)) })
 *
 * // 或使用 Options
 * repository.findAll(RoomRequestOptions.withLoading("加载列表..."))
 * ```
 *
 * @param T 实体类型
 * @param DB 继承 [BaseRoomDao] 的 Dao
 * @see RoomRequestOptions
 * @see RxRoomPagingSource
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/21
 */
open class RoomRepositoryImpl<T : Any, DB : BaseRoomDao<T>>(
    private val roomDao: DB
) : BaseRepository() {

    /**
     * 是否启用加载框动态省略号（全局默认）。
     * 新代码请使用 [RoomRequestOptions.Builder.setEnableDynamicEllipsis]。
     */
    @Deprecated("Use RoomRequestOptions.Builder.setEnableDynamicEllipsis")
    var enableDynamicEllipsis: Boolean = false
        set(value) {
            field = value
            defaultOptionsEllipsis = value
        }

    private var defaultOptionsEllipsis: Boolean = false

    /** 获取底层 Dao，便于子类增加自定义 @Query 方法 */
    fun getRoomDao() = roomDao

    // ==================== 写操作 ====================

    fun insert(obj: T, showLoading: Boolean = false): Completable =
        insert(obj, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun insert(obj: T, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.insert(obj), options, "INSERT", obj)

    fun insert(objs: List<T>, showLoading: Boolean = false): Completable =
        insert(objs, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun insert(objs: List<T>, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.insert(objs.toList()), options, "INSERT_LIST", objs)

    /** 插入或替换单条（冲突 REPLACE） */
    fun upsert(obj: T, showLoading: Boolean = false): Completable =
        upsert(obj, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun upsert(obj: T, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.upsert(obj), options, "UPSERT", obj)

    fun upsert(objs: List<T>, showLoading: Boolean = false): Completable =
        upsert(objs, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun upsert(objs: List<T>, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.upsert(objs.toList()), options, "UPSERT_LIST", objs)

    fun delete(obj: T, showLoading: Boolean = false): Completable =
        delete(obj, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun delete(obj: T, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.delete(obj), options, "DELETE", obj)

    fun update(obj: T, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: T, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.update(obj), options, "UPDATE", obj)

    fun update(obj: List<T>, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: List<T>, options: RoomRequestOptions): Completable =
        applyWrite(roomDao.update(obj), options, "UPDATE_LIST", obj)

    // ==================== 条件删除（返回影响行数，推荐） ====================

    fun deleteAllCount(showLoading: Boolean = false): Single<Int> =
        deleteAllCount(RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteAllCount(options: RoomRequestOptions): Single<Int> =
        applyRead(roomDao.deleteAllCount(), options, "DELETE_ALL")

    fun deleteByParamsCount(column: String, value: Any, showLoading: Boolean = false): Single<Int> =
        deleteByParamsCount(column, value, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParamsCount(column: String, value: Any, options: RoomRequestOptions): Single<Int> =
        applyRead(roomDao.deleteByParamsCount(column, value), options, "DELETE_BY_PARAMS", mapOf(column to value))

    fun deleteByParamsCount(params: Map<String, Any>, showLoading: Boolean = false): Single<Int> =
        deleteByParamsCount(params, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParamsCount(params: Map<String, Any>, options: RoomRequestOptions): Single<Int> =
        applyRead(roomDao.deleteByParamsCount(params), options, "DELETE_BY_PARAMS", params)

    // ==================== 条件删除（Flowable，兼容老签名，已废弃） ====================

    @Deprecated("Use deleteAllCount()", ReplaceWith("deleteAllCount(showLoading)"))
    fun deleteAll(showLoading: Boolean = false): Flowable<List<T>> =
        deleteAll(RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteAllCount(options)")
    fun deleteAll(options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        applyDeleteList(roomDao.deleteAll(), options, "DELETE_ALL")

    @Deprecated("Use deleteByParamsCount(column, value, showLoading)")
    fun deleteByParams(params: String, value: String, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, value, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteByParamsCount(column, value, options)")
    fun deleteByParams(params: String, value: String, options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        applyDeleteList(roomDao.deleteByParams(params, value), options, "DELETE_BY_PARAMS", mapOf(params to value))

    @Deprecated("Use deleteByParamsCount(params, showLoading)")
    fun deleteByParams(params: Map<String, Any>, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteByParamsCount(params, options)")
    fun deleteByParams(params: Map<String, Any>, options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        applyDeleteList(roomDao.deleteByParams(params), options, "DELETE_BY_PARAMS", params)

    // ==================== count / exists ====================

    fun countAll(showLoading: Boolean = false): Single<Long> =
        countAll(RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun countAll(options: RoomRequestOptions): Single<Long> =
        applyRead(roomDao.countAllSingle(), options, "COUNT_ALL")

    fun count(params: Map<String, Any>, showLoading: Boolean = false): Single<Long> =
        count(params, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun count(params: Map<String, Any>, options: RoomRequestOptions): Single<Long> =
        applyRead(roomDao.countSingle(params), options, "COUNT", params)

    fun countAllSync(): Long = roomDao.countAll()

    fun countSync(params: Map<String, Any>): Long = roomDao.count(params)

    fun existsAny(): Boolean = roomDao.existsAny()

    fun exists(params: Map<String, Any>): Boolean = roomDao.exists(params)

    fun existsById(primaryKey: String, id: Any): Boolean = roomDao.existsById(primaryKey, id)

    // ==================== 查询（Rx） ====================

    fun findAll(showLoading: Boolean = false): Flowable<List<T>> =
        findAll(RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findAll(options: RoomRequestOptions): Flowable<List<T>> =
        applyQueryList(roomDao.findAll(), options, "FIND_ALL")

    fun findInfoById(id: Long, showLoading: Boolean = false): Single<T> =
        findInfoById(id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(id: Long, options: RoomRequestOptions): Single<T> =
        applyRead(roomDao.findInfoById(id), options, "FIND_BY_ID", id)

    fun findInfoById(id: String, showLoading: Boolean = false): Single<T> =
        findInfoById(id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(id: String, options: RoomRequestOptions): Single<T> =
        applyRead(roomDao.findInfoById(id), options, "FIND_BY_ID", id)

    fun findInfoById(primaryKey: String, id: Long, showLoading: Boolean = false): Single<T> =
        findInfoById(primaryKey, id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(primaryKey: String, id: Long, options: RoomRequestOptions): Single<T> =
        applyRead(roomDao.findInfoById(primaryKey, id), options, "FIND_BY_ID", mapOf(primaryKey to id))

    fun findInfoById(primaryKey: String, id: String, showLoading: Boolean = false): Single<T> =
        findInfoById(primaryKey, id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(primaryKey: String, id: String, options: RoomRequestOptions): Single<T> =
        applyRead(roomDao.findInfoById(primaryKey, id), options, "FIND_BY_ID", mapOf(primaryKey to id))

    fun findByIn(column: String, values: Collection<*>): List<T> =
        roomDao.findByIn(column, values)

    fun findByInFlowable(
        column: String,
        values: Collection<*>,
        showLoading: Boolean = false
    ): Flowable<List<T>> =
        applyQueryList(
            roomDao.findByInFlowable(column, values),
            RoomRequestOptions.query(showLoading, defaultOptionsEllipsis),
            "FIND_BY_IN",
            mapOf(column to values)
        )

    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        doQueryByLimit(params, orderBy, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis), limit, offset)

    fun doQueryByLimit(
        params: Map<String, Any>,
        orderBy: String,
        options: RoomRequestOptions,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        applyQueryList(
            roomDao.doQueryByLimit(params, orderBy, limit, offset),
            options,
            "QUERY_BY_LIMIT",
            params
        )

    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        doQueryByOrderDesc(
            params, keywordsKey, keywords, orderBy,
            RoomRequestOptions.query(showLoading, defaultOptionsEllipsis), limit, offset
        )

    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        options: RoomRequestOptions,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        applyQueryList(
            roomDao.doQueryByOrderDesc(params, keywordsKey, keywords, orderBy, limit, offset),
            options,
            "QUERY_ORDER_DESC",
            params
        )

    fun doQueryByOrderDesc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        doQueryByOrderDesc(
            params, null, null, orderBy,
            RoomRequestOptions.query(showLoading, defaultOptionsEllipsis), limit, offset
        )

    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        doQueryByOrderAsc(params, orderBy, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis), limit, offset)

    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        options: RoomRequestOptions,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        applyQueryList(
            roomDao.doQueryByOrderAsc(params, orderBy, limit, offset),
            options,
            "QUERY_ORDER_ASC",
            params
        )

    // ==================== 同步分页 ====================

    fun findPageList(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = roomDao.findPageList(params, orderBy, limit, offset)

    fun findPageList(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = roomDao.findPageList(params, keywordsKey, keywords, orderBy, limit, offset)

    fun findPageListAsc(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = roomDao.findPageListAsc(params, orderBy, limit, offset)

    fun findPageListAsc(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = roomDao.findPageListAsc(params, keywordsKey, keywords, orderBy, limit, offset)

    // ==================== LiveData ====================

    @Deprecated("Use deleteAllCount(); LiveData after DELETE is not recommended")
    fun deleteAllLiveData(): LiveData<List<T>> =
        @Suppress("DEPRECATION")
        roomDao.deleteAllLiveData()

    @Deprecated("Use deleteByParamsCount")
    fun deleteByParamsLiveData(params: String, value: String): LiveData<List<T>> =
        @Suppress("DEPRECATION")
        roomDao.deleteByParamsLiveData(params, value)

    @Deprecated("Use deleteByParamsCount")
    fun deleteByParamsLiveData(params: Map<String, Any>): LiveData<List<T>> =
        @Suppress("DEPRECATION")
        roomDao.deleteByParamsLiveData(params)

    fun findAllLiveData(): LiveData<List<T>> = roomDao.findAllLiveData()

    fun findInfoByIdLiveData(id: Long): LiveData<T> = roomDao.findInfoByIdLiveData(id)

    fun findInfoByIdLiveData(id: String): LiveData<T> = roomDao.findInfoByIdLiveData(id)

    fun doQueryByLimitLiveData(
        params: Map<String, Any>,
        orderBy: String?,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> = roomDao.doQueryByLimitLiveData(params, orderBy, limit, offset)

    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> =
        roomDao.doQueryByOrderDescLiveData(params, keywordsKey, keywords, orderBy, limit, offset)

    fun doQueryByOrderDescLiveData(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> = roomDao.doQueryByOrderDescLiveData(params, orderBy, limit, offset)

    fun doQueryByOrderAscLiveData(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): LiveData<List<T>> = roomDao.doQueryByOrderAscLiveData(params, orderBy, limit, offset)

    private fun applyWrite(
        source: Completable,
        options: RoomRequestOptions,
        operation: String,
        payload: Any? = null
    ): Completable = RoomRepositorySupport.applyCompletable(
        this, source, options, roomDao.getTableName(), operation, payload
    )

    private fun <R : Any> applyRead(
        source: Single<R>,
        options: RoomRequestOptions,
        operation: String,
        payload: Any? = null
    ): Single<R> = RoomRepositorySupport.applySingle(
        this, source, options, roomDao.getTableName(), operation, payload
    )

    private fun applyQueryList(
        source: Flowable<List<T>>,
        options: RoomRequestOptions,
        operation: String,
        payload: Any? = null
    ): Flowable<List<T>> = RoomRepositorySupport.applyListFlowable(
        this, source, options, roomDao.getTableName(), operation, payload
    )

    private fun applyDeleteList(
        source: Flowable<List<T>>,
        options: RoomRequestOptions,
        operation: String,
        payload: Any? = null
    ): Flowable<List<T>> = RoomRepositorySupport.applyDeleteFlowable(
        this, source, options, roomDao.getTableName(), operation, payload
    )
}
