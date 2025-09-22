package com.casic.otitan.media.callback;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import com.casic.otitan.media.MediaHelper;
import com.casic.otitan.media.bean.MediaBean;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.helper.OpenPickMediaSelector;
import com.casic.otitan.media.helper.OpenSingleSelector;
import com.casic.otitan.media.utils.LogUtil;


/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class SingleSelectorCallBack implements ActivityResultCallback<Uri> {
    private OpenSingleSelector singleSelector;
    private final MutableLiveData<MediaBean> mutableLiveData;
    private OpenPickMediaSelector pickMediaSelector;

    public SingleSelectorCallBack(OpenSingleSelector singleSelector, MutableLiveData<MediaBean> mutableLiveData) {
        this.singleSelector = singleSelector;
        this.mutableLiveData = mutableLiveData;
    }


    public SingleSelectorCallBack(OpenPickMediaSelector pickMediaSelector, MutableLiveData<MediaBean> mutableLiveData) {
        this.pickMediaSelector = pickMediaSelector;
        this.mutableLiveData = mutableLiveData;
    }

    @Override
    public void onActivityResult(Uri result) {
        LogUtil.show(MediaHelper.TAG, "单选回调：" + result);
        if (result == null) {
            return;
        }
        if (MediaTypeEnum.IMAGE == getMediaType()) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE));
        } else if (MediaTypeEnum.VIDEO == getMediaType()) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.VIDEO));
        } else if (MediaTypeEnum.AUDIO == getMediaType()) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.AUDIO));
        } else if (MediaTypeEnum.FILE == getMediaType()) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.FILE));
        } else if (MediaTypeEnum.IMAGE_AND_VIDEO == getMediaType()) {
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE_AND_VIDEO));
        }
    }

    public MediaTypeEnum getMediaType() {
        if (singleSelector != null) {
            return singleSelector.getMediaType();
        } else if (pickMediaSelector != null) {
            return pickMediaSelector.getMediaType();
        }
        return MediaTypeEnum.IMAGE_AND_VIDEO;
    }

}
