package com.casic.titan.usercomponent.activity;

import android.os.Bundle;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.databinding.PersonalCenterActivityBinding;
import com.casic.titan.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;

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
