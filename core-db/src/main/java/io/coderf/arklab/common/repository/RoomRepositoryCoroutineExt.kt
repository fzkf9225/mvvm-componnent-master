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

    // ==================== 写 ====================

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.insertAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            insert(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.insertAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            insert(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.upsertAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            upsert(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.upsertAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            upsert(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.updateAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            update(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.updateAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            update(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.deleteAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            delete(obj, options).blockingAwait()
        }
    }

    /** 条件删除，返回影响行数 */
    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.deleteByParamsCountAwait(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Int = withContext(Dispatchers.IO) {
        deleteByParamsCount(params, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.deleteAllCountAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Int = withContext(Dispatchers.IO) {
        deleteAllCount(options).blockingGet()
    }

    // ==================== 查 ====================

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findAllAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): List<T> = withContext(Dispatchers.IO) {
        findAll(options).blockingFirst()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdAwait(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdAwait(
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdAwait(
        primaryKey: String,
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(primaryKey, id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdAwait(
        primaryKey: String,
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(primaryKey, id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findByInAwait(
        column: String,
        values: Collection<*>
    ): List<T> = withContext(Dispatchers.IO) {
        findByIn(column, values)
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.countAllAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Long = withContext(Dispatchers.IO) {
        countAll(options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.countAwait(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Long = withContext(Dispatchers.IO) {
        count(params, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.existsAwait(
        params: Map<String, Any>
    ): Boolean = withContext(Dispatchers.IO) {
        exists(params)
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

    suspend fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findPageListAwait(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = withContext(Dispatchers.IO) {
        findPageList(params, orderBy, limit, offset)
    }
}
