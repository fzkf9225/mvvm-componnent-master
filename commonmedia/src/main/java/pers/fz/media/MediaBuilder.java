package pers.fz.media;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pers.fz.media.listener.MediaListener;
import pers.fz.media.listener.OnLoadingListener;

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
    private OnLoadingListener onLoadingListener;
    private final ComponentActivity mActivity;

    private Context mContext;
    private String waterMark;
    private String imageOutPutPath;
    private String videoOutPutPath;
    private Fragment fragment;

    private MediaListener mediaListener;
    private int chooseType = MediaHelper.DEFAULT_TYPE;
    public MediaBuilder(@NotNull ComponentActivity mActivity) {
        this.mActivity = mActivity;
        setContext(this.mActivity);
        String basePath = MediaUtil.getDefaultBasePath(mActivity);
        imageOutPutPath = mActivity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() +
                File.separator + basePath;
        videoOutPutPath = mActivity.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES).getAbsolutePath() +
                File.separator + basePath;
    }

    public MediaBuilder(@NotNull Fragment fragment) {
        this.mActivity = fragment.getActivity();
        this.fragment = fragment;
        setContext(this.fragment.getContext());
        String basePath = MediaUtil.getDefaultBasePath(fragment.requireContext());
        imageOutPutPath = mActivity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() +
                File.separator + basePath;
        videoOutPutPath = mActivity.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES).getAbsolutePath() +
                File.separator + basePath;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public String getImageOutPutPath() {
        return imageOutPutPath;
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
     * 图片自定义输出路径，暂不支持，仅支持内部存储目录即私有目录，媒体文件目录，Android 10+在Picture下，10之前可以自定义
     *
     * @param imageOutPutPath 自定义输出路径，默认是/Pictures/OutPutPath最后一节/image/下
     */
    public MediaBuilder setImageOutPutPath(String imageOutPutPath) {
        this.imageOutPutPath = imageOutPutPath;
        return this;
    }

    public String getVideoOutPutPath() {
        return videoOutPutPath;
    }

    public MediaBuilder setMediaListener(MediaListener mediaListener) {
        this.mediaListener = mediaListener;
        return this;
    }

    /**
     * 视频自定义输出路径，暂不支持，仅支持内部存储目录即私有目录,媒体文件目录，Android 10+在Picture下，10之前可以自定义
     *
     * @param videoOutPutPath 自定义输出路径，默认是/Pictures/OutPutPath最后一节/video/下
     */
    public MediaBuilder setVideoOutPutPath(String videoOutPutPath) {
        this.videoOutPutPath = videoOutPutPath;
        return this;
    }

    public MediaBuilder setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
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

    public OnLoadingListener getOnLoadingListener() {
        return onLoadingListener;
    }

    public ComponentActivity getActivity() {
        return mActivity;
    }

    public MediaHelper builder() {
        return new MediaHelper(this);
    }

}
