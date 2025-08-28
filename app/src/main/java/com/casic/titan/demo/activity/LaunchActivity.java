package com.casic.titan.demo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityLaunchBinding;
import com.casic.titan.usercomponent.api.UserAccountHelper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.permission.PermissionManager;
import pers.fz.mvvm.viewmodel.EmptyViewModel;
import pers.fz.mvvm.widget.dialog.ProtectionGuidelinesDialog;

/**
 * Created by fz on 2017/5/15.
 * 启动页
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class LaunchActivity extends BaseActivity<EmptyViewModel, ActivityLaunchBinding> {
    private Disposable disposable;
    private final int countDown = 3;
    private SplashScreen splashScreen;

    protected PermissionManager permissionManager;
    /**
     * 控制是否保持启动页面的变量,值为false时继续往下走
     */
    private final AtomicBoolean keepOnScreenCondition = new AtomicBoolean(false);

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
        return R.layout.activity_launch;
    }

    @Override
    protected boolean enableImmersionBar() {
        return true;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    private String[] permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            return new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        permissionManager = new PermissionManager(this);
        permissionManager.setOnDeniedCallback(map -> {
            showToast("拒绝权限可能会导致应用软件运行异常");
            keepOnScreenCondition.compareAndSet(false, true);
            startCountDown();
        });
        permissionManager.setOnGrantedCallback(map -> {
            keepOnScreenCondition.compareAndSet(false, true);
            startCountDown();
        });
    }

    @Override
    public void initData(Bundle bundle) {
        splashScreen.setKeepOnScreenCondition(keepOnScreenCondition::get);

        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            LogUtil.show(TAG, "---------------setOnExitAnimationListener--------------");
            showToast("SplashScreen动画播放结束");
        });
        if (UserAccountHelper.isAgree()) {
            if (permissionManager.lacksPermissions(permissions())) {
                keepOnScreenCondition.compareAndSet(false, true);
                startCountDown();
            } else {
                permissionManager.request(permissions());
            }
            return;
        }
        new ProtectionGuidelinesDialog(this)
                .setCanOutSide(false)
                .setPositiveBackgroundColor(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green))
                .setSpannableContent(getSpannableContent())
                .setOnNegativeClickListener(dialog -> {

                    showToast("拒绝可能会导致部分功能使用异常");
                    keepOnScreenCondition.compareAndSet(false, true);
                    startCountDown();
                })
                .setOnPositiveClickListener(dialog -> {
                    UserAccountHelper.setAgreement(true);
                    if (!permissionManager.lacksPermissions(permissions())) {
                        keepOnScreenCondition.compareAndSet(false, true);
                        startCountDown();
                    } else {
                        permissionManager.request(permissions());
                    }
                })
                .builder()
                .show();
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
        if (permissionManager != null) {
            permissionManager.unregister();
        }
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private SpannableString getSpannableContent() {
        String content = "为了保护您的个人信息权益，我们将遵循合法、正当、必要和诚信原则，按照《用户信息保护指引》和《隐私保护协议》收集、使用您的信息。我们将收集相机权限、设备信息、位置权限、存储权限等，用于提供个性化内容推荐、本地文件存储、拍照、录音、录像、推送等功能。您可以在【系统设置】中管理、修改权限，拒绝提供仅会影响相关功能，相关功能可能会无法正常使用，但不会影响其他功能的使用。请仔细阅读相关协议内容了解详细信息，您也可以在【我的】右上角齿轮【设置】中找到相关协议详细信息。如您同意，请点击\\\"同意并继续\\\"开始使用我们的服务。";
        SpannableString spannableString = new SpannableString(content);

        // 找到关键文本的位置
        int userAgreementStart = content.indexOf("《用户信息保护指引》");
        int userAgreementEnd = userAgreementStart + "《用户信息保护指引》".length();

        int privacyPolicyStart = content.indexOf("《隐私保护协议》");
        int privacyPolicyEnd = privacyPolicyStart + "《隐私保护协议》".length();

        // 设置用户协议点击
        ClickableSpan userAgreementSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showToast("用户协议");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(LaunchActivity.this, pers.fz.mvvm.R.color.theme_green));
                ds.setUnderlineText(false); // 无下划线
            }
        };

        // 设置隐私保护政策点击
        ClickableSpan privacyPolicySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showToast("隐私保护协议");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(LaunchActivity.this, pers.fz.mvvm.R.color.theme_green));
                ds.setUnderlineText(false); // 无下划线
            }
        };

        // 应用点击范围
        spannableString.setSpan(userAgreementSpan, userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyPolicySpan, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}

