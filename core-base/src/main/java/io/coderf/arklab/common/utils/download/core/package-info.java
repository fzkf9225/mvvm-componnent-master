/**
 * 下载模块核心层：配置类、Retrofit 网络封装、断点续传与进度写入。
 * <p>
 * 调用链：{@link io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadInterceptor}
 * → {@link io.coderf.arklab.common.utils.download.core.DownloadObservable}
 */
package io.coderf.arklab.common.utils.download.core;
