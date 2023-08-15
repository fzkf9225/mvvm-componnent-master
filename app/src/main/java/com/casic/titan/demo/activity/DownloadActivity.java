package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityDownloadBinding;
import com.casic.titan.demo.viewmodel.DownloadViewModel;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.update.DownloadManger;

public class DownloadActivity extends BaseActivity<DownloadViewModel, ActivityDownloadBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_download;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.buttonDownload.setOnClickListener(v->{
            if(TextUtils.isEmpty(binding.editUrl.getText().toString().trim())){
                return;
            }
            DownloadManger.getInstance().download(this,binding.editUrl.getText().toString().trim());
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