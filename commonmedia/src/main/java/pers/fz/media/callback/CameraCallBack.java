package pers.fz.media.callback;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.bean.MediaBean;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.helper.TakeCameraUri;
import pers.fz.media.helper.TakeVideoUri;
import pers.fz.media.utils.LogUtil;


/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class CameraCallBack implements ActivityResultCallback<Uri> {
    private final MediaBuilder mediaBuilder;
    private final MutableLiveData<MediaBean> mutableLiveData;

    private TakeCameraUri takeCameraUri;

    private TakeVideoUri takeVideoUri;

    public CameraCallBack(MediaBuilder mediaBuilder, TakeCameraUri takeCameraUri, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaBuilder = mediaBuilder;
        this.takeCameraUri = takeCameraUri;
        this.mutableLiveData = mutableLiveData;
    }

    public CameraCallBack(MediaBuilder mediaBuilder, TakeVideoUri takeVideoUri, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaBuilder = mediaBuilder;
        this.takeVideoUri = takeVideoUri;
        this.mutableLiveData = mutableLiveData;
    }

    @Override
    public void onActivityResult(Uri result) {
        if (result == null) {
            return;
        }
        LogUtil.show(MediaHelper.TAG, "拍照录像：" + getMediaType() + "，回调：" + result.toString());
        if (MediaTypeEnum.IMAGE == getMediaType()) {
            if (!isFileUriExists(result)) {
                return;
            }
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE));
        } else if (MediaTypeEnum.VIDEO == getMediaType()) {
            if (!isFileUriExists(result)) {
                return;
            }
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.VIDEO));
        } else if (MediaTypeEnum.IMAGE_AND_VIDEO == getMediaType()) {
            if (!isFileUriExists(result)) {
                return;
            }
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE_AND_VIDEO));
        }
    }

    public MediaTypeEnum getMediaType() {
        if (takeCameraUri != null) {
            return takeCameraUri.getMediaType();
        } else if (takeVideoUri != null) {
            return takeVideoUri.getMediaType();
        } else {
            return MediaTypeEnum.IMAGE_AND_VIDEO;
        }
    }


    /**
     * 判断uri是否存在，因为新版打开拍照后不拍照也会返回uri因此需要判断一下
     *
     * @param uri 文件uri路径
     * @return true存在，false不存在
     */
    private boolean isFileUriExists(Uri uri) {
        ContentResolver contentResolver = mediaBuilder.getContext().getContentResolver();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            // 文件不存在
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
