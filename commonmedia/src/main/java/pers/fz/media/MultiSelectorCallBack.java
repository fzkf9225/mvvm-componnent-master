package pers.fz.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class MultiSelectorCallBack implements ActivityResultCallback<List<Uri>> {
    private MediaBuilder mediaBuilder;
    private MediaTypeEnum mediaType;
    private MutableLiveData<MediaBean> mutableLiveData;

    public MultiSelectorCallBack(MediaBuilder mediaBuilder, MediaTypeEnum mediaType, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaBuilder = mediaBuilder;
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
            if (mediaBuilder.getMediaListener() != null) {
                if (result.size() > mediaBuilder.getImageMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedImageCount()) {
                    mediaBuilder.getBaseView().showToast("您最多还可选" + (mediaBuilder.getImageMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedImageCount())
                            + "张图片");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.IMAGE.getMediaType()));
        } else if (MediaTypeEnum.VIDEO == mediaType) {
            if (mediaBuilder.getMediaListener() != null) {
                if (result.size() > mediaBuilder.getVideoMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedVideoCount()) {
                    mediaBuilder.getBaseView().showToast("您最多还可再选" + (mediaBuilder.getVideoMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedVideoCount()
                    ) + "条视频");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.VIDEO.getMediaType()));
        } else if (MediaTypeEnum.AUDIO == mediaType) {
            if (mediaBuilder.getMediaListener() != null) {
                if (result.size() > mediaBuilder.getAudioMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedAudioCount()) {
                    mediaBuilder.getBaseView().showToast("您最多还可再选" + (mediaBuilder.getAudioMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedAudioCount()
                    ) + "条音频");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.AUDIO.getMediaType()));
        } else if (MediaTypeEnum.FILE == mediaType) {
            if (mediaBuilder.getMediaListener() != null) {
                if (result.size() > mediaBuilder.getFileMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedFileCount()) {
                    mediaBuilder.getBaseView().showToast("您最多还可再选" + (mediaBuilder.getFileMaxSelectedCount() - mediaBuilder.getMediaListener().onSelectedFileCount()
                    ) + "个文件");
                    return;
                }
            }
            mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.FILE.getMediaType()));
        }

    }
}
