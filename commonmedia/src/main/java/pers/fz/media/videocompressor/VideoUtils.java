package pers.fz.media.videocompressor;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pers.fz.media.MediaUtil;

/**
 * created by fz on 2024/11/14 9:15
 * describe:
 */
public class VideoUtils {
    public static VideoInfo getVideoInfo(Context context, Uri sourcePath) {
        if (context == null || context.getApplicationContext().getContentResolver() == null) {
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, sourcePath);

            String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            int width = widthStr != null ? Integer.parseInt(widthStr) : 0;
            int height = heightStr != null ? Integer.parseInt(heightStr) : 0;
            int rotation = rotationStr != null ? Integer.parseInt(rotationStr) : 0;
            long duration = durationStr != null ? Long.parseLong(durationStr) * 1000 : 0;

            return new VideoInfo(width, height, rotation, duration);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("Range")
    public static File copyFileToCacheDir(Context mContext, Uri uri) throws IOException {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        String fileName = null;
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        // 创建临时文件
        File tempFile = new File(mContext.getCacheDir(), TextUtils.isEmpty(fileName) ? "file" : fileName);
        tempFile.deleteOnExit();//删除旧文件
        InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[4 * 1024]; // 4K buffer
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        inputStream.close();
        return tempFile;
    }

    @SuppressLint("Range")
    public static File createCompressVideoFileByTempFile(File tempFile) throws IOException {
        if (tempFile == null) {
            return null;
        }
        String basePath = tempFile.getParentFile() + File.separator + "compress" + File.separator;
        String noRepeatFileName = MediaUtil.getNoRepeatFileName(basePath, "VIDEO_", ".temp");
        return new File(basePath + noRepeatFileName + ".temp");
    }

    @SuppressLint("Range")
    public static FileDescriptor getFileDescriptor(Context mContext, Uri uri) throws IOException {
        ContentResolver contentResolver = mContext.getContentResolver();
        if (contentResolver == null) {
            return null;
        }
        ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
        if (parcelFileDescriptor == null) {
            return null;
        }
        return parcelFileDescriptor.getFileDescriptor();
    }
}

