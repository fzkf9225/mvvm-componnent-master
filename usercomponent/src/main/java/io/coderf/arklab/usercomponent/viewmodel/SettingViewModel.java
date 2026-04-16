package io.coderf.arklab.usercomponent.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.utils.download.UpdateManager;
import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.activity.FeedBackActivity;
import io.coderf.arklab.usercomponent.activity.LoginActivity;
import io.coderf.arklab.usercomponent.activity.TechnicalSupportActivity;
import io.coderf.arklab.usercomponent.api.UserAccountHelper;

import io.coderf.arklab.common.api.ConstantsHelper;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;
import io.coderf.arklab.common.utils.common.CacheUtil;
import io.coderf.arklab.common.widget.customview.IconLabelValueView;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;

/**
 * Create by fz on 2020/3/26 0026
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
                UpdateManager.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "qq_hd_mini_1.4.apk",
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
                            CacheUtil.getInstance().clearCache(view.getContext());
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
        return CacheUtil.getInstance().calculateCacheSize(getApplication());
    }

}
