package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityDemoPagingBinding;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.ThreadExecutor;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.viewmodel.MainViewModel;

@AndroidEntryPoint
public class DemoPagingActivity extends BaseActivity<MainViewModel, ActivityDemoPagingBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_demo_paging;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            ThreadExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    LogUtil.show(TAG, "输出：" + finalI + ",时间：" + DateUtil.getDateTimeFromMillis(System.currentTimeMillis()));
                }
            });
        }
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