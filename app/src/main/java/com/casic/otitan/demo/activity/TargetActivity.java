package com.casic.otitan.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.casic.otitan.demo.R;
import com.casic.otitan.demo.databinding.ActivityTargetBinding;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.annotations.interrupte.NeedLogin;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.viewmodel.EmptyViewModel;

@NeedLogin(enable = true)
@AndroidEntryPoint
public class TargetActivity extends BaseActivity<EmptyViewModel, ActivityTargetBinding> {
    public final static String ARGS = "ARGS";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_target;
    }

    @Override
    public String setTitleBar() {
        return "测试登录拦截";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.buttonLogin.setOnClickListener(v-> errorService.toLogin(this));
    }

    @Override
    public void initData(Bundle bundle) {
        String args = bundle.getString(ARGS);
        binding.tvArgs.setText(TextUtils.isEmpty(args) ? "暂无参数" : args);
    }
}