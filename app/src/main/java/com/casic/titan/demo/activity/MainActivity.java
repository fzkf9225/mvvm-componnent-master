package com.casic.titan.demo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityMainBinding;
import com.casic.titan.demo.view.MainView;
import com.casic.titan.demo.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * created by fz on 2023/4/27 14:51
 * describe：
 */
@AndroidEntryPoint
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements MainView {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public String setTitleBar() {
        return "首页";
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }
    @Override
    public void initView(Bundle savedInstanceState) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public void initData(Bundle bundle) {
        checkNotificationPermission();
    }

    /**
     * 通知权限检测
     */
    private void checkNotificationPermission() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            new ConfirmDialog(this)
                    .setMessage("通知权限已关闭，是否前往设置中心开启此功能？")
                    .setCanOutSide(false)
                    .setSureText("前往开启")
                    .setOnSureClickListener(dialog -> {
                        try {
                            Intent intent = new Intent();// 进入设置系统应用权限界面
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                                intent.putExtra(Settings.EXTRA_CHANNEL_ID, this.getApplicationInfo().uid);
                            } else {
                                intent.setAction("android.intent.action.MAIN");
                                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                            }
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .builder()
                    .show();
        }
    }

}