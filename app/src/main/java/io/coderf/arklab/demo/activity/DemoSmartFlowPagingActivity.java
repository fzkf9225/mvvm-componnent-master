package io.coderf.arklab.demo.activity;


import android.os.Build;
import android.os.Bundle;

import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityDemoSmartFlowPagingBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DemoSmartFlowPagingActivity extends BaseActivity<EmptyViewModel, ActivityDemoSmartFlowPagingBinding> {

    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_demo_smart_flow_paging;
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