package pers.fz.media.videocompressor;

/**
 * created by fz on 2024/11/14 9:12
 * describe:
 */
public interface CompressListener {
    void onStart();

    void onSuccess();

    void onFail();

    void onProgress(float percent);
}
