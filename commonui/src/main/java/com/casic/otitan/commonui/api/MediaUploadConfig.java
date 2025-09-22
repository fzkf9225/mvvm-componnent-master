package com.casic.otitan.commonui.api;

/**
 * created by fz on 2025/9/11 14:37
 * describe:文件自动上传配置，目前只支持图片视频
 */
public class MediaUploadConfig {
    /**
     * 文件上传服务
     */
    protected FileApiService fileApiService;
    /**
     * 数量标签控件
     */
    protected String uploadUrl = null;

    private MediaUploadConfig() {
    }

    private static final class InstanceHolder {
        private static final MediaUploadConfig instance = new MediaUploadConfig();
    }

    public static MediaUploadConfig getInstance() {
        return InstanceHolder.instance;
    }
    /**
     * 设置文件上传接口服务
     * @param fileApiService 代理
     */
    public MediaUploadConfig setFileApiService(FileApiService fileApiService) {
        this.fileApiService = fileApiService;
        return this;
    }

    /**
     * 获取文件上传接口服务
     * @return 代理
     */
    public FileApiService getFileApiService() {
        return fileApiService;
    }
    /**
     * 设置视频自动上传接口地址
     * @param uploadUrl 上传接口地址，相对地址
     */
    public MediaUploadConfig setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
        return this;
    }

    /**
     * 获取视频自动上传接口地址
     * @return 上传接口地址
     */
    public String getUploadUrl() {
        return uploadUrl;
    }
}

