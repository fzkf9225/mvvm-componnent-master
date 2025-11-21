package com.casic.otitan.common.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.casic.otitan.common.helper.AuthManager;
import com.casic.otitan.common.helper.UIController;
import com.casic.otitan.common.inter.ErrorService;

/**
 * Created by fz on 2017/11/22.
 * BaseFragment封装
 */
public abstract class BaseFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends Fragment implements BaseView ,AuthManager.AuthCallback{
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
    protected ErrorService errorService;
    /**
     * 认证管理，管理登录相关
     */
    protected AuthManager authManager;
    /**
     * UI控制器。管理弹框、toast之类
     */
    protected UIController uiController;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createAuthManager();
        createUIController();
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        binding.setLifecycleOwner(this);
        createViewModel();
        initView(savedInstanceState);
        initData(getArguments() == null ? new Bundle() : getArguments());
        return binding.getRoot();
    }

    protected void createAuthManager() {
        if (authManager == null) {
            authManager = new AuthManager(this);
        }
        authManager.setAuthCallback(this);
    }

    protected void createUIController() {
        if (uiController == null) {
            uiController = new UIController(requireContext(), getLifecycle());
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
            mViewModel = (VM) new ViewModelProvider(useActivityViewModel() ? requireActivity() : this).get(modelClass);
            mViewModel.createRepository(useActivityViewModel() ? ((BaseActivity)requireActivity()) : this);
        }
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
    public void onLoginSuccessCallback(@Nullable Bundle data) {

    }

    @Override
    public void onLoginFailCallback(int resultCode, @Nullable Bundle data) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authManager != null) {
            authManager.unregister();
        }
    }


    @Override
    public void showLoading(String dialogMessage,boolean enableDynamicEllipsis) {
        if (requireActivity().isFinishing()) {
            return;
        }
        uiController.showLoading(requireActivity(),dialogMessage,enableDynamicEllipsis,false);
    }

    @Override
    public void hideLoading() {
        if (requireActivity().isFinishing()) {
            return;
        }
        uiController.hideLoading();
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        if (requireActivity().isFinishing()) {
            return;
        }
        uiController.refreshLoading(dialogMessage);
    }

    @Override
    public void showToast(String msg) {
        uiController.showToast(msg);
    }

    @Override
    public void onErrorCode(BaseResponse model) {
        if (errorService == null || model == null) {
            return;
        }
        if (errorService.isLoginPast(model.getCode())) {
            errorService.toLogin(requireContext(), authManager.getLauncher());
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
