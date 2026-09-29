package io.coderf.arklab.common.widget.gallery.gestures;

/**
 * 图片预览的拖动、惯性滑动和缩放手势回调。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public interface OnGestureListener {

    /**
     * 单指拖动图片。
     *
     * @param dx 相对上一次触摸点的水平位移，向右为正
     * @param dy 相对上一次触摸点的垂直位移，向下为正
     */
    void onDrag(float dx, float dy);

    /**
     * 手指快速滑动后松手，开始惯性滚动。
     *
     * @param startX    松手时的 X 坐标
     * @param startY    松手时的 Y 坐标
     * @param velocityX 水平速度，向右为正，单位像素/秒
     * @param velocityY 垂直速度，向下为正，单位像素/秒
     */
    void onFling(float startX, float startY, float velocityX, float velocityY);

    /**
     * 双指缩放图片。
     *
     * @param scaleFactor 相对当前缩放的增量倍数，大于 1 放大，小于 1 缩小
     * @param focusX      缩放中心的 X 坐标
     * @param focusY      缩放中心的 Y 坐标
     */
    void onScale(float scaleFactor, float focusX, float focusY);

}