package io.coderf.arklab.common.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.coderf.arklab.common.api.ApiRetrofit
import io.coderf.arklab.common.repository.PagingFlowRepositoryImpl
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Created by fz on 2023/8/7 9:17
 * describe : Kotlin 协程版本，使用 Flow 的分页数据源。
 * [query] 为创建本 Source 时的快照；变更条件须由 ViewModel refreshData 重建 Source。
 */
class FlowPagingSource<T : Any, Q : PagingQuery>(
    private val pagingRepository: PagingFlowRepositoryImpl<*, T, *, Q>,
    private val startPage: Int = 1,
    private val query: Q
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val currentPage = params.key ?: startPage

        return try {
            val flow: Flow<List<T>>? =
                pagingRepository.requestPaging(currentPage, params.loadSize, query)

            flow?.map { mBeans ->
                toLoadResult(mBeans, currentPage, params.loadSize)
            }?.catch { e ->
                pagingRepository.handleFlowError(e, pagingRepository.apiRequestOptions)
                emit(LoadResult.Error(e))
            }?.firstOrNull() ?: run {
                LoadResult.Error(Exception("Flow is null"))
            }

        } catch (e: Exception) {
            LogUtil.logger(ApiRetrofit.TAG, "FlowPagingSource请求错误：$e")
            pagingRepository.onError(e)
            LoadResult.Error(e)
        }
    }

    private fun toLoadResult(
        mBeans: List<T>,
        page: Int,
        requestedLoadSize: Int
    ): LoadResult<Int, T> {
        val prevKey = if (page == startPage) null else page - 1
        val endReached = mBeans.isEmpty() || mBeans.size < requestedLoadSize
        val nextKey = if (endReached) null else page + 1
        return LoadResult.Page(
            data = mBeans,
            prevKey = prevKey,
            nextKey = nextKey,
            itemsBefore = LoadResult.Page.COUNT_UNDEFINED,
            itemsAfter = LoadResult.Page.COUNT_UNDEFINED
        )
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = startPage
}
