package com.casic.otitan.demo.activity;

import android.os.Bundle;

import com.casic.otitan.demo.R;
import com.casic.otitan.demo.databinding.ActivityHiltBinding;
import com.casic.otitan.demo.viewmodel.TestHiltViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseActivity;

@AndroidEntryPoint
public class HiltActivity extends BaseActivity<TestHiltViewModel, ActivityHiltBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_hilt;
    }

    @Override
    public String setTitleBar() {
        return "Hilt依赖注入";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setHiltViewModel(mViewModel);
    }

    @Override
    public void initData(Bundle bundle) {

    }
}