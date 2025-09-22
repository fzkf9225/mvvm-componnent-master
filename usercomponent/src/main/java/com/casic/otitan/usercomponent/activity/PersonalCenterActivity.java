package com.casic.otitan.usercomponent.activity;

import android.os.Bundle;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.api.UserAccountHelper;
import com.casic.otitan.usercomponent.databinding.PersonalCenterActivityBinding;
import com.casic.otitan.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseActivity;

/**
 * Created by fz on 2020/12/25 13:48
 * describe:
 */
@AndroidEntryPoint
public class PersonalCenterActivity extends BaseActivity<UserViewModel, PersonalCenterActivityBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.personal_center_activity;
    }

    @Override
    public String setTitleBar() {
        return "我的信息";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setUserInfo(UserAccountHelper.getUser());
    }

    @Override
    public void initData(Bundle bundle) {

    }
}
