package pers.fz.media.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;

import pers.fz.media.utils.LogUtil;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.utils.MediaUtil;

/**
 * Created by fz on 2023/4/25 17:23
 * describe :
 */
public class TakeVideoUri extends ActivityResultContract<MediaTypeEnum, Uri> {
    /**
     * 拍照返回的uri
     */
    private Uri uri;
    private final MediaBuilder mediaBuilder;
    /**
     * 录制时长
     */
    private int durationLimit = 30;

    private MediaTypeEnum mediaType;

    public TakeVideoUri(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
    }

    public TakeVideoUri(MediaBuilder mediaBuilder, int durationLimit) {
        this.mediaBuilder = mediaBuilder;
        this.durationLimit = durationLimit;
    }

    public MediaTypeEnum getMediaType() {
        return mediaType;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, MediaTypeEnum input) {
        this.mediaType = input;
        String mimeType = "video/mp4";
        String fileName = "VIDEO_" + System.currentTimeMillis() + ".mp4";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //如果保存到公共目录
            if (mediaBuilder.isSavePublicPath()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                //如果没有设置子目录的话，则默认取包名
                if (TextUtils.isEmpty(mediaBuilder.getVideoSubPath())) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + MediaUtil.getDefaultBasePath(context) + File.separator + "video");
                } else {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator +
                            mediaBuilder.getVideoSubPath());
                }
                uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                File file = new File(mediaBuilder.getVideoOutPutPath() + File.separator + fileName);
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    boolean isCreated = file.getParentFile().mkdirs();
                }
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", file);
            }
        } else {
            File file = new File(mediaBuilder.getVideoOutPutPath() + File.separator + fileName);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                boolean isCreated = file.getParentFile().mkdirs();
            }
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", file);
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
