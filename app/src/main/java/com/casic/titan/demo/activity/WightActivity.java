package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityWightBinding;
import com.casic.titan.demo.viewmodel.WightViewModel;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.apiUtil.DensityUtil;

public class WightActivity extends BaseActivity<WightViewModel, ActivityWightBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wight;
    }

    @Override
    public String setTitleBar() {
        return "组件示例";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.autoTextView.setText("这是AutoTextView测试文字");
        //有bug，需要优化
//        int viewWidth = getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(this, 60f);
//        binding.scalingTextView.initWidth(viewWidth);
//        binding.scalingTextView.setMaxLines(3);
//        binding.scalingTextView.setHasAnimation(true);
//        binding.scalingTextView.setCloseInNewLine(true);
//        binding.scalingTextView.setOpenSuffixColor(getResources().getColor(pers.fz.mvvm.R.color.themeColor));
//        binding.scalingTextView.setCloseSuffixColor(getResources().getColor(pers.fz.mvvm.R.color.themeColor));
//        binding.scalingTextView.setOriginalText(getResources().getString(R.string.scaling_str));
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