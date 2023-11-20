package pers.fz.mvvm.util.media;

import android.app.Activity;
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

import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.util.apiUtil.FileUtils;
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

    public TakeVideoUri(String savePath, int durationLimit) {
        this.savePath = savePath;
        this.durationLimit = durationLimit;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Object input) {
        String mimeType = "video/mp4";
        String fileName = "VIDEO_" + System.currentTimeMillis() + ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator +
                    FileUtils.getLastPath(savePath, FileUtils.getDefaultBasePath(context)) + File.separator + "video");
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File file = new File(savePath + File.separator + "video" + File.separator + fileName);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            //应用认证表示
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider",
                    file);
        }
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                // 视频质量。0 低质量；1 高质量
                .putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                // 时长限制，单位秒
                .putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit)
                .putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    /**
     * 系统这里有bug。ACTION_VIDEO_CAPTURE返回的resultCode永远都为0，及Activity.RESULT_CANCELED,所以这个没办法判断resultCode这里属于系统bug
     */
    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        LogUtil.show(MediaHelper.TAG, "录像回调resultCode：" + resultCode);
        return uri;
    }
}
