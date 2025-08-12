package pers.fz.media.compressor.video;

/**
 * created by fz on 2024/11/14 9:12
 * describe:
 */
public interface CompressListener {
    void onStart();

    void onResult(boolean isSuccess,String message);

    void onProgress(float percent);
}
