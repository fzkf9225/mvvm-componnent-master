package io.coderf.arklab.common.utils.common;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.coderf.arklab.common.listener.FileUploadProgressListener;
import io.coderf.arklab.common.utils.upload.ProgressRequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 文件上传工具类，负责构建 OkHttp Multipart 上传请求体。
 */
public final class FileUploadUtil {

    private static final int BUFFER_SIZE = 4096;
    private static final MediaType MULTIPART_MEDIA_TYPE = MediaType.parse("multipart/form-data");

    private FileUploadUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 构建表单字段 RequestBody（含文件名）。
     *
     * @param context 上下文
     * @param uri     文件 URI
     * @return 表单字段 Map
     */
    @SuppressLint("Range")
    public static Map<String, RequestBody> createFormDataRequestBody(Context context, Uri uri) {
        Map<String, RequestBody> formDataMap = new HashMap<>(1);
        String name = queryDisplayName(context.getContentResolver(), uri);
        if (name == null) {
            throw new RuntimeException("无附件操作权限");
        }
        formDataMap.put("name", RequestBody.create(MULTIPART_MEDIA_TYPE, TextUtils.isEmpty(name) ? "unknown" : name));
        return formDataMap;
    }

    /**
     * 批量构建文件上传 Part（FileDescriptor 方式）。
     */
    public static List<MultipartBody.Part> createFilePart(Context context, List<Uri> uriList,
                                                          FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        for (int i = 0; i < uriList.size(); i++) {
            multiList.add(createFilePart(context, uriList.get(i), i, uriList.size(), uploadListener));
        }
        return multiList;
    }

    /**
     * 构建单个文件上传 Part（FileDescriptor 方式）。
     */
    public static MultipartBody.Part createFilePart(Context context, Uri uri,
                                                    FileUploadProgressListener uploadListener) {
        return createFilePart(context, uri, 0, 1, uploadListener);
    }

    /**
     * 构建单个文件上传 Part（FileDescriptor 方式，支持进度回调）。
     */
    @SuppressLint("Range")
    public static MultipartBody.Part createFilePart(Context context, Uri uri, int currentPos, int totalCount,
                                                    FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String fileName = queryDisplayName(contentResolver, uri);
            ParcelFileDescriptor pdf = contentResolver.openFileDescriptor(uri, "r");
            if (pdf == null) {
                throw new RuntimeException("读取文件失败");
            }
            FileDescriptor fileDescriptor = pdf.getFileDescriptor();
            RequestBody requestFile = RequestBody.create(fileDescriptor, MULTIPART_MEDIA_TYPE);
            MultipartBody.Part part = buildFilePart(fileName, requestFile, uploadListener, uri, currentPos, totalCount);
            pdf.close();
            return part;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 批量构建 Assets 文件上传 Part（读入内存后上传）。
     */
    public static List<MultipartBody.Part> createAssetsFilePart(Context context, List<Uri> uriList,
                                                                FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        for (int i = 0; i < uriList.size(); i++) {
            multiList.add(createAssetsFilePart(context, uriList.get(i), i, uriList.size(), uploadListener));
        }
        return multiList;
    }

    /**
     * 构建单个 Assets 文件上传 Part。
     */
    public static MultipartBody.Part createAssetsFilePart(Context context, Uri uri,
                                                          FileUploadProgressListener uploadListener) {
        return createAssetsFilePart(context, uri, 0, 1, uploadListener);
    }

    /**
     * 构建单个 Assets 文件上传 Part（读入内存后上传，支持进度回调）。
     */
    @SuppressLint("Range")
    public static MultipartBody.Part createAssetsFilePart(Context context, Uri uri, int currentPos, int totalCount,
                                                          FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String fileName = queryDisplayName(contentResolver, uri);
            AssetFileDescriptor afd = contentResolver.openAssetFileDescriptor(uri, "r");
            if (afd == null) {
                throw new RuntimeException("读取文件失败");
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            try (InputStream inputStream = afd.createInputStream()) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
            RequestBody requestFile = RequestBody.create(byteArrayOutputStream.toByteArray(), MULTIPART_MEDIA_TYPE);
            MultipartBody.Part part = buildFilePart(fileName, requestFile, uploadListener, uri, currentPos, totalCount);
            afd.close();
            return part;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 批量构建临时文件上传 Part（先复制到缓存目录再上传）。
     */
    public static List<MultipartBody.Part> createTempFilePart(Context context, List<Uri> uriList,
                                                              FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        for (int i = 0; i < uriList.size(); i++) {
            multiList.add(createTempFilePart(context, uriList.get(i), i, uriList.size(), uploadListener));
        }
        return multiList;
    }

    /**
     * 构建单个临时文件上传 Part。
     */
    public static MultipartBody.Part createTempFilePart(Context context, Uri uri,
                                                        FileUploadProgressListener uploadListener) {
        return createTempFilePart(context, uri, 0, 1, uploadListener);
    }

    /**
     * 构建单个临时文件上传 Part（先复制到缓存目录再上传，支持进度回调）。
     */
    @SuppressLint("Range")
    public static MultipartBody.Part createTempFilePart(Context context, Uri uri, int currentPos, int totalCount,
                                                        FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String fileName = queryDisplayName(contentResolver, uri);
            File tempFile = new File(context.getCacheDir(), TextUtils.isEmpty(fileName) ? "file" : fileName);
            tempFile.deleteOnExit();
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            try (InputStream in = inputStream;
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            if (!tempFile.exists()) {
                throw new RuntimeException("临时文件生成失败");
            }
            RequestBody requestFile = RequestBody.create(tempFile, MULTIPART_MEDIA_TYPE);
            return buildFilePart(fileName, requestFile, uploadListener, uri, currentPos, totalCount);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        }
    }

    @SuppressLint("Range")
    private static String queryDisplayName(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private static MultipartBody.Part buildFilePart(String fileName, RequestBody requestFile,
                                                    FileUploadProgressListener uploadListener, Uri uri,
                                                    int currentPos, int totalCount) {
        String partName = TextUtils.isEmpty(fileName) ? "file" : fileName;
        if (uploadListener == null) {
            return MultipartBody.Part.createFormData("file", partName, requestFile);
        }
        ProgressRequestBody progressRequestBody =
                new ProgressRequestBody(requestFile, uri, currentPos, totalCount, uploadListener);
        return MultipartBody.Part.createFormData("file", partName, progressRequestBody);
    }
}
