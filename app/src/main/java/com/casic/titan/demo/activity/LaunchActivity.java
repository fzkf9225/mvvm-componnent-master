package com.casic.titan.demo.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.splashscreen.SplashScreen;

import com.casic.titan.usercomponent.activity.LoginActivity;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.gyf.immersionbar.ImmersionBar;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.databinding.LaunchLayoutBinding;

/**
 * Created by fz on 2017/5/15.
 * 启动页
 */
@AndroidEntryPoint
public class LaunchActivity extends BaseActivity<BaseViewModel, LaunchLayoutBinding> {
    private final Handler handler = new Handler(Looper.myLooper());
    private int countDown = 3;
    private final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final String[] PERMISSIONS_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private SplashScreen splashScreen;

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.launch_layout;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        registerPermissionLauncher();
//        Glide.with(this)
//                .load(R.mipmap.launcher_image)
//                .into(binding.img);
    }

    @Override
    public void initData(Bundle bundle) {
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            showToast("SplaseScreen动画播放结束");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (lacksPermissions(PERMISSIONS_TIRAMISU)) {
                    requestPermission(PERMISSIONS_TIRAMISU);
                    return;
                }
            } else {
                if (lacksPermissions(PERMISSIONS)) {
                    requestPermission(PERMISSIONS);
                    return;
                }
            }
            startCountDown();
        });

    }

    @Override
    protected void onPermissionGranted() {
        startCountDown();
    }

    @Override
    protected void onPermissionRefused() {
        super.onPermissionRefused();
        startCountDown();
    }

    private void startCountDown() {
        handler.postDelayed(runnable, 1000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countDown--;
            handler.postDelayed(this, 1000);
            if (countDown == 0) {
                //千万不要再startActivity后直接finish，一定要在回调事件里finish
                if (UserAccountHelper.isLogin()) {
                    startActivity(MainActivity.class);
                } else {
                    startActivity(LoginActivity.class);
                }
                finish();
            }
        }
    };

}

