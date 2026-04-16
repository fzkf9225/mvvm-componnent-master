package io.coderf.arklab.demo.activity;

import android.os.Bundle;

import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityHiltBinding;
import io.coderf.arklab.demo.viewmodel.TestHiltViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;

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