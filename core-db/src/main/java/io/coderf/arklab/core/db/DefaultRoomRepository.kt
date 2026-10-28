package io.coderf.arklab.core.db

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.RequestUiHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * Room 协程仓库默认实现，对齐 [io.coderf.arklab.core.network.DefaultNetworkRepository]。
 *
 * - 不依赖旧 [io.coderf.arklab.common.repository.RoomRepositoryImpl] / Rx blocking 桥接
 * - Loading / 错误仅经 [RequestUi]
 * - [block] 内应调用 Dao 的 Room 原生 `suspend`（如 `insertSuspend`）
 *
 * ```
 * class PersonRoomRepository(
 *     private val dao: PersonDao
 * ) : DefaultRoomRepository() {
 *     fun loadAll() = execute(RoomRequestOptions.withLoading("加载中...")) {
 *         dao.findAllSuspend()
 *     }
 * }
 * ```
 *
 * 需要接入 ViewModel `ensureRepository` 时请继承 [BaseRoomRepository]。
 * 需要通用 CRUD 时可继承 [RoomDataRepository]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/28
 */
open class DefaultRoomRepository(
    requestUi: RequestUi = NoOpRequestUi
) : RoomRepository, RequestUiHost {

    @Volatile
    private var activeRequestUi: RequestUi = requestUi

    override fun setRequestUi(ui: RequestUi?) {
        activeRequestUi = ui ?: NoOpRequestUi
    }

    fun getRequestUi(): RequestUi = activeRequestUi

    /**
     * @param tableName 日志表名；子类可传 Dao 表名
     * @param operation 日志操作名
     * @param payload   日志载荷
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
                    this@DefaultRoomRepository.javaClass.simpleName,
                    operation,
                    tableName,
                    options,
                    payload
                )
                if (options.isShowDialog) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.showLoading(
                            options.dialogMessage,
                            options.isEnableDynamicEllipsis
                        )
                    }
                }
            }
            .onCompletion {
                if (options.isShowDialog) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.hideLoading()
                    }
                }
            }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                sessionHolder[0]?.error(throwable)
                val error = AppError.from(throwable)
                if (error !is AppError.Cancelled) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.showError(error)
                    }
                }
                emit(RequestResult.Error(error))
            }
            .flowOn(Dispatchers.IO)
    }

    override fun <R> execute(
        options: RoomRequestOptions,
        block: suspend () -> R
    ): Flow<RequestResult<R>> = executeInternal(options, "-", "EXECUTE", null, block)
}
