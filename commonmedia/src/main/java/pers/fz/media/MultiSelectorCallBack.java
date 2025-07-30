package pers.fz.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;

import java.util.List;

/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class MultiSelectorCallBack implements ActivityResultCallback<List<Uri>> {
    private final MediaHelper mediaHelper;
    private final MediaTypeEnum mediaType;

    public MultiSelectorCallBack(MediaHelper mediaHelper, MediaTypeEnum mediaType) {
        this.mediaHelper = mediaHelper;
        this.mediaType = mediaType;
    }

    @Override
    public void onActivityResult(List<Uri> result) {
        LogUtil.show(MediaHelper.TAG, "多选回调：" + result);
        if (result == null || result.isEmpty()) {
            return;
        }
        if (MediaTypeEnum.IMAGE == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount()) {
                    mediaHelper.getUIController().showToast("您最多还可选" + (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount())
                            + "张图片");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.IMAGE.getMediaType()));
        } else if (MediaTypeEnum.VIDEO == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()
                    ) + "条视频");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.VIDEO.getMediaType()));
        } else if (MediaTypeEnum.AUDIO == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()
                    ) + "条音频");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.AUDIO.getMediaType()));
        } else if (MediaTypeEnum.FILE == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()
                    ) + "个文件");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.FILE.getMediaType()));
        }
    }
}
