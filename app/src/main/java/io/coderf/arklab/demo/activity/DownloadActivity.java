package io.coderf.arklab.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.utils.download.UpdateManager;
import io.coderf.arklab.common.utils.download.core.UpdateConfig;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityDownloadBinding;
import io.coderf.arklab.demo.viewmodel.DownloadViewModel;

/**
 * 下载 Demo：单文件 / 批量 / APK 更新均走协程 {@link io.coderf.arklab.common.utils.download.DownloadClient} 真内核
 * （页面经 {@link DownloadViewModel} / {@link UpdateManager}）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/12
 */
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
        mViewModel.getToast().observe(this, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                showToast(msg);
            }
        });

        binding.buttonDownload.setOnClickListener(v -> {
            String url = binding.editUrl.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            // 权限由 DownloadClient 内部 awaitStoragePermission 处理
            mViewModel.download(this, url);
        });

        binding.buttonUpdate.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(binding.editApk.getText()).toString().trim())) {
                return;
            }
            UpdateConfig updateConfig = new UpdateConfig.Builder(this, binding.editApk.getText().toString().trim())
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
                            LogUtil.logger(ApiRetrofit.TAG, "下载失败：" + e.getMessage());
                        }
                    })
                    .build();

            UpdateManager.getInstance().checkUpdateInfo(updateConfig);
        });

        binding.buttonMulti.setOnClickListener(v -> {
            String raw = binding.editMulti.getText().toString().trim();
            if (TextUtils.isEmpty(raw)) {
                return;
            }
            List<String> urlList = Arrays.asList(raw.split(";"));
            mViewModel.downloadBatch(this, urlList);
        });
    }

    @Override
    public void initData(Bundle bundle) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
    }
}
