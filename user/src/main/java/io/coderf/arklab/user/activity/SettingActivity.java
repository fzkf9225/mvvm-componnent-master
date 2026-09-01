package io.coderf.arklab.user.activity;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.databinding.SettingBinding;
import io.coderf.arklab.user.viewmodel.SettingViewModel;

/**
 * Create by fz on 2019/10/11
 * 设置
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
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
