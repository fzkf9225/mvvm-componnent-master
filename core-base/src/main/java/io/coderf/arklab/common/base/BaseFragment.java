package io.coderf.arklab.common.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.LoadingCancelPolicy;
import io.coderf.arklab.core.request.RequestUi;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.coderf.arklab.common.helper.AuthManager;
import io.coderf.arklab.common.helper.LoadingDelegate;
import io.coderf.arklab.common.helper.ViewModelHelper;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.core.ui.delegate.AlwaysInitData;
import io.coderf.arklab.core.ui.delegate.InitDataPolicy;
import io.coderf.arklab.core.ui.delegate.PageArguments;
import io.coderf.arklab.core.ui.delegate.PageArgumentsResolver;
import io.coderf.arklab.core.ui.delegate.UiSafety;
import io.coderf.arklab.core.ui.delegate.UiSafetyChecker;

/**
 * Fragment MVVM 基类，生命周期约定与 {@link BaseActivity} 对齐。
 * <p>
 * UI 策略与 Activity 对称：{@link InitDataPolicy}、{@link PageArgumentsResolver}、{@link UiSafetyChecker}。
 * 换预设给 policy 字段赋值；要写分支则重写 {@code shouldXxx()}，重写覆盖字段。
 * Loading 由 {@link LoadingDelegate} 承担，登录 / 权限由 {@link AuthManager} 承担。
 * {@link #authManager} / {@link #loadingDelegate} 仍可直接使用。
 * 需要「仅首次创建时 initData」可继承 {@link BaseStatefulFragment}，或设置
 * {@link io.coderf.arklab.core.ui.delegate.FirstCreateOnlyInitData}。
 *
 * @see BaseStatefulFragment
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/16
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements RequestUi, AuthManager.AuthCallback {
    protected String TAG = this.getClass().getSimpleName();
    /**
     * viewModel
     */
    protected VM mViewModel;
    /**
     * 正文布局
     */
    protected VDB binding;

    @Inject
    public ErrorService errorService;
    /**
     * 登录 / 权限管理。子类可在 {@code super.onCreateView} 之前赋值以替换实例。
     */
    protected AuthManager authManager;
    /** Loading / Toast。子类可在 {@code super.onCreateView} 之前赋值以替换实例。 */
    @Nullable
    protected LoadingDelegate loadingDelegate;

    /**
     * initData 是否在配置变更后再次执行；默认每次都执行（历史行为）。
     */
    @NonNull
    protected InitDataPolicy initDataPolicy = AlwaysInitData.INSTANCE;

    /** 页面参数解析；默认 Fragment arguments。 */
    @Nullable
    protected PageArgumentsResolver pageArgumentsResolver;

    /** UI 安全检查。 */
    @Nullable
    protected UiSafetyChecker uiSafetyChecker;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ensureDelegates();
        createAuthManager();
        createUIController();
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        binding.setLifecycleOwner(this);
        createViewModel();
        initView(savedInstanceState);
        if (shouldRunInitData(savedInstanceState)) {
            initData(resolvePageArguments());
        }
        return binding.getRoot();
    }

    protected void ensureDelegates() {
        if (pageArgumentsResolver == null) {
            pageArgumentsResolver = PageArguments.fromFragment(this);
        }
        if (uiSafetyChecker == null) {
            uiSafetyChecker = UiSafety.forFragment(this);
        }
    }

    /**
     * 是否在 {@code onCreateView} 中调用 {@link #initData(Bundle)}。默认走 {@link #initDataPolicy}。
     */
    protected boolean shouldRunInitData(@Nullable Bundle savedInstanceState) {
        return initDataPolicy.shouldRunInitData(savedInstanceState);
    }

    @NonNull
    protected Bundle resolvePageArguments() {
        ensureDelegates();
        return pageArgumentsResolver.resolve();
    }

    protected final boolean isFirstCreation(@Nullable Bundle savedInstanceState) {
        return savedInstanceState == null;
    }

    protected boolean isUiSafe() {
        ensureDelegates();
        return uiSafetyChecker.isUiSafe();
    }

    /**
     * 创建 {@link AuthManager}。子类可在 {@code super.onCreateView} 之前赋值 {@link #authManager} 以替换实例。
     */
    protected void createAuthManager() {
        if (authManager == null) {
            boolean unify = errorService == null || errorService.unifyHandling();
            authManager = new AuthManager(this, unify);
        }
        authManager.setLoginCallback(this);
    }

    /**
     * 创建 Loading 委托；Dialog 挂 Activity，Lifecycle 跟 View。
     * 并挂接 Loading 用户取消 → {@link #onLoadingCancelled(LoadingCancelPolicy)}。
     */
    protected void createUIController() {
        if (loadingDelegate == null) {
            // Dialog 挂 Activity Window；Lifecycle 跟 View，与 onDestroyView 对齐
            loadingDelegate = LoadingDelegate.forFragment(this, getViewLifecycleOwner());
        }
        loadingDelegate.setOnLoadingCancelListener(this::onLoadingCancelled);
    }

    /**
     * Loading 被用户取消时的统一入口（与 {@link BaseActivity#onLoadingCancelled} 对齐）。
     *
     * @param policy 取消时 Dialog 上的策略
     */
    protected void onLoadingCancelled(@NonNull LoadingCancelPolicy policy) {
        if (mViewModel != null) {
            mViewModel.cancelUiRequests();
        } else if (loadingDelegate != null) {
            loadingDelegate.hideLoading();
        }
        if (policy != null && policy.shouldExitOnCancel() && isUiSafe()) {
            finishAfterLoadingCancel();
        }
    }

    /**
     * {@link LoadingCancelPolicy#CancelRequestAndExit} 时离开本页。
     * 默认走宿主 Activity 的 OnBackPressedDispatcher（通常 pop Fragment 或 finish）。
     */
    protected void finishAfterLoadingCancel() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    /**
     * 创建并绑定 ViewModel。请求 UI 的 {@link NetworkRequestUiBinder#bind} 在 {@link #onViewCreated} 中执行。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void createViewModel() {
        if (mViewModel == null) {
            Class modelClass = ViewModelHelper.resolveViewModelClass(getClass());
            mViewModel = (VM) new ViewModelProvider(useActivityViewModel() ? requireActivity() : this).get(modelClass);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindNetworkRequestUi();
    }

    /**
     * 将 ViewModel 内 {@link NetworkRequestUiHost} 派发到本页。
     * 与 Activity 共用 VM 时默认仍 bind 到本 Fragment（loading 跟当前可见页）；
     * 若希望只由 Activity 展示，可重写为空并依赖 Activity 的 bind。
     */
    protected void bindNetworkRequestUi() {
        if (mViewModel == null) {
            return;
        }
        NetworkRequestUiBinder.bind(getViewLifecycleOwner(), mViewModel.getNetworkRequestUiHost(), resolveRequestUi());
    }

    /**
     * 请求 UI 渲染落点；与 Activity 共用 VM 时可落到宿主 Activity。
     */
    @NonNull
    protected RequestUi resolveRequestUi() {
        if (!useActivityViewModel()) {
            return this;
        }
        if (!(requireActivity() instanceof BaseActivity)) {
            throw new IllegalStateException(
                    getClass().getSimpleName()
                            + ".useActivityViewModel()==true 时，宿主 Activity 必须继承 BaseActivity");
        }
        return (BaseActivity<?, ?>) requireActivity();
    }

    /**
     * 是否和activity共用同一个viewModel
     * @return true代表与Activity共用
     */
    public boolean useActivityViewModel() {
        return false;
    }

    /**
     * 该抽象方法就是 onCreateView中需要的layoutID
     *
     * @return 布局资源id
     */
    protected abstract int getLayoutId();

    /**
     * 该抽象方法就是 初始化view
     */
    protected abstract void initView(Bundle savedInstanceState);

    /**
     * 执行数据的加载
     */
    protected abstract void initData(Bundle bundle);

    @Override
    public void onAuthSuccess(@Nullable Bundle data) {

    }

    @Override
    public void onAuthFail(int resultCode, @Nullable Bundle data) {

    }

    @Override
    public void onDestroyView() {
        if (loadingDelegate != null) {
            loadingDelegate.hideLoading();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authManager != null) {
            authManager.unregister();
        }
    }

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
        showLoading(dialogMessage, enableDynamicEllipsis, LoadingCancelPolicy.None);
    }

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis,
                            @NonNull LoadingCancelPolicy cancelPolicy) {
        LoadingCancelPolicy policy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        if (loadingDelegate == null) {
            return;
        }
        loadingDelegate.showLoading(dialogMessage, enableDynamicEllipsis, policy);
    }

    @Override
    public void hideLoading() {
        if (loadingDelegate == null) {
            return;
        }
        loadingDelegate.hideLoading();
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        if (loadingDelegate == null) {
            return;
        }
        loadingDelegate.refreshLoading(dialogMessage);
    }

    public void showToast(String msg) {
        if (loadingDelegate == null) {
            return;
        }
        loadingDelegate.showToast(msg);
    }

    public void showToast(@StringRes int strRes) {
        if (loadingDelegate == null) {
            return;
        }
        loadingDelegate.showToast(strRes);
    }

    /**
     * 请求错误渲染：业务码走登录/权限；其它走 Toast。
     */
    @Override
    public void showError(AppError error) {
        if (error == null || !isUiSafe()) {
            return;
        }
        RequestErrorPresenter.show(error, requireContext(), errorService, authManager, this::showToast);
    }

    @Override
    public void onBusinessCode(String code, String message) {
        showError(new AppError.Business(code != null ? code : "", message != null ? message : "", null));
    }

    public void startActivity(Class<?> toClx) {
        startActivity(toClx, null);
    }

    public void startActivity(Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(requireContext(), toClx);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Class<?> toClx) {
        startForResult(activityResultLauncher, toClx, null);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(requireContext(), toClx);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activityResultLauncher.launch(intent);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Intent intent) {
        activityResultLauncher.launch(intent);
    }

}
