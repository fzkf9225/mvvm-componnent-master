package com.casic.titan.usercomponent.activity;

import android.os.Bundle;

import com.casic.titan.usercomponent.fragment.SettingFragment;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.BaseFragmentContainerBinding;
import pers.fz.mvvm.viewmodel.MainViewModel;

/**
 * Create by CherishTang on 2019/10/11
 * describe:设置
 */
public class SettingActivity extends BaseActivity<MainViewModel, BaseFragmentContainerBinding> {
    private Bundle bundle;

    @Override
    protected int getLayoutId() {
        return R.layout.base_fragment_container;
    }

    @Override
    public String setTitleBar() {
        return "设置";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
    }

    @Override
    public void initData(Bundle bundle) {
        bundle = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
        mViewModel.setFragment(getSupportFragmentManager(), SettingFragment.instantiate(bundle),R.id.fragment_container);
    }


}
