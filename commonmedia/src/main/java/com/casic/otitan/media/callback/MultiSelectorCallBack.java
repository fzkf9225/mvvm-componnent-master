package com.casic.otitan.media.callback;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;

import java.util.List;

import com.casic.otitan.media.MediaHelper;
import com.casic.otitan.media.bean.MediaBean;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.helper.OpenMultiSelector;
import com.casic.otitan.media.helper.OpenPickMultipleMediaSelector;
import com.casic.otitan.media.utils.LogUtil;

/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class MultiSelectorCallBack implements ActivityResultCallback<List<Uri>> {
    private final MediaHelper mediaHelper;
    private OpenMultiSelector multiSelector;
    private OpenPickMultipleMediaSelector pickMultipleMediaSelector;

    public MultiSelectorCallBack(MediaHelper mediaHelper, OpenMultiSelector multiSelector) {
        this.mediaHelper = mediaHelper;
        this.multiSelector = multiSelector;
    }

    public MultiSelectorCallBack(MediaHelper mediaHelper, OpenPickMultipleMediaSelector pickMultipleMediaSelector) {
        this.mediaHelper = mediaHelper;
        this.pickMultipleMediaSelector = pickMultipleMediaSelector;
    }


    @Override
    public void onActivityResult(List<Uri> result) {
        LogUtil.show(MediaHelper.TAG, "多选回调：" + result);
        if (result == null || result.isEmpty()) {
            return;
        }
        if (MediaTypeEnum.IMAGE == getMediaType()) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount()) {
                    mediaHelper.getUIController().showToast("您最多还可选" + (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount())
                            + "张图片");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.IMAGE));
        } else if (MediaTypeEnum.VIDEO == getMediaType()) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()
                    ) + "条视频");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.VIDEO));
        } else if (MediaTypeEnum.AUDIO == getMediaType()) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()
                    ) + "条音频");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.AUDIO));
        } else if (MediaTypeEnum.FILE == getMediaType()) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()
                    ) + "个文件");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.FILE));
        } else if (MediaTypeEnum.IMAGE_AND_VIDEO == getMediaType()) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedMediaCount()) {
                    mediaHelper.getUIController().showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedMediaCount()
                    ) + "个文件");
                    return;
                }
            }
            mediaHelper.getMutableLiveData().setValue(new MediaBean(result, MediaTypeEnum.IMAGE_AND_VIDEO));
        }
    }

    public MediaTypeEnum getMediaType() {
        if (multiSelector != null) {
            return multiSelector.getMediaType();
        } else if (pickMultipleMediaSelector != null) {
            return pickMultipleMediaSelector.getMediaType();
        }
        return MediaTypeEnum.IMAGE_AND_VIDEO;
    }
}
