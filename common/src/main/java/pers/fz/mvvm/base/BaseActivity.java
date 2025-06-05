package pers.fz.mvvm.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

import pers.fz.mvvm.R;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.databinding.BaseActivityConstraintBinding;
import pers.fz.mvvm.helper.AuthManager;
import pers.fz.mvvm.helper.UIController;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.wight.dialog.LoginDialog;

/**
 * Create by CherishTang on 2019/8/1
 * describe:BaseActivity封装
 */
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity implements BaseView, LoginDialog.OnLoginClickListener, AuthManager.AuthCallback {
    protected String TAG = this.getClass().getSimpleName();
    protected VM mViewModel;
    protected VDB binding;
    protected BaseActivityConstraintBinding toolbarBind;
    @Inject
    public ErrorService errorService;

    protected AuthManager authManager;

    private UIController uiController;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createAuthManager();
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        createUIController();
        initToolbar();
        createViewModel();
        initView(savedInstanceState);
        initData((getIntent() == null || getIntent().getExtras() == null) ? new Bundle() : getIntent().getExtras());
    }

    protected void createAuthManager() {
        if (authManager == null) {
            authManager = new AuthManager(this);
        }
        authManager.setAuthCallback(this);
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
    public void onLoginSuccessCallback(@Nullable Bundle data) {

    }

    @Override
    public void onLoginFailCallback(int resultCode, @Nullable Bundle data) {

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
    public void onLoginClick(View v, int code) {
        if (errorService == null) {
            return;
        }
        errorService.toLogin(this, authManager.getLauncher());
    }

    @Override
    public void showLoading(String dialogMessage) {
        uiController.showLoading(dialogMessage, false);
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
    public void onErrorCode(BaseModelEntity model) {
        if (errorService == null || model == null) {
            return;
        }
        if (errorService.isLoginPast(model.getCode())) {
            errorService.toLogin(this, authManager.getLauncher());
            return;
        }
        if (!errorService.hasPermission(model.getCode())) {
            errorService.toNoPermission(this);
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
