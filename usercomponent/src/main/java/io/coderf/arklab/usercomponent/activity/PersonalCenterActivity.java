package io.coderf.arklab.usercomponent.activity;

import android.os.Bundle;

import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.api.UserAccountHelper;
import io.coderf.arklab.usercomponent.databinding.PersonalCenterActivityBinding;
import io.coderf.arklab.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;

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
