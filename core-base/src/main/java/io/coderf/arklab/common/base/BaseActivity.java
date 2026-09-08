package io.coderf.arklab.common.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.BaseActivityConstraintBinding;
import io.coderf.arklab.common.helper.AuthManager;
import io.coderf.arklab.common.helper.UIController;
import io.coderf.arklab.common.helper.ViewModelHelper;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.download.DownloadPermissionHelper;
import io.coderf.arklab.common.utils.theme.EdgeToEdgeHelper;
import io.coderf.arklab.common.utils.theme.ThemeUtils;
import io.coderf.arklab.core.ui.delegate.AlwaysInitData;
import io.coderf.arklab.core.ui.delegate.EdgeToEdgeDisabled;
import io.coderf.arklab.core.ui.delegate.EdgeToEdgeEnabled;
import io.coderf.arklab.core.ui.delegate.EdgeToEdgePolicy;
import io.coderf.arklab.core.ui.delegate.HideKeyboardOnTouchOutsideDelegate;
import io.coderf.arklab.core.ui.delegate.HideKeyboardOnTouchOutsidePolicy;
import io.coderf.arklab.core.ui.delegate.ImeAdjustDisabled;
import io.coderf.arklab.core.ui.delegate.ImeInsetPolicy;
import io.coderf.arklab.core.ui.delegate.ImmersiveBarPolicy;
import io.coderf.arklab.core.ui.delegate.ImmersiveBarsVisible;
import io.coderf.arklab.core.ui.delegate.InitDataPolicy;
import io.coderf.arklab.core.ui.delegate.PageArguments;
import io.coderf.arklab.core.ui.delegate.PageArgumentsResolver;
import io.coderf.arklab.core.ui.delegate.UiSafety;
import io.coderf.arklab.core.ui.delegate.UiSafetyChecker;

/**
 * Activity MVVM 基类：统一 MaterialToolbar、DataBinding、ViewModel、登录/权限与 Loading。
 * <p>
 * UI 行为策略来自 {@code core-ui} 委托（{@link InitDataPolicy}、{@link EdgeToEdgePolicy}、
 * {@link HideKeyboardOnTouchOutsideDelegate} 等）；子类可通过改 policy 字段或重写钩子方法定制。
 * <p>
 * <b>生命周期约定（与历史行为兼容）</b>
 * <ul>
 *   <li>{@link #initView(Bundle)}：每次 {@code onCreate} 都会调用；可用 {@code savedInstanceState} 恢复 View 状态。</li>
 *   <li>{@link #initData(Bundle)}：默认每次 {@code onCreate} 都会调用（含旋转、进程恢复），参数来自 {@link #resolvePageArguments()}。</li>
 *   <li>若只需「首次进入」拉数，重写 {@link #shouldRunInitData(Bundle)}、改 {@link #initDataPolicy}，或继承 {@link BaseStatefulActivity}。</li>
 * </ul>
 *
 * @param <VM>  ViewModel 类型
 * @param <VDB> 页面 DataBinding 类型
 * @see BaseStatefulActivity
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/25 13:12
 */
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity
        implements BaseView, AuthManager.AuthCallback {

    protected String TAG = this.getClass().getSimpleName();

    /** 页面 ViewModel，由 {@link #createViewModel()} 惰性创建。 */
    protected VM mViewModel;

    /** 正文 DataBinding。 */
    protected VDB binding;

    /** 带 MaterialToolbar 外壳时的根 Binding；{@link #hasToolBar()} 为 false 时为 null。 */
    @Nullable
    protected BaseActivityConstraintBinding toolbarBind;

    @Inject
    public ErrorService errorService;

    protected AuthManager authManager;

    protected UIController uiController;

    /**
     * initData 是否在配置变更后再次执行；默认每次都执行（历史行为）。
     * 可改为 {@link io.coderf.arklab.core.ui.delegate.FirstCreateOnlyInitData}。
     */
    @NonNull
    protected InitDataPolicy initDataPolicy = AlwaysInitData.INSTANCE;

    /** 页面参数解析；默认 Intent extras。可替换为深链/路由实现。 */
    @Nullable
    protected PageArgumentsResolver pageArgumentsResolver;

    /** UI 安全检查；默认 Activity 未 finish/destroy。 */
    @Nullable
    protected UiSafetyChecker uiSafetyChecker;

    /** 点击空白收键盘策略；默认跟随 {@link Config#isHideKeyboardOnTouchOutside()}。 */
    @Nullable
    protected HideKeyboardOnTouchOutsidePolicy hideKeyboardOnTouchOutsidePolicy;

    @Nullable
    private HideKeyboardOnTouchOutsideDelegate hideKeyboardDelegate;

    /**
     * Edge-to-Edge；未赋值时跟随 {@link Config#isEdgeToEdgeEnabled()}（默认开启）。
     * 全屏视频等可改为 {@link io.coderf.arklab.core.ui.delegate.EdgeToEdgeDisabled}。
     */
    @Nullable
    protected EdgeToEdgePolicy edgeToEdgePolicy;

    /** 键盘顶起底部；默认关闭。表单页可改为 {@link io.coderf.arklab.core.ui.delegate.ImeAdjustEnabled}。 */
    @NonNull
    protected ImeInsetPolicy imeInsetPolicy = ImeAdjustDisabled.INSTANCE;

    /** 全屏隐藏系统栏；默认关闭。 */
    @NonNull
    protected ImmersiveBarPolicy immersiveBarPolicy = ImmersiveBarsVisible.INSTANCE;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldApplyEdgeToEdge()) {
            EdgeToEdgeHelper.enable(this);
        }
        super.onCreate(savedInstanceState);
        ensureDelegates();
        createAuthManager();
        AppManager.getAppManager().addActivity(this);
        createUIController();
        initToolbar();
        createViewModel();
        initView(savedInstanceState);
        if (shouldRunInitData(savedInstanceState)) {
            initData(resolvePageArguments());
        }
        applyHideStatusBarIfNeeded();
    }

    /**
     * 惰性初始化默认委托（可在 onCreate 前由子类先赋值覆盖）。
     */
    protected void ensureDelegates() {
        if (pageArgumentsResolver == null) {
            pageArgumentsResolver = PageArguments.fromActivity(this);
        }
        if (uiSafetyChecker == null) {
            uiSafetyChecker = UiSafety.forActivity(this);
        }
        if (hideKeyboardOnTouchOutsidePolicy == null) {
            hideKeyboardOnTouchOutsidePolicy = () -> Config.getInstance().isHideKeyboardOnTouchOutside();
        }
        if (hideKeyboardDelegate == null) {
            hideKeyboardDelegate = new HideKeyboardOnTouchOutsideDelegate(this, hideKeyboardOnTouchOutsidePolicy);
        }
        ensureEdgeToEdgePolicy();
    }

    private void ensureEdgeToEdgePolicy() {
        if (edgeToEdgePolicy == null) {
            edgeToEdgePolicy = Config.getInstance().isEdgeToEdgeEnabled()
                    ? EdgeToEdgeEnabled.INSTANCE
                    : EdgeToEdgeDisabled.INSTANCE;
        }
    }

    /**
     * 是否在 {@code onCreate} 中调用 {@link #initData(Bundle)}。
     * <p>
     * 默认走 {@link #initDataPolicy}。子类可改为仅首次创建时加载。
     */
    protected boolean shouldRunInitData(@Nullable Bundle savedInstanceState) {
        return initDataPolicy.shouldRunInitData(savedInstanceState);
    }

    /**
     * 传给 {@link #initData(Bundle)} 的页面参数。
     * 默认 {@link PageArguments#fromActivity}；子类可赋值 {@link #pageArgumentsResolver} 或重写本方法。
     */
    @NonNull
    protected Bundle resolvePageArguments() {
        ensureDelegates();
        return pageArgumentsResolver.resolve();
    }

    /** 是否为首次创建（非配置变更/进程恢复后的重建）。 */
    protected final boolean isFirstCreation(@Nullable Bundle savedInstanceState) {
        return savedInstanceState == null;
    }

    /** 当前是否适合展示 Dialog/Toast（未 finish 且未 destroy）。 */
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
            uiController = new UIController(this, getLifecycle());
        }
    }

    /**
     * @deprecated 请改用 {@link #hideKeyboardOnTouchOutsidePolicy}。本方法仅作兼容转发。
     */
    @Deprecated
    protected boolean hideKeyboardOnTouchOutside() {
        ensureDelegates();
        return hideKeyboardOnTouchOutsidePolicy.isEnabled();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ensureDelegates();
        // 经 hideKeyboardOnTouchOutside()，兼容仍重写该方法的子类；新代码请改 policy 字段
        if (hideKeyboardOnTouchOutside()) {
            HideKeyboardOnTouchOutsideDelegate.handleTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void initToolbar() {
        if (hasToolBar()) {
            toolbarBind = DataBindingUtil.setContentView(this, R.layout.base_activity_constraint);
            toolbarBind.setLifecycleOwner(this);
            toolbarBind.setContext(this);
            binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutId(), toolbarBind.mainContainer, true);
            binding.setLifecycleOwner(this);
            toolbarBind.setToolbarConfig(createdToolbarConfig());
            applyToolbarHeight();
            if (shouldApplyEdgeToEdge()) {
                EdgeToEdgeHelper.applyNavigationBarInsets(toolbarBind.mainContainer, shouldAdjustBottomForIme());
            }
            toolbarBind.mainBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        } else {
            createdToolbarConfig();
            binding = DataBindingUtil.setContentView(this, getLayoutId());
            binding.setLifecycleOwner(this);
            if (shouldApplyEdgeToEdge()) {
                boolean adjustForIme = shouldAdjustBottomForIme();
                if (enableImmersionBar()) {
                    EdgeToEdgeHelper.applyNavigationBarInsets(binding.getRoot(), adjustForIme);
                } else {
                    EdgeToEdgeHelper.applySystemBarInsets(binding.getRoot(), adjustForIme);
                }
            }
        }
    }

    /**
     * 是否启用 Android 15 Edge-to-Edge。默认读 {@link #edgeToEdgePolicy}，未赋值时跟随 {@link Config}。
     */
    protected boolean shouldApplyEdgeToEdge() {
        ensureEdgeToEdgePolicy();
        EdgeToEdgePolicy policy = edgeToEdgePolicy;
        return policy != null && policy.shouldApply();
    }

    /**
     * 软键盘弹出时是否将底部内容顶起。默认读 {@link #imeInsetPolicy}。
     */
    protected boolean shouldAdjustBottomForIme() {
        return imeInsetPolicy.shouldAdjustBottomForIme();
    }

    /**
     * 应用 {@link ToolbarConfig#getHeight()} 到 MaterialToolbar。
     * 若在 {@link #initView} / {@link #initData} 中调用 {@code setHeight()}，请在此后再次调用本方法。
     */
    protected void applyToolbarHeight() {
        if (!hasToolBar() || toolbarBind == null) {
            return;
        }
        ToolbarConfig toolbarConfig = toolbarBind.getToolbarConfig();
        int height = toolbarConfig.getHeight();
        boolean customHeight = toolbarConfig.hasCustomHeight();
        if (shouldApplyEdgeToEdge()) {
            EdgeToEdgeHelper.applyToolbarInsets(toolbarBind.mainBar, height, customHeight);
        } else {
            EdgeToEdgeHelper.applyToolbarHeight(toolbarBind.mainBar, height, customHeight);
        }
    }

    /**
     * 获取 MaterialToolbar；{@link #hasToolBar()} 为 false 时返回 null，避免 NPE。
     */
    @Nullable
    public MaterialToolbar getToolbar() {
        return toolbarBind != null ? toolbarBind.mainBar : null;
    }

    /**
     * 向 MaterialToolbar 填充菜单。不走 {@code setSupportActionBar} / {@code onCreateOptionsMenu}。
     */
    protected void inflateToolbarMenu(@MenuRes int menuRes,
                                      @Nullable MaterialToolbar.OnMenuItemClickListener listener) {
        MaterialToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }
        toolbar.getMenu().clear();
        toolbar.inflateMenu(menuRes);
        toolbar.setOnMenuItemClickListener(listener);
    }

    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).setEnableImmersionBar(enableImmersionBar()).setLightMode(false).setTitle(setTitleBar()).setBgColor(R.color.cardSurface).applyStatusBar();
    }

    /**
     * 创建并绑定 ViewModel。沿继承链解析泛型，兼容 Hilt 生成类与子类只写 {@code extends XxxActivity} 的场景。
     * <p>
     * ViewModel 由 {@link ViewModelProvider} 缓存；每次 {@code onCreate} 都会
     * {@link BaseViewModel#createRepository(BaseView)} 重绑当前页面，并
     * {@link NetworkRequestUiBinder#bind} 订阅请求 UI LiveData，避免配置变更后仍指向旧 Activity。
     */
    @SuppressWarnings("unchecked")
    public void createViewModel() {
        if (mViewModel == null) {
            Class modelClass = ViewModelHelper.resolveViewModelClass(getClass());
            mViewModel = (VM) new ViewModelProvider(this).get(modelClass);
        }
        mViewModel.createRepository(this);
        bindNetworkRequestUi();
    }

    /**
     * 将 ViewModel 内 {@link NetworkRequestUiHost} 的 loading/toast/error 派发到本页 {@link BaseView}。
     * 子类若完全自定义请求 UI，可重写为空实现并自行 observe {@link BaseViewModel#getNetworkRequestUiHost()}。
     */
    protected void bindNetworkRequestUi() {
        if (mViewModel == null) {
            return;
        }
        NetworkRequestUiBinder.bind(this, mViewModel.getNetworkRequestUiHost(), this);
    }

    @Override
    public void onAuthSuccess(@Nullable Bundle data) {
    }

    @Override
    public void onAuthFail(int resultCode, @Nullable Bundle data) {
    }

    protected boolean hasToolBar() {
        return true;
    }

    protected boolean enableImmersionBar() {
        return false;
    }

    /**
     * 是否隐藏系统状态栏与导航栏。默认读 {@link #immersiveBarPolicy}。
     */
    protected boolean shouldHideStatusBar() {
        return immersiveBarPolicy.shouldHideSystemBars();
    }

    /**
     * 按 {@link #shouldHideStatusBar()} 应用或维持系统栏隐藏。
     */
    protected void applyHideStatusBarIfNeeded() {
        if (shouldHideStatusBar()) {
            ThemeUtils.applyHideSystemBarsImmersive(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyHideStatusBarIfNeeded();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyHideStatusBarIfNeeded();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyHideStatusBarIfNeeded();
    }

    public abstract String setTitleBar();

    /**
     * 初始化 View 与事件绑定；配置变更后会再次调用。
     */
    public abstract void initView(Bundle savedInstanceState);

    /**
     * 加载页面数据；是否在重建时调用由 {@link #shouldRunInitData(Bundle)} 控制。
     */
    public abstract void initData(Bundle bundle);

    @Override
    protected void onDestroy() {
        if (uiController == null) {
            // no-op
        } else {
            uiController.hideLoading();
        }
        if (mViewModel != null) {
            mViewModel.unbindView();
        }
        super.onDestroy();
        // 仅从栈移除；不可 finish()，否则旋转屏等配置变更后 Activity 无法重建
        AppManager.getAppManager().removeActivity(this);
        if (authManager != null) {
            authManager.unregister();
        }
    }

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.showLoading(dialogMessage, enableDynamicEllipsis, false);
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.refreshLoading(dialogMessage);
    }

    @Override
    public void hideLoading() {
        if (uiController == null) {
            return;
        }
        uiController.hideLoading();
    }

    @Override
    public void showToast(String msg) {
        if (!isUiSafe() || uiController == null) {
            return;
        }
        uiController.showToast(msg);
    }

    @Override
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
            errorService.toLogin(this, authManager.getLoginLauncher());
            return;
        }
        if (!errorService.hasPermission(model.getCode())) {
            errorService.toNoPermission(this, authManager.getPermissionLauncher());
        }
    }

    public void startActivity(Class<?> toClx) {
        startActivity(toClx, null);
    }

    public void startActivity(Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(this, toClx);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Class<?> toClx) {
        startForResult(activityResultLauncher, toClx, null);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(this, toClx);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activityResultLauncher.launch(intent);
    }

    public void startForResult(ActivityResultLauncher<Intent> activityResultLauncher, Intent intent) {
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (DownloadPermissionHelper.dispatchPermissionResult(this, requestCode, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

