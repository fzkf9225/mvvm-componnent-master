package pers.fz.media;

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

import pers.fz.media.dialog.TipDialog;

/**
 * created by fz on 2025/7/30 9:35
 * describe:
 */
public class MediaLifecycleObserver implements DefaultLifecycleObserver {
    private final MediaHelper mediaHelper;
    //旧版文件
    private ActivityResultLauncher<String[]> imageMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> imageSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> audioMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> audioSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> fileMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> fileSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> permissionLauncher = null;
    private ActivityResultLauncher<Object> cameraLauncher = null;
    private ActivityResultLauncher<String[]> videoLauncher = null;
    private ActivityResultLauncher<String[]> videoMultiLauncher = null;
    private ActivityResultLauncher<Object> shootLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiVideoSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoSelectorLauncher = null;

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
        if (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() > 1) {
            pickMuLtiImageSelectorLauncher = registry.register("pickMuLtiImageSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickMultipleVisualMedia(mediaHelper.getMediaBuilder().getImageMaxSelectedCount()),
                    new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.IMAGE));
        }
        pickImageSelectorLauncher = registry.register("pickImageSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickVisualMedia(),
                new SingleSelectorCallBack(MediaTypeEnum.IMAGE, mediaHelper.getMutableLiveData()));
        if (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() > 1) {
            pickMuLtiVideoSelectorLauncher = registry.register("pickMuLtiVideoSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickMultipleVisualMedia(mediaHelper.getMediaBuilder().getVideoMaxSelectedCount()),
                    new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.VIDEO));
        }
        pickVideoSelectorLauncher = registry.register("pickVideoSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickVisualMedia(),
                new SingleSelectorCallBack(MediaTypeEnum.VIDEO, mediaHelper.getMutableLiveData()));

        //传统选择器
        imageMuLtiSelectorLauncher = registry.register("imageMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.IMAGE));
        imageSingleSelectorLauncher = registry.register("imageSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.IMAGE, mediaHelper.getMutableLiveData()));
        //传统选择器
        audioMuLtiSelectorLauncher = registry.register("audioMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.AUDIO));
        audioSingleSelectorLauncher = registry.register("audioSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.AUDIO, mediaHelper.getMutableLiveData()));
        //传统选择器
        fileMuLtiSelectorLauncher = registry.register("fileMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.FILE));
        fileSingleSelectorLauncher = registry.register("fileSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.FILE, mediaHelper.getMutableLiveData()));

        //权限监听
        permissionLauncher = registry.register("permission" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
        cameraLauncher = registry.register("camera" + UUID.randomUUID().toString(), owner, new TakeCameraUri(mediaHelper.getMediaBuilder()),
                new CameraCallBack(mediaHelper.getMediaBuilder(), MediaTypeEnum.IMAGE, mediaHelper.getMutableLiveData()));
        videoLauncher = registry.register("video" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new CameraCallBack(mediaHelper.getMediaBuilder(), MediaTypeEnum.VIDEO, mediaHelper.getMutableLiveData()));
        videoMultiLauncher = registry.register("videoMulti" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(mediaHelper, MediaTypeEnum.VIDEO));
        shootLauncher = registry.register("shoot" + UUID.randomUUID().toString(), owner, new TakeVideoUri(mediaHelper.getMediaBuilder(), mediaHelper.getMediaBuilder().getMaxVideoTime()),
                new CameraCallBack(mediaHelper.getMediaBuilder(), MediaTypeEnum.VIDEO, mediaHelper.getMutableLiveData()));
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
        if (pickImageSelectorLauncher != null) {
            pickImageSelectorLauncher.unregister();
        }
        if (pickMuLtiVideoSelectorLauncher != null) {
            pickMuLtiVideoSelectorLauncher.unregister();
        }
        if (pickVideoSelectorLauncher != null) {
            pickVideoSelectorLauncher.unregister();
        }
        //取消默认选择监听
        if (imageMuLtiSelectorLauncher != null) {
            imageMuLtiSelectorLauncher.unregister();
        }
        if (imageSingleSelectorLauncher != null) {
            imageSingleSelectorLauncher.unregister();
        }
        if (videoMultiLauncher != null) {
            videoMultiLauncher.unregister();
        }
        if (videoLauncher != null) {
            videoLauncher.unregister();
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
        //音频
        if (audioSingleSelectorLauncher != null) {
            audioSingleSelectorLauncher.unregister();
        }
        if (audioMuLtiSelectorLauncher != null) {
            audioMuLtiSelectorLauncher.unregister();
        }
        //附件
        if (fileSingleSelectorLauncher != null) {
            fileSingleSelectorLauncher.unregister();
        }
        if (fileMuLtiSelectorLauncher != null) {
            fileMuLtiSelectorLauncher.unregister();
        }
        mediaHelper.getMediaBuilder().getLifecycleOwner().getLifecycle().removeObserver(this);
    }

    public ActivityResultLauncher<String[]> getImageMuLtiSelectorLauncher() {
        return imageMuLtiSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getImageSingleSelectorLauncher() {
        return imageSingleSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getAudioMuLtiSelectorLauncher() {
        return audioMuLtiSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getAudioSingleSelectorLauncher() {
        return audioSingleSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getFileMuLtiSelectorLauncher() {
        return fileMuLtiSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getFileSingleSelectorLauncher() {
        return fileSingleSelectorLauncher;
    }

    public ActivityResultLauncher<String[]> getPermissionLauncher() {
        return permissionLauncher;
    }

    public ActivityResultLauncher<Object> getCameraLauncher() {
        return cameraLauncher;
    }

    public ActivityResultLauncher<String[]> getVideoLauncher() {
        return videoLauncher;
    }

    public ActivityResultLauncher<String[]> getVideoMultiLauncher() {
        return videoMultiLauncher;
    }

    public ActivityResultLauncher<Object> getShootLauncher() {
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
}

