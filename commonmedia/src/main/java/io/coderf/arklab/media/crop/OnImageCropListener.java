package io.coderf.arklab.media.crop;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * 图片裁剪结果回调。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public interface OnImageCropListener {

    /**
     * 裁剪成功
     *
     * @param outputFile 裁剪后的本地文件
     * @param outputUri  裁剪后的 Uri（优先 FileProvider，失败时回退 file Uri）
     */
    void onCropSuccess(@NonNull File outputFile, @NonNull Uri outputUri);

    /**
     * 用户取消裁剪
     */
    void onCropCancel();

    /**
     * 裁剪失败
     *
     * @param message 错误信息，可能为空
     * @param throwable 异常对象，可能为空
     */
    void onCropError(@Nullable String message, @Nullable Throwable throwable);
}
