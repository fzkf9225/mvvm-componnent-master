package com.casic.titan.demo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.core.app.ComponentActivity;

import com.casic.titan.demo.R;
import com.casic.titan.demo.adapter.UseCaseAdapter;
import com.casic.titan.demo.databinding.FragmentHomeBinding;
import com.casic.titan.demo.enumbean.UseCaseEnum;
import com.casic.titan.demo.viewmodel.HomeFragmentViewModel;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;


import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2023/4/28
 * describe：
 */
@AndroidEntryPoint
public class HomeFragment extends BaseFragment<HomeFragmentViewModel, FragmentHomeBinding> implements BaseRecyclerViewAdapter.OnItemClickListener{
    private UseCaseAdapter useCaseAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        registerPermissionLauncher();
        ImmersionBar.with(this)
                .autoStatusBarDarkModeEnable(true,0.2f)
                .statusBarColor(pers.fz.mvvm.R.color.default_background)
                .init();
    }

    @Override
    protected void initData(Bundle bundle) {
        binding.setIsLogin(UserAccountHelper.isLogin());
        binding.setToken(UserAccountHelper.isLogin() ? "已登录" : "暂未登录");
        useCaseAdapter = new UseCaseAdapter(requireContext(), UseCaseEnum.toUseCaseList());
        useCaseAdapter.setOnItemClickListener(this);
        binding.mRecyclerViewUseCase.setAdapter(useCaseAdapter);
    }

    @Override
    protected void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);

    }

    @Override
    public void onItemClick(View view, int position) {
        if(Activity.class.isAssignableFrom(useCaseAdapter.getList().get(position).getClx())){
            Bundle bundle = new Bundle();
            bundle.putParcelable("args",useCaseAdapter.getList().get(position));
            startActivity(useCaseAdapter.getList().get(position).getClx(),bundle);
            return;
        }
    }
}