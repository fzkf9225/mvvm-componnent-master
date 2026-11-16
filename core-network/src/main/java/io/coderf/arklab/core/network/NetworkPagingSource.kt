package io.coderf.arklab.core.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.coderf.arklab.core.bean.PagingQuery
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.AppErrorThrowable
import io.coderf.arklab.core.request.RequestErrorMapper
import kotlinx.coroutines.flow.firstOrNull

/**
 * 新版 PagingSource，对接 [NetworkPagingRepository]。
 *
 * [query] 为创建本 Source 时的查询参数快照；一次分页过程内条件不变。
 * 变更条件须由 ViewModel [NetworkFlowPagingViewModel.refreshData] 重建 Source。
 *
 * **分页不经 [io.coderf.arklab.common.base.BaseViewModel.launchRequest]**：
 * 错误 UI（Toast / 401 登录等）必须经非空 [onAppError] 交到
 * [io.coderf.arklab.common.base.NetworkRequestUiHost]（或业务等效 RequestUi）。
 * 默认由 [NetworkFlowPagingViewModel.createPagingSource] /
 * [NetworkFlowPagingViewModel.defaultPagingAppErrorHandler] 注入。
 *
 * [loadMoreEnabled] 为 false 时，本次成功加载后不再 append（全量接口走这一支）。
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @updated 2026/11/16 增加 loadMoreEnabled，关闭时 nextKey 恒为 null
 */
class NetworkPagingSource<T : Any, Q : PagingQuery>(
    private val repository: NetworkPagingRepository<T, Q>,
    private val startPage: Int = 1,
    private val query: Q,
    private val onAppError: (AppError) -> Unit,
    private val loadMoreEnabled: Boolean = true
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val currentPage = params.key ?: startPage
        return try {
            val page = repository.loadPage(currentPage, params.loadSize, query).firstOrNull()
                ?: emptyList()
            val prevKey = if (currentPage == startPage) null else currentPage - 1
            val endReached = !loadMoreEnabled || page.isEmpty() || page.size < params.loadSize
            val nextKey = if (endReached) null else currentPage + 1
            LoadResult.Page(
                data = page,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            val appError = when (e) {
                is AppErrorThrowable -> e.appError
                else -> RequestErrorMapper.toAppError(e)
            }
            if (appError !is AppError.Cancelled) {
                onAppError(appError)
            }
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = startPage
}
