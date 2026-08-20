package io.coderf.arklab.common.repository

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.dao.BaseRoomDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * [RoomRepositoryImpl] 的 Kotlin Flow 扩展。
 *
 * 通过 callbackFlow 桥接既有 RxJava3 API，**不替换**原有 Completable / Flowable / Single 调用方式，
 * 便于协程 ViewModel 与 Flow 分页等场景按需选用。
 *
 * ## 用法示例
 * ```kotlin
 * import io.coderf.arklab.common.repository.RoomRepositoryFlowExt.findAllFlow
 *
 * class MyViewModel(repo: PersonRepository) : ViewModel() {
 *     val list = repo.findAllFlow()
 *         .catch { ... }
 *         .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
 * }
 * ```
 *
 * @author fz
 * @see RoomRepositoryCoroutineExt
 * @see RoomRepositoryImpl
 */
object RoomRepositoryFlowExt {

    /**
     * 查询全表，发射一次列表后结束。
     *
     * @param options 默认静默，无 Loading
     */
    fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findAllFlow(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<List<T>> = rxFlowableToFlow { findAll(options) }

    /**
     * 按字符串主键查询单条。
     *
     * @param id 主键值
     * @param options 请求配置
     */
    fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findInfoByIdFlow(
        id: String,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<T> = rxSingleToFlow { findInfoById(id, options) }

    /**
     * 插入单条，完成时发射 [Unit]。
     */
    fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.insertFlow(
        obj: T,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<Unit> = rxCompletableToFlow { insert(obj, options) }

    /**
     * 同步分页查询转 Flow（在 IO 线程执行 Dao 同步方法）。
     * 适用于已在 [RoomRepositoryImpl.findPageList] 封装的条件/关键字/排序分页。
     *
     * @param params 等值条件
     * @param keywordsKey 模糊搜索列集合，可为 null
     * @param keywords 搜索关键字
     * @param orderBy 排序列
     * @param limit 每页条数
     * @param offset 偏移（页码 × limit）
     */
    fun <T : Any, DB : BaseRoomDao<T>, BV : BaseView?> RoomRepositoryImpl<T, DB, BV>.findPageListFlow(
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

    /** Flowable → Flow，订阅在 IO，取消时 dispose */
    private fun <T : Any> rxFlowableToFlow(block: () -> io.reactivex.rxjava3.core.Flowable<T>): Flow<T> =
        callbackFlow {
            val disposable = block().subscribe(
                { value -> trySend(value); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)

    /** Single → Flow */
    private fun <T : Any> rxSingleToFlow(block: () -> io.reactivex.rxjava3.core.Single<T>): Flow<T> =
        callbackFlow {
            val disposable = block().subscribe(
                { value -> trySend(value); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)

    /** Completable → Flow&lt;Unit&gt; */
    private fun rxCompletableToFlow(block: () -> io.reactivex.rxjava3.core.Completable): Flow<Unit> =
        callbackFlow {
            val disposable = block().subscribe(
                { trySend(Unit); close() },
                { error -> close(error) }
            )
            awaitClose { disposable.dispose() }
        }.flowOn(Dispatchers.IO)
}
