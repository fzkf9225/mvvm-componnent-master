package io.coderf.arklab.common.helper.bean;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * 自定义扫码页 {@link io.coderf.arklab.common.activity.CaptureActivity} 启动参数。
 * <p>
 * 通过 {@link io.coderf.arklab.common.helper.QrScanHelper#buildScanOptions(QrScanConfig)} 转为
 * {@link ScanOptions}，供 {@link com.journeyapps.barcodescanner.ScanContract} 使用。
 * </p>
 */
public class QrScanConfig {

    /** 未设置时使用 CaptureActivity 默认主题色 */
    public static final int SCAN_COLOR_UNSET = 0;

    @ColorInt
    private int scanColor = SCAN_COLOR_UNSET;
    private boolean showFlashLight = true;
    private boolean showGallery = true;
    private boolean beepEnabled = true;
    private boolean torchEnabled = false;
    private boolean orientationLocked = false;
    @Nullable
    private String prompt = "";
    @Nullable
    private Collection<String> barcodeFormats = Collections.singletonList(ScanOptions.QR_CODE);

    public static QrScanConfig defaults() {
        return new QrScanConfig();
    }

    @ColorInt
    public int getScanColor() {
        return scanColor;
    }

    public QrScanConfig setScanColor(@ColorInt int scanColor) {
        this.scanColor = scanColor;
        return this;
    }

    public boolean isShowFlashLight() {
        return showFlashLight;
    }

    public QrScanConfig setShowFlashLight(boolean showFlashLight) {
        this.showFlashLight = showFlashLight;
        return this;
    }

    public boolean isShowGallery() {
        return showGallery;
    }

    public QrScanConfig setShowGallery(boolean showGallery) {
        this.showGallery = showGallery;
        return this;
    }

    public boolean isBeepEnabled() {
        return beepEnabled;
    }

    public QrScanConfig setBeepEnabled(boolean beepEnabled) {
        this.beepEnabled = beepEnabled;
        return this;
    }

    public boolean isTorchEnabled() {
        return torchEnabled;
    }

    public QrScanConfig setTorchEnabled(boolean torchEnabled) {
        this.torchEnabled = torchEnabled;
        return this;
    }

    public boolean isOrientationLocked() {
        return orientationLocked;
    }

    public QrScanConfig setOrientationLocked(boolean orientationLocked) {
        this.orientationLocked = orientationLocked;
        return this;
    }

    @Nullable
    public String getPrompt() {
        return prompt;
    }

    public QrScanConfig setPrompt(@Nullable String prompt) {
        this.prompt = prompt;
        return this;
    }

    @Nullable
    public Collection<String> getBarcodeFormats() {
        return barcodeFormats;
    }

    public QrScanConfig setBarcodeFormats(@Nullable Collection<String> barcodeFormats) {
        this.barcodeFormats = barcodeFormats;
        return this;
    }

    public QrScanConfig setBarcodeFormats(@Nullable String... formats) {
        if (formats == null || formats.length == 0) {
            this.barcodeFormats = Collections.singletonList(ScanOptions.QR_CODE);
        } else {
            this.barcodeFormats = Collections.unmodifiableList(Arrays.asList(formats));
        }
        return this;
    }
}
