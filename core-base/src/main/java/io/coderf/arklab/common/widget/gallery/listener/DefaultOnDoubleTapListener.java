package io.coderf.arklab.common.widget.gallery.listener;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.widget.gallery.attacher.PhotoViewAttacher;

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overriden with custom behavior, if needed
 * <p>&nbsp;</p>
 * To be used via {@link uk.co.senab.photoview.PhotoViewAttacher#( GestureDetector.OnDoubleTapListener)}
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

    /**
     * 当前绑定的图片缩放控制器
     */
    private PhotoViewAttacher photoViewAttacher;

    /**
     * Default constructor
     *
     * @param photoViewAttacher PhotoViewAttacher to bind to
     */
    public DefaultOnDoubleTapListener(PhotoViewAttacher photoViewAttacher) {
        setPhotoViewAttacher(photoViewAttacher);
    }

    /**
     * Allows to change PhotoViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacher PhotoViewAttacher to bind to
     */
    public void setPhotoViewAttacher(PhotoViewAttacher newPhotoViewAttacher) {
        this.photoViewAttacher = newPhotoViewAttacher;
    }

    /**
     * 确认单击。点在图片上回调图片点击，否则回调整块视图点击
     *
     * @param event 单击事件
     * @return 点击被图片或视图监听消费时返回 true
     */
    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
        if (this.photoViewAttacher == null) {
            return false;
        }
        ShapeableImageView imageView = photoViewAttacher.getImageView();
        if (null != photoViewAttacher.getOnPhotoTapListener()) {
            final RectF displayRect = photoViewAttacher.getDisplayRect();

            if (null != displayRect) {
                final float x = event.getX(), y = event.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    photoViewAttacher.getOnPhotoTapListener().onPhotoTap(imageView, xResult, yResult);
                    return true;
                }
            }
        }
        if (null != photoViewAttacher.getOnViewTapListener()) {
            photoViewAttacher.getOnViewTapListener().onViewTap(imageView, event.getX(), event.getY());
        }
        return false;
    }

    /**
     * 双击在最小和中等缩放之间切换，并以点击位置为缩放中心
     *
     * @param event 双击事件
     * @return 已处理时返回 true
     */
    @Override
    public boolean onDoubleTap(@NonNull MotionEvent event) {
        if (photoViewAttacher == null) {
            return false;
        }

        float scale = photoViewAttacher.getScale();
        float x = event.getX();
        float y = event.getY();

        if (scale < photoViewAttacher.getMediumScale()) {
            photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
        } else {
            photoViewAttacher.setScale(photoViewAttacher.getMinimumScale(), x, y, true);
        }
        return true;
    }

    /**
     * 双击过程中的中间事件不处理，等确认后的 {@link #onDoubleTap}
     *
     * @param event 双击过程事件
     */
    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent event) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

}

