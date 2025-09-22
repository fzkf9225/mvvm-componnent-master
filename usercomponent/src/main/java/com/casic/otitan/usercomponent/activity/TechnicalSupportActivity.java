package com.casic.otitan.usercomponent.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.databinding.TechnicalSupportActivityBinding;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.viewmodel.EmptyViewModel;
import com.casic.otitan.common.widget.gallery.PreviewPhotoDialog;


/**
 * Created by fz on 2019/12/23
 * describe:技术支持
 */
@AndroidEntryPoint
public class TechnicalSupportActivity extends BaseActivity<EmptyViewModel, TechnicalSupportActivityBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.technical_support_activity;
    }

    @Override
    public String setTitleBar() {
        return "技术支持";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.imageQrCode.setOnClickListener(v -> new PreviewPhotoDialog(this)
                .createImageResInfo(R.mipmap.icon_wechat)
                .currentPosition(0)
                .show());
    }

    @Override
    public void initData(Bundle bundle) {

    }

    /**
     * show the MainActivity
     *
     * @param context context
     */
    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, TechnicalSupportActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}
