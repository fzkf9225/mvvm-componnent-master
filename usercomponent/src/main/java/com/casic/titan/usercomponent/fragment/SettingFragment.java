package com.casic.titan.usercomponent.fragment;

import android.os.Bundle;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.databinding.SettingBinding;
import com.casic.titan.usercomponent.viewmodel.SettingViewModel;

import androidx.fragment.app.Fragment;

import pers.fz.mvvm.activity.TechnicalSupportActivity;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.util.apiUtil.GetVersion;

/**
 * Created by fz on 2017/5/24.
 * 设置页面
 */
public class SettingFragment extends BaseFragment<SettingViewModel, SettingBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.setting;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        binding.versionName.setText("v" + GetVersion.getVersion(requireActivity()));
        binding.setSetViewModel(mViewModel);
        binding.tvSupport.setOnClickListener(v->startActivity(TechnicalSupportActivity.class));
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    public static Fragment instantiate(Bundle bundle) {
        Fragment fragment = new SettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
