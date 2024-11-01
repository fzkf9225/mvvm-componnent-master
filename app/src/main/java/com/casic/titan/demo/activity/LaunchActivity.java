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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.LaunchLayoutBinding;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

/**
 * Created by fz on 2017/5/15.
 * 启动页
 */
@AndroidEntryPoint
public class LaunchActivity extends BaseActivity<EmptyViewModel, LaunchLayoutBinding> {
    private Disposable disposable;
    private int countDown = 3;
    private final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final String[] PERMISSIONS_TIRAMISU = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };
    private SplashScreen splashScreen;
    /**
     * 控制是否保持启动页面的变量,值为false时继续往下走
     */
    private AtomicBoolean keepOnScreenCondition = new AtomicBoolean(false);

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
    }

    @Override
    public void initData(Bundle bundle) {
        splashScreen.setKeepOnScreenCondition(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (lacksPermissions(PERMISSIONS_TIRAMISU)) {
                    requestPermission(PERMISSIONS_TIRAMISU);
                } else {
                    startCountDown();
                }
            } else {
                if (lacksPermissions(PERMISSIONS)) {
                    requestPermission(PERMISSIONS);
                } else {
                    startCountDown();
                }
            }
            return keepOnScreenCondition.get();
        });
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            LogUtil.show(TAG, "---------------setOnExitAnimationListener--------------");
            showToast("SplashScreen动画播放结束");
        });
    }

    @Override
    protected void onPermissionGranted(Map<String, Boolean> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        keepOnScreenCondition.compareAndSet(false, true);
        startCountDown();
    }

    @Override
    protected void onPermissionRefused(Map<String, Boolean> permissions) {
        super.onPermissionRefused(permissions);
        keepOnScreenCondition.compareAndSet(false, true);
        startCountDown();
    }

    private void startCountDown() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.interval(1, TimeUnit.SECONDS)
                .map(aLong -> countDown - aLong)
                .take(countDown + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    LogUtil.show(TAG, "欢迎页面倒计时：" + aLong);
                    if (aLong == 0) {
//                        if (UserAccountHelper.isLogin()) {
                            startActivity(MainActivity.class);
//                        } else {
//                            startActivity(LoginActivity.class);
//                        }
                        finish();
                    }
                }, throwable -> LogUtil.show(TAG, "倒计时异常：" + throwable));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}

