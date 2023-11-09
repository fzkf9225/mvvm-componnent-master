package com.casic.titan.demo.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityQrCodeBinding;
import com.casic.titan.demo.view.ScanQrCodeView;
import com.casic.titan.demo.viewmodel.ScanQrCodeViewModel;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.activity.CaptureActivity;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.apiUtil.QRCodeUtil;
import pers.fz.mvvm.util.log.LogUtil;


@AndroidEntryPoint
public class ScanQrCodeActivity extends BaseActivity<ScanQrCodeViewModel, ActivityQrCodeBinding> implements ScanQrCodeView {

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qr_code;
    }

    @Override
    public String setTitleBar() {
        return "扫码示例";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        barcodeLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() == null) {
                        Intent originalIntent = result.getOriginalIntent();
                        if (originalIntent == null) {
                            binding.tvScanInfo.setText("取消扫码");
                        } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                            binding.tvScanInfo.setText("无摄像头权限");
                        }
                    } else {
                        binding.tvScanInfo.setText("扫码结果：" + result.getContents());
                    }
                }
        );
    }

    @Override
    public void initData(Bundle bundle) {
        binding.buttonScan.setOnClickListener(v -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setBeepEnabled(true);
            //设置是否在扫描成功时保存条形码图像
            scanOptions.setBarcodeImageEnabled(false);
            // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
            scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            // Use a specific camera of the device
            scanOptions.setCameraId(0);
            //底部的提示文字，设为""可以置空
            scanOptions.setPrompt("");
            //设置是否启用闪光灯
            scanOptions.setTorchEnabled(false);
            scanOptions.setOrientationLocked(false);
            scanOptions.setCaptureActivity(CaptureActivity.class);
            barcodeLauncher.launch(scanOptions);
        });

        binding.createQrCode.setOnClickListener(v -> {
            Bitmap bitmap = QRCodeUtil.createQRCodeBitmap("https://www.baidu.com",
                    ContextCompat.getColor(this, pers.fz.mvvm.R.color.themeColor));
            if (bitmap == null) {
                showToast("生成二维码失败");
            }
            binding.imageQrCode.setImageBitmap(bitmap);
        });
    }

}