package pers.fz.media;

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

    public TakeCameraUri(String savePath) {
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
                    MediaUtil.getLastPath(savePath, MediaUtil.getDefaultBasePath(context)) + File.separator + "image");
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File file = new File(savePath + File.separator + "image" + File.separator+ fileName);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            //应用认证表示
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider",
                    file);
        }
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    /**
     * 下面注释代码是正常的，但是由于视频录像的时候返回的resultCode有问题，这里虽然没有问题，但是担心兼容性不好，索性就不判断了
     */
    @Override
    public Uri parseResult(int resultCode, @Nullable Intent intent) {
        LogUtil.show(MediaHelper.TAG, "拍照回调resultCode：" + resultCode);
//        if (resultCode == Activity.RESULT_OK) {
//            return uri;
//        }
        return uri;
    }

}
