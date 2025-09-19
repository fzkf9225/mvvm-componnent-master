package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityDialogBinding;
import com.casic.titan.demo.viewmodel.DialogViewModel;


import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;

@AndroidEntryPoint
public class DialogActivity extends BaseActivity<DialogViewModel, ActivityDialogBinding> {
    private UseCase useCase;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_dialog;
    }

    @Override
    public String setTitleBar() {
        return "dialog示例";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setDialogViewModel(mViewModel);
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
    }
}