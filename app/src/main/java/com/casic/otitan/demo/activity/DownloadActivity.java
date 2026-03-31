package com.casic.otitan.demo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.utils.download.DownloadManager;
import com.casic.otitan.common.utils.download.UpdateManager;
import com.casic.otitan.common.utils.download.core.DownloadConfig;
import com.casic.otitan.common.utils.download.core.UpdateConfig;
import com.casic.otitan.common.utils.download.listener.DownloadListener;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.UseCase;
import com.casic.otitan.demo.databinding.ActivityDownloadBinding;
import com.casic.otitan.demo.viewmodel.DownloadViewModel;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.Disposable;
import com.casic.otitan.common.api.ApiRetrofit;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.utils.log.LogUtil;

@AndroidEntryPoint
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
        binding.buttonDownload.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.editUrl.getText().toString().trim())) {
                return;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0x02);
                    return;
                }
            }
            Disposable disposable = DownloadManager.getInstance().download(this, binding.editUrl.getText().toString().trim())
                    .subscribe(file -> {
                        LogUtil.show(ApiRetrofit.TAG, "下载成功：" + file.getAbsolutePath());
                        showToast("下载成功！");
                    }, throwable -> {
                        if (throwable instanceof BaseException baseException) {
                            showToast(baseException.getErrorMsg());
                            return;
                        }
                        showToast(throwable.getMessage());
                    });
        });
        binding.buttonUpdate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(binding.editApk.getText()).toString().trim())) {
                return;
            }
//            // 方式1：使用配置类进行简单更新
//            DownloadConfig config = new DownloadConfig.Builder(this,  binding.editUrl.getText().toString().trim())
//                    .setSaveFileName("app_v2.0.apk")
//                    .setVerifyRepeatDownload(true)
//                    .setDownloadListener(new DownloadListener() {
//                        @Override
//                        public void onStart() {
//
//                        }
//
//                        @Override
//                        public void onProgress(int progress) {
//
//                        }
//
//                        @Override
//                        public void onFinish(File file) {
//
//                        }
//
//                        @Override
//                        public void onError(Exception e) {
//
//                        }
//                    })
//                    .build();
//
//            UpdateManager.getInstance().update(config);
//
//// 方式2：使用配置类显示更新对话框
//            DownloadConfig dialogConfig = new DownloadConfig.Builder(this,  binding.editUrl.getText().toString().trim())
//                    .setSaveFileName("app_v2.0.apk")
//                    .setVerifyRepeatDownload(true)
//                    .build();
//
//            UpdateManager.getInstance().checkUpdateInfo(dialogConfig,
//                    "1. 修复已知问题\n2. 优化用户体验\n3. 新增功能模块",
//                    "1.0.0",
//                    true);

// 方式3：使用扩展配置类（更清晰）
            UpdateConfig updateConfig = new UpdateConfig.Builder(this,  binding.editUrl.getText().toString().trim())
                    .setSaveFileName("app_v2.0.apk")
                    .setUpdateMessage("1. 修复已知问题\n2. 优化用户体验")
                    .setCurrentVersionName("1.0.0")
                    .setCancelEnable(true)
                    .setVerifyRepeatDownload(true)
                    .addHeader("Authorization", "Bearer token")
                    .setDownloadListener(new DownloadListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onProgress(int progress) {

                        }

                        @Override
                        public void onFinish(File file) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    })
                    .build();

            UpdateManager.getInstance().checkUpdateInfo(updateConfig,
                    updateConfig.getUpdateMessage(),
                    updateConfig.getCurrentVersionName(),
                    updateConfig.isCancelEnable());
        });
        binding.buttonMulti.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.editMulti.getText().toString().trim())) {
                return;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0x02);
                    return;
                }
            }
            List<String> urlList = Arrays.asList(binding.editMulti.getText().toString().split(";"));
            Disposable disposable = DownloadManager.getInstance().downloadBatch(this, urlList)
                    .subscribe(file -> {
                        showToast("下载成功！");
                    }, throwable -> {
                        if (throwable instanceof BaseException baseException) {
                            showToast(baseException.getErrorMsg());
                            return;
                        }
                        showToast(throwable.getMessage());
                    });
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