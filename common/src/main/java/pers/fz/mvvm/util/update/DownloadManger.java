package pers.fz.mvvm.util.update;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.util.log.ToastUtils;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;


/**
 * Created by fz on 2020/08/07.
 * describe：文件下载
 */
public class DownloadManger {
    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile DownloadManger updateManger;
    private DownloadNotificationUtil downloadNotificationUtil;
    protected DownloadManger() {

    }

    public static DownloadManger getInstance() {
        if (updateManger == null) {
            synchronized (DownloadManger.class) {
                if(updateManger==null){
                    updateManger = new DownloadManger();
                }
            }
        }
        return updateManger;
    }

    /**
     * 下载文件
     * @param mContext 当前视图
     * @param fileUrl 下载文件路径
     * @param saveBasePath 保存文件路径默认文件路径为RxNet.PATH,
     */
    public void download(Activity mContext, String fileUrl, String saveBasePath) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0x01);
        } else {
            if (StringUtil.isEmpty(fileUrl)) {
                ToastUtils.showShort(mContext.getApplicationContext(), "文件地址错误！");
                return;
            }
            if (downloadNotificationUtil == null) {
                downloadNotificationUtil = new DownloadNotificationUtil(mContext.getApplicationContext());
            }
            RxNet.download(fileUrl, saveBasePath + FileUtils.getFileName(fileUrl), new DownloadCallback() {
                @Override
                public void onStart(Disposable d) {
                    new Handler(Looper.getMainLooper()).post(()-> downloadNotificationUtil.showNotification(fileUrl.hashCode()));
                }

                @Override
                public void onProgress(long totalByte, long currentByte, int progress) {
                    new Handler(Looper.getMainLooper()).post(()->downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtils.getFileName(fileUrl)));
                }

                @Override
                public void onFinish(File file) {
                    new Handler(Looper.getMainLooper()).post(()->{
                        downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                        Toast.makeText(mContext.getApplicationContext(),"文件已保存至"+file.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String msg) {
                    new Handler(Looper.getMainLooper()).post(()->{
                        downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                        Toast.makeText(mContext.getApplicationContext(),"文件下载错误，"+msg,Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    public void download(Activity mContext, String fileUrl) {
        download(mContext, fileUrl, RxNet.PATH);
    }

}
