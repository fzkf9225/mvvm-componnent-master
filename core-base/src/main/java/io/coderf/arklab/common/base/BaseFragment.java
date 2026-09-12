package io.coderf.arklab.common.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import io.coderf.arklab.common.inter.RequestUiCallback;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.coderf.arklab.common.helper.AuthManager;
import io.coderf.arklab.common.helper.UIController;
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
 * UI 策略委托与 Activity 对称：{@link InitDataPolicy}、{@link PageArgumentsResolver}、{@link UiSafetyChecker}。
 * 需要「仅首次创建时 initData」可继承 {@link BaseStatefulFragment}，或设置
 * {@link io.coderf.arklab.core.ui.delegate.FirstCreateOnlyInitData}。
 *
 * @see BaseStatefulFragment
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/25 13:12
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements RequestUiCallback, AuthManager.AuthCallback {
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
     * 认证管理，管理登录相关
     */
    protected AuthManager authManager;
    /**
     * UI控制器。管理弹框、toast之类
     */
    protected UIController uiController;

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

    protected void createAuthManager() {
        if (authManager == null) {
            authManager = new AuthManager(this, errorService == null || errorService.unifyHandling());
        }
        authManager.setLoginCallback(this);
    }

    protected void createUIController() {
        if (uiController == null) {
            uiController = new UIController(requireContext(), getLifecycle());
        }
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
        mViewModel.ensureRepository();
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
    protected RequestUiCallback resolveRequestUi() {
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
        if (uiController != null) {
            uiController.hideLoading();
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
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.showLoading(requireActivity(), dialogMessage, enableDynamicEllipsis, false);
    }

    @Override
    public void hideLoading() {
        if (uiController == null) {
            return;
        }
        uiController.hideLoading();
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.refreshLoading(dialogMessage);
    }

    @Override
    public void showToast(String msg) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.showToast(msg);
    }

    public void showToast(@StringRes int strRes) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.showToast(strRes);
    }

    @Override
    public void onErrorCode(BaseResponse model) {
        if (errorService == null || model == null || !isUiSafe()) {
            return;
        }
        if (errorService.isLoginPast(model.getCode())) {
            errorService.toLogin(requireContext(), authManager.getLoginLauncher());
            return;
        }
        if (!errorService.hasPermission(model.getCode())) {
            errorService.toNoPermission(requireContext(), authManager.getPermissionLauncher());
        }
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
