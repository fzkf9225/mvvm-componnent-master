package io.coderf.arklab.core.network

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import io.coderf.arklab.common.viewmodel.BasePagingViewModel
import io.coderf.arklab.core.bean.PagingQuery
import io.coderf.arklab.core.request.AppError
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * 新版 Flow 分页 ViewModel，对接 [NetworkPagingRepository]。
 *
 * 同时提供 [dataFlow]（Kotlin）与 [items]（LiveData，兼容旧 Java Fragment）。
 *
 * **分页 vs launchRequest**：列表翻页在 [PagingSource.load] 中执行，**禁止**为每一页
 * 调用 [launchRequest]（会误弹全屏 Loading、与 Paging 生命周期冲突）。
 * 页内单次请求（详情 / 删除）仍用 [launchRequest]。
 * 分页错误经 [NetworkPagingSource.onAppError] → [defaultPagingAppErrorHandler] → Host。
 *
 * 业务筛选条件放在 [pagingQuery]，经 [NetworkPagingSource] 快照传给 Repository 的
 * [NetworkPagingRepository.fetchPage]，**不要**在 Repository 里强转页面取参。
 *
 * 仓库由构造参数注入；首次 [refreshData] 在 init 中延迟启动（等子类构造完成）。
 * 更新 [pagingQuery] 默认**不会**自动请求；需要重新拉数时显式调用 [refreshData]，
 * 或 [updatePagingQuery] 时传入 `refresh = true`。
 *
 * @param T  列表元素
 * @param Q  查询参数类型
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @updated 2026/11/13
 */
abstract class NetworkFlowPagingViewModel<
        T : Any,
        Q : PagingQuery
        >(
    application: Application,
    protected val pagingRepository: NetworkPagingRepository<T, Q>
) : BasePagingViewModel(application) {

    companion object {
        const val DEFAULT_START_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PREFETCH_DISTANCE = 3
    }

    protected val _pagingDataFlow = MutableStateFlow<PagingData<T>>(PagingData.empty())
    val dataFlow: StateFlow<PagingData<T>> = _pagingDataFlow.asStateFlow()

    private val _items = MutableLiveData<PagingData<T>>(PagingData.empty())
    /** 兼容旧 [io.coderf.arklab.common.viewmodel.PagingViewModel.getItems] */
    val items: LiveData<PagingData<T>> get() = _items

    open var pagingConfig: PagingConfig = PagingConfig(
        pageSize = DEFAULT_PAGE_SIZE,
        prefetchDistance = DEFAULT_PREFETCH_DISTANCE,
        enablePlaceholders = false,
        initialLoadSize = DEFAULT_PAGE_SIZE
    )

    open var startPage: Int = DEFAULT_START_PAGE

    /**
     * 当前分页查询参数。子类通过 [createPagingQuery] 提供初值。
     *
     * 仅赋值不会触发网络请求；改完后请调用 [refreshData]，或使用 [updatePagingQuery]。
     */
    var pagingQuery: Q
        get() = _pagingQuery ?: createPagingQuery().also { _pagingQuery = it }
        set(value) {
            _pagingQuery = value
        }

    private var _pagingQuery: Q? = null

    private var pagingCollectJob: Job? = null

    init {
        // 须 yield：Dispatchers.Main.immediate 下 launch 体可能仍在父类构造内同步执行，
        // 此时子类 override 的 pagingConfig / startPage 等属性初始化器尚未跑完。
        viewModelScope.launch {
            yield()
            refreshData()
        }
    }

    /**
     * 提供初始查询参数。首次读取 [pagingQuery] 时调用一次。
     */
    protected abstract fun createPagingQuery(): Q

    /**
     * 更新查询参数。
     *
     * @param query   新条件
     * @param refresh 是否立即 [refreshData] 重建 Paging 并请求；默认 false，由调用方自行刷新
     */
    fun updatePagingQuery(query: Q, refresh: Boolean = false) {
        pagingQuery = query
        if (refresh) {
            refreshData()
        }
    }

    override fun refreshData() {
        pagingCollectJob?.cancel()
        pagingCollectJob = viewModelScope.launch {
            createPagingData().collect { pagingData ->
                _pagingDataFlow.value = pagingData
                _items.value = pagingData
            }
        }
    }

    protected open fun createPagingData(): Flow<PagingData<T>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { createPagingSource() }
        ).flow.cachedIn(viewModelScope)
    }

    /**
     * 默认把分页 [AppError] 交给 [networkRequestUiHost]（401/403 登录 / Toast 等）。
     * [createPagingSource] 与子类 override 请复用，避免漏接 Host。
     */
    protected fun defaultPagingAppErrorHandler(): (AppError) -> Unit = { error ->
        networkRequestUiHost.showError(error)
    }

    /**
     * 创建 PagingSource；[pagingQuery] 在创建时快照，本轮分页过程内不变。
     *
     * **override 约定**：必须把失败 [AppError] 交 [networkRequestUiHost.showError]
     *（推荐复用 [defaultPagingAppErrorHandler]），或页面自管登录 / 业务码；
     * **禁止**创建不带 [NetworkPagingSource.onAppError] 的 Source。
     * 分页不经 [launchRequest]。
     */
    protected open fun createPagingSource(): PagingSource<Int, T> {
        return NetworkPagingSource(
            pagingRepository,
            startPage,
            pagingQuery,
            defaultPagingAppErrorHandler()
        )
    }
}
