package com.casic.otitan.demo.activity;


import android.os.Build;
import android.os.Bundle;

import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.UseCase;
import com.casic.otitan.demo.databinding.ActivityDemoSmartPagingBinding;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.viewmodel.EmptyViewModel;

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