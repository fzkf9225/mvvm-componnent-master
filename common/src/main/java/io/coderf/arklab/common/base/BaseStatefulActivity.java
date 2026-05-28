package io.coderf.arklab.common.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

/**
 * 可选的状态感知 Activity 基类：在 {@link BaseActivity} 之上提供常见增强，老项目无需改动即可继续用 {@link BaseActivity}。
 * <p>
 * <b>相对 {@link BaseActivity} 的默认差异</b>
 * <ul>
 *   <li>仅在首次创建时调用 {@link #initData(Bundle)}（配置变更/进程恢复后不重复拉数）。</li>
 *   <li>处理 {@link #onNewIntent(Intent)}，便于 singleTop/singleTask 更新页面参数。</li>
 *   <li>可选 {@link #applySecureWindow()} 防截屏（默认关闭）。</li>
 * </ul>
 * <p>
 * 接入方式：将 {@code extends BaseActivity} 改为 {@code extends BaseStatefulActivity}，并按需重写钩子。
 */
public abstract class BaseStatefulActivity<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseActivity<VM, VDB> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (applySecureWindow()) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        }
        super.onCreate(savedInstanceState);
    }

    /**
     * 仅在首次进入时执行 {@link #initData(Bundle)}，避免旋转屏/进程恢复重复请求。
     */
    @Override
    protected boolean shouldRunInitData(@Nullable Bundle savedInstanceState) {
        return isFirstCreation(savedInstanceState);
    }

    /**
     * 是否在窗口上启用 {@code FLAG_SECURE}（验证码、支付等敏感页可返回 true）。
     */
    protected boolean applySecureWindow() {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (reloadDataOnNewIntent()) {
            onPageIntentUpdated(intent);
        }
    }

    /**
     * 收到新 Intent 时是否重新执行 {@link #initData(Bundle)}。
     */
    protected boolean reloadDataOnNewIntent() {
        return true;
    }

    /**
     * singleTop / singleTask 再次进入时，用最新 Intent 刷新页面数据。
     */
    protected void onPageIntentUpdated(@NonNull Intent intent) {
        initData(resolvePageArguments());
    }
}
