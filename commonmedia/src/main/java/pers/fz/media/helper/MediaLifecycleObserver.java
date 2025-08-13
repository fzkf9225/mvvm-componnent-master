package pers.fz.media.helper;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Map;
import java.util.UUID;

import pers.fz.media.MediaHelper;
import pers.fz.media.bean.SelectorOptions;
import pers.fz.media.callback.CameraCallBack;
import pers.fz.media.callback.MultiSelectorCallBack;
import pers.fz.media.callback.SingleSelectorCallBack;
import pers.fz.media.dialog.TipDialog;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.utils.LogUtil;

/**
 * created by fz on 2025/7/30 9:35
 * describe:
 */
public class MediaLifecycleObserver implements DefaultLifecycleObserver {
    private final MediaHelper mediaHelper;

    private ActivityResultLauncher<String[]> permissionLauncher = null;

    private ActivityResultLauncher<MediaTypeEnum> cameraLauncher = null;
    private ActivityResultLauncher<MediaTypeEnum> shootLauncher = null;

    private ActivityResultLauncher<SelectorOptions> singleLauncher = null;
    private ActivityResultLauncher<SelectorOptions> multiLauncher = null;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageSelectorLauncher = null;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiMediaSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaSelectorLauncher = null;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiVideoSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoSelectorLauncher = null;

    private TakeVideoUri takeVideoUri;
    private TakeCameraUri takeCameraUri;

    private OpenPickMediaSelector pickMediaSelector;
    private OpenPickMultipleMediaSelector pickMuLtiVideoSelector;
    private OpenPickMultipleMediaSelector pickMuLtiImageSelector;
    private OpenPickMultipleMediaSelector pickMuLtiImageAndViewSelector;
    private OpenMultiSelector multiSelector;
    private OpenSingleSelector singleSelector;

    public MediaLifecycleObserver(MediaHelper mediaHelper) {
        this.mediaHelper = mediaHelper;
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
            throw new RuntimeException("请使用Activity或Fragment的lifecycle对象");
        }
        //新选择器，兼容性不是很好,register中的key不能重复，如果重复则默认为同一个因此当你一个页面有多个实例的时候就会有问题
        pickMediaSelector = new OpenPickMediaSelector();

        //图片
        if (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() > 1) {
            pickMuLtiImageSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getImageMaxSelectedCount());
        } else {
            pickMuLtiImageSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
        }
        pickMuLtiImageSelectorLauncher = registry.register("pickMuLtiImageSelector" + UUID.randomUUID().toString(), owner, pickMuLtiImageSelector,
                new MultiSelectorCallBack(mediaHelper, pickMuLtiImageSelector));
        pickImageSelectorLauncher = registry.register("pickImageSelector" + UUID.randomUUID().toString(), owner, pickMediaSelector,
                new SingleSelectorCallBack(pickMediaSelector, mediaHelper.getMutableLiveData()));

        //图片和视频
        if (mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() > 1) {
            pickMuLtiImageAndViewSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getMediaMaxSelectedCount());
        } else {
            pickMuLtiImageAndViewSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_MEDIA_MAX_COUNT);
        }
        pickMuLtiMediaSelectorLauncher = registry.register("pickMuLtiMediaSelector" + UUID.randomUUID().toString(), owner, pickMuLtiImageAndViewSelector,
                new MultiSelectorCallBack(mediaHelper, pickMuLtiImageAndViewSelector));
        pickMediaSelectorLauncher = registry.register("pickMediaSelector" + UUID.randomUUID().toString(), owner, pickMediaSelector,
                new SingleSelectorCallBack(pickMediaSelector, mediaHelper.getMutableLiveData()));
        //视频
        if (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() > 1) {
            pickMuLtiVideoSelector = new OpenPickMultipleMediaSelector(mediaHelper.getMediaBuilder().getVideoMaxSelectedCount());
        } else {
            pickMuLtiVideoSelector = new OpenPickMultipleMediaSelector(MediaHelper.DEFAULT_VIDEO_MAX_COUNT);
        }
        pickMuLtiVideoSelectorLauncher = registry.register("pickMuLtiVideoSelector" + UUID.randomUUID().toString(), owner, pickMuLtiVideoSelector,
                new MultiSelectorCallBack(mediaHelper, pickMuLtiVideoSelector));
        pickVideoSelectorLauncher = registry.register("pickVideoSelector" + UUID.randomUUID().toString(), owner, pickMediaSelector,
                new SingleSelectorCallBack(pickMediaSelector, mediaHelper.getMutableLiveData()));

        //传统选择器，单选
        singleSelector = new OpenSingleSelector();
        singleLauncher = registry.register("singleSelector" + UUID.randomUUID().toString(), owner, singleSelector,
                new SingleSelectorCallBack(singleSelector, mediaHelper.getMutableLiveData()));
        //传统选择器，多选
        multiSelector = new OpenMultiSelector();
        multiLauncher = registry.register("multiSelector" + UUID.randomUUID().toString(), owner, multiSelector,
                new MultiSelectorCallBack(mediaHelper, multiSelector));

        //权限
        permissionLauncher = registry.register("permission" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
        //拍照
        takeCameraUri = new TakeCameraUri(mediaHelper.getMediaBuilder());
        cameraLauncher = registry.register("camera" + UUID.randomUUID().toString(), owner, takeCameraUri,
                new CameraCallBack(mediaHelper.getMediaBuilder(), takeCameraUri, mediaHelper.getMutableLiveData()));
        //录像
        takeVideoUri = new TakeVideoUri(mediaHelper.getMediaBuilder(), mediaHelper.getMediaBuilder().getMaxVideoTime());
        shootLauncher = registry.register("shoot" + UUID.randomUUID().toString(), owner, takeVideoUri,
                new CameraCallBack(mediaHelper.getMediaBuilder(), takeVideoUri, mediaHelper.getMutableLiveData()));
    }

    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                LogUtil.show(MediaHelper.TAG, entry.getKey() + ":" + entry.getValue());
                if (Boolean.FALSE.equals(entry.getValue())) {
                    new TipDialog(mediaHelper.getMediaBuilder().getContext())
                            .setMessage("您拒绝了当前权限，可能导致无法使用该功能，可前往设置修改")
                            .setNegativeText("取消")
                            .setPositiveText("前往设置")
                            .setOnPositiveClickListener(dialog -> {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", mediaHelper.getMediaBuilder().getContext().getPackageName(), null);
                                intent.setData(uri);
                                mediaHelper.getMediaBuilder().getContext().startActivity(intent);
                            })
                            .builder()
                            .show();
                    return;
                }
            }
        }
    };

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
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

