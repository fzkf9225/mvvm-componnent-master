package io.coderf.arklab.core.db

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.request.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * Room 协程仓库基类（对齐 [io.coderf.arklab.core.network.BaseNetworkRepository]）。
 *
 * **不负责 UI**：Loading / Toast / 业务错误由 ViewModel
 * [io.coderf.arklab.common.base.BaseViewModel.launchRequest] + collectSuccess 处理；
 * [RoomRequestOptions.isShowDialog] / [RoomRequestOptions.isDeliverErrorToUi] 对本类无效。
 *
 * `
 * class PersonRoomRepository(private val dao: PersonDao) : DefaultRoomRepository() {
 *     fun loadAll() = execute(RoomRequestOptions.query(false)) {
 *         dao.findAllSuspend()
 *     }
 * }
 * // ViewModel:
 * launchRequest(RequestOptions.cancellable()) {
 *     repository.loadAll().collectSuccess { ... }
 * }
 * `
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/11/13
 */
open class BaseRoomRepository : RoomRepository {

    /**
     * @param tableName 表名（日志用，可取 Dao 名）
     * @param operation 操作名
     * @param payload   可选载荷
     */
    protected open fun <R> executeInternal(
        options: RoomRequestOptions,
        tableName: String,
        operation: String,
        payload: Any? = null,
        block: suspend () -> R
    ): Flow<RequestResult<R>> {
        val sessionHolder = arrayOfNulls<RoomLog.Session>(1)
        return flow {
            val timeoutSec = options.timeoutSeconds
            val raw = if (timeoutSec > 0) {
                withTimeout(timeoutSec.seconds) { block() }
            } else {
                block()
            }
            @Suppress("UNCHECKED_CAST")
            val data = raw as R
            if (options.isThrowOnEmptyList && data is List<*> && data.isEmpty()) {
                throw BaseException(BaseException.ErrorType.NOT_FOUND)
            }
            sessionHolder[0]?.success(data)
            @Suppress("UNCHECKED_CAST")
            emit(RequestResult.Success(data) as RequestResult<R>)
        }
            .onStart {
                sessionHolder[0] = RoomLog.begin(
                    this@BaseRoomRepository.javaClass.simpleName,
                    operation,
                    tableName,
                    options,
                    payload
                )
            }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                sessionHolder[0]?.error(throwable)
                emit(RequestResult.Error(AppError.from(throwable)))
            }
            .flowOn(Dispatchers.IO)
    }

    override fun <R> execute(
        options: RoomRequestOptions,
        block: suspend () -> R
    ): Flow<RequestResult<R>> = executeInternal(options, "-", "EXECUTE", null, block)
}
