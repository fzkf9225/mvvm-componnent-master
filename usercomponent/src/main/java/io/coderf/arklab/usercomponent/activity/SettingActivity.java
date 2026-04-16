package io.coderf.arklab.usercomponent.activity;

import android.os.Bundle;

import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.databinding.SettingBinding;
import io.coderf.arklab.usercomponent.viewmodel.SettingViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.base.BaseActivity;

/**
 * Create by fz on 2019/10/11
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
