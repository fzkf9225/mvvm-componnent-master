package io.coderf.arklab.core.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.coderf.arklab.common.base.BaseView
import kotlinx.coroutines.flow.firstOrNull

/**
 * 新版 PagingSource，对接 [NetworkPagingRepository]。
 */
class NetworkPagingSource<T : Any, BV : BaseView>(
    private val repository: NetworkPagingRepository<T, BV>,
    private val startPage: Int = 1
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val currentPage = params.key ?: startPage
        return try {
            val page = repository.loadPage(currentPage, params.loadSize).firstOrNull()
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
