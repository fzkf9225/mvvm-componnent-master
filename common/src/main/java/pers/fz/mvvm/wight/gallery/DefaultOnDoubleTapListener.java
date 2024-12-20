package pers.fz.mvvm.wight.gallery;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.util.log.LogUtil;

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overriden with custom behavior, if needed
 * <p>&nbsp;</p>
 * To be used via {@link uk.co.senab.photoview.PhotoViewAttacher#( GestureDetector.OnDoubleTapListener)}
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {

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

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
        if (this.photoViewAttacher == null) {
            return false;
        }
        try {
            ImageView imageView = photoViewAttacher.getImageView();
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
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(PreviewPhotoDialog.TAG, "onSingleTapConfirmed:" + e);
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent event) {
        if (photoViewAttacher == null) {
            return false;
        }

        try {
            float scale = photoViewAttacher.getScale();
            float x = event.getX();
            float y = event.getY();

            if (scale < photoViewAttacher.getMediumScale()) {
                photoViewAttacher.setScale(photoViewAttacher.getMediumScale(), x, y, true);
            } else {
                photoViewAttacher.setScale(photoViewAttacher.getMinimumScale(), x, y, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            LogUtil.e(PreviewPhotoDialog.TAG, "onDoubleTap:" + e);
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

}
