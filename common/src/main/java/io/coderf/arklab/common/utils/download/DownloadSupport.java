package io.coderf.arklab.common.utils.download;

import io.coderf.arklab.common.base.BaseException;

import java.util.List;

/**
 * 下载/更新公共前置校验（包内使用）。
 * <p>
 * 权限相关逻辑已迁移至 {@link DownloadPermissionHelper}。
 */
final class DownloadSupport {

    private DownloadSupport() {
    }

    /** @throws BaseException {@link BaseException.ErrorType#DOWNLOAD_URL_404} URL 为空 */
    static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BaseException(BaseException.ErrorType.DOWNLOAD_URL_404);
        }
    }

    /**
     * @throws BaseException {@link BaseException.ErrorType#DOWNLOADING_ERROR} 同一 URL 已在下载中
     */
    static void checkRepeatDownload(List<String> downloadingUrls, String url, boolean verifyRepeatDownload) {
        if (verifyRepeatDownload && downloadingUrls.contains(url)) {
            throw new BaseException(BaseException.ErrorType.DOWNLOADING_ERROR);
        }
    }
}
