package pers.fz.media;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pers.fz.media.listener.MediaListener;

/**
 * Created by fz on 2021/2/7 9:10
 * describe:mediaUtil配置类
 */
public class MediaBuilder {
    /**
     * 设置视频拍摄时长，单位：秒
     */
    public int maxVideoTime = 30;
    /**
     * 视频压缩质量
     */
    private int videoQuality = MediaHelper.VIDEO_LOW;
    /**
     * 最大相册选择数量
     */
    public int imageMaxSelectedCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    /**
     * 最大视频选择数量
     */
    public int videoMaxSelectedCount = MediaHelper.DEFAULT_VIDEO_MAX_COUNT;
    /**
     * 最大音频选择数量
     */
    public int audioMaxSelectedCount = MediaHelper.DEFAULT_AUDIO_MAX_COUNT;
    /**
     * 最大文件选择数量
     */
    public int fileMaxSelectedCount = MediaHelper.DEFAULT_FILE_MAX_COUNT;
    /**
     * 图片压缩大小限制，默认200
     */
    public int imageQualityCompress = 200;
    private final ComponentActivity mActivity;

    private Context mContext;
    private String waterMark;
    private Fragment fragment;
    /**
     * 是否将拍摄的图片和视频保存到公共目录，默认false
     */
    private boolean savePublicPath = true;
    /**
     * 是否显示加载dialog
     */
    private boolean showLoading = true;

    private String imageSubPath;

    private String videoSubPath;

    private MediaListener mediaListener;
    private int chooseType = MediaHelper.DEFAULT_TYPE;

    public MediaBuilder(@NotNull ComponentActivity mActivity) {
        this.mActivity = mActivity;
        setContext(this.mActivity);
    }

    public MediaBuilder(@NotNull Fragment fragment) {
        this.mActivity = fragment.getActivity();
        this.fragment = fragment;
        setContext(this.fragment.requireContext());
    }

    public Fragment getFragment() {
        return fragment;
    }

    public int getAudioMaxSelectedCount() {
        return audioMaxSelectedCount;
    }

    public MediaBuilder setAudioMaxSelectedCount(int audioMaxSelectedCount) {
        this.audioMaxSelectedCount = audioMaxSelectedCount;
        return this;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public int getFileMaxSelectedCount() {
        return fileMaxSelectedCount;
    }

    public MediaBuilder setFileMaxSelectedCount(int fileMaxSelectedCount) {
        this.fileMaxSelectedCount = fileMaxSelectedCount;
        return this;
    }

    /**
     * 设置图片子目录
     * @param subPath 子目录地址
     * @return 新的父路径
     */
    public MediaBuilder setDefaultImageSubPath(String subPath) {
        imageSubPath = subPath;
        return this;
    }

    /**
     * 设置视频子目录地址
     * @param subPath 子目录地址
     * @return 新的父路径
     */
    public MediaBuilder setDefaultVideoSubPath(String subPath) {
        if (TextUtils.isEmpty(subPath)) {
            return this;
        }
        videoSubPath = subPath;
        return this;
    }

    /**
     * 是否保存到公共目录
     * true代表保存到公共目录
     */
    public MediaBuilder setSavePublic(boolean isPublic) {
        this.savePublicPath = isPublic;
        return this;
    }

    public MediaBuilder setMediaListener(MediaListener mediaListener) {
        this.mediaListener = mediaListener;
        return this;
    }

    public MediaBuilder setChooseType(int chooseType) {
        this.chooseType = chooseType;
        return this;
    }

    public MediaBuilder setWaterMark(String waterMark) {
        this.waterMark = waterMark;
        return this;
    }

    public String getWaterMark() {
        return waterMark;
    }

    public String getImageSubPath() {
        return imageSubPath;
    }

    public String getVideoSubPath() {
        return videoSubPath;
    }

    public boolean isSavePublicPath() {
        return savePublicPath;
    }


    public String getImageOutPutPath() {
        if (savePublicPath) {
            if (TextUtils.isEmpty(imageSubPath)) {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + MediaUtil.getDefaultBasePath(mActivity) + File.separator + "image";
            } else {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + imageSubPath;
            }
        } else {
            if (TextUtils.isEmpty(imageSubPath)) {
                return mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "image";
            } else {
                return mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imageSubPath;
            }
        }
    }

    public String getVideoOutPutPath() {
        if (savePublicPath) {
            if (TextUtils.isEmpty(videoSubPath)) {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + MediaUtil.getDefaultBasePath(mActivity) + File.separator + "video";
            } else {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + imageSubPath;
            }
        } else {
            if (TextUtils.isEmpty(videoSubPath)) {
                return mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "video";
            } else {
                return mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + videoSubPath;
            }
        }
    }

    /**
     * 图片压缩质量
     *
     * @param imageQualityCompress 单位大小kb,默认200kb
     * @return this
     */
    public MediaBuilder setImageQualityCompress(int imageQualityCompress) {
        this.imageQualityCompress = imageQualityCompress;
        return this;
    }

    /**
     * 相册最大选择数量
     *
     * @param imageMaxSelectedCount 最大选择的图片数量，最多9张
     * @return this
     */
    public MediaBuilder setImageMaxSelectedCount(int imageMaxSelectedCount) {
        this.imageMaxSelectedCount = imageMaxSelectedCount;
        return this;
    }

    /**
     * 相册最大选择数量
     *
     * @param videoMaxSelectedCount 最大选择的图片数量，最多9张
     * @return this
     */
    public MediaBuilder setVideoMaxSelectedCount(int videoMaxSelectedCount) {
        this.videoMaxSelectedCount = videoMaxSelectedCount;
        return this;
    }

    public MediaBuilder setMaxVideoTime(int maxVideoTime) {
        this.maxVideoTime = maxVideoTime;
        return this;
    }

    public MediaBuilder setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
        return this;
    }

    public boolean isShowLoading() {
        return showLoading;
    }

    public MediaBuilder setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        return this;
    }

    public int getMaxVideoTime() {
        return maxVideoTime;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public int getImageMaxSelectedCount() {
        return imageMaxSelectedCount;
    }

    public int getImageQualityCompress() {
        return imageQualityCompress;
    }

    public MediaListener getMediaListener() {
        return mediaListener;
    }

    public int getChooseType() {
        return chooseType;
    }

    public int getVideoMaxSelectedCount() {
        return videoMaxSelectedCount;
    }

    public ComponentActivity getActivity() {
        return mActivity;
    }

    public MediaHelper builder() {
        return new MediaHelper(this);
    }

}
