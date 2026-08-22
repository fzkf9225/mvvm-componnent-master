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
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.viewmodel.BasePagingViewModel
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 新版 Flow 分页 ViewModel，对接 [NetworkPagingRepository]。
 *
 * 同时提供 [dataFlow]（Kotlin）与 [items]（LiveData，兼容旧 Java Fragment）。
 *
 * 业务筛选条件放在 [pagingQuery]，经 [NetworkPagingSource] 快照传给 Repository 的
 * [NetworkPagingRepository.fetchPage]，**不要**在 Repository 里强转 BaseView 取参。
 *
 * 更新 [pagingQuery] 默认**不会**自动请求；需要重新拉数时显式调用 [refreshData]，
 * 或 [updatePagingQuery] 时传入 `refresh = true`。
 *
 * @param IR Repository，须带同一 [Q]
 * @param T  列表元素
 * @param V  BaseView
 * @param Q  查询参数类型
 */
abstract class NetworkFlowPagingViewModel<
        IR : NetworkPagingRepository<T, V, Q>,
        T : Any,
        V : BaseView,
        Q : PagingQuery
        >(
    application: Application
) : BasePagingViewModel<IR, V>(application) {

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

    override fun createRepository(baseView: V?) {
        super.createRepository(baseView)
        refreshData()
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
     * 创建 PagingSource；[pagingQuery] 在创建时快照，本轮分页过程内不变。
     */
    protected open fun createPagingSource(): PagingSource<Int, T> {
        val repo = iRepository
            ?: error("iRepository is null; ensure createRepository has been called")
        return NetworkPagingSource(repo, startPage, pagingQuery)
    }
}
