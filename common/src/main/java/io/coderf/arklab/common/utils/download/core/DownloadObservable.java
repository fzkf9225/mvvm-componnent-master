package io.coderf.arklab.common.utils.download.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.coderf.arklab.common.api.BaseApplication;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import okhttp3.Headers;
import okhttp3.MediaType;
import okio.BufferedSource;

/**
 * 文件下载的 RxJava {@link ObservableOnSubscribe} 实现。
 * <p>
 * 职责：
 * <ol>
 *   <li>从 {@link DownloadInterceptor} 读取响应体，追加写入临时文件（支持断点续传）</li>
 *   <li>更新通知栏进度并回调 {@link DownloadListener}</li>
 *   <li>下载完成后重命名临时文件为目标文件名（自动去重命名）</li>
 * </ol>
 * 取消订阅（dispose）时会关闭通知并触发 {@link DownloadListener#onCancel()}。
 *
 * @author fz
 * @see DownloadRetrofitFactory
 * @since 2024/11/7
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

    public DownloadObservable(DownloadInterceptor interceptor, String fileUrl, File tempFile, String saveBasePath, String saveFileName) {
        this(interceptor, fileUrl, tempFile, saveBasePath, saveFileName, null);
    }

    public DownloadObservable(DownloadInterceptor interceptor, String fileUrl, File tempFile, String saveBasePath, String saveFileName, DownloadListener downloadListener) {
        this.tempFile = tempFile;
        this.interceptor = interceptor;
        this.saveBasePath = saveBasePath;
        this.saveFileName = saveFileName;
        this.fileUrl = fileUrl;
        this.downloadListener = downloadListener;
        downloadNotificationUtil = new DownloadNotificationUtil(BaseApplication.getInstance());
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
        try (BufferedSource source = interceptor.getResponseBody().source()) {
            long totalByte = interceptor.getResponseBody().contentLength();
            long downloadByte = 0;
            if (!Objects.requireNonNull(tempFile.getParentFile()).exists()) {
                boolean mkdir = tempFile.getParentFile().mkdirs();
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
                int progress = (int) ((downloadByte * 100) / totalByte);

                mainHandler.post(() -> {
                    downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtil.getFileName(fileUrl));
                    if (downloadListener != null) {
                        downloadListener.onProgress(progress);
                    }
                });
            }
            randomAccessFile.close();

            String fileName;
            if (TextUtils.isEmpty(saveFileName)) {
                MediaType mediaType = interceptor.getResponseBody().contentType();
                String contentDisposition = findHeaderIgnoreCase(interceptor.getHeaders(), "Content-Disposition");
                //获取请求头中的Content-Disposition，有值的话说明指定了文件名和后缀名
                if (mediaType != null && !TextUtils.isEmpty(contentDisposition)) {
                    fileName = FileUtil.autoRenameFileName(saveBasePath, getFileNameFromForceDownloadHeader(contentDisposition));
                } else {
                    fileName = FileUtil.autoRenameFileName(saveBasePath, FileUtil.getFileNameByUrl(fileUrl));
                }
            } else {
                File targetFile = new File(saveBasePath, saveFileName);
                targetFile.deleteOnExit();
                fileName = saveFileName;
            }
            File newFile = new File(saveBasePath, fileName);
            boolean renameSuccess = tempFile.renameTo(newFile);
            mainHandler.post(() -> downloadNotificationUtil.cancelNotification(fileUrl.hashCode()));
            if (renameSuccess) {
                mainHandler.post(() -> {
                    Toast.makeText(BaseApplication.getInstance(), "文件已保存至" + newFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    if (downloadListener != null) {
                        downloadListener.onFinish(newFile);
                    }
                });
                emitter.onNext(newFile);
            } else {
                mainHandler.post(() -> {
                    Toast.makeText(BaseApplication.getInstance(), "文件已保存至" + tempFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    if (downloadListener != null) {
                        downloadListener.onFinish(tempFile);
                    }
                });
                emitter.onNext(tempFile);
            }
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

    /**
     * 忽略大小写查找请求头参数
     */
    private static String findHeaderIgnoreCase(Headers headers, String headerName) {
        for (String name : headers.names()) {
            if (name.equalsIgnoreCase(headerName)) {
                return headers.get(name);
            }
        }
        return null;
    }

    private static String getFileNameFromForceDownloadHeader(String contentDispositionHeader) {
        if (TextUtils.isEmpty(contentDispositionHeader)) {
            return "unknown";
        }
        // 匹配Content-Disposition中的filename属性
        Pattern pattern = Pattern.compile(".*filename=\"?([^\\s;]+)\"?.*");
        Matcher matcher = pattern.matcher(contentDispositionHeader.toLowerCase());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "unknown";
    }
}


