package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityQrCodeBinding;
import com.casic.titan.demo.viewmodel.QrCodeViewModel;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.zxing.encode.QRCodeUtil;

public class QrCodeActivity extends BaseActivity<QrCodeViewModel, ActivityQrCodeBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qr_code;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.buttonScan.setOnClickListener(v -> {
            QRCodeUtil.getInstance().decode(this);
        });
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