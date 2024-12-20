package pers.fz.mvvm.util.update.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import okhttp3.Headers;
import okhttp3.MediaType;
import okio.BufferedSource;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;

/**
 * created by fz on 2024/11/7 14:41
 * describe:
 */
public class DownloadObservable implements ObservableOnSubscribe<File> {
    private final File tempFile;
    private final DownloadInterceptor interceptor;
    private final DownloadNotificationUtil downloadNotificationUtil;
    private final String saveBasePath;
    private final String fileUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DownloadObservable(DownloadInterceptor interceptor, String fileUrl, File tempFile, String saveBasePath) {
        this.tempFile = tempFile;
        this.interceptor = interceptor;
        this.saveBasePath = saveBasePath;
        this.fileUrl = fileUrl;
        downloadNotificationUtil = new DownloadNotificationUtil(BaseApplication.getInstance());
        mainHandler.post(() -> downloadNotificationUtil.showNotification(fileUrl.hashCode()));
    }

    @Override
    public void subscribe(ObservableEmitter<File> emitter) throws Exception {
        try (BufferedSource source = interceptor.getResponseBody().source()) {
            long totalByte = interceptor.getResponseBody().contentLength();
            long downloadByte = 0;
            if (!tempFile.getParentFile().exists()) {
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

                mainHandler.post(() -> downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                        progress, FileUtil.getFileName(fileUrl)));
            }
            randomAccessFile.close();

            String fileName;
            MediaType mediaType = interceptor.getResponseBody().contentType();
            String contentDisposition = findHeaderIgnoreCase(interceptor.getHeaders(), "Content-Disposition");
            //获取请求头中的Content-Disposition，有值的话说明指定了文件名和后缀名
            if (mediaType != null && !TextUtils.isEmpty(contentDisposition)) {
                fileName = FileUtil.autoRenameFileName(saveBasePath, getFileNameFromForceDownloadHeader(contentDisposition));
            } else {
                fileName = FileUtil.autoRenameFileName(saveBasePath, FileUtil.getFileNameByUrl(fileUrl));
            }
            File newFile = new File(saveBasePath + fileName);
            boolean renameSuccess = tempFile.renameTo(newFile);
            mainHandler.post(() -> downloadNotificationUtil.cancelNotification(fileUrl.hashCode()));
            if (renameSuccess) {
                mainHandler.post(() -> Toast.makeText(BaseApplication.getInstance(), "文件已保存至" + newFile.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                emitter.onNext(newFile);
            } else {
                mainHandler.post(() -> Toast.makeText(BaseApplication.getInstance(), "文件已保存至" + tempFile.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                emitter.onNext(tempFile);
            }
            emitter.onComplete();
        } catch (IOException e) {
            mainHandler.post(() -> downloadNotificationUtil.cancelNotification(fileUrl.hashCode()));
            emitter.onError(e);
            emitter.onComplete();
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


