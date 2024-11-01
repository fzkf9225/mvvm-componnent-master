package pers.fz.mvvm.base;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.gyf.immersionbar.ImmersionBar;

import javax.inject.Inject;

import pers.fz.mvvm.R;
import pers.fz.mvvm.annotations.interrupte.NeedLogin;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.databinding.BaseActivityConstraintBinding;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.permission.PermissionsChecker;
import pers.fz.mvvm.wight.dialog.CustomProgressDialog;
import pers.fz.mvvm.wight.dialog.LoginDialog;

/**
 * Create by CherishTang on 2019/8/1
 * describe:BaseActivity封装
 */
public abstract class BaseActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends AppCompatActivity implements BaseView, LoginDialog.OnLoginClickListener {
    protected String TAG = this.getClass().getSimpleName();
    protected VM mViewModel;
    protected VDB binding;
    protected BaseActivityConstraintBinding toolbarBind;
    private ActivityResultLauncher<String[]> permissionLauncher;
    protected ActivityResultLauncher<Intent> loginLauncher;
    @Inject
    public ErrorService errorService;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                onLoginSuccessCallback(result.getData() == null ? null : result.getData().getExtras());
            } else {
                onLoginFailCallback(result.getResultCode(), result.getData() == null ? null : result.getData().getExtras());
            }
        });
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        initToolbar();
        if (enableImmersionBar()) {
            initImmersionBar();
        }
        createViewModel();
        //是否启用登录拦截器了，写在最前面，防止数据请求等重复检测登录的行为
        if (onInterceptLoginAnnotation()) {
            return;
        }
        initView(savedInstanceState);
        initData((getIntent() == null || getIntent().getExtras() == null) ? new Bundle() : getIntent().getExtras());
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
            toolbarBind.mainBar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
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
        return new ToolbarConfig().setTitle(setTitleBar()).setBgColor(R.color.white);
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected void initImmersionBar() {
        //设置共同沉浸式样式
        if (toolbarBind == null) {
            ImmersionBar.with(this)
                    .keyboardEnable(true)
                    .init();
        } else {
            ImmersionBar.with(this)
                    .statusBarColor(toolbarBind.getToolbarConfig().getBgColor())
                    .autoStatusBarDarkModeEnable(true, 0.2f)
                    .keyboardEnable(true)
                    .titleBar(toolbarBind.mainBar)
                    .init();
        }
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


    public boolean lacksPermissions(String... permission) {
        return PermissionsChecker.getInstance().lacksPermissions(this, permission);
    }

    /**
     * 权限请求
     *
     * @param permissions 权限
     */
    public void requestPermission(String[] permissions) {
        // 缺少权限时, 进入权限配置页面
        permissionLauncher.launch(permissions);
    }

    /**
     * 注册权限请求监听
     */
    protected void registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            for (Map.Entry<String, Boolean> stringBooleanEntry : result.entrySet()) {
                if (Boolean.FALSE.equals(stringBooleanEntry.getValue())) {
                    onPermissionRefused(result);
                    return;
                }
            }
            onPermissionGranted(result);
        });
    }

    protected void unregisterPermission() {
        if (permissionLauncher != null) {
            permissionLauncher.unregister();
        }
    }

    protected void onLoginSuccessCallback(Bundle bundle) {

    }

    protected void onLoginFailCallback(int resultCode, Bundle bundle) {

    }

    /**
     * 权限同意
     */
    protected void onPermissionGranted(Map<String, Boolean> permissions) {

    }

    /**
     * 权限拒绝
     */
    protected void onPermissionRefused(Map<String, Boolean> permissions) {
        showToast("拒绝权限可能会导致应用软件运行异常!");
    }

    protected boolean hasToolBar() {
        return true;
    }

    protected boolean enableImmersionBar() {
        return true;
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
        if (loginLauncher != null) {
            loginLauncher.unregister();
        }
    }

    @Override
    public void onLoginClick(View v, int code) {
        if (errorService == null) {
            return;
        }
        errorService.toLogin(this, loginLauncher);
    }

    private void showLoadingDialog(String dialogMessage, boolean isCanCancel) {
        CustomProgressDialog.getInstance(this)
                .setCanCancel(isCanCancel)
                .setMessage(dialogMessage)
                .builder()
                .show();
    }

    @Override
    public void showLoading(String dialogMessage) {
        runOnUiThread(() -> showLoadingDialog(dialogMessage, false));
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        runOnUiThread(() ->
                CustomProgressDialog.getInstance(this)
                        .refreshMessage(dialogMessage));
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> CustomProgressDialog.getInstance(this).dismiss());
    }

    @Override
    public void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
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
        if (!errorService.isLoginPast(model.getCode())) {
            errorService.toLogin(this, loginLauncher);
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

    public boolean onInterceptLoginAnnotation() {
        try {
            if (errorService == null) {
                return false;
            }
            //不包含注解或者登录注解未开启
            if (!isNeedLogin()) {
                return false;
            }
            //已登录，则跳转登录
            if (!checkLogin()) {
                return false;
            }
            //如果未登录跳转登录并且把当前页的信息传递过去，以便于登录后回传
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString(ConstantsHelper.TARGET_ACTIVITY, getClass().getName());
            errorService.toLogin(this, bundle);
            finish();
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "登录拦截器异常：" + e);
        }
        return false;
    }

    private boolean checkLogin() {
        // 检查登录状态的逻辑
        return !errorService.isLogin();
    }

    private boolean isNeedLogin() {
        // 通过反射或注解处理器获取当前 Activity 是否需要登录
        boolean isAnnotation = getClass().isAnnotationPresent(NeedLogin.class);
        if (!isAnnotation) {
            return false;
        }
        NeedLogin needLogin = getClass().getAnnotation(NeedLogin.class);
        if (needLogin == null) {
            return false;
        }
        return needLogin.enable();
    }

}
