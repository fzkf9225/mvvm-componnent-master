package com.casic.otitan.usercomponent.activity;

import android.os.Bundle;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.databinding.ModifyPasswordActivityBinding;
import com.casic.otitan.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseActivity;

/**
 * Created by fz on 2020/12/25 13:48
 * describe:修改密码
 */
@AndroidEntryPoint
public class ModifyPasswordActivity extends BaseActivity<UserViewModel, ModifyPasswordActivityBinding> {
    @Override
    protected int getLayoutId() {
        return R.layout.modify_password_activity;
    }

    @Override
    public String setTitleBar() {
        return "修改密码";
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void initData(Bundle bundle) {
        binding.modifyPasswordSubmit.setOnClickListener(v->{
        });
    }

}
