package pers.fz.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.util.List;


/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class SingleSelectorCallBack implements ActivityResultCallback<Uri> {
    private final MediaTypeEnum mediaType;
    private final MutableLiveData<MediaBean> mutableLiveData;

    public SingleSelectorCallBack(MediaTypeEnum mediaType, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaType = mediaType;
        this.mutableLiveData = mutableLiveData;
    }

    @Override
    public void onActivityResult(Uri result) {
        LogUtil.show(MediaHelper.TAG, "单选回调：" + result);
        if (result == null) {
            return;
        }
        if (MediaTypeEnum.IMAGE == mediaType) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE.getMediaType()));
        } else if (MediaTypeEnum.VIDEO == mediaType) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.VIDEO.getMediaType()));
        } else if (MediaTypeEnum.AUDIO == mediaType) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.AUDIO.getMediaType()));
        } else if (MediaTypeEnum.FILE == mediaType) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.FILE.getMediaType()));
        }

    }
}
