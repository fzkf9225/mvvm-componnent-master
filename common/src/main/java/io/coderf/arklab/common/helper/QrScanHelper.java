package io.coderf.arklab.common.helper;

import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Collections;

import io.coderf.arklab.common.activity.CaptureActivity;
import io.coderf.arklab.common.helper.bean.QrScanConfig;

/**
 * 快速启动 {@link CaptureActivity} 扫码，并统一组装自定义参数。
 *
 * <pre>{@code
 * QrScanHelper scanHelper = new QrScanHelper(this, new QrScanHelper.Callback() {
 *     @Override
 *     public void onSuccess(@NonNull String content) {
 *         // 扫码成功
 *     }
 *
 *     @Override
 *     public void onCancel() {
 *         // 用户取消
 *     }
 * });
 *
 * // 默认配置
 * scanHelper.launch();
 *
 * // 自定义配置
 * scanHelper.launch(QrScanConfig.defaults()
 *     .setScanColor(Color.RED)
 *     .setShowFlashLight(false)
 *     .setShowGallery(false));
 * }</pre>
 */
public class QrScanHelper {

    public interface Callback {
        void onSuccess(@NonNull String content);

        void onCancel();

        default void onError(@NonNull String message) {
        }
    }

    private final ActivityResultLauncher<ScanOptions> launcher;
    @Nullable
    private QrScanConfig defaultConfig;

    public QrScanHelper(@NonNull ComponentActivity activity, @NonNull Callback callback) {
        launcher = activity.registerForActivityResult(new ScanContract(), result -> dispatchResult(result, callback));
    }

    public QrScanHelper(@NonNull Fragment fragment, @NonNull Callback callback) {
        launcher = fragment.registerForActivityResult(new ScanContract(), result -> dispatchResult(result, callback));
    }

    /**
     * 设置后续 {@link #launch()} 使用的默认配置。
     */
    @NonNull
    public QrScanHelper setDefaultConfig(@Nullable QrScanConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    /**
     * 使用默认或已设置的 {@link #setDefaultConfig(QrScanConfig)} 配置启动扫码。
     */
    public void launch() {
        launch(defaultConfig != null ? defaultConfig : QrScanConfig.defaults());
    }

    /**
     * 使用指定配置启动扫码。
     */
    public void launch(@NonNull QrScanConfig config) {
        launcher.launch(buildScanOptions(config));
    }

    /**
     * 将 {@link QrScanConfig} 转为 {@link ScanOptions}，可配合自行注册的 {@link ScanContract} 使用。
     */
    @NonNull
    public static ScanOptions buildScanOptions(@NonNull QrScanConfig config) {
        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(CaptureActivity.class);
        options.setBeepEnabled(config.isBeepEnabled());
        options.setBarcodeImageEnabled(false);
        options.setTorchEnabled(config.isTorchEnabled());
        options.setOrientationLocked(config.isOrientationLocked());
        options.setPrompt(config.getPrompt() != null ? config.getPrompt() : "");
        options.setCameraId(0);

        if (config.getBarcodeFormats() != null && !config.getBarcodeFormats().isEmpty()) {
            options.setDesiredBarcodeFormats(config.getBarcodeFormats());
        } else {
            options.setDesiredBarcodeFormats(Collections.singletonList(ScanOptions.QR_CODE));
        }

        if (config.getScanColor() != QrScanConfig.SCAN_COLOR_UNSET) {
            options.addExtra(CaptureActivity.SCAN_COLOR, config.getScanColor());
        }
        options.addExtra(CaptureActivity.SHOW_FLASH_LIGHT, config.isShowFlashLight());
        options.addExtra(CaptureActivity.SHOW_GALLERY, config.isShowGallery());
        return options;
    }

    private static void dispatchResult(@NonNull ScanIntentResult result, @NonNull Callback callback) {
        if (result.getContents() != null) {
            callback.onSuccess(result.getContents());
            return;
        }
        Intent originalIntent = result.getOriginalIntent();
        if (originalIntent != null && originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
            callback.onError("无摄像头权限");
            return;
        }
        callback.onCancel();
    }
}
