package pers.fz.mvvm.util.update;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.common.CommonUtil;
import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.callback.DownloadListener;
import pers.fz.mvvm.util.update.core.RetrofitFactory;

/**
 * @author fz
 * @date 2020/06/19
 */
public class RxNet {
    public static final String TAG = RxNet.class.getSimpleName();
    /**
     * 文件的保存路径
     */
    public static final String PATH = BaseApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator;

    public static void download(final String url, final String saveBasePath, final DownloadCallback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(saveBasePath)) {
            if (null != callback) {
                callback.onError("url or path empty");
            }
            return;
        }
//        File oldFile = new File(filePath);
//        if (oldFile.exists()) {
//            if (null != callback) {
//                callback.onFinish(oldFile);
//            }
//            return;
//        }
        File tempFile = CommonUtil.getTempFile(url, saveBasePath);
        DownloadListener listener = (responseHeaders, responseBody) -> saveFile(responseHeaders, responseBody, url, tempFile, saveBasePath, callback);
        RetrofitFactory.downloadFile(url, tempFile.length(), listener, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (null != callback) {
                    callback.onStart(d);
                }
            }

            @Override
            public void onNext(final ResponseBody responseBody) {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                LogUtil.show(TAG, "onError:" + e.getMessage());
                if (null != callback) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onComplete() {
                LogUtil.show(TAG, "--------onComplete---------");
            }
        });
    }

    private static void saveFile(Headers responseHeaders, final ResponseBody responseBody, String url, final File tempFile, final String saveBasePath, final DownloadCallback callback) {
        boolean downloadSuccss = true;
        try {
            writeFileToDisk(responseBody, tempFile, callback);
        } catch (Exception e) {
            e.printStackTrace();
            downloadSuccss = false;
            if (null != callback) {
                callback.onError("下载错误");
            }
        }

        if (downloadSuccss) {
            String fileName;
            MediaType mediaType = responseBody.contentType();
            String contentDisposition = findHeaderIgnoreCase(responseHeaders, "Content-Disposition");
            //获取请求头中的Content-Disposition，有值的话说明指定了文件名和后缀名
            if (mediaType != null && !TextUtils.isEmpty(contentDisposition)) {
                fileName = FileUtils.autoRenameFileName(saveBasePath, getFileNameFromForceDownloadHeader(contentDisposition));
            } else {
                fileName = FileUtils.autoRenameFileName(saveBasePath, FileUtils.getFileNameByUrl(url));
            }
            File newFile = new File(saveBasePath + fileName);
            boolean renameSuccess = tempFile.renameTo(newFile);
            if (null != callback) {
                if (renameSuccess) {
                    callback.onFinish(newFile);
                } else {
                    callback.onFinish(tempFile);
                }
            }
        }
    }

    private static void writeFileToDisk(ResponseBody responseBody, File tempFile, final DownloadCallback callback) throws IOException {
        long totalByte = responseBody.contentLength();
        long downloadByte = 0;
        if (!Objects.requireNonNull(tempFile.getParentFile()).exists()) {
            boolean mkdir = tempFile.getParentFile().mkdirs();
        }

        byte[] buffer = new byte[1024 * 4];
        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rwd");
        long tempFileLen = tempFile.length();
        randomAccessFile.seek(tempFileLen);
        while (true) {
            int len = responseBody.byteStream().read(buffer);
            if (len == -1) {
                break;
            }
            randomAccessFile.write(buffer, 0, len);
            downloadByte += len;
            callbackProgress(tempFileLen + totalByte, tempFileLen + downloadByte, callback);
        }
        randomAccessFile.close();
    }

    private static void callbackProgress(final long totalByte, final long downloadByte, final DownloadCallback callback) {
        if (null != callback) {
            callback.onProgress(totalByte, downloadByte, (int) ((downloadByte * 100) / totalByte));
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
