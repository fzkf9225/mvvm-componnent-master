package com.casic.otitan.common.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.repository.PagingFlowRepositoryImpl
import com.casic.otitan.common.api.ApiRetrofit
import com.casic.otitan.common.utils.log.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by fz on 2023/8/7 9:17
 * describe : Kotlin协程版本，使用Flow的分页数据源
 */
class FlowPagingSource<T : Any, BV : BaseView>(
    private val pagingRepository: PagingFlowRepositoryImpl<*, T, BV>,
    private val startPage: Int = 1
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val currentPage = params.key ?: startPage

        return try {
            val flow: Flow<List<T>>? = pagingRepository.requestPaging(currentPage, params.loadSize)

            flow?.map { mBeans ->
                toLoadResult(mBeans, currentPage)
            }?.catch { e ->
                // 相当于 doOnError：执行副作用
                pagingRepository.handleFlowError(e,pagingRepository.apiRequestOptions)
                // 相当于 onErrorReturn：返回错误结果
                emit(LoadResult.Error(e))
            }?.firstOrNull() ?: run {
                LoadResult.Error(Exception("Flow is null"))
            }

        } catch (e: Exception) {
            LogUtil.show(ApiRetrofit.TAG, "FlowPagingSource请求错误：$e")
            pagingRepository.onError(e)
            LoadResult.Error(e)
        }
    }

    /**
     * 将获取的集合对象转化为需加载的结果对象
     */
    private fun toLoadResult(mBeans: List<T>, page: Int): LoadResult<Int, T> {
        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (mBeans.isEmpty()) null else page + 1
        return LoadResult.Page(
            data = mBeans,
            prevKey = prevKey,
            nextKey = nextKey,
            itemsBefore = LoadResult.Page.COUNT_UNDEFINED,
            itemsAfter = LoadResult.Page.COUNT_UNDEFINED
        )
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        // 刷新时从起始页开始
        return startPage
    }
}