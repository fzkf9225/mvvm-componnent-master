package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;

import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityDialogBinding;
import io.coderf.arklab.demo.viewmodel.DialogViewModel;


import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;

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