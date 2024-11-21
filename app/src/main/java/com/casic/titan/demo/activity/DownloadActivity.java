package com.casic.titan.demo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityDownloadBinding;
import com.casic.titan.demo.viewmodel.DownloadViewModel;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.update.DownloadManger;
import pers.fz.mvvm.util.update.UpdateManger;

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
            Disposable disposable = DownloadManger.getInstance().download(this, binding.editUrl.getText().toString().trim())
                    .subscribe(file -> {
                        LogUtil.show(ApiRetrofit.TAG,"下载成功："+file.getAbsolutePath());
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
            if (TextUtils.isEmpty(binding.editApk.getText().toString().trim())) {
                return;
            }
            UpdateManger.getInstance().checkUpdateInfo(this, binding.editUrl.getText().toString().trim(), "v1.0.1", "修复已知问题", true);
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
            Disposable disposable = DownloadManger.getInstance().download(this, urlList)
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