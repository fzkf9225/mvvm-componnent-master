package io.coderf.arklab.common.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.coderf.arklab.common.activity.CameraActivity;
import io.coderf.arklab.common.helper.bean.CameraConfig;
import io.coderf.arklab.common.widget.camera.CameraView;

/**
 * 快速启动 {@link CameraActivity} 拍摄，并统一处理回调。
 *
 * <pre>{@code
 * CameraHelper cameraHelper = new CameraHelper(this, new CameraHelper.Callback() {
 *     @Override
 *     public void onSuccess(@NonNull Uri uri, @NonNull String mediaType) {
 *         // 拍摄成功
 *     }
 *
 *     @Override
 *     public void onCancel() {
 *         // 用户取消
 *     }
 * });
 *
 * // 默认配置
 * cameraHelper.launch();
 *
 * // 自定义配置
 * cameraHelper.launch(CameraConfig.defaults()
 *     .setMaxDuration(60)
 *     .captureAndRecord());
 * }</pre>
 */
public class CameraHelper {

    private static final String EXTRA_MAX_DURATION = "maxDuration";
    private static final String EXTRA_IMAGE_OUTPUT_PATH = "imageOutputPath";
    private static final String EXTRA_VIDEO_OUTPUT_PATH = "videoOutputPath";
    private static final String EXTRA_IMAGE_OUTPUT_FILE_NAME = "imageOutputFileName";
    private static final String EXTRA_VIDEO_OUTPUT_FILE_NAME = "videoOutputFileName";
    private static final String EXTRA_IMAGE_OUTPUT_FILE_MIME_TYPE = "imageOutputFileMimeType";
    private static final String EXTRA_VIDEO_OUTPUT_FILE_MIME_TYPE = "videoOutputFileMimeType";
    private static final String EXTRA_BUTTON_FEATURES = "buttonFeatures";
    private static final String RESULT_PATH = "path";
    private static final String RESULT_MEDIA_TYPE = "mediaType";

    public interface Callback {
        /**
         * @param uri       图片或视频 Uri
         * @param mediaType {@link CameraView.Companion.Result#IMAGE} 或 {@link CameraView.Companion.Result#VIDEO}
         */
        void onSuccess(@NonNull Uri uri, @NonNull String mediaType);

        void onCancel();

        default void onError(@NonNull String message) {
        }
    }

    private final Context context;
    private final ActivityResultLauncher<Intent> launcher;
    @Nullable
    private CameraConfig defaultConfig;

    public CameraHelper(@NonNull ComponentActivity activity, @NonNull Callback callback) {
        this.context = activity;
        launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> dispatchResult(result.getResultCode(), result.getData(), callback)
        );
    }

    public CameraHelper(@NonNull Fragment fragment, @NonNull Callback callback) {
        this.context = fragment.requireContext();
        launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> dispatchResult(result.getResultCode(), result.getData(), callback)
        );
    }

    /**
     * 设置后续 {@link #launch()} 使用的默认配置。
     */
    @NonNull
    public CameraHelper setDefaultConfig(@Nullable CameraConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    /**
     * 使用默认或已设置的 {@link #setDefaultConfig(CameraConfig)} 配置启动相机。
     */
    public void launch() {
        launch(defaultConfig != null ? defaultConfig : CameraConfig.defaults());
    }

    /**
     * 使用指定配置启动相机。
     */
    public void launch(@NonNull CameraConfig config) {
        launcher.launch(buildIntent(context, config));
    }

    /**
     * 将 {@link CameraConfig} 转为启动 {@link CameraActivity} 的 Intent。
     */
    @NonNull
    public static Intent buildIntent(@NonNull Context context, @NonNull CameraConfig config) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(EXTRA_MAX_DURATION, config.getMaxDuration());
        putExtraIfPresent(intent, EXTRA_IMAGE_OUTPUT_PATH, config.getImageOutputPath());
        putExtraIfPresent(intent, EXTRA_VIDEO_OUTPUT_PATH, config.getVideoOutputPath());
        putExtraIfPresent(intent, EXTRA_IMAGE_OUTPUT_FILE_NAME, config.getImageOutputFileName());
        putExtraIfPresent(intent, EXTRA_VIDEO_OUTPUT_FILE_NAME, config.getVideoOutputFileName());
        putExtraIfPresent(intent, EXTRA_IMAGE_OUTPUT_FILE_MIME_TYPE, config.getImageOutputFileMimeType());
        putExtraIfPresent(intent, EXTRA_VIDEO_OUTPUT_FILE_MIME_TYPE, config.getVideoOutputFileMimeType());
        intent.putExtra(EXTRA_BUTTON_FEATURES, config.getButtonFeatures());
        return intent;
    }

    private static void putExtraIfPresent(@NonNull Intent intent, @NonNull String key, @Nullable String value) {
        if (value != null) {
            intent.putExtra(key, value);
        }
    }

    private static void dispatchResult(int resultCode, @Nullable Intent data, @NonNull Callback callback) {
        if (resultCode != ComponentActivity.RESULT_OK || data == null) {
            callback.onCancel();
            return;
        }
        Bundle bundle = data.getExtras();
        if (bundle == null) {
            callback.onCancel();
            return;
        }
        Uri uri = getResultUri(bundle);
        String mediaType = bundle.getString(RESULT_MEDIA_TYPE);
        if (uri == null || mediaType == null) {
            callback.onCancel();
            return;
        }
        callback.onSuccess(uri, mediaType);
    }

    @Nullable
    private static Uri getResultUri(@NonNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(RESULT_PATH, Uri.class);
        }
        return bundle.getParcelable(RESULT_PATH);
    }
}
