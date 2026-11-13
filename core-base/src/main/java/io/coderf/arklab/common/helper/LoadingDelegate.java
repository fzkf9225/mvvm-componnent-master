package io.coderf.arklab.common.helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.core.request.LoadingCancelPolicy;
import io.coderf.arklab.core.ui.delegate.LoadingHost;
import io.coderf.arklab.core.ui.delegate.UiMessageHost;
import io.coderf.arklab.core.ui.delegate.UiSafety;
import io.coderf.arklab.core.ui.delegate.UiSafetyChecker;

/**
 * Loading / Toast 委托：包装 {@link UIController}，实现 {@link UiMessageHost}。
 * <p>
 * 供 {@link io.coderf.arklab.common.base.BaseActivity} / {@link io.coderf.arklab.common.base.BaseFragment}
 * 内部使用；对外仍可通过 {@link #getUiController()} 访问原 {@link UIController}，行为与改造前一致。
 *
 * @author fz
 * @version 1.1
 * @since 1.2.0
 * @updated 2026/11/12
 */
public class LoadingDelegate implements UiMessageHost {

    @NonNull
    private final UIController uiController;

    @NonNull
    private final UiSafetyChecker safety;

    @Nullable
    private final Context dialogContext;

    /**
     * @param context   用于构造 UIController（Activity 或 Application 均可；Loading 会校验 Activity 有效性）
     * @param lifecycle 绑定 Lifecycle，ON_DESTROY 时自动 hideLoading
     * @param safety    UI 安全检查（未 finish / destroy）
     */
    public LoadingDelegate(@NonNull Context context,
                           @NonNull Lifecycle lifecycle,
                           @NonNull UiSafetyChecker safety) {
        this(context, lifecycle, safety, null);
    }

    /**
     * @param dialogContext 弹 Loading 时优先使用的 Context（Fragment 场景传 Activity）
     */
    public LoadingDelegate(@NonNull Context context,
                           @NonNull Lifecycle lifecycle,
                           @NonNull UiSafetyChecker safety,
                           @Nullable Context dialogContext) {
        this.uiController = new UIController(context, lifecycle);
        this.safety = safety;
        this.dialogContext = dialogContext;
    }

    /**
     * Activity 场景便捷工厂。
     */
    @NonNull
    public static LoadingDelegate forActivity(@NonNull android.app.Activity activity,
                                              @NonNull Lifecycle lifecycle) {
        return new LoadingDelegate(activity, lifecycle, UiSafety.forActivity(activity));
    }

    /**
     * Fragment 场景：Dialog 挂 Activity，Lifecycle 跟 View。
     */
    @NonNull
    public static LoadingDelegate forFragment(@NonNull androidx.fragment.app.Fragment fragment,
                                              @NonNull LifecycleOwner viewLifecycleOwner) {
        android.app.Activity activity = fragment.requireActivity();
        return new LoadingDelegate(
                activity,
                viewLifecycleOwner.getLifecycle(),
                UiSafety.forFragment(fragment),
                activity
        );
    }

    @NonNull
    public UIController getUiController() {
        return uiController;
    }

    /**
     * 设置用户取消 Loading 的监听（系统返回 / 手势返回；点外侧不取消）。
     *
     * @param listener 回调；传 null 清除
     */
    public void setOnLoadingCancelListener(@Nullable UIController.OnLoadingCancelListener listener) {
        uiController.setOnLoadingCancelListener(listener);
    }

    @Override
    public void showLoading(String message, boolean enableDynamicEllipsis) {
        showLoading(message, enableDynamicEllipsis, LoadingCancelPolicy.None);
    }

    /**
     * 展示 Loading，并应用取消策略。
     *
     * @param message               加载文案
     * @param enableDynamicEllipsis 动态省略号
     * @param cancelPolicy          用户取消策略
     */
    public void showLoading(
            String message,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        if (!safety.isUiSafe()) {
            return;
        }
        String msg = message != null ? message : LoadingHost.DEFAULT_LOADING_MESSAGE;
        LoadingCancelPolicy policy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        if (dialogContext != null) {
            uiController.showLoading(dialogContext, msg, enableDynamicEllipsis, policy);
        } else {
            uiController.showLoading(msg, enableDynamicEllipsis, policy);
        }
    }

    @Override
    public void refreshLoading(String message) {
        if (!safety.isUiSafe()) {
            return;
        }
        uiController.refreshLoading(message != null ? message : "");
    }

    @Override
    public void hideLoading() {
        uiController.hideLoading();
    }

    @Override
    public boolean isLoading() {
        return uiController.isLoadingShowing();
    }

    @Override
    public void showToast(String message) {
        if (!safety.isUiSafe() || message == null || message.isEmpty()) {
            return;
        }
        uiController.showToast(message);
    }

    @Override
    public void showToast(@StringRes int resId) {
        if (!safety.isUiSafe()) {
            return;
        }
        uiController.showToast(resId);
    }

    /** 取消尚未执行的主线程 UI 任务并立即关闭 Loading。 */
    public void cancelPendingUi() {
        uiController.cancelPendingUi();
    }
}
