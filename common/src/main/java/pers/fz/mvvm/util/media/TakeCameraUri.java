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

import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.util.apiUtil.FileUtils;

/**
 * Created by fz on 2023/4/25 17:23
 * describe :
 */
public class TakeCameraUri extends ActivityResultContract<Object, Uri> {
    /**
     * 拍照返回的uri
     */
    private Uri uri;
    private String savePath;
    private Context context;

    public TakeCameraUri(Context context,String savePath) {
        this.context = context;
        this.savePath = savePath;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Object input) {
        String mimeType = "image/jpeg";
        String fileName = "IMAGE_" + System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator +
                    FileUtils.getLastPath(savePath, FileUtils.getDefaultBasePath(context)) + File.separator + "image");
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            //应用认证表示
            String authorities = context.getPackageName() + ".FileProvider";
            uri = FileProvider.getUriForFile(context, authorities,
                    new File(savePath + File.separator + "image", fileName));
        }
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        return uri;
    }
}
