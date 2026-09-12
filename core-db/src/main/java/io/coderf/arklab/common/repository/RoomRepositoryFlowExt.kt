package io.coderf.arklab.common.repository

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * [RoomRepositoryImpl] �?Kotlin Flow 扩展�?
 *
 * 通过 callbackFlow 桥接既有 RxJava3 API，不替换原有 Completable / Flowable / Single�?
 * 多数为「发射一次后结束」的桥接，持续观察表变更请用 LiveData�?
 *
 * @see RoomRepositoryCoroutineExt
 * @see RoomRepositoryImpl
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
object RoomRepositoryFlowExt {

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findAllFlow(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<List<T>> = rxFlowableToFlow { findAll(options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdFlow(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<T> = rxSingleToFlow { findInfoById(id, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdFlow(
        id: Long,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<T> = rxSingleToFlow { findInfoById(id, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findInfoByIdFlow(
        primaryKey: String,
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<T> = rxSingleToFlow { findInfoById(primaryKey, id, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findByInFlow(
        column: String,
        values: Collection<*>,
        showLoading: Boolean = false
    ): Flow<List<T>> = rxFlowableToFlow { findByInFlowable(column, values, showLoading) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.countAllFlow(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Long> = rxSingleToFlow { countAll(options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.countFlow(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Long> = rxSingleToFlow { count(params, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.insertFlow(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { insert(obj, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.insertFlow(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { insert(objs, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.upsertFlow(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { upsert(obj, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.upsertFlow(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { upsert(objs, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.updateFlow(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { update(obj, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.updateFlow(
        objs: List<T>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { update(objs, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteFlow(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { delete(obj, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteByParamsCountFlow(
        params: Map<String, Any>,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Int> = rxSingleToFlow { deleteByParamsCount(params, options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.deleteAllCountFlow(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Int> = rxSingleToFlow { deleteAllCount(options) }

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findPageListFlow(
        params: Map<String, Any>,
        keywordsKey: Set<String>?,
        keywords: String?,
        orderBy: String,
        limit: Int,
        offset: Int
    ): Flow<List<T>> = callbackFlow {
        trySend(findPageList(params, keywordsKey, keywords, orderBy, limit, offset))
        close()
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    fun <T : Any, DB : BaseRoomDao<T>> RoomRepositoryImpl<T, DB>.findPageListFlow(
        params: Map<String, Any>,
        orderBy: String,
        limit: Int = 10,
        offset: Int = 0
    ): Flow<List<T>> = callbackFlow {
        trySend(findPageList(params, orderBy, limit, offset))
        close()
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    private fun <T : Any> rxFlowableToFlow(block: () -> io.reactivex.rxjava3.core.Flowable<T>): Flow<T> =
        callbackFlow {
            val disposable = block().subscribe(
                { value -> trySend(value); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)

    private fun <T : Any> rxSingleToFlow(block: () -> io.reactivex.rxjava3.core.Single<T>): Flow<T> =
        callbackFlow {
            val disposable = block().subscribe(
                { value -> trySend(value); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)

    private fun rxCompletableToFlow(block: () -> io.reactivex.rxjava3.core.Completable): Flow<Unit> =
        callbackFlow {
            val disposable = block().subscribe(
                { trySend(Unit); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)
}
