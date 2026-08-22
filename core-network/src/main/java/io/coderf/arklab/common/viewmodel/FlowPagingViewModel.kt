package io.coderf.arklab.common.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.datasource.FlowPagingSource
import io.coderf.arklab.common.repository.PagingFlowRepositoryImpl
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Kotlin 协程版本的 Paging ViewModel 基类。
 *
 * 业务筛选条件放在 [pagingQuery]，经 [FlowPagingSource] 快照传给
 * [PagingFlowRepositoryImpl.requestPaging]，禁止在 Repository 内强转 BaseView 取参。
 *
 * 更新 [pagingQuery] 默认不会自动请求；需重新拉数时调用 [refreshData]，
 * 或 [updatePagingQuery] 传入 `refresh = true`。
 */
abstract class FlowPagingViewModel<
        IR : PagingFlowRepositoryImpl<*, T, V, Q>,
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

    open var pagingConfig: PagingConfig = PagingConfig(
        pageSize = DEFAULT_PAGE_SIZE,
        prefetchDistance = DEFAULT_PREFETCH_DISTANCE,
        enablePlaceholders = false,
        initialLoadSize = DEFAULT_PAGE_SIZE
    )

    open var startPage: Int = DEFAULT_START_PAGE

    /**
     * 当前分页查询参数。子类通过 [createPagingQuery] 提供初值。
     * 仅赋值不会触发网络请求。
     */
    var pagingQuery: Q
        get() = _pagingQuery ?: createPagingQuery().also { _pagingQuery = it }
        set(value) {
            _pagingQuery = value
        }

    private var _pagingQuery: Q? = null

    private var pagingCollectJob: Job? = null

    protected abstract fun createPagingQuery(): Q

    /**
     * @param query   新条件
     * @param refresh 是否立即 [refreshData]；默认 false
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
            }
        }
    }

    protected open fun createPagingData(): Flow<PagingData<T>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { createPagingSource() }
        ).flow.cachedIn(viewModelScope)
    }

    protected open fun createPagingSource(): PagingSource<Int, T> {
        val repo = iRepository
            ?: error("iRepository is null; ensure createRepository has been called")
        return FlowPagingSource(repo, startPage, pagingQuery)
    }

}
