package com.casic.otitan.common.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.datasource.FlowPagingSource
import com.casic.otitan.common.repository.FlowRepositoryImpl
import com.casic.otitan.common.repository.PagingFlowRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Kotlin协程版本的Paging ViewModel基类
 */
abstract class FlowPagingViewModel<IR : FlowRepositoryImpl<*, V>, T : Any, V : BaseView>(
    application: Application
) : BasePagingViewModel<IR, V>(application) {

    companion object {
        const val DEFAULT_START_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PREFETCH_DISTANCE = 3
    }

    // 分页数据流（纯Flow版本）
    protected val _pagingDataFlow = MutableStateFlow<PagingData<T>>(PagingData.empty())
    val dataFlow: StateFlow<PagingData<T>> = _pagingDataFlow.asStateFlow()

    // 分页配置
   open var pagingConfig: PagingConfig = PagingConfig(
        pageSize = DEFAULT_PAGE_SIZE,
        prefetchDistance = DEFAULT_PREFETCH_DISTANCE,
        enablePlaceholders = true,
        initialLoadSize = DEFAULT_PAGE_SIZE
    )

    // 起始页码
    open var startPage: Int = DEFAULT_START_PAGE

    override fun createRepository(baseView: V?) {
        super.createRepository(baseView)
        refreshData()
    }

    override fun refreshData() {
        viewModelScope.launch {
            createPagingData().collect { pagingData ->
                _pagingDataFlow.value = pagingData
            }
        }
    }

    /**
     * 获取分页数据流
     */
    protected open fun createPagingData(): Flow<PagingData<T>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { createPagingSource() }
        ).flow.cachedIn(viewModelScope)
    }

    /**
     * 创建PagingSource
     */
    protected open fun createPagingSource(): PagingSource<Int, T> {
        return FlowPagingSource(iRepository as PagingFlowRepositoryImpl<*, T, V>, startPage)
    }

}