package io.coderf.arklab.core.network

import io.coderf.arklab.common.inter.FlowRetryService

/**
 * Token 刷新契约（新版网络栈，鉴权刷新能力统一入口）。
 *
 * 对应旧版 [io.coderf.arklab.common.inter.RetryService] / [FlowRetryService]，
 * 由业务模块（如 user）提供 Singleton 实现，并在实现内部做 **Single-Flight**
 * （协程 [kotlinx.coroutines.CompletableDeferred] 多路 await，等价于 RxJava `share()`），
 * 避免并发 401 时重复消费 refresh_token。
 *
 * **新业务接入只需实现本接口**，无需再实现 [FlowRetryService]：
 * 旧栈方法（[shouldRetry] / [refreshToken] / [setMaxRetryCount] / [getMaxRetryCount]）
 * 均提供默认实现并委托给新方法；[io.coderf.arklab.common.api.ApiRetrofit.Builder.setTokenRefresher]
 * 会经 [asFlowRetryService] 自动适配旧 Flow 栈（FlowRepositoryImpl），功能保持不变。
 *
 * **作用域：按 ApiRetrofit / ApiService 实例，不是整个 App 进程全局。**
 * 在 Module 里对某个 Builder `setTokenRefresher` 或 `setFlowRetryService`（实现类同时是
 * TokenRefresher 时会自动挂上）后，只有使用该 ApiService 的 [DefaultNetworkRepository]
 * （构造传入 boundApiService）才会鉴权重试；另一个未配置的 ApiService 不受影响。
 *
 * 解析顺序见 [DefaultNetworkRepository.resolveTokenRefresher]：
 * 1. Repository 构造局部 TokenRefresher
 * 2. boundApiService.retrofit.builder.tokenRefresher（或 FlowRetryService as TokenRefresher）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 15:08
 * @updated 2026/10/13
 */
interface TokenRefresher {

    /**
     * 是否因登录过期 / 401 等进入刷新并重试原请求。
     * 实现内应维护共享重试计数，超过上限返回 false。
     */
    suspend fun shouldRefresh(throwable: Throwable): Boolean

    /**
     * 执行刷新；并发调用必须复用同一次网络请求（Single-Flight）。
     * 刷新失败应抛出异常，由 [DefaultNetworkRepository] 转为 [io.coderf.arklab.core.bean.RequestResult.Error]。
     */
    suspend fun refresh()

    /**
     * 是否属于鉴权类失败（用于避免鉴权失败后再走通用指数退避重试）。
     */
    fun isAuthFailure(throwable: Throwable): Boolean = false

    /**
     * 旧栈入口：判断是否应该重试，默认委托 [shouldRefresh]。
     */
    suspend fun shouldRetry(throwable: Throwable): Boolean = shouldRefresh(throwable)

    /**
     * 旧栈入口：执行刷新 Token 等重试操作，默认委托 [refresh]。
     */
    suspend fun refreshToken() = refresh()

    /**
     * 旧栈入口：设置最大重试次数。重试上限由实现内部维护时可忽略；
     * 若旧 Flow 栈需按 [getMaxRetryCount] 做外部次数判定，应在实现中覆写。
     */
    fun setMaxRetryCount(maxRetryCount: Int) = Unit

    /**
     * 旧栈入口：获取最大重试次数，默认不限（由 [shouldRefresh] 内部计数兜底）。
     */
    fun getMaxRetryCount(): Int = Int.MAX_VALUE

    companion object {
        /**
         * 适配为旧 [FlowRetryService]，供旧 Flow 栈（[io.coderf.arklab.common.repository.FlowRepositoryImpl]）使用。
         * 适配器无状态、全部委托底层刷新器，Single-Flight 状态仍在 [TokenRefresher] 实现内，
         * 保证新旧两栈共享同一刷新实例。
         */
        @JvmStatic
        fun asFlowRetryService(refresher: TokenRefresher?): FlowRetryService? =
            refresher?.let { FlowRetryServiceAdapter(it) }
    }
}

/**
 * [TokenRefresher] → [FlowRetryService] 无状态适配器。
 * 使只实现 [TokenRefresher] 的业务实现也能被旧 Flow 栈消费，免去业务侧双接口实现。
 */
private class FlowRetryServiceAdapter(
    private val refresher: TokenRefresher
) : FlowRetryService {

    override suspend fun shouldRetry(throwable: Throwable): Boolean =
        refresher.shouldRetry(throwable)

    override suspend fun refreshToken() = refresher.refreshToken()

    override fun setMaxRetryCount(maxRetryCount: Int) = refresher.setMaxRetryCount(maxRetryCount)

    override fun getMaxRetryCount(): Int = refresher.getMaxRetryCount()
}
