package io.coderf.arklab.common.utils.download.core;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.core.utils.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * 文件下载的 RxJava {@link ObservableOnSubscribe} 实现（存量 Rx 栈）。
 * <p>
 * 文件名解析见 {@link DownloadFileNames}；协程写盘见 {@link DownloadWriter}。
 *
 * @see DownloadRetrofitFactory
 * @see DownloadWriter
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7
 * @updated 2026/11/12
 */
public class DownloadObservable implements ObservableOnSubscribe<File> {

    /** 断点续传临时文件（{@code .download} 后缀） */
    private final File tempFile;
    /** 持有本次 HTTP 响应，供读取 body 与 headers */
    private final DownloadInterceptor interceptor;
    private final DownloadNotificationUtil downloadNotificationUtil;
    private final String saveBasePath;
    private final String saveFileName;
    private final String fileUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final DownloadListener downloadListener;
    /** 下载完成后是否展示默认的"文件已保存至..." Toast */
    private final boolean showToast;

    public DownloadObservable(DownloadInterceptor interceptor, String fileUrl, File tempFile,
                              String saveBasePath, String saveFileName,
                              DownloadListener downloadListener, boolean showToast) {
        this.tempFile = tempFile;
        this.interceptor = interceptor;
        this.saveBasePath = saveBasePath;
        this.saveFileName = saveFileName;
        this.fileUrl = fileUrl;
        this.downloadListener = downloadListener;
        this.showToast = showToast;
        downloadNotificationUtil = new DownloadNotificationUtil(Config.getInstance().getApplication());
        mainHandler.post(() -> {
            downloadNotificationUtil.showNotification(fileUrl.hashCode());
            if (downloadListener != null) {
                downloadListener.onStart();
            }
        });
    }

    @Override
    public void subscribe(ObservableEmitter<File> emitter) throws Exception {
        emitter.setCancellable(() -> mainHandler.post(() -> {
            downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
            if (downloadListener != null) {
                downloadListener.onCancel();
            }
        }));
        try (okio.BufferedSource ignored = interceptor.getResponseBody().source()) {
            long totalByte = interceptor.getResponseBody().contentLength();
            long downloadByte = 0;
            if (!Objects.requireNonNull(tempFile.getParentFile()).exists()) {
                //noinspection ResultOfMethodCallIgnored
                tempFile.getParentFile().mkdirs();
            }

            byte[] buffer = new byte[1024 * 4];
            RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rwd");
            long tempFileLen = tempFile.length();

            randomAccessFile.seek(tempFileLen);
            while (true) {
                int len = interceptor.getResponseBody().byteStream().read(buffer);
                if (len == -1) {
                    break;
                }

                randomAccessFile.write(buffer, 0, len);
                downloadByte += len;
                final int progress = totalByte > 0
                        ? (int) ((downloadByte * 100) / totalByte)
                        : 0;

                mainHandler.post(() -> {
                    downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtil.getFileName(fileUrl));
                    if (downloadListener != null) {
                        downloadListener.onProgress(progress);
                    }
                });
            }
            randomAccessFile.close();

            String fileName = DownloadFileNames.resolveSaveFileName(
                    saveBasePath, saveFileName, fileUrl,
                    interceptor.getHeaders(), interceptor.getResponseBody());
            File newFile = new File(saveBasePath, fileName);
            // 重命名失败（如目标文件被占用）时仍以临时文件作为下载结果
            File resultFile = tempFile.renameTo(newFile) ? newFile : tempFile;
            mainHandler.post(() -> downloadNotificationUtil.cancelNotification(fileUrl.hashCode()));
            mainHandler.post(() -> {
                if (showToast) {
                    Toast.makeText(Config.getInstance().getApplication(),
                            "文件已保存至" + resultFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                if (downloadListener != null) {
                    downloadListener.onFinish(resultFile);
                }
            });
            emitter.onNext(resultFile);
            emitter.onComplete();
        } catch (IOException e) {
            mainHandler.post(() -> {
                downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                if (downloadListener != null) {
                    downloadListener.onError(e);
                }
            });
            if (!emitter.isDisposed()) {
                emitter.onError(e);
            }
        }
    }
}
