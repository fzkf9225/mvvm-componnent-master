package io.coderf.arklab.common.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

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
import io.coderf.arklab.common.utils.common.KeyBoardUtil;
import io.coderf.arklab.common.utils.theme.EdgeToEdgeHelper;
/**
 * Activity MVVM 基类：统一 Toolbar、DataBinding、ViewModel、登录/权限与 Loading。
 * <p>
 * <b>生命周期约定（与历史行为兼容）</b>
 * <ul>
 *   <li>{@link #initView(Bundle)}：每次 {@code onCreate} 都会调用；可用 {@code savedInstanceState} 恢复 View 状态。</li>
 *   <li>{@link #initData(Bundle)}：默认每次 {@code onCreate} 都会调用（含旋转、进程恢复），参数来自 {@link #resolvePageArguments()}。</li>
 *   <li>若只需「首次进入」拉数，重写 {@link #shouldRunInitData(Bundle)} 或继承 {@link BaseStatefulActivity}。</li>
 * </ul>
 *
 * @param <VM>  ViewModel 类型
 * @param <VDB> 页面 DataBinding 类型
 * @see BaseStatefulActivity
 */
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity
        implements BaseView, AuthManager.AuthCallback {

    protected String TAG = this.getClass().getSimpleName();

    /** 页面 ViewModel，由 {@link #createViewModel()} 惰性创建。 */
    protected VM mViewModel;

    /** 正文 DataBinding。 */
    protected VDB binding;

    /** 带 Toolbar 外壳时的根 Binding；{@link #hasToolBar()} 为 false 时为 null。 */
    @Nullable
    protected BaseActivityConstraintBinding toolbarBind;

    @Inject
    public ErrorService errorService;

    protected AuthManager authManager;

    protected UIController uiController;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldApplyEdgeToEdge()) {
            EdgeToEdgeHelper.enable(this);
        }
        super.onCreate(savedInstanceState);
        createAuthManager();
        AppManager.getAppManager().addActivity(this);
        createUIController();
        initToolbar();
        createViewModel();
        initView(savedInstanceState);
        if (shouldRunInitData(savedInstanceState)) {
            initData(resolvePageArguments());
        }
    }

    /**
     * 是否在 {@code onCreate} 中调用 {@link #initData(Bundle)}。
     * <p>
     * 默认 {@code true}，与历史版本一致。子类可改为仅首次创建时加载，例如：
     * {@code return savedInstanceState == null;}
     */
    protected boolean shouldRunInitData(@Nullable Bundle savedInstanceState) {
        return true;
    }

    /**
     * 传给 {@link #initData(Bundle)} 的页面参数，默认取自 {@link #getIntent()} extras。
     * 子类可重写以统一处理深链、路由等参数来源。
     */
    @NonNull
    protected Bundle resolvePageArguments() {
        Intent intent = getIntent();
        Bundle extras = intent != null ? intent.getExtras() : null;
        return extras != null ? extras : new Bundle();
    }

    /** 是否为首次创建（非配置变更/进程恢复后的重建）。 */
    protected final boolean isFirstCreation(@Nullable Bundle savedInstanceState) {
        return savedInstanceState == null;
    }

    /** 当前是否适合展示 Dialog/Toast（未 finish 且未 destroy）。 */
    protected boolean isUiSafe() {
        return !isFinishing() && !isDestroyed();
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
     * 特性开关：点击空白区域关闭键盘。
     */
    protected boolean hideKeyboardOnTouchOutside() {
        return Config.getInstance().isHideKeyboardOnTouchOutside();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (hideKeyboardOnTouchOutside()) {
            KeyBoardUtil.handleDispatchTouchEvent(this, ev);
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
            setSupportActionBar(toolbarBind.mainBar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarBind.setToolbarConfig(createdToolbarConfig());
            applyToolbarHeight();
            if (shouldApplyEdgeToEdge()) {
                EdgeToEdgeHelper.applyNavigationBarInsets(toolbarBind.mainContainer);
            }
            toolbarBind.mainBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        } else {
            createdToolbarConfig();
            binding = DataBindingUtil.setContentView(this, getLayoutId());
            binding.setLifecycleOwner(this);
            if (shouldApplyEdgeToEdge()) {
                if (enableImmersionBar()) {
                    EdgeToEdgeHelper.applyNavigationBarInsets(binding.getRoot());
                } else {
                    EdgeToEdgeHelper.applySystemBarInsets(binding.getRoot());
                }
            }
        }
    }

    /**
     * 是否启用 Android 15 Edge-to-Edge（全屏视频等页面可返回 false）。
     */
    protected boolean shouldApplyEdgeToEdge() {
        return true;
    }

    /**
     * 应用 {@link ToolbarConfig#getHeight()} 到 Toolbar。
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
     * 获取 Toolbar；{@link #hasToolBar()} 为 false 时返回 null，避免 NPE。
     */
    @Nullable
    public Toolbar getToolbar() {
        return toolbarBind != null ? toolbarBind.mainBar : null;
    }

    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).setEnableImmersionBar(enableImmersionBar()).setLightMode(false).setTitle(setTitleBar()).setBgColor(R.color.white).applyStatusBar();
    }

    /**
     * 创建 ViewModel。沿继承链解析泛型，兼容 Hilt 生成类与子类只写 {@code extends XxxActivity} 的场景。
     */
    @SuppressWarnings("unchecked")
    public void createViewModel() {
        if (mViewModel == null) {
            Class modelClass = ViewModelHelper.resolveViewModelClass(getClass());
            mViewModel = (VM) new ViewModelProvider(this).get(modelClass);
            mViewModel.createRepository(this);
        }
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
        if (uiController != null) {
            uiController.hideLoading();
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
}
