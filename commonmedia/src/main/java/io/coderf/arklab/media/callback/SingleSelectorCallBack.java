package io.coderf.arklab.media.callback;

import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;

import java.util.Collections;
import java.util.List;

import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.bean.MediaBean;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.helper.OpenPickMediaSelector;
import io.coderf.arklab.media.helper.OpenSingleSelector;
import io.coderf.arklab.media.utils.LogUtil;


/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class SingleSelectorCallBack implements ActivityResultCallback<Uri> {
    private OpenSingleSelector singleSelector;
    private final MediaHelper mediaHelper;
    private OpenPickMediaSelector pickMediaSelector;

    public SingleSelectorCallBack(OpenSingleSelector singleSelector, MediaHelper mediaHelper) {
        this.singleSelector = singleSelector;
        this.mediaHelper = mediaHelper;
    }


    public SingleSelectorCallBack(OpenPickMediaSelector pickMediaSelector, MediaHelper mediaHelper) {
        this.pickMediaSelector = pickMediaSelector;
        this.mediaHelper = mediaHelper;
    }

    @Override
    public void onActivityResult(Uri result) {
        LogUtil.show(MediaHelper.TAG, "单选回调：" + result);
        if (result == null) {
            mediaHelper.postPickResult(new MediaBean(Collections.emptyList(), getMediaType()));
            return;
        }
        if (MediaTypeEnum.IMAGE == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.IMAGE));
        } else if (MediaTypeEnum.VIDEO == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.VIDEO));
        } else if (MediaTypeEnum.AUDIO == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.AUDIO));
        } else if (MediaTypeEnum.FILE == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.FILE));
        } else if (MediaTypeEnum.IMAGE_AND_VIDEO == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.IMAGE_AND_VIDEO));
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
