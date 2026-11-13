/**
 * 文件下载与 APK 更新模块。
 * <p>
 * <b>双栈说明（1.2.0+）</b>
 * <ul>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadClient} — <b>协程真内核</b>（推荐新代码）：
 *       Flow / suspend，调度与写盘不经过 RxJava</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadManager} — 存量 RxJava3 栈，冻结保留直至调用方迁完</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.UpdateManager} — APK 更新（已委托 {@code DownloadClient}）</li>
 * </ul>
 * <p>
 * <b>模块结构</b>
 * <ul>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadPermissionHelper} — 存储权限；协程用
 *       {@link io.coderf.arklab.common.utils.download.DownloadPermissionHelperKtx}</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.core} — 配置、任务模型、HttpClients、Rx/协程引擎</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.listener} — 下载进度 / 更新按钮监听</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.util} — 安装 APK、通知栏等工具</li>
 * </ul>
 * <p>
 * <b>推荐用法（协程 + 可选 RequestUi）</b>
 * <pre>{@code
 * launchRequest(RequestOptions(showLoading = false, deliverErrorToUi = false), onSuccess = { file -> ... }) {
 *     flow {
 *         emit(RequestResult.Success(
 *             DownloadClient.getInstance().downloadAwait(
 *                 DownloadConfig.Builder(activity, url).build(),
 *                 DownloadOptions.defaults(),
 *                 networkRequestUiHost // 失败自动 Toast；取消不报错
 *             )
 *         ))
 *     }
 * }
 * // 需要进度条 / 自定义失败时再 collect DownloadEvent
 * }</pre>
 * <p>
 * <b>存量用法（Rx）</b>
 * <pre>{@code
 * DownloadManager.getInstance().download(
 *     new DownloadConfig.Builder(activity, url).build()
 * ).subscribe(file -> { }, Throwable::printStackTrace);
 * }</pre>
 *
 * @author fz
 * @version 1.2.0
 * @since 1.0
 * @created 2024/11/7
 * @updated 2026/11/12
 */
package io.coderf.arklab.common.utils.download;
