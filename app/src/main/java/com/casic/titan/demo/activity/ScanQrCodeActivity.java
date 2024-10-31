package com.casic.titan.demo.activity;

import androidx.activity.result.ActivityResultLauncher;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ActivityQrCodeBinding;
import com.casic.titan.demo.view.ScanQrCodeView;
import com.casic.titan.demo.viewmodel.ScanQrCodeViewModel;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.mvvm.activity.CaptureActivity;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.common.QRCodeUtil;
import pers.fz.mvvm.util.update.DownloadManger;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.media.dialog.OpenImageDialog;


@AndroidEntryPoint
public class ScanQrCodeActivity extends BaseActivity<ScanQrCodeViewModel, ActivityQrCodeBinding> implements ScanQrCodeView {

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = null;
    private MediaHelper mediaHelper;
    private String base64Image;
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
        mediaHelper = new MediaBuilder(this)
                .setImageMaxSelectedCount(1)
                .setChooseType(MediaHelper.DEFAULT_TYPE)
                .builder();
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                identifyUri(mediaBean.getMediaList().get(0).toString(),callbackContext);
//                base64Image = uriToBase64(mediaBean.getMediaList().get(0));
//                binding.editBase64.setText(base64Image);
            }
        });
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
                    Color.parseColor("#FFFF0000"));
            if (bitmap == null) {
                showToast("生成二维码失败");
            }
            binding.imageQrCode.setImageBitmap(bitmap);
        });
        binding.buttonUri.setOnClickListener(v-> mediaHelper.openImageDialog(v, OpenImageDialog.ALBUM));
        binding.buttonBase64.setOnClickListener(v-> identifyBase64(binding.editBase64.getText().toString(),callbackContext));
        binding.buttonUrl.setOnClickListener(v-> identifyUrl(binding.editUrl.getText().toString(),callbackContext));
        binding.buttonString.setOnClickListener(v-> identifyLocalString("/sdcard/Pictures/微信图片_20231110141942.jpg",callbackContext));
    }
    CallbackContext callbackContext = new CallbackContext() {
        @Override
        public void success(String data) {
            binding.tvScanInfo.setText("识别成功:"+data);
        }

        @Override
        public void error(String msg) {
            binding.tvScanInfo.setText(msg);
        }
    };

    public String uriToBase64(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);

            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                return Base64.encodeToString(byteArray, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    /**
     * 识别uri格式图片
     */
    private void identifyUri(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败");
                return;
            }
            Uri imageUri = Uri.parse(image);
            if (imageUri == null) {
                callbackContext.error("识别失败");
                return;
            }
            ContentResolver contentResolver = getContentResolver();
            if (contentResolver == null) {
                callbackContext.error("识别失败");
                return;
            }
            ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(imageUri, "r");
            if (parcelFileDescriptor == null) {
                callbackContext.error("识别失败");
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
            if (bitmap == null) {
                callbackContext.error("未识别二维码");
                return;
            }
            Result result = QRCodeUtil.getRawResult(bitmap);
            if (result == null) {
                Toast.makeText(ScanQrCodeActivity.this, "未识别二维码", Toast.LENGTH_SHORT).show();
                return;
            }
            callbackContext.success(result.getText());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败," + e);
        }
    }

    /**
     * 识别本地String格式图片
     */
    private void identifyLocalString(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败");
                return;
            }
            File imageFile = new File(image);
            if (!imageFile.exists()) {
                callbackContext.error("图片不存在");
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(image);
            if (bitmap == null) {
                callbackContext.error("未识别二维码");
                return;
            }
            Result result = QRCodeUtil.getRawResult(bitmap);
            if (result == null) {
                Toast.makeText(ScanQrCodeActivity.this, "未识别二维码", Toast.LENGTH_SHORT).show();
                return;
            }
            callbackContext.success(result.getText());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败," + e);
        }
    }

    /**
     * 识别本地String格式图片
     */
    private void identifyBase64(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败");
                return;
            }
            byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap == null) {
                callbackContext.error("未识别二维码");
                return;
            }
            Result result = QRCodeUtil.getRawResult(bitmap);
            if (result == null) {
                Toast.makeText(ScanQrCodeActivity.this, "未识别二维码", Toast.LENGTH_SHORT).show();
                return;
            }
            callbackContext.success(result.getText());
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败," + e);
        }
    }

    /**
     * 识别在线图片
     */
    private void identifyUrl(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败");
                return;
            }
            DownloadManger.getInstance().download(ScanQrCodeActivity.this, image, new DownloadCallback() {
                @Override
                public void onStart(Disposable d) {

                }

                @Override
                public void onProgress(long totalByte, long currentByte, int progress) {

                }

                @Override
                public void onFinish(File file) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap == null) {
                        callbackContext.error("未识别到二维码");
                        return;
                    }
                    Result result = QRCodeUtil.getRawResult(bitmap);
                    if (result == null) {
                        Toast.makeText(ScanQrCodeActivity.this, "未识别到二维码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    callbackContext.success(result.getText());
                }

                @Override
                public void onError(String msg) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败," + e);
        }
    }

    private interface CallbackContext{
        void success(String data);
        void error(String msg);
    }
}