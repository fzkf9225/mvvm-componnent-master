package io.coderf.arklab.demo.activity;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.activity.CaptureActivity;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.helper.QrScanHelper;
import io.coderf.arklab.common.helper.bean.QrScanConfig;
import io.coderf.arklab.common.utils.common.QRCodeUtil;
import io.coderf.arklab.common.utils.download.DownloadManager;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityQrCodeBinding;
import io.coderf.arklab.demo.view.ScanQrCodeView;
import io.coderf.arklab.demo.viewmodel.ScanQrCodeViewModel;
import io.coderf.arklab.media.MediaBuilder;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.dialog.OpenImageDialog;
import io.coderf.arklab.media.enums.MediaPickerTypeEnum;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 二维码能力演示：实时扫码、相册识码、Base64/URL/本地路径识别、生成二维码。
 * <p>扫码页统一使用 {@link CaptureActivity}（支持相册选图识码），与 {@link io.coderf.arklab.common.activity.WebViewActivity} JSBridge 一致。</p>
 */
@AndroidEntryPoint
public class ScanQrCodeActivity extends BaseActivity<ScanQrCodeViewModel, ActivityQrCodeBinding>
        implements ScanQrCodeView {

    private QrScanHelper qrScanHelper;
    private MediaHelper mediaHelper;

    /** 相册识码时复用，与 {@link #identifyUri} 配合 */
    private CallbackContext callbackContext;

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
        callbackContext = new CallbackContext() {
            @Override
            public void success(String data) {
                binding.tvScanInfo.setText("识别成功: " + data);
            }

            @Override
            public void error(String msg) {
                binding.tvScanInfo.setText(msg);
            }
        };

        mediaHelper = new MediaBuilder(this)
                .bindLifeCycle(this)
                .setImageMaxSelectedCount(1)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .builder();
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE
                    && mediaBean.getMediaList() != null
                    && !mediaBean.getMediaList().isEmpty()) {
                identifyUri(mediaBean.getMediaList().get(0).toString(), callbackContext);
            }
        });

        qrScanHelper = new QrScanHelper(this, new QrScanHelper.Callback() {
            @Override
            public void onSuccess(@NonNull String content) {
                binding.tvScanInfo.setText("扫码结果：" + content);
            }

            @Override
            public void onCancel() {
                binding.tvScanInfo.setText("取消扫码");
            }

            @Override
            public void onError(@NonNull String message) {
                binding.tvScanInfo.setText(message);
            }
        });
    }

    @Override
    public void initData(Bundle bundle) {
        binding.buttonScan.setOnClickListener(v -> qrScanHelper.launch());
        binding.buttonCustomScan.setOnClickListener(v -> qrScanHelper.launch(
                QrScanConfig.defaults()
                        .setScanColor(ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_red))
                        .setShowFlashLight(false)
                        .setShowGallery(false)
        ));
        binding.createQrCode.setOnClickListener(v -> {
            String content = binding.editCreateQrcode.getText() != null
                    ? binding.editCreateQrcode.getText().toString().trim()
                    : "https://www.baidu.com";
            createQrCode(content);
        });
        binding.buttonUri.setOnClickListener(v ->
                mediaHelper.openImageDialog(v, OpenImageDialog.ALBUM));
        binding.buttonBase64.setOnClickListener(v ->
                identifyBase64(binding.editBase64.getText().toString(), callbackContext));
        binding.buttonUrl.setOnClickListener(v ->
                identifyUrl(binding.editUrl.getText().toString(), callbackContext));
        binding.buttonString.setOnClickListener(v -> {
            String path = binding.editLocalPath.getText() != null
                    ? binding.editLocalPath.getText().toString().trim()
                    : "";
            if (TextUtils.isEmpty(path)) {
                showToast("请输入本地图片绝对路径");
                return;
            }
            identifyLocalString(path, callbackContext);
        });
    }

    /** 根据输入内容生成二维码（红色前景） */
    private void createQrCode(String content) {
        if (TextUtils.isEmpty(content)) {
            showToast("请输入要编码的内容");
            return;
        }
        Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(
                content,
                Color.parseColor("#FFFF0000")
        );
        if (bitmap == null) {
            showToast("生成二维码失败");
            return;
        }
        binding.imageQrCode.setImageBitmap(bitmap);
    }

    /**
     * 将 Uri 对应图片转为 Base64（演示用，非扫码主流程）。
     */
    public String uriToBase64(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            try (InputStream inputStream = resolver.openInputStream(uri)) {
                if (inputStream == null) {
                    return null;
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    return null;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 识别 content:// 或 file:// Uri 图片中的二维码。
     */
    private void identifyUri(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败：地址为空");
                return;
            }
            Uri imageUri = Uri.parse(image);
            ContentResolver contentResolver = getContentResolver();
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(imageUri, "r")) {
                if (pfd == null) {
                    callbackContext.error("识别失败：无法打开文件");
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                decodeBitmapAndCallback(bitmap, callbackContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 识别本地绝对路径图片中的二维码。
     */
    private void identifyLocalString(String imagePath, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(imagePath)) {
                callbackContext.error("识别失败：路径为空");
                return;
            }
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                callbackContext.error("图片不存在: " + imagePath);
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            decodeBitmapAndCallback(bitmap, callbackContext);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 识别 Base64 编码图片中的二维码。
     */
    private void identifyBase64(String image, CallbackContext callbackContext) {
        try {
            if (TextUtils.isEmpty(image)) {
                callbackContext.error("识别失败：Base64 为空");
                return;
            }
            byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            decodeBitmapAndCallback(bitmap, callbackContext);
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 下载网络图片后识别二维码。
     */
    private void identifyUrl(String imageUrl, CallbackContext callbackContext) {
        if (TextUtils.isEmpty(imageUrl)) {
            callbackContext.error("识别失败：URL 为空");
            return;
        }
        Disposable disposable = DownloadManager.getInstance()
                .download(this, imageUrl.trim())
                .subscribe(
                        file -> {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            decodeBitmapAndCallback(bitmap, callbackContext);
                        },
                        throwable -> {
                            if (throwable instanceof BaseException baseException) {
                                callbackContext.error("识别失败: " + baseException.getErrorMsg());
                            } else {
                                callbackContext.error("识别失败: " + throwable.getMessage());
                            }
                        }
                );
    }

    private void decodeBitmapAndCallback(Bitmap bitmap, CallbackContext callbackContext) {
        if (bitmap == null) {
            callbackContext.error("未识别二维码：无法解码图片");
            return;
        }
        Result result = QRCodeUtil.getRawResult(bitmap);
        if (result == null) {
            Toast.makeText(this, "未识别二维码", Toast.LENGTH_SHORT).show();
            callbackContext.error("未识别二维码");
            return;
        }
        callbackContext.success(result.getText());
    }

    private interface CallbackContext {
        void success(String data);

        void error(String msg);
    }
}
