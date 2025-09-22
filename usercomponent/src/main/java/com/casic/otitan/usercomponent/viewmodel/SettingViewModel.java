package com.casic.otitan.usercomponent.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.activity.FeedBackActivity;
import com.casic.otitan.usercomponent.activity.LoginActivity;
import com.casic.otitan.usercomponent.activity.TechnicalSupportActivity;
import com.casic.otitan.usercomponent.api.UserAccountHelper;

import com.casic.otitan.common.api.ConstantsHelper;
import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.RepositoryImpl;
import com.casic.otitan.common.utils.common.CacheUtil;
import com.casic.otitan.common.utils.download.UpdateManger;
import com.casic.otitan.common.widget.customview.IconLabelValueView;
import com.casic.otitan.common.widget.dialog.ConfirmDialog;

/**
 * Create by CherishTang on 2020/3/26 0026
 * describe:
 */
public class SettingViewModel extends BaseViewModel<BaseRepository<BaseView>,BaseView> {

    public SettingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.setting_exit) {
            UserAccountHelper.exit();
            startActivity(view.getContext(), LoginActivity.class, null);
        } else if (id == R.id.tv_feedback) {
            startActivity(view.getContext(), FeedBackActivity.class);
        } else if (id == R.id.versionName) {
            baseView.showLoading("正在检测新版本...",true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ConstantsHelper.isSuccessRequestUpdate = true;
                UpdateManger.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "1、修复已知bug",
                        "1.0.1");
                baseView.hideLoading();
            }, 2000);
        } else if (id == R.id.cleanUp) {
            IconLabelValueView tvCleanUp = (IconLabelValueView) view;
            new ConfirmDialog(view.getContext())
                    .setMessage("是否确定清理缓存？")
                    .setOnPositiveClickListener(dialog -> {
                        try {
                            // 清理所有缓存
                            CacheUtil.getInstance().clearAllCache(view.getContext());
                            tvCleanUp.setValue(getCacheSize());
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
        return CacheUtil.getInstance().getTotalCacheSize(getApplication());
    }

}
