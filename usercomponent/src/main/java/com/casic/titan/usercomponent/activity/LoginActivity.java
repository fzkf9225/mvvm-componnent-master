package com.casic.titan.usercomponent.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.bean.RequestLoginBean;
import com.casic.titan.usercomponent.databinding.LoginBinding;
import com.casic.titan.usercomponent.view.UserView;
import com.casic.titan.usercomponent.viewmodel.UserViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.common.KeyBoardUtil;

/**
 * Created by fz on 2019/8/23.
 * describe：登录，登录方式：账号密码登录
 */
@AndroidEntryPoint
public class LoginActivity extends BaseActivity<UserViewModel, LoginBinding> implements UserView {
    private String password = "admin";
    private Bundle bundle;
    @Inject
    ErrorService errorService;

    @Override
    public String setTitleBar() {
        return "登录";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.login;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setLoginViewModel(mViewModel);
        RequestLoginBean requestLoginBean = new RequestLoginBean();
        requestLoginBean.setUsername("admin");
        binding.setLoginBean(requestLoginBean);
        binding.setPassword(password);
    }

    @Override
    public void initData(Bundle bundle) {
        this.bundle = bundle;
        mViewModel.getLiveData().observe(this, userInfo -> mViewModel.loginCallback(userInfo, binding.userEdit.getText().toString()));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mIntent.addCategory(Intent.CATEGORY_HOME);
            startActivity(mIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void hideKeyboard() {
        try {
            KeyBoardUtil.closeKeyboard(binding.userEdit, this);
            KeyBoardUtil.closeKeyboard(binding.passwordEdit, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    @Override
    public void toLast() {
        showToast("登录成功！");
        setResult(RESULT_OK, getIntent().putExtras(bundle));
        finish();
    }

    @Override
    public boolean hasTarget() {
        String targetActivity = bundle.getString(ConstantsHelper.TARGET_ACTIVITY);
        if (TextUtils.isEmpty(targetActivity)) {
            return false;
        }
        try {
            //是否报错，不报错说明目标页面存在
            Class.forName(targetActivity);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void toTarget() {
        String targetActivity = bundle.getString(ConstantsHelper.TARGET_ACTIVITY);
        if (TextUtils.isEmpty(targetActivity)) {
            toLast();
            return;
        }
        try {
            //是否报错，不报错说明目标页面存在
            Intent intent = new Intent(this, Class.forName(targetActivity));
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            toLast();
        }
    }

    @Override
    public void toMain() {
        showToast("登录成功！");
        AppManager.getAppManager().finishAllActivity();
        startActivity(errorService.getMainActivity());
    }
}
