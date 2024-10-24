package com.casic.titan.demo.activity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityCoordinatorBinding;
import com.gyf.immersionbar.ImmersionBar;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.viewmodel.MainViewModel;

/**
 * Created by fz on 2023/8/17 16:12
 * describe :
 */
@AndroidEntryPoint
public class CoordinatorActivity extends BaseActivity<MainViewModel, ActivityCoordinatorBinding> {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_coordinator;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected void initImmersionBar() {
        ImmersionBar.with(this)
                .autoStatusBarDarkModeEnable(true, 0.2f)
                .keyboardEnable(true)
                .titleBar(binding.detailToolbar).init();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.text.setText("关于Snackbar在4.4和emui3.1上高度显示不准确的问题是由于沉浸式使用了系统的" +
                "WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS或者WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION" +
                "属性造成的，目前尚不知有什么解决办法");
        Glide.with(this).asBitmap().load("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42")
                .apply(new RequestOptions().placeholder(pers.fz.mvvm.R.mipmap.ic_default_image))
                .into(binding.mIv);
        binding.detailToolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsingToolbarTitleStyle);
        binding.toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedToolbarTitleStyle);
        binding.fab.setOnClickListener(v-> showToast("点击事件"));
    }

    @Override
    public void initData(Bundle bundle) {

    }

}
