package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.DefaultNetworkRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.RequestUi
import kotlinx.coroutines.flow.Flow

/**
 * 新版 NetworkRepository 用法示例（可直接对照迁移旧 Repository）。
 *
 * ```
 * viewModelScope.launch {
 *   sample.requestHello()
 *     .collect { result ->
 *       result.onSuccess { data -> ... }
 *             .onError { err -> /* 一般已由 RequestUi 展示 */ }
 *     }
 * }
 * ```
 */
class SampleCoreNetworkRepository(
    requestUi: RequestUi
) : DefaultNetworkRepository(requestUi) {

    fun requestHello(
        options: RequestOptions = RequestOptions.defaults()
    ): Flow<RequestResult<String>> {
        return request(options) {
            // 替换为真实 api 调用，例如 api.hello()
            "hello-from-core-network"
        }
    }
}
