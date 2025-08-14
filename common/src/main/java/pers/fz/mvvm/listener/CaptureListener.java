package pers.fz.mvvm.listener;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface CaptureListener {
    void takePictures();

    void recordShort(long time);

    void recordStart();

    void recordEnd(long time);

    void recordZoom(float zoom);

    void recordError();
}
