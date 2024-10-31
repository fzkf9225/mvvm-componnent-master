package pers.fz.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class MultiSelectorCallBack implements ActivityResultCallback<List<Uri>> {
    private MediaHelper mediaHelper;
    private MediaTypeEnum mediaType;
    private MutableLiveData<MediaBean> mutableLiveData;

    public MultiSelectorCallBack(MediaHelper mediaHelper, MediaTypeEnum mediaType, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaHelper = mediaHelper;
        this.mediaType = mediaType;
        this.mutableLiveData = mutableLiveData;
    }

    @Override
    public void onActivityResult(List<Uri> result) {
        LogUtil.show(MediaHelper.TAG, "多选回调：" + result);
        if (result == null || result.size() == 0) {
            return;
        }
        if (MediaTypeEnum.IMAGE == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount()) {
                    mediaHelper.showToast("您最多还可选" + (mediaHelper.getMediaBuilder().getImageMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedImageCount())
                            + "张图片");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.IMAGE.getMediaType()));
        } else if (MediaTypeEnum.VIDEO == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()) {
                    mediaHelper.showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedVideoCount()
                    ) + "条视频");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.VIDEO.getMediaType()));
        } else if (MediaTypeEnum.AUDIO == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()) {
                    mediaHelper.showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getAudioMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedAudioCount()
                    ) + "条音频");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.AUDIO.getMediaType()));
        } else if (MediaTypeEnum.FILE == mediaType) {
            if (mediaHelper.getMediaBuilder().getMediaListener() != null) {
                if (result.size() > mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()) {
                    mediaHelper.showToast("您最多还可再选" + (mediaHelper.getMediaBuilder().getFileMaxSelectedCount() - mediaHelper.getMediaBuilder().getMediaListener().onSelectedFileCount()
                    ) + "个文件");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.FILE.getMediaType()));
        }

    }
}
