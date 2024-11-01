package com.casic.titan.demo.activity;

import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityHiltBinding;
import com.casic.titan.demo.viewmodel.TestHiltViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;

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