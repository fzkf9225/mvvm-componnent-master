package pers.fz.mvvm.util.download;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.apiUtil.DateUtil;
import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

/**
 * Created by fz on 2017/6/14.
 * Glide缓存照片到本地
 * new Thread(service).start();千万不要忘了开启线程。很重要，很重要，很重要！！！
 */

public class DownLoadImageService implements Runnable{
    private final String TAG = this.getClass().getSimpleName();
    private Object url;
    private Context context;
    private ImageDownLoadCallBack callBack;
    private File currentFile;
    private String name;
    private String filePath;

    public DownLoadImageService(Context context, Object url,String filePath, ImageDownLoadCallBack callBack) {
        this.url = url;
        this.callBack = callBack;
        this.context = context;
        name = FileUtils.getNoRepeatFileName(filePath,"IMG_",".jpg");
        this.filePath = filePath;
    }

    @Override
    public void run() {
        File file = null;
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            if (bitmap != null){
                // 在这里执行图片保存方法
                saveImageToGallery(context,bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG,"保存图片异常:"+e);
        } finally {
            if (bitmap != null && currentFile.exists()) {
                callBack.onDownLoadSuccess(currentFile);
                callBack.onDownLoadSuccess(bitmap);
            } else {
                callBack.onDownLoadFailed();
            }
        }
    }

    public void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separator,filePath);
        if (!appDir.exists()) {
            boolean isCreated =appDir.mkdirs();
        }
        currentFile = new File(appDir, name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    currentFile.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(currentFile.getPath()))));
    }
}
