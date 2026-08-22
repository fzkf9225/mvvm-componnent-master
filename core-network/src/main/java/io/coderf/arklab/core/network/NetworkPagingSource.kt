package io.coderf.arklab.core.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.flow.firstOrNull

/**
 * 新版 PagingSource，对接 [NetworkPagingRepository]。
 *
 * [query] 为创建本 Source 时的查询参数快照；一次分页过程内条件不变。
 * 变更条件须由 ViewModel [NetworkFlowPagingViewModel.refreshData] 重建 Source。
 */
class NetworkPagingSource<T : Any, Q : PagingQuery>(
    private val repository: NetworkPagingRepository<T, *, Q>,
    private val startPage: Int = 1,
    private val query: Q
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val currentPage = params.key ?: startPage
        return try {
            val page = repository.loadPage(currentPage, params.loadSize, query).firstOrNull()
                ?: emptyList()
            val prevKey = if (currentPage == startPage) null else currentPage - 1
            val endReached = page.isEmpty() || page.size < params.loadSize
            val nextKey = if (endReached) null else currentPage + 1
            LoadResult.Page(
                data = page,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = startPage
}
