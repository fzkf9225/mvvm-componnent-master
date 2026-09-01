package io.coderf.arklab.user.activity;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.api.UserAccountHelper;
import io.coderf.arklab.user.databinding.PersonalCenterActivityBinding;
import io.coderf.arklab.user.viewmodel.UserViewModel;

/**
 * PersonalCenterActivity 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2020/12/25 13:48
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
