package pers.fz.mvvm.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.gyf.immersionbar.ImmersionBar;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import pers.fz.mvvm.R;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.databinding.BaseActivityConstraintBinding;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.permission.PermissionsChecker;
import pers.fz.mvvm.wight.dialog.LoadingProgressDialog;
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
            toolbarBind.mainBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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

    public boolean lacksPermissions(List<String> permission) {
        return PermissionsChecker.getInstance().lacksPermissions(this, permission);
    }

    /**
     * 权限请求
     *
     * @param permissions 权限
     */
    public void requestPermission(String... permissions) {
        // 缺少权限时, 进入权限配置页面
        permissionLauncher.launch(permissions);
    }

    public void requestPermission(List<String> permissions) {
        // 缺少权限时, 进入权限配置页面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(permissions.toArray(String[]::new));
        } else {
            permissionLauncher.launch(permissions.stream().toArray(String[]::new));
        }
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
        LoadingProgressDialog.getInstance(this)
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
                LoadingProgressDialog.getInstance(this)
                        .refreshMessage(dialogMessage));
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> LoadingProgressDialog.getInstance(this).dismiss());
    }

    @Override
    public void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
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
}
