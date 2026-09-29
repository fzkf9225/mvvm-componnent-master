package io.coderf.arklab.common.widget.gallery.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

/**
 * 在单指拖动之上增加双指缩放，并在多指切换时保持当前拖动触点。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class EclairGestureDetector extends CupcakeGestureDetector {

    /**
     * 无效触点编号
     */
    private static final int INVALID_POINTER_ID = -1;
    /**
     * 当前用于拖动的触点编号
     */
    private int mActivePointerId = INVALID_POINTER_ID;
    /**
     * 当前拖动触点在事件中的下标
     */
    private int mActivePointerIndex = 0;

    /**
     * 系统缩放手势检测器
     */
    protected final ScaleGestureDetector mDetector;

    /**
     * 创建缩放检测器，并把缩放中心转交给 {@link OnGestureListener#onScale}
     *
     * @param context 用于创建 ScaleGestureDetector
     */
    public EclairGestureDetector(Context context) {
        super(context);
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }

                mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                return true;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                // NO-OP
            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    /**
     * 取当前拖动触点的 X 坐标。触点已失效时退回第一个触点
     *
     * @param ev 触摸事件
     */
    @Override
    float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    /**
     * 取当前拖动触点的 Y 坐标。触点已失效时退回第一个触点
     *
     * @param ev 触摸事件
     */
    @Override
    float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    /**
     * 双指缩放是否仍在进行
     */
    @Override
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    /**
     * 先交给缩放检测器，再维护当前拖动触点，最后交给父类处理拖动
     *
     * @param ev 触摸事件
     * @return 事件已被消费时返回 true
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN -> mActivePointerId = ev.getPointerId(0);
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                    mActivePointerId = INVALID_POINTER_ID;
            case MotionEvent.ACTION_POINTER_UP -> {
                // Ignore deprecation, ACTION_POINTER_ID_MASK and
                // ACTION_POINTER_ID_SHIFT has same value and are deprecated
                // You can have either deprecation or lint target api warning
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
            }
            default -> {}
        }

        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return super.onTouchEvent(ev);
    }


}
