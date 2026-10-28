package io.coderf.arklab.core.db

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.coderf.arklab.common.dao.BaseRoomDao
import io.coderf.arklab.core.bean.RoomPagingQuery

/**
 * 基于本地 Room 的 Paging3 分页源（协程）。
 *
 * 对齐 Rx 栈 [io.coderf.arklab.common.datasource.RxRoomPagingSource]：页码 key，
 * `offset = page × pageSize`，数据来自 Dao [BaseRoomDao.findPageListSuspend]。
 *
 * ```
 * Pager(config) { RoomPagingSource(repository, getPagingQuery()) }
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/28
 */
class RoomPagingSource<T : Any, DB : BaseRoomDao<T>>(
    private val repository: RoomDataRepository<T, DB>,
    private val query: RoomPagingQuery
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val limit = params.loadSize
            val page = params.key ?: 0
            val offset = page * limit
            val sortColumn = query.orderBy?.takeIf { it.isNotBlank() } ?: "id"
            @Suppress("UNCHECKED_CAST")
            val queryParams = (query.queryParams ?: emptyMap()) as Map<String, Any>
            val list = repository.getRoomDao().findPageListSuspend(
                queryParams,
                query.keywordsKey,
                query.keywords,
                sortColumn,
                limit,
                offset
            )
            val prevKey = if (page <= 0) null else page - 1
            val nextKey = if (list.size < limit) null else page + 1
            LoadResult.Page(
                data = list,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestPageToPosition(anchor) ?: return null
        val prev = closest.prevKey ?: 0
        val next = closest.nextKey ?: 0
        return (prev + next) / 2
    }
}
