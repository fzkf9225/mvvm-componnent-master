package io.coderf.arklab.media.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.Map;
import java.util.UUID;

import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.R;
import io.coderf.arklab.media.bean.SelectorOptions;
import io.coderf.arklab.media.callback.CameraCallBack;
import io.coderf.arklab.media.callback.MultiSelectorCallBack;
import io.coderf.arklab.media.callback.SingleSelectorCallBack;
import io.coderf.arklab.media.dialog.TipDialog;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.utils.LogUtil;

/**
 * 绑定 Lifecycle，注册权限 / 相机 / 相册等 {@link ActivityResultLauncher}。
 * ON_CREATE 注册，ON_DESTROY 注销并 {@link MediaHelper#release()}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/7/30 9:35
 * @updated 2026/10/28
 */
public class MediaLifecycleObserver implements DefaultLifecycleObserver {
    private final MediaHelper mediaHelper;

    /** 媒体读/相机等通用权限 */
    private ActivityResultLauncher<String[]> permissionLauncher = null;
    /** 拍照 EXIF 定位权限（与通用权限独立） */
    private ActivityResultLauncher<String[]> captureExifPermissionLauncher = null;

    /** 系统相机拍照 */
    private ActivityResultLauncher<MediaTypeEnum> cameraLauncher = null;
    /** 系统相机录像 */
    private ActivityResultLauncher<MediaTypeEnum> shootLauncher = null;

    /** 传统单选（GetContent） */
    private ActivityResultLauncher<SelectorOptions> singleLauncher = null;
    /** 传统多选（GetMultipleContents） */
    private ActivityResultLauncher<SelectorOptions> multiLauncher = null;

    /** Photo Picker：多图 / 单图 */
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageSelectorLauncher = null;

    /** Photo Picker：多图+视频 / 单路图+视频 */
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiMediaSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaSelectorLauncher = null;

    /** Photo Picker：多视频 / 单视频 */
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiVideoSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoSelectorLauncher = null;

    /** 录像 Intent/输出 Uri 合约 */
    private TakeVideoUri takeVideoUri;
    /** 拍照 Intent/输出 Uri 合约 */
    private TakeCameraUri takeCameraUri;
    /** 拍照过程中采集姿态/GPS，供 EXIF 写入 */
    private CaptureMetadataHelper captureMetadataHelper;

    /** Photo Picker 单选合约实例 */
    private OpenPickMediaSelector pickMediaSelector;
    private OpenPickMultipleMediaSelector pickMuLtiVideoSelector;
    private OpenPickMultipleMediaSelector pickMuLtiImageSelector;
    private OpenPickMultipleMediaSelector pickMuLtiImageAndViewSelector;
    /** 传统多选 / 单选合约 */
    private OpenMultiSelector multiSelector;
    private OpenSingleSelector singleSelector;

    public MediaLifecycleObserver(MediaHelper mediaHelper) {
        this.mediaHelper = mediaHelper;
    }


    /**
     * Activity 已 STARTED/RESUMED 后才创建 MediaHelper（如底部 Tab 首次注入）时，
     * 带 LifecycleOwner 的 register 会抛 IllegalStateException；改用无 Lifecycle 版本，
     * 并在 {@link #onDestroy} 里手动 unregister（该类原本也会 unregister）。
     */
    @NonNull
    private <I, O> ActivityResultLauncher<I> registerLauncher(
            @NonNull ActivityResultRegistry registry,
            @NonNull LifecycleOwner owner,
            @NonNull String key,
            @NonNull ActivityResultContract<I, O> contract,
            @NonNull ActivityResultCallback<O> callback) {
        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            return registry.register(key, contract, callback);
        }
        return registry.register(key, owner, contract, callback);
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        ActivityResultRegistry registry;
        if (owner instanceof ComponentActivity) {
            registry = ((ComponentActivity) owner).getActivityResultRegistry();
        } else if (owner instanceof Fragment) {
            registry = ((Fragment) owner).requireActivity().getActivityResultRegistry();
        } else {
            throw new RuntimeException(mediaHelper.getMediaBuilder().getContext().getString(R.string.media_lifecycle_error));
        }
        //新选择器，兼容性不是很好,register中的key不能重复，如果重复则默认为同一个因此当你一个页面有多个实例的时候就会有问题
        pickMediaSelector = new OpenPickMediaSelector();

        //图片
        if (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() > 1) {
            pickMuLtiImageSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getImageMaxSelectedCount());
        } else {
            pickMuLtiImageSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
        }
        pickMuLtiImageSelectorLauncher = registerLauncher(registry, owner, "pickMuLtiImageSelector" + UUID.randomUUID(),
                pickMuLtiImageSelector, new MultiSelectorCallBack(mediaHelper, pickMuLtiImageSelector));
        pickImageSelectorLauncher = registerLauncher(registry, owner, "pickImageSelector" + UUID.randomUUID(),
                pickMediaSelector, new SingleSelectorCallBack(pickMediaSelector, mediaHelper));

        //图片和视频
        if (mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() > 1) {
            pickMuLtiImageAndViewSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getMediaMaxSelectedCount());
        } else {
            pickMuLtiImageAndViewSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_MEDIA_MAX_COUNT);
        }
        pickMuLtiMediaSelectorLauncher = registerLauncher(registry, owner, "pickMuLtiMediaSelector" + UUID.randomUUID(),
                pickMuLtiImageAndViewSelector, new MultiSelectorCallBack(mediaHelper, pickMuLtiImageAndViewSelector));
        pickMediaSelectorLauncher = registerLauncher(registry, owner, "pickMediaSelector" + UUID.randomUUID(),
                pickMediaSelector, new SingleSelectorCallBack(pickMediaSelector, mediaHelper));
        //视频
        if (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() > 1) {
            pickMuLtiVideoSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getVideoMaxSelectedCount());
        } else {
            pickMuLtiVideoSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_VIDEO_MAX_COUNT);
        }
        pickMuLtiVideoSelectorLauncher = registerLauncher(registry, owner, "pickMuLtiVideoSelector" + UUID.randomUUID(),
                pickMuLtiVideoSelector, new MultiSelectorCallBack(mediaHelper, pickMuLtiVideoSelector));
        pickVideoSelectorLauncher = registerLauncher(registry, owner, "pickVideoSelector" + UUID.randomUUID(),
                pickMediaSelector, new SingleSelectorCallBack(pickMediaSelector, mediaHelper));

        //传统选择器，单选
        singleSelector = new OpenSingleSelector();
        singleLauncher = registerLauncher(registry, owner, "singleSelector" + UUID.randomUUID(),
                singleSelector, new SingleSelectorCallBack(singleSelector, mediaHelper));
        //传统选择器，多选
        multiSelector = new OpenMultiSelector();
        multiLauncher = registerLauncher(registry, owner, "multiSelector" + UUID.randomUUID(),
                multiSelector, new MultiSelectorCallBack(mediaHelper, multiSelector));

        //权限
        permissionLauncher = registerLauncher(registry, owner, "permission" + UUID.randomUUID(),
                new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
        captureExifPermissionLauncher = registerLauncher(registry, owner, "captureExifPermission" + UUID.randomUUID(),
                new ActivityResultContracts.RequestMultiplePermissions(), captureExifPermissionCallback);
        captureMetadataHelper = new CaptureMetadataHelper(mediaHelper.getMediaBuilder().getContext());
        if (mediaHelper.getMediaBuilder().isWriteCaptureExifMetadata()) {
            captureMetadataHelper.start();
        }
        //拍照
        takeCameraUri = new TakeCameraUri(mediaHelper.getMediaBuilder());
        cameraLauncher = registerLauncher(registry, owner, "camera" + UUID.randomUUID(), takeCameraUri,
                new CameraCallBack(mediaHelper.getMediaBuilder(), takeCameraUri, captureMetadataHelper,
                        mediaHelper));
        //录像
        takeVideoUri = new TakeVideoUri(mediaHelper.getMediaBuilder(), mediaHelper.getMediaBuilder().getMaxVideoTime());
        shootLauncher = registerLauncher(registry, owner, "shoot" + UUID.randomUUID(), takeVideoUri,
                new CameraCallBack(mediaHelper.getMediaBuilder(), takeVideoUri, mediaHelper));
    }

    /**
     * 拍照 EXIF 定位权限回调（与通用权限回调独立）
     */
    ActivityResultCallback<Map<String, Boolean>> captureExifPermissionCallback = new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                LogUtil.logger(MediaHelper.TAG, "拍照EXIF定位权限 " + entry.getKey() + ":" + entry.getValue());
            }
            mediaHelper.continueAfterCaptureExifPermissionResult(mediaHelper.hasCaptureExifLocationPermission());
        }
    };

    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                LogUtil.logger(MediaHelper.TAG, entry.getKey() + ":" + entry.getValue());
            }
            Runnable continuation = mediaHelper.drainPendingPermissionContinuation();
            // Android 14 可能只授予 READ_MEDIA_VISUAL_USER_SELECTED（部分授权），
            // 不能要求 map 全 true；有任一授予则交给业务方法自行 hasXxxAccess 续传。
            boolean anyGranted = result.containsValue(Boolean.TRUE);
            if (continuation != null) {
                if (anyGranted) {
                    continuation.run();
                } else {
                    showPermissionDeniedTip();
                }
                return;
            }
            if (!anyGranted || result.containsValue(Boolean.FALSE)) {
                // 无 continuation 的旧路径（如 openAudio 补全前）：部分拒绝也提示去设置
                if (result.containsValue(Boolean.FALSE)) {
                    showPermissionDeniedTip();
                }
            }
        }
    };

    private void showPermissionDeniedTip() {
        Context context = mediaHelper.getMediaBuilder().getContext();
        new TipDialog(context)
                .setMessage(context.getString(R.string.media_permission_denied_message))
                .setNegativeText(context.getString(R.string.media_cancel))
                .setPositiveText(context.getString(R.string.media_go_to_settings))
                .setOnPositiveClickListener(dialog -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",
                            mediaHelper.getMediaBuilder().getContext().getPackageName(), null);
                    intent.setData(uri);
                    mediaHelper.getMediaBuilder().getContext().startActivity(intent);
                })
                .builder()
                .show();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (captureMetadataHelper != null) {
            captureMetadataHelper.stop();
            captureMetadataHelper = null;
        }
        mediaHelper.release();
        DefaultLifecycleObserver.super.onDestroy(owner);
        //取消pick监听
        if (pickMuLtiImageSelectorLauncher != null) {
            pickMuLtiImageSelectorLauncher.unregister();
        }
        if (pickMuLtiMediaSelectorLauncher != null) {
            pickMuLtiMediaSelectorLauncher.unregister();
        }
        if (pickMediaSelectorLauncher != null) {
            pickMediaSelectorLauncher.unregister();
        }
        if (pickImageSelectorLauncher != null) {
            pickImageSelectorLauncher.unregister();
        }
        if (pickMuLtiVideoSelectorLauncher != null) {
            pickMuLtiVideoSelectorLauncher.unregister();
        }
        if (pickVideoSelectorLauncher != null) {
            pickVideoSelectorLauncher.unregister();
        }
        //拍照录像、权限
        if (cameraLauncher != null) {
            cameraLauncher.unregister();
        }
        if (shootLauncher != null) {
            shootLauncher.unregister();
        }
        if (permissionLauncher != null) {
            permissionLauncher.unregister();
        }
        if (captureExifPermissionLauncher != null) {
            captureExifPermissionLauncher.unregister();
        }
        if (singleLauncher != null) {
            singleLauncher.unregister();
        }
        if (multiLauncher != null) {
            multiLauncher.unregister();
        }
        mediaHelper.getMediaBuilder().getLifecycleOwner().getLifecycle().removeObserver(this);
        takeCameraUri = null;
        takeVideoUri = null;
    }

    public TakeCameraUri getTakeCameraUri() {
        return takeCameraUri;
    }

    public TakeVideoUri getTakeVideoUri() {
        return takeVideoUri;
    }

    public ActivityResultLauncher<SelectorOptions> getMultiLauncher() {
        return multiLauncher;
    }

    public ActivityResultLauncher<SelectorOptions> getSingleLauncher() {
        return singleLauncher;
    }

    public ActivityResultLauncher<String[]> getPermissionLauncher() {
        return permissionLauncher;
    }

    public ActivityResultLauncher<String[]> getCaptureExifPermissionLauncher() {
        return captureExifPermissionLauncher;
    }

    public ActivityResultLauncher<MediaTypeEnum> getCameraLauncher() {
        return cameraLauncher;
    }

    public ActivityResultLauncher<MediaTypeEnum> getShootLauncher() {
        return shootLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickMuLtiImageSelectorLauncher() {
        return pickMuLtiImageSelectorLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickImageSelectorLauncher() {
        return pickImageSelectorLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickMuLtiVideoSelectorLauncher() {
        return pickMuLtiVideoSelectorLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickVideoSelectorLauncher() {
        return pickVideoSelectorLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickMuLtiMediaSelectorLauncher() {
        return pickMuLtiMediaSelectorLauncher;
    }

    public ActivityResultLauncher<PickVisualMediaRequest> getPickMediaSelectorLauncher() {
        return pickMediaSelectorLauncher;
    }

    public OpenPickMultipleMediaSelector getPickMuLtiImageAndViewSelector() {
        return pickMuLtiImageAndViewSelector;
    }

    public OpenPickMultipleMediaSelector getPickMuLtiImageSelector() {
        return pickMuLtiImageSelector;
    }

    public OpenPickMultipleMediaSelector getPickMuLtiVideoSelector() {
        return pickMuLtiVideoSelector;
    }

    public OpenPickMediaSelector getPickMediaSelector() {
        return pickMediaSelector;
    }

    public OpenMultiSelector getMultiSelector() {
        return multiSelector;
    }

    public OpenSingleSelector getSingleSelector() {
        return singleSelector;
    }
}

