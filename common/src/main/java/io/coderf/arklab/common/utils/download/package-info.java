/**
 * 文件下载与 APK 更新模块。
 * <p>
 * <b>模块结构</b>
 * <ul>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadManager} — 通用文件下载（单文件 / 批量）</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.UpdateManager} — APK 静默更新与更新说明对话框</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.DownloadPermissionHelper} — 存储权限申请与授权后自动重试</li>
 *   <li>{@link io.coderf.arklab.common.utils.download.core} — 配置类与网络下载核心实现</li>
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
 * @since 2024/11/7
 */
package io.coderf.arklab.common.utils.download;
