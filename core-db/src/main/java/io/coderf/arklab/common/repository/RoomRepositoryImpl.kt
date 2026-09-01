package io.coderf.arklab.common.repository

import androidx.lifecycle.LiveData
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.base.BaseView
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
 * - **Loading** 仅通过 [getRequestUi]（[io.coderf.arklab.common.inter.RequestUiCallback]），
 *   由 [io.coderf.arklab.common.base.BaseViewModel] 注入，对齐 [RepositoryImpl]；
 * - **Flow / 协程**：见 [RoomRepositoryFlowExt]、[RoomRepositoryCoroutineExt]。
 *
 * ## 用法示例
 * ```kotlin
 * class PersonRepository(dao: PersonDao, view: BaseView) :
 *     RoomRepositoryImpl<Person, PersonDao, BaseView>(dao, view)
 *
 * // ViewModel 中
 * repository.insert(person, showLoading = true)
 *     .subscribe({ }, { getRequestUi()?.onErrorCode(it) })
 *
 * // 或使用 Options
 * repository.findAll(RoomRequestOptions.withLoading("加载列表..."))
 * ```
 *
 * @param T 实体类型
 * @param DB 继承 [BaseRoomDao] 的 Dao
 * @param BV 页面 View，可为 null（无 UI 的后台仓库）
 * @see RoomRequestOptions
 * @see RxRoomPagingSource
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
open class RoomRepositoryImpl<T : Any, DB : BaseRoomDao<T>, BV : BaseView?>(
    private val roomDao: DB,
    baseView: BV
) : BaseRepository<BV>(baseView) {

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
        RoomRepositorySupport.applyCompletable(this, roomDao.insert(obj), options)

    fun insert(objs: List<T>, showLoading: Boolean = false): Completable =
        insert(objs, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun insert(objs: List<T>, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.insert(objs.toList()), options)

    /** 插入或替换单条（冲突 REPLACE） */
    fun upsert(obj: T, showLoading: Boolean = false): Completable =
        upsert(obj, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun upsert(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.upsert(obj), options)

    fun upsert(objs: List<T>, showLoading: Boolean = false): Completable =
        upsert(objs, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun upsert(objs: List<T>, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.upsert(objs.toList()), options)

    fun delete(obj: T, showLoading: Boolean = false): Completable =
        delete(obj, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun delete(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.delete(obj), options)

    fun update(obj: T, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.update(obj), options)

    fun update(obj: List<T>, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: List<T>, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.update(obj), options)

    // ==================== 条件删除（返回影响行数，推荐） ====================

    fun deleteAllCount(showLoading: Boolean = false): Single<Int> =
        deleteAllCount(RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteAllCount(options: RoomRequestOptions): Single<Int> =
        RoomRepositorySupport.applySingle(this, roomDao.deleteAllCount(), options)

    fun deleteByParamsCount(column: String, value: Any, showLoading: Boolean = false): Single<Int> =
        deleteByParamsCount(column, value, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParamsCount(column: String, value: Any, options: RoomRequestOptions): Single<Int> =
        RoomRepositorySupport.applySingle(this, roomDao.deleteByParamsCount(column, value), options)

    fun deleteByParamsCount(params: Map<String, Any>, showLoading: Boolean = false): Single<Int> =
        deleteByParamsCount(params, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParamsCount(params: Map<String, Any>, options: RoomRequestOptions): Single<Int> =
        RoomRepositorySupport.applySingle(this, roomDao.deleteByParamsCount(params), options)

    // ==================== 条件删除（Flowable，兼容老签名，已废弃） ====================

    @Deprecated("Use deleteAllCount()", ReplaceWith("deleteAllCount(showLoading)"))
    fun deleteAll(showLoading: Boolean = false): Flowable<List<T>> =
        deleteAll(RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteAllCount(options)")
    fun deleteAll(options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteAll(), options)

    @Deprecated("Use deleteByParamsCount(column, value, showLoading)")
    fun deleteByParams(params: String, value: String, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, value, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteByParamsCount(column, value, options)")
    fun deleteByParams(params: String, value: String, options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteByParams(params, value), options)

    @Deprecated("Use deleteByParamsCount(params, showLoading)")
    fun deleteByParams(params: Map<String, Any>, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    @Deprecated("Use deleteByParamsCount(params, options)")
    fun deleteByParams(params: Map<String, Any>, options: RoomRequestOptions): Flowable<List<T>> =
        @Suppress("DEPRECATION")
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteByParams(params), options)

    // ==================== count / exists ====================

    fun countAll(showLoading: Boolean = false): Single<Long> =
        countAll(RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun countAll(options: RoomRequestOptions): Single<Long> =
        RoomRepositorySupport.applySingle(this, roomDao.countAllSingle(), options)

    fun count(params: Map<String, Any>, showLoading: Boolean = false): Single<Long> =
        count(params, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun count(params: Map<String, Any>, options: RoomRequestOptions): Single<Long> =
        RoomRepositorySupport.applySingle(this, roomDao.countSingle(params), options)

    fun countAllSync(): Long = roomDao.countAll()

    fun countSync(params: Map<String, Any>): Long = roomDao.count(params)

    fun existsAny(): Boolean = roomDao.existsAny()

    fun exists(params: Map<String, Any>): Boolean = roomDao.exists(params)

    fun existsById(primaryKey: String, id: Any): Boolean = roomDao.existsById(primaryKey, id)

    // ==================== 查询（Rx） ====================

    fun findAll(showLoading: Boolean = false): Flowable<List<T>> =
        findAll(RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findAll(options: RoomRequestOptions): Flowable<List<T>> =
        RoomRepositorySupport.applyListFlowable(this, roomDao.findAll(), options)

    fun findInfoById(id: Long, showLoading: Boolean = false): Single<T> =
        findInfoById(id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(id: Long, options: RoomRequestOptions): Single<T> =
        RoomRepositorySupport.applySingle(this, roomDao.findInfoById(id), options)

    fun findInfoById(id: String, showLoading: Boolean = false): Single<T> =
        findInfoById(id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(id: String, options: RoomRequestOptions): Single<T> =
        RoomRepositorySupport.applySingle(this, roomDao.findInfoById(id), options)

    fun findInfoById(primaryKey: String, id: Long, showLoading: Boolean = false): Single<T> =
        findInfoById(primaryKey, id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(primaryKey: String, id: Long, options: RoomRequestOptions): Single<T> =
        RoomRepositorySupport.applySingle(this, roomDao.findInfoById(primaryKey, id), options)

    fun findInfoById(primaryKey: String, id: String, showLoading: Boolean = false): Single<T> =
        findInfoById(primaryKey, id, RoomRequestOptions.query(showLoading, defaultOptionsEllipsis))

    fun findInfoById(primaryKey: String, id: String, options: RoomRequestOptions): Single<T> =
        RoomRepositorySupport.applySingle(this, roomDao.findInfoById(primaryKey, id), options)

    fun findByIn(column: String, values: Collection<*>): List<T> =
        roomDao.findByIn(column, values)

    fun findByInFlowable(
        column: String,
        values: Collection<*>,
        showLoading: Boolean = false
    ): Flowable<List<T>> =
        RoomRepositorySupport.applyListFlowable(
            this,
            roomDao.findByInFlowable(column, values),
            RoomRequestOptions.query(showLoading, defaultOptionsEllipsis)
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
        RoomRepositorySupport.applyListFlowable(
            this,
            roomDao.doQueryByLimit(params, orderBy, limit, offset),
            options
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
        RoomRepositorySupport.applyListFlowable(
            this,
            roomDao.doQueryByOrderDesc(params, keywordsKey, keywords, orderBy, limit, offset),
            options
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
        RoomRepositorySupport.applyListFlowable(
            this,
            roomDao.doQueryByOrderAsc(params, orderBy, limit, offset),
            options
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
}
