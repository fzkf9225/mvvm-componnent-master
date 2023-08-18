package com.casic.titan.demo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityCoordinatorBinding;
import com.gyf.immersionbar.ImmersionBar;

import java.io.File;
import java.util.Random;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/8/17 16:12
 * describe :
 */
public class CoordinatorActivity extends BaseActivity<BaseViewModel, ActivityCoordinatorBinding> {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_coordinator;
    }
    private ActivityResultLauncher<Uri> cameraLauncher;

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
        binding.fab.setOnClickListener(v->{
            startCamera();
        });
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    LogUtil.show(TAG,"---------------"+result+"----------------");
                    if (result) {
                        // 照片拍摄成功，处理返回的 Uri（照片保存路径）
                        // 这里可以处理你的业务逻辑，例如显示图片等
                    } else {
                        // 用户取消了拍照操作
                    }
                }
        );
    }

    @Override
    public void initData(Bundle bundle) {

    }

    public void startCamera() {
        File imageFile = new File(getExternalFilesDir("image"), "temp_image.jpg");
        Uri imageUri = FileProvider.getUriForFile(this, this.getPackageName()+".FileProvider", imageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(imageUri);
    }
}
