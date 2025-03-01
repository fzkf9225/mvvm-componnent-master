package pers.fz.mvvm.util.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;

import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2017/6/14.
 * Glide缓存照片到本地
 * new Thread(service).start();千万不要忘了开启线程。很重要，很重要，很重要！！！
 */

public class DownLoadImageService implements Runnable {
    private final String TAG = this.getClass().getSimpleName();
    private final Object url;
    private final Context context;
    private final ImageDownLoadCallBack callBack;
    private final String filePath;

    public DownLoadImageService(Context context, Object url, String filePath, ImageDownLoadCallBack callBack) {
        this.url = url;
        this.callBack = callBack;
        this.context = context;
        String extension = null;
        try {
            extension = FileUtil.getFileExtension(url.toString());
            LogUtil.show(TAG, "后缀名：" + extension);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "获取后缀名失败：" + e);
        }
        if (TextUtils.isEmpty(extension)) {
            extension = "jpg";
        }
        String name = FileUtil.getNoRepeatFileName(filePath, "IMG_", "." + extension);
        this.filePath = FileUtil.getDefaultBasePath(context) + File.separator + filePath + File.separator + name + "." + extension;
    }

    @Override
    public void run() {
        Glide.with(context.getApplicationContext()).downloadOnly().load(url).listener(new RequestListener<>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                LogUtil.e(TAG, "文件下载失败:" + e);
                if (callBack != null) {
                    callBack.onDownLoadFailed("文件保存失败");
                }
                return false;
            }

            @Override
            public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                File currentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), filePath);
                if (currentFile.getParentFile() != null && !currentFile.getParentFile().exists()) {
                    boolean isCreated = currentFile.getParentFile().mkdirs();
                }
                try {
                    boolean isCopy = FileUtil.copyFile(resource.getAbsolutePath(), currentFile.getAbsolutePath());
                    if (!isCopy) {
                        if (callBack != null) {
                            callBack.onDownLoadFailed("文件重命名失败");
                        }
                        return false;
                    }
                    if (callBack != null) {
                        callBack.onDownLoadSuccess(currentFile);
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "重命名文件异常：" + e);
                    if (callBack != null) {
                        callBack.onDownLoadFailed("文件重命名失败");
                    }
                    e.printStackTrace();
                }
                return false;
            }
        }).submit();
    }
}
