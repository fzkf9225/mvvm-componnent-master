/**
 * 下载模块核心层：配置类、任务模型、共享 Http 构建、Rx / 协程双引擎。
 * <p>
 * 任务模型：{@link io.coderf.arklab.common.utils.download.core.DownloadTask}、
 * {@link io.coderf.arklab.common.utils.download.core.DownloadState}。
 * <p>
 * 共享：{@link io.coderf.arklab.common.utils.download.core.DownloadHttpClients}、
 * {@link io.coderf.arklab.common.utils.download.core.DownloadInterceptor}、
 * {@link io.coderf.arklab.common.utils.download.core.DownloadFileNames}。
 * <p>
 * Rx 调用链：{@link io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadApi}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadObservable}
 * <p>
 * 协程调用链：{@link io.coderf.arklab.common.utils.download.core.DownloadEngine}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadCoroutineApi}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadWriter}
 *
 * @author fz
 * @version 1.2.0
 * @since 1.0
 * @updated 2026/11/12
 */
package io.coderf.arklab.common.utils.download.core;
