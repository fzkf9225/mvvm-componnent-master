/**
 * 文件下载与 APK 更新模块。
 * <p>
 * <b>模块结构</b>
 * <ul>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadManager} — 通用文件下载（单文件 / 批量），任务查询/取消与全局并行调度</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.UpdateManager} — APK 静默更新与更新说明对话框（内部委托 DownloadManager）</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadPermissionHelper} — 存储权限申请与授权后自动重试</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.core} — 配置、{@code DownloadTask}/{@code DownloadState} 与网络下载核心</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.listener} — 下载进度 / 更新按钮监听</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.util} — 安装 APK、通知栏等工具</li>
 * </ul>
 * <p>
 * <b>推荐用法</b>
 * <pre>{@code
 * // 单文件下载
 * DownloadManager.getInstance().download(
 *     new DownloadConfig.Builder(activity, url)
 *         .setSaveFileName("doc.pdf")
 *         .setVerifyRepeatDownload(true)
 *         .build()
 * ).subscribe(file -> { }, Throwable::printStackTrace);
 *
 * // 可选：全局最多 3 路并行，其余排队（默认不限制）
 * DownloadManager.getInstance().setMaxParallel(3);
 *
 * // 查询 / 取消进行中任务
 * List&lt;DownloadTask&gt; tasks = DownloadManager.getInstance().getDownloadingTasks();
 * DownloadManager.getInstance().cancel(url);
 *
 * // 显示更新对话框
 * UpdateManager.getInstance().checkUpdateInfo(
 *     new UpdateConfig.Builder(activity, apkUrl)
 *         .setUpdateMessage("修复已知问题")
 *         .setCurrentVersionName("2.0.0")
 *         .build()
 * );
 * }</pre>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7
 */
package io.coderf.arklab.common.utils.download;
