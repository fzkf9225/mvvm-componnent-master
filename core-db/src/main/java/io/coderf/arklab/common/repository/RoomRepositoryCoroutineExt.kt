package io.coderf.arklab.common.repository

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [RoomRepositoryImpl] 的协�?suspend 扩展�?
 *
 * �?[Dispatchers.IO] 上通过 Rx �?`blocking*` 桥接，与 [RoomRepositoryFlowExt] 一�?*不破�?*
 * 现有 Rx 仓库 API，供 `viewModelScope.launch` 等场景使用�?
 *
 * ## 用法示例
 * ```kotlin
 * import io.coderf.arklab.common.repository.RoomRepositoryCoroutineExt.findAllAwait
 *
 * viewModelScope.launch {
 *     val list = repository.findAllAwait(RoomRequestOptions.withLoading("加载�?.."))
 * }
 * ```
 *
 * @see RoomRepositoryFlowExt
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
object RoomRepositoryCoroutineExt {

    // ==================== �?====================

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.insertAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            insert(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.insertAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            insert(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.upsertAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            upsert(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.upsertAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            upsert(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.updateAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            update(obj, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.updateAwait(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            update(objs, options).blockingAwait()
        }
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteAwait(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ) {
        withContext(Dispatchers.IO) {
            delete(obj, options).blockingAwait()
        }
    }

    /** 条件删除，返回影响行�?*/
    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteByParamsCountAwait(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Int = withContext(Dispatchers.IO) {
        deleteByParamsCount(params, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteAllCountAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Int = withContext(Dispatchers.IO) {
        deleteAllCount(options).blockingGet()
    }

    // ==================== �?====================

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findAllAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): List<T> = withContext(Dispatchers.IO) {
        findAll(options).blockingFirst()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdAwait(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdAwait(
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdAwait(
        primaryKey: String,
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(primaryKey, id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdAwait(
        primaryKey: String,
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): T = withContext(Dispatchers.IO) {
        findInfoById(primaryKey, id, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findByInAwait(
        column: String,
        values: Collection<*>
    ): List<T> = withContext(Dispatchers.IO) {
        findByIn(column, values)
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.countAllAwait(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Long = withContext(Dispatchers.IO) {
        countAll(options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.countAwait(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Long = withContext(Dispatchers.IO) {
        count(params, options).blockingGet()
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.existsAwait(
        params: Map<String, Any>
    ): Boolean = withContext(Dispatchers.IO) {
        exists(params)
    }

    /**
     * 挂起并执行同步分页查询（直接�?Dao 同步方法，无 Rx 链）�?
     *
     * @see RoomRepositoryImpl.findPageList
     */
    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findPageListAwait(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int,
        offset: Int
    ): List<T> = withContext(Dispatchers.IO) {
        findPageList(params, keywordsKey, keywords, orderBy, limit, offset)
    }

    suspend fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findPageListAwait(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): List<T> = withContext(Dispatchers.IO) {
        findPageList(params, orderBy, limit, offset)
    }
}
