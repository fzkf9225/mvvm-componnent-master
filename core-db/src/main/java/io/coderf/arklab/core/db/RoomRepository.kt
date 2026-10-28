package io.coderf.arklab.core.db

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.bean.RequestResult
import kotlinx.coroutines.flow.Flow

/**
 * 新版 Room 协程 Repository 契约：统一 [execute] 入口，返回 [Flow]&lt;[RequestResult]&gt;。
 *
 * 对齐网络层 [io.coderf.arklab.core.network.NetworkRepository]：
 * Loading / 错误走 [io.coderf.arklab.core.request.RequestUi]，业务块内使用 Dao 的 Room 原生 suspend。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/28
 */
interface RoomRepository {
    /**
     * 执行一次本地数据库操作。
     *
     * @param options Loading / 超时 / 空列表异常等配置
     * @param block   挂起代码块，通常直接调用 `dao.xxxSuspend`
     */
    fun <R> execute(
        options: RoomRequestOptions = RoomRequestOptions.silent(),
        block: suspend () -> R
    ): Flow<RequestResult<R>>
}
