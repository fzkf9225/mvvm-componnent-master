package pers.fz.media;

import android.content.Context;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.media.listener.OnDialogInterfaceClickListener;
import pers.fz.media.utils.MediaUtil;

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
    private VideoQualityEnum videoQuality = VideoQualityEnum.MEDIUM;
    /**
     * lifecycle绑定
     */
    private LifecycleOwner lifecycleOwner;
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
     * 最大文图片和视频选择数量，同时选择图片和视频
     */
    public int mediaMaxSelectedCount = MediaHelper.DEFAULT_MEDIA_MAX_COUNT;
    /**
     * 图片压缩大小限制，默认200，单位kb
     */
    public int imageQualityCompress = 200;

    private Context mContext;
    /**
     * 水印文字
     */
    private String waterMark;
    /**
     * 文件格式，默认所有，只在file类型选择时才有效
     */
    private String[] fileType;
    /**
     * 图片格式，默认所有，只在image类型选择时才有效
     */
    private String[] imageType;
    /**
     * 图片格式和视频，默认所有，只在image/video两种类型选择时才有效
     */
    private String[] mediaType;
    /**
     * 音频格式，默认所有，只在音频类型选择时才有效
     */
    private String[] audioType;
    /**
     * 视频格式，默认所有，只在video类型选择时才有效
     */
    private String[] videoType;
    /**
     * 是否将拍摄的图片和视频保存到公共目录，默认false
     */
    private boolean savePublicPath = true;
    /**
     * 是否显示加载dialog
     */
    private boolean showLoading = true;
    /**
     * 图片子目录
     */
    private String imageSubPath;
    /**
     * 视频子目录
     */
    private String videoSubPath;
    /**
     * 监听
     */
    private MediaListener mediaListener;
    /**
     * 是否展示请求权限前的dialog
     */
    private boolean isShowPermissionDialog = true;
    /**
     * 请求权限前的dialog确认按钮文字颜色
     */
    private String permissionPositiveText = "前往授权";
    /**
     * 请求权限前的dialog确认按钮文字颜色
     */
    private String permissionNegativeText = "取消";
    /**
     * 请求权限前的dialog提示文字，优先级高于permissionMessage
     */
    private SpannableString permissionSpannableContent;
    /**
     * 请求权限前的dialog提示文字
     */
    private String permissionMessage = "接下来需要您同意相机、读取相册、录音等权限，以便拍照、录像和选择文件，我们承诺此次授权权限仅用于当前功能，不会涉及隐私安全。";
    /**
     * 请求权限前的dialog确认按钮点击事件
     */
    private OnDialogInterfaceClickListener onPermissionPositiveClickListener;
    /**
     * 请求权限前的dialog取消按钮点击事件
     */
    private OnDialogInterfaceClickListener onPermissionNegativeClickListener;
    /**
     * 请求权限前的dialog确认按钮文字颜色
     */
    private @ColorInt int permissionPositiveTextColor = 0xFF333333;
    /**
     * 请求权限前的dialog取消按钮文字颜色
     */
    private @ColorInt int permissionNegativeTextColor = 0xFF666666;
    /**
     * 选择器类型，默认为DEFAULT，枚举MediaPickerTypeEnum
     */
    private MediaPickerTypeEnum chooseType = MediaPickerTypeEnum.DEFAULT;

    public MediaBuilder(@NotNull Context context) {
        this.mContext = context;
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

    /**
     * 是否展示请求权限前的dialog
     * @return true展示，默认展示
     */
    public boolean isShowPermissionDialog() {
        return isShowPermissionDialog;
    }

    /**
     * 设置是否展示请求权限前的dialog
     * @param showPermissionDialog true展示，默认展示
     * @return MediaBuilder
     */
    public MediaBuilder setShowPermissionDialog(boolean showPermissionDialog) {
        isShowPermissionDialog = showPermissionDialog;
        return this;
    }

    /**
     * 获取请求权限前的dialog取消按钮文字内容
     * @return 确认按钮文字内容
     */
    public String getPermissionNegativeText() {
        return permissionNegativeText;
    }

    /**
     * 设置请求权限前的dialog确认按钮文字内容
     * @param permissionNegativeText 确认按钮文字内容
     * @return MediaBuilder
     */
    public MediaBuilder setPermissionNegativeText(String permissionNegativeText) {
        this.permissionNegativeText = permissionNegativeText;
        return this;
    }

    /**
     * 获取请求权限前的dialog确认按钮文字内容
     * @return 确认按钮文字内容
     */
    public String getPermissionPositiveText() {
        return permissionPositiveText;
    }

    /**
     * 获取请求权限前的dialog确认按钮文字内容
     * @param permissionPositiveText 确认按钮文字内容
     * @return MediaBuilder
     */
    public MediaBuilder setPermissionPositiveText(String permissionPositiveText) {
        this.permissionPositiveText = permissionPositiveText;
        return this;
    }

    /**
     * 获取请求权限前的dialog提示内容，优先级高于permissionMessage
     * @return 确认按钮文字颜色
     */
    public SpannableString getPermissionSpannableContent() {
        return permissionSpannableContent;
    }

    /**
     * 设置请求权限前的dialog提示内容，优先级高于permissionMessage
     * @param permissionSpannableContent 确认按钮文字颜色
     * @return MediaBuilder
     */
    public MediaBuilder setPermissionSpannableContent(SpannableString permissionSpannableContent) {
        this.permissionSpannableContent = permissionSpannableContent;
        return this;
    }

    /**
     * 获取请求权限前的dialog提示文字，优先级低于permissionSpannableContent
     * @return 确认按钮文字颜色
     */
    public String getPermissionMessage() {
        return permissionMessage;
    }

    /**
     * 设置请求权限前的dialog提示文字，优先级低于permissionSpannableContent
     * @param permissionMessage 确认按钮文字颜色
     * @return MediaBuilder
     */
    public MediaBuilder setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    /**
     * 获取请求权限前的dialog确认按钮点击事件
     * @return 确认按钮点击事件
     */
    public OnDialogInterfaceClickListener getOnPermissionPositiveClickListener() {
        return onPermissionPositiveClickListener;
    }

    /**
     * 设置请求权限前的dialog确认按钮点击事件
     * @param onPermissionPositiveClickListener 点击事件
     * @return this
     */
    public MediaBuilder setOnPermissionPositiveClickListener(OnDialogInterfaceClickListener onPermissionPositiveClickListener) {
        this.onPermissionPositiveClickListener = onPermissionPositiveClickListener;
        return this;
    }

    /**
     * 获取请求权限前的dialog取消按钮点击事件
     * @return 取消按钮点击事件
     */
    public OnDialogInterfaceClickListener getOnPermissionNegativeClickListener() {
        return onPermissionNegativeClickListener;
    }

    /**
     * 设置请求权限前的dialog取消按钮点击事件
     * @param onPermissionNegativeClickListener 点击事件
     * @return this
     */
    public MediaBuilder setOnPermissionNegativeClickListener(OnDialogInterfaceClickListener onPermissionNegativeClickListener) {
        this.onPermissionNegativeClickListener = onPermissionNegativeClickListener;
        return this;
    }

    /**
     * 获取请求权限前的dialog确认按钮文字颜色
     * @return 确认按钮文字颜色
     */
    public @ColorInt int getPermissionPositiveTextColor() {
        return permissionPositiveTextColor;
    }

    /**
     * 获取请求权限前的dialog确认按钮文字颜色
     */
    public MediaBuilder setPermissionPositiveTextColor(@ColorInt int permissionPositiveTextColor) {
        this.permissionPositiveTextColor = permissionPositiveTextColor;
        return this;
    }

    /**
     * 获取请求权限前的dialog取消按钮文字颜色
     * @return 取消按钮文字颜色
     */
    public @ColorInt int getPermissionNegativeTextColor() {
        return permissionNegativeTextColor;
    }

    /**
     * 设置请求权限前的dialog取消按钮文字颜色
     * @param permissionNegativeTextColor 确认按钮文字颜色
     * @return this
     */
    public MediaBuilder setPermissionNegativeTextColor(@ColorInt int permissionNegativeTextColor) {
        this.permissionNegativeTextColor = permissionNegativeTextColor;
        return this;
    }

    /**
     * 最大可选文件数量
     * @return 最大可选文件数量
     */
    public int getFileMaxSelectedCount() {
        return fileMaxSelectedCount;
    }

    /**
     * 设置最大可选文件数量
     * @param fileMaxSelectedCount 最大可选文件数量
     * @return this
     */
    public MediaBuilder setFileMaxSelectedCount(int fileMaxSelectedCount) {
        this.fileMaxSelectedCount = fileMaxSelectedCount;
        return this;
    }

    /**
     * 最大可选图片、视频数量
     * @return 最大可选图片、视频数量
     */
    public int getMediaMaxSelectedCount() {
        return mediaMaxSelectedCount;
    }

    /**
     * 设置最大可选图片、视频数量
     * @param mediaMaxSelectedCount 最大可选图片、视频数量
     * @return this
     */
    public MediaBuilder setMediaMaxSelectedCount(int mediaMaxSelectedCount) {
        this.mediaMaxSelectedCount = mediaMaxSelectedCount;
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
     * 文件选择器的文件类型
     * @return 数组
     */
    public String[] getFileType() {
        return fileType;
    }

    /**
     * 设置文件类型，file选择时有效
     * @param fileType 默认为所有
     * @return this
     */
    public MediaBuilder setFileType(String[] fileType) {
        this.fileType = fileType;
        return this;
    }

    /**
     * 音频选择器的文件类型
     * @return 数组
     */
    public String[] getAudioType() {
        return audioType;
    }

    /**
     * 设置音频类型，audio选择时有效
     * @param audioType 默认为所有
     * @return this
     */
    public MediaBuilder setAudioType(String[] audioType) {
        this.audioType = audioType;
        return this;
    }

    /**
     * 视频选择器的文件类型
     * @return 数组
     */
    public String[] getVideoType() {
        return videoType;
    }

    /**
     * 设置视频类型，video选择时有效
     * @param videoType 默认为所有
     * @return this
     */
    public MediaBuilder setVideoType(String[] videoType) {
        this.videoType = videoType;
        return this;
    }

    /**
     * 图片选择器的文件类型
     * @return 数组
     */
    public String[] getImageType() {
        return imageType;
    }

    /**
     * 设置图片类型，image选择时有效
     * @param imageType 默认为所有
     * @return this
     */
    public MediaBuilder setImageType(String[] imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * 图片和视频选择器的文件类型
     * @return 数组
     */
    public String[] getMediaType() {
        return mediaType;
    }

    /**
     * 媒体选择器的文件类型。图片和视频的类型
     * @param mediaType 默认为所有
     * @return this
     */
    public MediaBuilder setMediaType(String[] mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * 绑定生命周期
     * @param lifecycleOwner LifeCycle对象
     * @return this
     */
    public MediaBuilder bindLifeCycle(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
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

    /**
     * 设置监听
     * @param mediaListener 监听
     * @return this
     */
    public MediaBuilder setMediaListener(MediaListener mediaListener) {
        this.mediaListener = mediaListener;
        return this;
    }

    /**
     * 选择类型，枚举MediaPickerTypeEnum
     * @param chooseType 默认为0，PICK_TYPE-1 DEFAULT_TYPE-0(默认的选择传统选择器)
     * @return this
     */
    public MediaBuilder setChooseType(MediaPickerTypeEnum chooseType) {
        this.chooseType = chooseType;
        return this;
    }

    /**
     * 设置水印文字
     * @param waterMark 水印文字
     * @return this
     */
    public MediaBuilder setWaterMark(String waterMark) {
        this.waterMark = waterMark;
        return this;
    }

    /**
     * 水印文字
     * @return 水印文字
     */
    public String getWaterMark() {
        return waterMark;
    }

    /**
     * 图片子目录，示例：/casic/image
     * @return 子目录
     */
    public String getImageSubPath() {
        return imageSubPath;
    }

    /**
     * 视频子目录，示例：/casic/video
     * @return 子目录
     */
    public String getVideoSubPath() {
        return videoSubPath;
    }

    /**
     * 是否保存到公共目录
     * @return true代表保存到公共目录
     */
    public boolean isSavePublicPath() {
        return savePublicPath;
    }

    /**
     * 图片输出目录，根据设置的savePublicPath和imageSubPath等拼接输出付目录
     * @return 图片输出目录
     */
    public String getImageOutPutPath() {
        if (savePublicPath) {
            if (TextUtils.isEmpty(imageSubPath)) {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + MediaUtil.getDefaultBasePath(mContext) + File.separator + "image";
            } else {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + imageSubPath;
            }
        } else {
            if (TextUtils.isEmpty(imageSubPath)) {
                return mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "image";
            } else {
                return mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imageSubPath;
            }
        }
    }

    /**
     * 视频输出目录，根据设置的savePublicPath和videoSubPath等拼接输出付目录
     * @return 视频输出目录
     */
    public String getVideoOutPutPath() {
        if (savePublicPath) {
            if (TextUtils.isEmpty(videoSubPath)) {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + MediaUtil.getDefaultBasePath(mContext) + File.separator + "video";
            } else {
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + imageSubPath;
            }
        } else {
            if (TextUtils.isEmpty(videoSubPath)) {
                return mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "video";
            } else {
                return mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + videoSubPath;
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

    /**
     * 视频最大时长
     * @param maxVideoTime 单位秒
     * @return  this
     */
    public MediaBuilder setMaxVideoTime(int maxVideoTime) {
        this.maxVideoTime = maxVideoTime;
        return this;
    }

    /**
     * 视频压缩质量，枚举VideoQualityEnum
     * @param videoQuality 低质量-1，中质量-2，高质量-3
     * @return this
     */
    public MediaBuilder setVideoQuality(VideoQualityEnum videoQuality) {
        this.videoQuality = videoQuality;
        return this;
    }

    /**
     * 是否显示加载框
     * @return this
     */
    public boolean isShowLoading() {
        return showLoading;
    }

    /**
     * 是否显示加载框
     * @param showLoading true-显示
     * @return this
     */
    public MediaBuilder setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        return this;
    }

    /**
     * 获取LifeCycle
     * @return LifecycleOwner
     */
    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }

    /**
     * 获取视频最大时长
     * @return 单位描
     */
    public int getMaxVideoTime() {
        return maxVideoTime;
    }

    /**
     * 获取视频压缩质量，低质量-1，中质量-2，高质量-3
     * @return 视频质量
     */
    public VideoQualityEnum getVideoQuality() {
        return videoQuality;
    }

    /**
     * 获取图片最大选择数量
     * @return 图片最大选择数量
     */
    public int getImageMaxSelectedCount() {
        return imageMaxSelectedCount;
    }

    /**
     * 获取图片压缩质量，单位kb
     * @return 图片压缩质量
     */
    public int getImageQualityCompress() {
        return imageQualityCompress;
    }

    /**
     * 获取图片选择监听
     * @return 图片选择监听
     */
    public MediaListener getMediaListener() {
        return mediaListener;
    }

    /**
     * 获取选择的类型
     * @return 选择类型
     */
    public MediaPickerTypeEnum getChooseType() {
        return chooseType;
    }

    /**
     * 获取视频最大选择数量
     * @return 视频最大选择数量
     */
    public int getVideoMaxSelectedCount() {
        return videoMaxSelectedCount;
    }

    /**
     * 创建MediaHelper对象
     */
    public MediaHelper builder() {
        if (lifecycleOwner == null) {
            throw new RuntimeException("please bind lifecycle");
        }
        return new MediaHelper(this);
    }

}
