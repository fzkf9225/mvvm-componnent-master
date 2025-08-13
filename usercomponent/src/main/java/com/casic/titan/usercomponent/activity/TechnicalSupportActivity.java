package com.casic.titan.usercomponent.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.databinding.TechnicalSupportActivityBinding;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.viewmodel.EmptyViewModel;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;


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
        binding.imageQrCode.setOnClickListener(v -> new PreviewPhotoDialog(this, PreviewPhotoDialog.createImageInfo(R.mipmap.icon_wechat), 0).show());
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
