package pers.fz.mvvm.util.media;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;

import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/4/25 17:23
 * describe :
 */
public class TakeVideoUri extends ActivityResultContract<Object, Uri> {
    /**
     * 拍照返回的uri
     */
    private Uri uri;
    private String savePath;
    /**
     * 录制时长
     */
    private int durationLimit = 30;
    public TakeVideoUri(String savePath) {
        this.savePath = savePath;
    }
    public TakeVideoUri(String savePath,int durationLimit) {
        this.savePath = savePath;
        this.durationLimit = durationLimit;
    }
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Object input) {

        File saveFilePath = new File(savePath);
        if (!saveFilePath.exists()) {
            boolean isCreated = saveFilePath.mkdirs();
        }
        String mimeType = "video/mp4";
        String fileName = "VIDEO_" + System.currentTimeMillis() + ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            //应用认证表示
            String authorities = context.getPackageName() + ".FileProvider";
            uri = FileProvider.getUriForFile(context, authorities,
                    new File(savePath, fileName));
        }
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                .putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit)
                .putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        return uri;
    }
}