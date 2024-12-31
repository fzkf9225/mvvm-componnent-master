package pers.fz.media;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.lifecycle.MutableLiveData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


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
