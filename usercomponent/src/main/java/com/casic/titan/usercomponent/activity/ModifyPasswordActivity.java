package com.casic.titan.usercomponent.activity;

import android.os.Bundle;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.databinding.ModifyPasswordActivityBinding;
import com.casic.titan.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;

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
