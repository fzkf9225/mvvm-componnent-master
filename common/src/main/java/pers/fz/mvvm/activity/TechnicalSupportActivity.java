package pers.fz.mvvm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.TechnicalSupportActivityBinding;
import pers.fz.mvvm.viewmodel.MainViewModel;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;


/**
 * Created by fz on 2019/12/23
 * describe:技术支持
 */
public class TechnicalSupportActivity extends BaseActivity<MainViewModel, TechnicalSupportActivityBinding> {

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
        setThemeBarAndToolBarColor(R.color.default_background);
        binding.imageQrCode.setOnClickListener(v -> new PicShowDialog(this, PicShowDialog.createImageInfo(R.mipmap.icon_wechat), 0).show());
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
