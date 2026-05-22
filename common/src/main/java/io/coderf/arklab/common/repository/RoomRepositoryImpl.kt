package io.coderf.arklab.common.repository

import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import io.coderf.arklab.common.base.BaseRepository

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
 * ```java
 * PersonRepository repo = RepositoryFactory.create(
 *     PersonRepository.class,
 *     database.getPersonDao(),
 *     baseView
 * );
 * repo.findInfoById("id001", true).subscribe(...);
 * ```
 *
 * @param T 实体类型
 * @param DB 继承 [BaseRoomDao] 的 Dao
 * @param BV 页面 View，可为 null（无 UI 的后台仓库）
 * @author fz
 * @see RoomRequestOptions
 * @see RxRoomPagingSource
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

    /**
     * 插入单条。
     * @param showLoading 是否显示加载框（兼容老 API）
     */
    fun insert(obj: T, showLoading: Boolean = false): Completable =
        insert(obj, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    /** 插入单条，使用 [RoomRequestOptions] 控制 UI */
    fun insert(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.insert(obj), options)

    /** 批量插入 */
    fun insert(objs: List<T>, showLoading: Boolean = false): Completable =
        insert(objs, RoomRequestOptions.insert(showLoading, defaultOptionsEllipsis))

    fun insert(objs: List<T>, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.insert(objs.toList()), options)

    /** 按主键删除单条 */
    fun delete(obj: T, showLoading: Boolean = false): Completable =
        delete(obj, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun delete(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.delete(obj), options)

    /** 更新单条 */
    fun update(obj: T, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: T, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.update(obj), options)

    /** 批量更新 */
    fun update(obj: List<T>, showLoading: Boolean = false): Completable =
        update(obj, RoomRequestOptions.update(showLoading, defaultOptionsEllipsis))

    fun update(obj: List<T>, options: RoomRequestOptions): Completable =
        RoomRepositorySupport.applyCompletable(this, roomDao.update(obj), options)

    // ==================== 条件删除（Flowable） ====================

    /** 删除全表 */
    fun deleteAll(showLoading: Boolean = false): Flowable<List<T>> =
        deleteAll(RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteAll(options: RoomRequestOptions): Flowable<List<T>> =
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteAll(), options)

    /** 按单列删除 */
    fun deleteByParams(params: String, value: String, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, value, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParams(params: String, value: String, options: RoomRequestOptions): Flowable<List<T>> =
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteByParams(params, value), options)

    /** 按多条件 AND 删除 */
    fun deleteByParams(params: Map<String, Any>, showLoading: Boolean = false): Flowable<List<T>> =
        deleteByParams(params, RoomRequestOptions.delete(showLoading, defaultOptionsEllipsis))

    fun deleteByParams(params: Map<String, Any>, options: RoomRequestOptions): Flowable<List<T>> =
        RoomRepositorySupport.applyDeleteFlowable(this, roomDao.deleteByParams(params), options)

    // ==================== 查询（Rx） ====================

    /** 查询全表；空表且 throwOnEmptyList 时抛 NOT_FOUND */
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

    /**
     * 分页查询 Flowable（排序列不强制 ASC/DESC）。
     * @param orderBy 排序列名
     */
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

    /** 降序分页 + 可选关键字 */
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

    /** 降序分页，无关键字 */
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

    /** 升序分页 */
    fun doQueryByOrderAsc(
        params: Map<String, Any>,
        orderBy: String,
        showLoading: Boolean = false,
        limit: Int = 10,
        offset: Int = 0
    ): Flowable<List<T>> =
        RoomRepositorySupport.applyListFlowable(
            this,
            roomDao.doQueryByOrderAsc(params, orderBy, limit, offset),
            RoomRequestOptions.query(showLoading, defaultOptionsEllipsis)
        )

    // ==================== 同步分页（Paging / 子线程直接调） ====================

    /**
     * 同步分页查询，降序。
     * 常用于 [io.coderf.arklab.common.datasource.RxRoomPagingSource]，在 IO 线程调用。
     */
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

    // ==================== LiveData（无 Loading 包装） ====================

    /** 以下方法直接返回 Dao LiveData，表数据变化时自动刷新；UI 层自行 observe */

    fun deleteAllLiveData(): LiveData<List<T>> = roomDao.deleteAllLiveData()

    fun deleteByParamsLiveData(params: String, value: String): LiveData<List<T>> =
        roomDao.deleteByParamsLiveData(params, value)

    fun deleteByParamsLiveData(params: Map<String, Any>): LiveData<List<T>> =
        roomDao.deleteByParamsLiveData(params)

    fun findAllLiveData(): LiveData<List<T>> = roomDao.findAllLiveData()

    fun findInfoByIdLiveData(id: Long): LiveData<T> = roomDao.findInfoByIdLiveData(id)

    fun findInfoByIdLiveData(id: String): LiveData<T> = roomDao.findInfoByIdLiveData(id)

    fun doQueryByLimitLiveData(
        params: Map<String, Any>,
        orderBy: String,
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
