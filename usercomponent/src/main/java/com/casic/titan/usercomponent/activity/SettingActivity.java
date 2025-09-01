package com.casic.titan.usercomponent.activity;

import android.os.Bundle;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.databinding.SettingBinding;
import com.casic.titan.usercomponent.viewmodel.SettingViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.base.BaseActivity;

/**
 * Create by CherishTang on 2019/10/11
 * describe:设置
 */
@AndroidEntryPoint
public class SettingActivity extends BaseActivity<SettingViewModel, SettingBinding> {
    private Bundle bundle;

    @Override
    protected int getLayoutId() {
        return R.layout.setting;
    }

    @Override
    public String setTitleBar() {
        return "设置";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.versionName.setValue("v" + AppManager.getAppManager().getVersion(this));
        binding.setSetViewModel(mViewModel);
        binding.tvSupport.setOnClickListener(v->startActivity(TechnicalSupportActivity.class));
    }

    @Override
    public void initData(Bundle bundle) {

    }


}
