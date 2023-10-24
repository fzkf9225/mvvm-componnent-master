package com.casic.titan.demo.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.casic.titan.demo.R;
import com.casic.titan.demo.api.PortalSdkConstant;
import com.casic.titan.demo.databinding.ActivityCoordinatorBinding;
import com.casic.titan.portal.PortalSdk;
import com.casic.titan.portal.ShareDataListener;
import com.gyf.immersionbar.ImmersionBar;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/8/17 16:12
 * describe :
 */
@AndroidEntryPoint
public class CoordinatorActivity extends BaseActivity<BaseViewModel, ActivityCoordinatorBinding> implements ShareDataListener {
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
        super.initImmersionBar();
        ImmersionBar.with(this).titleBar(binding.detailToolbar).init();
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
        PortalSdk.getInstance().init(2000);
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            PortalSdk.getInstance().registerCallback(10086, this);
        },3000);
        binding.fab.setOnClickListener(v->{
            String content= PortalSdk.getInstance().getShareData(10086);
            LogUtil.show(TAG, " 获取Content:" + content);
        });
    }

    @Override
    public void initData(Bundle bundle) {

    }


    @Override
    public void notifyShareData(int id, String content) {
        showToast("成功！！！！");
        LogUtil.show(TAG, "ID:" + id + " Content:" + content);
    }
}
