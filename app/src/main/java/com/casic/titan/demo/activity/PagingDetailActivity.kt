package com.casic.titan.demo.activity;

import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityPagingDetailBinding;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

public class PagingDetailActivity extends BaseActivity<EmptyViewModel, ActivityPagingDetailBinding> {
    public final static String ARGS = "args";
    public final static String LINE = "line";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_paging_detail;
    }

    @Override
    public String setTitleBar() {
        return "详情";
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void initData(Bundle bundle) {
        binding.tvArgs.setText("这是第" + bundle.getInt(LINE) + "个页面传递过来的参数：" + bundle.getString(ARGS));
    }
}