package io.coderf.arklab.common.repository

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [RoomRepositoryImpl] 的协程 suspend 扩展。
 *
 * 在 [Dispatchers.IO] 上通过 Rx 的 `blocking*` 桥接，与 [RoomRepositoryFlowExt] 一样**不破坏**
 * 现有 Rx 仓库 API，供 `viewModelScope.launch` 等场景使用。
 *
 * ## 用法示例
 * ```kotlin
 * import io.coderf.arklab.common.repository.RoomRepositoryCoroutineExt.findAllAwait
 *
 * viewModelScope.launch {
 *     val list = repository.findAllAwait(RoomRequestOptions.withLoading("加载中..."))
 * }
 * ```
 *
 * @author fz
 * @see RoomRepositoryFlowExt
 */
object RoomRepositoryCoroutineExt {

    /**
     * 挂起直到插入完成。
     *
     * @param obj 实体
     * @param options 默认静默
     */
    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.insertAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            insert(obj, options).blockingAwait()
        }
    }

    /**
     * 挂起并返回全表列表（Flowable 首项）。
     */
    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findAllAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): List<T> = withContext(Dispatchers.IO) {
        findAll(options).blockingFirst()
    }

    /**
     * 挂起并按字符串主键查询单条。
     */
    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdAwait(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(id, options).blockingGet()
    }

    /**
     * 挂起并执行同步分页查询（直接调 Dao 同步方法，无 Rx 链）。
     *
     * @see RoomRepositoryImpl.findPageList
     */
    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findPageListAwait(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int,
        offset: Int
    ): List<T> = withContext(Dispatchers.IO) {
        findPageList(params, keywordsKey, keywords, orderBy, limit, offset)
    }
}
