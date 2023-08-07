package com.casic.titan.demo.fragment;

import android.Manifest;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.FragmentHomeBinding;
import com.casic.titan.demo.viewmodel.HomeFragmentViewModel;
import com.casic.titan.userapi.UserService;
import com.casic.titan.userapi.router.UserRouterService;
import com.casic.titan.usercomponent.api.UserAccountHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.inter.RetryService;

/**
 * created by fz on 2023/4/28
 * describe：
 */
@AndroidEntryPoint
public class HomeFragment extends BaseFragment<HomeFragmentViewModel, FragmentHomeBinding> {

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO
    };
    @Inject
    UserService userService;
    @Inject
    UserRouterService userRouterService;
    @Inject
    RetryService retryService;

    private Disposable disposable;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        registerPermissionLauncher();
    }

    @Override
    protected void initData(Bundle bundle) {
        binding.setIsLogin(UserAccountHelper.isLogin());
        binding.setToken(UserAccountHelper.isLogin() ? "已登录" : "暂未登录");
    }

    private void init() {

    }

    @Override
    protected void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);
        init();
    }
}