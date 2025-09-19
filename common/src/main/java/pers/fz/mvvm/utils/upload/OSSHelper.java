package pers.fz.mvvm.utils.upload;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.utils.common.FileUtil;
import pers.fz.mvvm.utils.log.LogUtil;
/**
 * Created by fz on 2023/5/29 15:54
 * describe :对象存储
 */
public class OSSHelper {
    public static final String TAG = OSSHelper.class.getSimpleName();

    private static OSSHelper sOSSHelper;

    public interface OSSCallback {
        void progress(String objectKey, String fileName, int progress);

        void onError(Exception exception);
    }

    private OSSHelper() {
    }

    public static OSSHelper getInstance() {
        if (sOSSHelper == null) {
            sOSSHelper = new OSSHelper();
        }
        return sOSSHelper;
    }

    /**
     * 文件下载
     */
    public void downloadOssObject(String accessKey, String secretKey, String secretToken, String bucketName, String objectName, String savePath, OSSCallback callback) throws Exception {
        ThreadExecutor.getInstance().execute(new DownloadRunnable(objectName, accessKey, secretKey, secretToken, bucketName, savePath, callback));
    }

    /**
     * 上传
     */
    private static final class DownloadRunnable implements Runnable {
        private final String objectName;
        private final String accessKey;
        private final String secretKey;
        private final String secretToken;
        private final String bucketName;
        private final String savePath;
        private final OSSCallback callback;

        public DownloadRunnable(String objectName, String accessKey, String secretKey, String secretToken, String bucketName, String savePath, OSSCallback callback) {
            this.objectName = objectName;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.secretToken = secretToken;
            this.bucketName = bucketName;
            this.savePath = savePath;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                AmazonS3 s3 = new AmazonS3Client(new AWSSessionCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return accessKey;//minio的key
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return secretKey;//minio的密钥
                    }

                    @Override
                    public String getSessionToken() {
                        return secretToken;
                    }
                }, Region.getRegion(Regions.CN_NORTH_1), new ClientConfiguration());
                ObjectMetadata metadata = s3.getObjectMetadata(bucketName, objectName);
                // 从对象键中提取文件名
                String objectKey = objectName;
                String fileName = FileUtil.autoRenameFileName(savePath, FileUtil.getFileNameByUrl(objectKey));
                String targetFilePath = savePath + fileName;
                LogUtil.d(TAG, "文件名：" + fileName);
                LogUtil.d(TAG, "object length = " + metadata.getContentLength() + " type = " + metadata.getContentType());
                S3Object object = s3.getObject(bucketName, objectName);
                InputStream stream = object.getObjectContent();

                // 读取输入流直到EOF并打印到控制台。
                byte[] buf = new byte[1024 * 10];
                int bytesRead;
                long currentBytes = 0;
                int prevProcess = 0;
                long totalBytes = metadata.getContentLength();
                FileOutputStream fileOutputStream = new FileOutputStream(targetFilePath);
                while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                    currentBytes += bytesRead;
                    int process = (int) (currentBytes * 100 / totalBytes);
                    if (callback != null && process != prevProcess) {
                        callback.progress(null, targetFilePath, process);
                        prevProcess = process;
                    }
                    fileOutputStream.write(buf, 0, bytesRead);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }
    }

    /**
     * 文件上传
     */
    public void uploadOssObject(String endPoint, String accessKey, String secretKey, String secretToken, String bucketName, Uri uri, OSSCallback callback) {
        ThreadExecutor.getInstance().execute(new UploadRunnable(endPoint, accessKey, secretKey, secretToken, bucketName, uri, callback));
    }

    private static final class UploadRunnable implements Runnable {
        private final String endPoint;
        private final String accessKey;
        private final String secretKey;
        private final String secretToken;
        private final String bucketName;
        private final Uri uri;
        private long transferred;
        private final OSSCallback callback;
        private long size = -1;
        private String fileName = null;
        private int lastProcess = -1;

        public UploadRunnable(String endPoint, String accessKey, String secretKey, String secretToken, String bucketName, Uri uri, OSSCallback callback) {
            this.endPoint = endPoint;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.secretToken = secretToken;
            this.bucketName = bucketName;
            this.uri = uri;
            this.callback = callback;
        }

        @SuppressLint("Range")
        @Override
        public void run() {
            try {
                AmazonS3 s3 = new AmazonS3Client(new AWSSessionCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return accessKey;//minio的key
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return secretKey;//minio的密钥
                    }

                    @Override
                    public String getSessionToken() {
                        return secretToken;//minio的Token
                    }
                }, Region.getRegion(Regions.CN_NORTH_1), new ClientConfiguration());
                //服务器地址
                s3.setEndpoint(endPoint);
                ContentResolver contentResolver = BaseApplication.getInstance().getContentResolver();
                if (contentResolver == null) {
                    throw new BaseException("文件管理器异常！", BaseException.OSS_ERROR);
                }
                InputStream inputStream = contentResolver.openInputStream(uri);

                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor == null) {
                    throw new BaseException("未查找到本地文件信息或本地文件不存在", BaseException.OSS_ERROR);
                }
                if (!cursor.moveToFirst()) {
                    throw new BaseException("未查找到本地文件信息或本地文件不存在", BaseException.OSS_ERROR);
                }
                size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                LogUtil.show(TAG, "文件大小：" + size);
                LogUtil.show(TAG, "文件名称：" + fileName);
                transferred = 0;
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(size);
                String objectKey = bucketName + "/" + fileName;
                PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream, objectMetadata);
                s3.putObject(request.withGeneralProgressListener(progressEvent -> {
                    if (callback != null) {
                        transferred += progressEvent.getBytesTransferred();
                        int process = (int) (transferred * 100 / size);
                        LogUtil.show(TAG, "--------------------上传中" + process + "--------------------");
                        //进度100会回调两次，这样就是避免重复调用完成的方法
                        if (lastProcess != process) {
                            callback.progress(objectKey, fileName, process);
                        }
                        lastProcess = process;
                    }
                }));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.show(TAG, "上传错误：" + e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }
    }

    /**
     * 文件上传
     */
    public void uploadOssObjectFile(String endPoint, String accessKey, String secretKey, String secretToken, String bucketName, File file, OSSCallback callback) {
        ThreadExecutor.getInstance().execute(new UploadFileRunnable(endPoint, accessKey, secretKey, secretToken, bucketName, file, callback));
    }

    private static final class UploadFileRunnable implements Runnable {
        private final String endPoint;
        private final String accessKey;
        private final String secretKey;
        private final String secretToken;
        private final String bucketName;
        private final File file;
        private long transferred;
        private final OSSCallback callback;
        private int lastProcess = -1;

        public UploadFileRunnable(String endPoint, String accessKey, String secretKey, String secretToken, String bucketName, File file, OSSCallback callback) {
            this.endPoint = endPoint;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.secretToken = secretToken;
            this.bucketName = bucketName;
            this.file = file;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                AmazonS3 s3 = new AmazonS3Client(new AWSSessionCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return accessKey;//minio的key
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return secretKey;//minio的密钥
                    }

                    @Override
                    public String getSessionToken() {
                        return secretToken;//minio的Token
                    }
                }, Region.getRegion(Regions.CN_NORTH_1), new ClientConfiguration());
                //服务器地址
                s3.setEndpoint(endPoint);
                LogUtil.show(TAG, "文件大小：" + file.length());
                LogUtil.show(TAG, "文件名称：" + file.getName());
                transferred = 0;
                String objectKey = bucketName + "/" + file.getName();
                PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, file);

                s3.putObject(request.withGeneralProgressListener(progressEvent -> {
                    if (callback != null) {
                        transferred += progressEvent.getBytesTransferred();
                        int process = (int) (transferred * 100 / file.length());
                        //进度100会回调两次，这样就是避免重复调用完成的方法
                        if (process != lastProcess) {
                            callback.progress(objectKey, file.getName(), process);
                        }
                        lastProcess = process;
                        LogUtil.show(TAG, "--------------------上传中" + process + "--------------------");
                    }
                }));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.show(TAG, "上传错误：" + e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }
    }

    /**
     * @author fz
     * @dec 上传文件
     * @date 2023/5/28 15:42
     */
    public static class ThreadExecutor extends ThreadPoolExecutor {
        private static final int CORE_POOL_SIZE = 1;
        //以CPU总数*2作为线程池上限
        private static final int MAXI_MUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
        private static final int KEEP_ALIVE_TIME = 3;
        private static volatile ThreadExecutor executor;

        private static final ThreadFactory S_THREAD_FACTORY = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "ThreadExecutor #" + mCount.getAndIncrement());
            }
        };

        public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        //单例模式
        public static ThreadExecutor getInstance() {
            if (null == executor) {
                synchronized (ThreadExecutor.class) {
                    if (null == executor) {
                        executor = new ThreadExecutor(CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<>(),
                                S_THREAD_FACTORY);
                    }
                }
            }
            return executor;
        }
    }
}