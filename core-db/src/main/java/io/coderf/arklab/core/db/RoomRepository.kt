package io.coderf.arklab.core.db

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.bean.RequestResult
import kotlinx.coroutines.flow.Flow

/**
 * 新版 Room 协程 Repository 契约：统一 [execute] 入口，返回 [Flow]&lt;[RequestResult]&gt;。
 *
 * 对齐网络层 [io.coderf.arklab.core.network.NetworkRepository]：仓库**无 UI**；
 * Loading / 错误由 ViewModel `launchRequest` + `collectSuccess` 处理。
 * 业务块内使用 Dao 的 Room 原生 suspend。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
interface RoomRepository {
    /**
     * 执行一次本地数据库操作。
     *
     * @param options 超时 / 空列表等；showDialog / deliverErrorToUi 对本实现无效
     * @param block   挂起代码块，通常直接调用 `dao.xxxSuspend`
     */
    fun <R> execute(
        options: RoomRequestOptions = RoomRequestOptions.silent(),
        block: suspend () -> R
    ): Flow<RequestResult<R>>
}
