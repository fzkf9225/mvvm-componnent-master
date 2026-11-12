package io.coderf.arklab.common.base

import androidx.lifecycle.viewModelScope
import io.coderf.arklab.common.repository.IRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 在 [viewModelScope] 中启动并登记 UI 请求，便于 Loading 取消时 [BaseViewModel.cancelUiRequests]。
 *
 * 推荐写法：
 * ```
 * launchUiRequest {
 *     iRepository.load().collect { result -> result.onSuccess { ... } }
 * }
 * ```
 *
 * @param context 额外 CoroutineContext，默认空
 * @param start 启动模式，默认 DEFAULT
 * @param block 挂起业务块
 * @return 已登记的 [Job]
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 * @created 2026/11/12
 */
fun <IR : IRepository> BaseViewModel<IR>.launchUiRequest(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val job = viewModelScope.launch(context, start, block)
    trackUiRequest(job)
    return job
}
