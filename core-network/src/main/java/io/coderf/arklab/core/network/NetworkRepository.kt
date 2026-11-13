package io.coderf.arklab.core.network

import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.bean.RetryPolicy
import kotlinx.coroutines.flow.Flow

/**
 * 新版网络 Repository 契约：只暴露 Flow 统一入口。
 *
 * 能力（由 [BaseNetworkRepository] 实现）：
 * - 瞬时网络失败的指数退避重试（[RetryPolicy]；业务错误不重试）
 * - 登录过期 Token 刷新 + Single-Flight（[TokenRefresher]）
 *
 * **不负责 UI**：Loading / Toast / 错误展示由 ViewModel
 * [io.coderf.arklab.common.base.BaseViewModel.launchRequest] + `collectSuccess` 处理；
 * 本层只返回 [RequestResult]。勿在块内对 Flow 裸 `collect`（否则错误不会进 Host）。
 *
 * 响应体 `code/msg/data` 自动拆包仍由 Retrofit
 * [io.coderf.arklab.common.base.BaseConverterFactory] 完成。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/11/13
 */
interface NetworkRepository {
    /**
     * 发起请求，返回 [RequestResult] Flow；不含 Loading / 错误 UI。
     */
    fun <T> request(
        options: RequestOptions = RequestOptions.defaults(),
        block: suspend () -> T
    ): Flow<RequestResult<T>>
}