package io.coderf.arklab.common.helper.bean;

import androidx.annotation.Nullable;

/**
 * 自定义相机 {@link io.coderf.arklab.common.activity.CameraActivity} 启动参数。
 */
public class CameraConfig {

    /** 与 {@link io.coderf.arklab.common.activity.CameraActivity.Params#DEFAULT_MAX_DURATION} 一致 */
    private static final int DEFAULT_MAX_DURATION = 30;
    /** 与 {@link io.coderf.arklab.common.widget.camera.CameraView.Mode#BUTTON_STATE_BOTH} 一致 */
    private static final int BUTTON_STATE_BOTH = 0x103;
    private static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;
    private static final int BUTTON_STATE_ONLY_RECORDER = 0x102;

    private int maxDuration = DEFAULT_MAX_DURATION;
    @Nullable
    private String imageOutputPath;
    @Nullable
    private String videoOutputPath;
    @Nullable
    private String imageOutputFileName;
    @Nullable
    private String videoOutputFileName;
    @Nullable
    private String imageOutputFileMimeType;
    @Nullable
    private String videoOutputFileMimeType;
    private int buttonFeatures = BUTTON_STATE_BOTH;

    public static CameraConfig defaults() {
        return new CameraConfig();
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public CameraConfig setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
        return this;
    }

    @Nullable
    public String getImageOutputPath() {
        return imageOutputPath;
    }

    public CameraConfig setImageOutputPath(@Nullable String imageOutputPath) {
        this.imageOutputPath = imageOutputPath;
        return this;
    }

    @Nullable
    public String getVideoOutputPath() {
        return videoOutputPath;
    }

    public CameraConfig setVideoOutputPath(@Nullable String videoOutputPath) {
        this.videoOutputPath = videoOutputPath;
        return this;
    }

    @Nullable
    public String getImageOutputFileName() {
        return imageOutputFileName;
    }

    public CameraConfig setImageOutputFileName(@Nullable String imageOutputFileName) {
        this.imageOutputFileName = imageOutputFileName;
        return this;
    }

    @Nullable
    public String getVideoOutputFileName() {
        return videoOutputFileName;
    }

    public CameraConfig setVideoOutputFileName(@Nullable String videoOutputFileName) {
        this.videoOutputFileName = videoOutputFileName;
        return this;
    }

    @Nullable
    public String getImageOutputFileMimeType() {
        return imageOutputFileMimeType;
    }

    public CameraConfig setImageOutputFileMimeType(@Nullable String imageOutputFileMimeType) {
        this.imageOutputFileMimeType = imageOutputFileMimeType;
        return this;
    }

    @Nullable
    public String getVideoOutputFileMimeType() {
        return videoOutputFileMimeType;
    }

    public CameraConfig setVideoOutputFileMimeType(@Nullable String videoOutputFileMimeType) {
        this.videoOutputFileMimeType = videoOutputFileMimeType;
        return this;
    }

    public int getButtonFeatures() {
        return buttonFeatures;
    }

    public CameraConfig setButtonFeatures(int buttonFeatures) {
        this.buttonFeatures = buttonFeatures;
        return this;
    }

    /** 仅拍照 */
    public CameraConfig captureOnly() {
        return setButtonFeatures(BUTTON_STATE_ONLY_CAPTURE);
    }

    /** 仅录像 */
    public CameraConfig recordOnly() {
        return setButtonFeatures(BUTTON_STATE_ONLY_RECORDER);
    }

    /** 拍照 + 录像 */
    public CameraConfig captureAndRecord() {
        return setButtonFeatures(BUTTON_STATE_BOTH);
    }
}
