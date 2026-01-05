package com.casic.otitan.common.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import javax.inject.Inject;

import com.casic.otitan.common.R;
import com.casic.otitan.common.api.AppManager;
import com.casic.otitan.common.bean.base.ToolbarConfig;
import com.casic.otitan.common.databinding.BaseActivityConstraintBinding;
import com.casic.otitan.common.helper.AuthManager;
import com.casic.otitan.common.helper.UIController;
import com.casic.otitan.common.inter.ErrorService;

/**
 * Create by CherishTang on 2019/8/1
 * describe:BaseActivity封装
 */
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity implements BaseView, AuthManager.AuthCallback {
    protected String TAG = this.getClass().getSimpleName();
    /**
     * viewModel
     */
    protected VM mViewModel;
    /**
     * 正文布局
     */
    protected VDB binding;
    /**
     * toolbar布局
     */
    protected BaseActivityConstraintBinding toolbarBind;
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

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createAuthManager();
        AppManager.getAppManager().addActivity(this);
        createUIController();
        initToolbar();
        createViewModel();
        initView(savedInstanceState);
        initData((getIntent() == null || getIntent().getExtras() == null) ? new Bundle() : getIntent().getExtras());
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
            toolbarBind.mainBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        } else {
            createdToolbarConfig();
            binding = DataBindingUtil.setContentView(this, getLayoutId());
            binding.setLifecycleOwner(this);
        }
    }

    public Toolbar getToolbar() {
        return toolbarBind.mainBar;
    }

    /**
     * 设置toolbar默认样式
     *
     * @return toolbar配置
     */
    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).setEnableImmersionBar(enableImmersionBar()).setLightMode(false).setTitle(setTitleBar()).setBgColor(R.color.white).applyStatusBar();
    }

    /**
     * 创建viewModel
     */
    public void createViewModel() {
        if (mViewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
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

    /**
     * 是否启用沉浸式状态栏
     * @return true:沉浸式状态栏
     */
    protected boolean enableImmersionBar() {
        return false;
    }

    public abstract String setTitleBar();

    /**
     * 初始化布局
     */
    public abstract void initView(Bundle savedInstanceState);

    /**
     * 设置数据
     */
    public abstract void initData(Bundle bundle);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
        if (authManager != null) {
            authManager.unregister();
        }
    }

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
        uiController.showLoading(dialogMessage, enableDynamicEllipsis, false);
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        uiController.refreshLoading(dialogMessage);
    }

    @Override
    public void hideLoading() {
        uiController.hideLoading();
    }

    @Override
    public void showToast(String msg) {
        uiController.showToast(msg);
    }

    /**
     * 注意判断空，根据自己需求更改
     *
     * @param model 错误吗实体
     */
    @Override
    public void onErrorCode(BaseResponse model) {
        if (errorService == null || model == null) {
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
