package pers.fz.mvvm.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.dialog.CustomProgressDialog;
import pers.fz.mvvm.util.log.ToastUtils;
import pers.fz.mvvm.util.permission.PermissionsChecker;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by fz on 2017/11/22.
 * BaseFragment封装
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements BaseView {
    protected String TAG = this.getClass().getSimpleName();

    /**
     * 权限检测器
     */
    private PermissionsChecker mPermissionsChecker;
    protected VM mViewModel;
    protected VDB binding;
    private ActivityResultLauncher<String[]> permissionLauncher;
    @Inject
    protected ErrorService errorService;
    protected ActivityResultLauncher<Intent> loginLauncher = null;

    public void requestPermission(String[] permissions) {
        permissionLauncher.launch(permissions);
    }

    private PermissionsChecker getPermissionsChecker() {
        if (mPermissionsChecker == null) {
            mPermissionsChecker = new PermissionsChecker(requireActivity());
        }
        return mPermissionsChecker;
    }

    public boolean lacksPermissions(String[] permissions) {
        return getPermissionsChecker().lacksPermissions(permissions);
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

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        binding.setLifecycleOwner(this);
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                onLoginSuccessCallback(result.getData() == null ? null : result.getData().getExtras());
            } else {
                onLoginFailCallback(result.getResultCode(), result.getData() == null ? null : result.getData().getExtras());
            }
        });
        createViewModel();
        initView(savedInstanceState);
        initData(getArguments() == null ? new Bundle() : getArguments());
        return binding.getRoot();
    }

    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultWidth() {
        try {
            ApplicationInfo info = requireActivity().getPackageManager()
                    .getApplicationInfo(requireActivity().getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_width_in_dp", 360);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 360;
    }

    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultHeight() {
        try {
            ApplicationInfo info = requireActivity().getPackageManager()
                    .getApplicationInfo(requireActivity().getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_height_in_dp", 640);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 640;
    }


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
            mViewModel.setBaseView(this);
        }
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

    protected void onLoginSuccessCallback(Bundle bundle) {

    }

    protected void onLoginFailCallback(int resultCode, Bundle bundle) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loginLauncher != null) {
            loginLauncher.unregister();
        }
    }

    public long lastClick = 0;

    /**
     * [防止快速点击]
     *
     * @return true-快速点击
     */
    public boolean fastClick(long intervalTime) {
        if (System.currentTimeMillis() - lastClick <= intervalTime) {
            return true;
        }
        lastClick = System.currentTimeMillis();
        return false;
    }

    /**
     * 关闭弹框
     */
    private void closeLoadingDialog() {
        CustomProgressDialog.getInstance(getContext()).hide();
    }

    /**
     * 显示加载弹框
     *
     * @param dialogMessage 弹框内容，如果内容为空则不展示文字部分
     */
    private void showLoadingDialog(String dialogMessage, boolean isCanCancel) {
        CustomProgressDialog.getInstance(getContext())
                .setCanCancel(isCanCancel)
                .setMessage(dialogMessage)
                .builder()
                .show();
    }

    @Override
    public void showLoading(String dialogMessage) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> showLoadingDialog(dialogMessage, false));
        }
    }

    @Override
    public void hideLoading() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(this::closeLoadingDialog);
        }
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                CustomProgressDialog.getInstance(requireActivity())
                        .refreshMessage(dialogMessage);
            });
        }
    }

    @Override
    public void showToast(String msg) {
        requireActivity().runOnUiThread(() -> ToastUtils.showShort(getActivity(), msg));
    }

    @Override
    public void onErrorCode(BaseModelEntity model) {
        if (errorService == null || model == null) {
            return;
        }
        if (!errorService.isLogin(model.getCode())) {
            errorService.toLogin(requireContext(), loginLauncher);
            return;
        }
        if (!errorService.hasPermission(model.getCode())) {
            errorService.toNoPermission(requireContext());
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
