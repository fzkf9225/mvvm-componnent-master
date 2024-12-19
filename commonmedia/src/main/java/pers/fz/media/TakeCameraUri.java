package pers.fz.media;

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


/**
 * Created by fz on 2023/4/25 17:23
 * describe :
 */
public class TakeCameraUri extends ActivityResultContract<Object, Uri> {
    /**
     * 拍照返回的uri
     */
    private Uri uri;
    private final MediaBuilder mediaBuilder;

    public TakeCameraUri(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Object input) {
        String mimeType = "image/jpeg";
        String fileName = "IMAGE_" + System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //如果保存到公共目录
            if (mediaBuilder.isSavePublicPath()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                //如果没有设置子目录的话，则默认取包名
                if (TextUtils.isEmpty(mediaBuilder.getImageSubPath())) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + MediaUtil.getDefaultBasePath(context) + File.separator + "image");
                } else {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator +
                            mediaBuilder.getImageSubPath());
                }
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                File file = new File(mediaBuilder.getImageOutPutPath()+File.separator + fileName);
                if(file.getParentFile() != null && !file.getParentFile().exists()){
                    boolean isCreated = file.getParentFile().mkdirs();
                }
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider",file);
            }
        } else {
            File file = new File(mediaBuilder.getImageOutPutPath()+File.separator + fileName);
            if(file.getParentFile() != null && !file.getParentFile().exists()){
                boolean isCreated = file.getParentFile().mkdirs();
            }
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider",file);
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
