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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 新版 Flow 分页 ViewModel，对接 [NetworkPagingRepository]。
 * 同时提供 [dataFlow]（Kotlin）与 [items]（LiveData，兼容旧 Java Fragment）。
 */
abstract class NetworkFlowPagingViewModel<IR : NetworkPagingRepository<T, V>, T : Any, V : BaseView>(
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

    private var pagingCollectJob: Job? = null

    override fun createRepository(baseView: V) {
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

    protected open fun createPagingSource(): PagingSource<Int, T> {
        return NetworkPagingSource(iRepository, startPage)
    }
}
