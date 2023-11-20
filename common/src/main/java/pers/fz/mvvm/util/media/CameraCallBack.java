package pers.fz.mvvm.util.media;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class CameraCallBack implements ActivityResultCallback<Uri> {
    private MediaBuilder mediaBuilder;
    private MediaTypeEnum mediaType;
    private MutableLiveData<MediaBean> mutableLiveData;

    public CameraCallBack(MediaBuilder mediaBuilder, MediaTypeEnum mediaType, MutableLiveData<MediaBean> mutableLiveData) {
        this.mediaBuilder = mediaBuilder;
        this.mediaType = mediaType;
        this.mutableLiveData = mutableLiveData;
    }

    @Override
    public void onActivityResult(Uri result) {
        LogUtil.show(MediaHelper.TAG, "相机相关回调：" + result);
        if (result == null) {
            return;
        }
        if (MediaTypeEnum.IMAGE == mediaType) {
            if (!isFileUriExists(result)) {
                return;
            }
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.IMAGE.getMediaType()));
        } else if (MediaTypeEnum.VIDEO == mediaType) {
            if (!isFileUriExists(result)) {
                return;
            }
            mutableLiveData.postValue(new MediaBean(List.of(result), MediaTypeEnum.VIDEO.getMediaType()));
        }

    }


    /**
     * 判断uri是否存在，因为新版打开拍照后不拍照也会返回uri因此需要判断一下
     *
     * @param uri 文件uri路径
     * @return
     */
    public boolean isFileUriExists(Uri uri) {
        ContentResolver contentResolver = mediaBuilder.getActivity().getContentResolver();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                LogUtil.show(MediaHelper.TAG, uri + "：文件存在");
                return true;
            }
        } catch (FileNotFoundException e) {
            // 文件不存在
            LogUtil.show(MediaHelper.TAG, uri + "，判断文件是否存在出现异常：" + e);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.show(MediaHelper.TAG, uri + "，判断文件是否存在出现IO异常：" + e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LogUtil.show(MediaHelper.TAG, uri + "：文件不存在");
        return false;
    }
}
