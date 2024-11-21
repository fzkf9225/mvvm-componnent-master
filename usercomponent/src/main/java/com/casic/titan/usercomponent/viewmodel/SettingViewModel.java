package com.casic.titan.usercomponent.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.activity.FeedBackActivity;
import com.casic.titan.usercomponent.activity.LoginActivity;
import com.casic.titan.usercomponent.api.UserAccountHelper;

import pers.fz.mvvm.activity.TechnicalSupportActivity;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;
import pers.fz.mvvm.util.cache.GlideCacheUtil;
import pers.fz.mvvm.util.update.UpdateManger;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * Create by CherishTang on 2020/3/26 0026
 * describe:
 */
public class SettingViewModel extends BaseViewModel<RepositoryImpl,BaseView> {

    public SettingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl repository() {
        return null;
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.setting_exit) {
            UserAccountHelper.exit();
            startActivity(view.getContext(), LoginActivity.class, null);
        } else if (id == R.id.feedBack) {
            startActivity(view.getContext(), FeedBackActivity.class);
        } else if (id == R.id.versionName) {
            baseView.showLoading("正在检测新版本...");
            new Handler(Looper.myLooper()).postDelayed(() -> {
                ConstantsHelper.isSuccessRequestUpdate = true;
                UpdateManger.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "1、修复已知bug",
                        "1.0.1");
                baseView.hideLoading();
            }, 2000);
        } else if (id == R.id.cleanUp) {
            TextView tvCleanUp = (TextView) view;
            new ConfirmDialog(view.getContext())
                    .setMessage("是否确定清理缓存？")
                    .setOnSureClickListener(dialog -> {
                        try {
                            clearCache();
                            tvCleanUp.setText(getCacheSize());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .builder()
                    .show();
        } else if (id == R.id.tv_support) {
            startActivity(view.getContext(), TechnicalSupportActivity.class);
        }
    }

    /**
     * 计算缓存大小
     */
    public String getCacheSize() {
        String size = null;
        try {
            long cacheSize = GlideCacheUtil.getInstance().getFolderSize(getApplication().getCacheDir());
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                cacheSize += GlideCacheUtil.getInstance().getFolderSize(getApplication().getExternalCacheDir());
            }
            size = GlideCacheUtil.getFormatSize(cacheSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        GlideCacheUtil.getInstance().deleteFolderFile(getApplication().getCacheDir().getAbsolutePath(), false);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            GlideCacheUtil.getInstance().deleteFolderFile(getApplication().getExternalCacheDir().getAbsolutePath(), false);
        }
    }

}
