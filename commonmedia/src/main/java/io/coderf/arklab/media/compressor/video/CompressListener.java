package io.coderf.arklab.media.compressor.video;

/**
 * CompressListener 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/14 9:12
 */
public interface CompressListener {
    void onStart();

    void onResult(boolean isSuccess,String message);

    void onProgress(float percent);
}
