package io.coderf.arklab.user.viewmodel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import io.coderf.arklab.common.activity.WebViewActivity;
import io.coderf.arklab.common.api.ConstantsHelper;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;
import io.coderf.arklab.common.utils.common.CacheUtil;
import io.coderf.arklab.common.utils.download.UpdateManager;
import io.coderf.arklab.common.widget.customview.IconLabelValueView;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.activity.FeedBackActivity;
import io.coderf.arklab.user.activity.LoginActivity;
import io.coderf.arklab.user.activity.TechnicalSupportActivity;
import io.coderf.arklab.user.api.UserAccountHelper;

/**
 * Create by fz on 2020/3/26 0026
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/5
 */
public class SettingViewModel extends BaseViewModel {

    private static final String AGREEMENT_HOST = "io.coderf.arklab.demo";

    public SettingViewModel(@NonNull Application application) {
        super(application);
    }

    @SuppressLint("BatteryLife")
    public void onClick(View view) {
        int id = view.getId();
        Context context = view.getContext();
        if (id == R.id.setting_exit) {
            UserAccountHelper.exit();
            startActivity(context, LoginActivity.class, null);
        } else if (id == R.id.tv_feedback) {
            startActivity(context, FeedBackActivity.class);
        } else if (id == R.id.versionName) {
            getNetworkRequestUiHost().showLoading("正在检测新版本...", true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ConstantsHelper.isSuccessRequestUpdate = true;
                UpdateManager.getInstance().checkUpdateInfo((Activity) context,
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "leakcanary-android-release.apk",
                        "1、修复已知bug",
                        "1.0.1");
                getNetworkRequestUiHost().hideLoading();
            }, 2000);
        } else if (id == R.id.cleanUp) {
            IconLabelValueView tvCleanUp = (IconLabelValueView) view;
            new ConfirmDialog(context)
                    .setMessage("是否确定清理缓存？")
                    .setOnPositiveClickListener(dialog -> {
                        try {
                            CacheUtil.getInstance().clearCache(context);
                            tvCleanUp.setValue(getCacheSize());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .builder()
                    .show();
        } else if (id == R.id.tv_support) {
            startActivity(context, TechnicalSupportActivity.class);
        } else if (id == R.id.tv_setting_notice) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (id == R.id.tv_setting_battery) {
            openBatteryOptimizationSettings(context);
        } else if (id == R.id.tv_setting_overlay) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName())
            );
            context.startActivity(intent);
        } else if (id == R.id.tv_background_run_settings) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(intent);
        } else if (id == R.id.tv_privacy_protection_policy) {
            WebViewActivity.show(
                    context,
                    "隐私保护政策.html",
                    "隐私保护政策",
                    true,
                    false,
                    AGREEMENT_HOST,
                    WebViewUrlTypeEnum.ASSETS.type
            );
        } else if (id == R.id.tv_user_agreement) {
            WebViewActivity.show(
                    context,
                    "用户协议.html",
                    "用户协议",
                    true,
                    false,
                    AGREEMENT_HOST,
                    WebViewUrlTypeEnum.ASSETS.type
            );
        }
    }

    @SuppressLint("BatteryLife")
    private void openBatteryOptimizationSettings(Context context) {
        if (isIgnoringBatteryOptimizations(context)) {
            context.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            context.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        }
    }

    public boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager != null
                && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public boolean canDrawOverlays(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public String permissionStatusText(Context context, boolean enabled) {
        return context.getString(enabled
                ? R.string.user_permission_enabled
                : R.string.user_permission_disabled);
    }

    /**
     * 计算缓存大小
     */
    public String getCacheSize() {
        return CacheUtil.getInstance().calculateCacheSize(getApplication());
    }

}
