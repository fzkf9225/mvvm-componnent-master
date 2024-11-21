package pers.fz.mvvm.util.update;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.util.update.listener.ApkUpdateListener;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;

/**
 * updated by fz on 2024/11/7.
 * describe：软件版本更新
 */
public class UpdateManger{

    private final List<String> downloadMap = new ArrayList<>();

    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile UpdateManger updateManger;

    private UpdateManger() {

    }

    public static UpdateManger getInstance() {
        if (updateManger == null) {
            synchronized (UpdateManger.class) {
                if (updateManger == null) {
                    updateManger = new UpdateManger();
                }
            }
        }
        return updateManger;
    }

    /**
     * 显示更新程序对话框，供主程序调用
     *
     * @param mContext            视图
     * @param apkUrl              apk地址
     * @param updateMsg           更新提示信息
     * @param mCurrentVersionName 当前版本名称
     */
    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName,  false);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable) {
        new UpdateMessageDialog(mContext)
                .setOnUpdateListener(new ApkUpdateListener(mContext, apkUrl, downloadMap))
                .setCanCancel(cancelEnable)
                .setUpdateMsgString(updateMsg)
                .setVersionName(mCurrentVersionName)
                .builder()
                .show();
    }
}
