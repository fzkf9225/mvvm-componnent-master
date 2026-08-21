package io.coderf.arklab.core.request

/**
 * Token 刷新契约（新版网络栈）。
 *
 * 对应旧版 [io.coderf.arklab.common.inter.RetryService] / [io.coderf.arklab.common.inter.FlowRetryService]，
 * 由业务模块（如 user）提供 Singleton 实现，并在实现内部做 **Single-Flight**
 *（协程 [kotlinx.coroutines.CompletableDeferred] 多路 await，等价于 RxJava `share()`），
 * 避免并发 401 时重复消费 refresh_token。
 *
 * **作用域：按 ApiRetrofit / ApiService 实例，不是整个 App 进程全局。**
 * 在 Module 里对某个 Builder `setTokenRefresher` 或 `setFlowRetryService`（实现类同时是
 * TokenRefresher 时会自动挂上）后，只有使用该 ApiService 的 [io.coderf.arklab.core.network.DefaultNetworkRepository]
 *（构造传入 boundApiService）才会鉴权重试；另一个未配置的 ApiService 不受影响。
 *
 * 解析顺序见 [io.coderf.arklab.core.network.DefaultNetworkRepository.resolveTokenRefresher]：
 * 1. Repository 构造局部 TokenRefresher
 * 2. boundApiService.retrofit.builder.tokenRefresher（或 FlowRetryService as TokenRefresher）
 */
interface TokenRefresher {

    /**
     * 是否因登录过期 / 401 等进入刷新并重试原请求。
     * 实现内应维护共享重试计数，超过上限返回 false。
     */
    suspend fun shouldRefresh(throwable: Throwable): Boolean

    /**
     * 执行刷新；并发调用必须复用同一次网络请求（Single-Flight）。
     * 刷新失败应抛出异常，由 [io.coderf.arklab.core.network.DefaultNetworkRepository] 转为 [RequestResult.Error]。
     */
    suspend fun refresh()

    /**
     * 是否属于鉴权类失败（用于避免鉴权失败后再走通用指数退避重试）。
     */
    fun isAuthFailure(throwable: Throwable): Boolean = false
}
