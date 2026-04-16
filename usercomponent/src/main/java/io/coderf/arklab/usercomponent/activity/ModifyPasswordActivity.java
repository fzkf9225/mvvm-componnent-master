package io.coderf.arklab.usercomponent.activity;

import android.os.Bundle;

import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.databinding.ModifyPasswordActivityBinding;
import io.coderf.arklab.usercomponent.viewmodel.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;

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
