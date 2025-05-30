package com.casic.titan.demo.activity;


import android.os.Build;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityDemoSmartPagingBinding;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

@AndroidEntryPoint
public class DemoSmartPagingActivity extends BaseActivity<EmptyViewModel, ActivityDemoSmartPagingBinding> {

    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_demo_smart_paging;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());

    }
}