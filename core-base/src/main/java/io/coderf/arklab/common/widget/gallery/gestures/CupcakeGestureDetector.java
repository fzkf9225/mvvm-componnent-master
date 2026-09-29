package io.coderf.arklab.common.widget.gallery.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;

/**
 * 单指拖动和惯性滑动检测。缩放由子类补充。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class CupcakeGestureDetector {

    /**
     * 拖动、惯性滑动回调
     */
    protected OnGestureListener mListener;
    /**
     * 上一次有效触摸点的 X 坐标
     */
    float mLastTouchX;
    /**
     * 上一次有效触摸点的 Y 坐标
     */
    float mLastTouchY;
    /**
     * 判定为拖动所需的最小位移
     */
    final float mTouchSlop;
    /**
     * 触发惯性滑动的最小速度，单位像素/秒
     */
    final float mMinimumVelocity;

    /**
     * 设置手势结果回调
     *
     * @param listener 拖动和惯性滑动监听
     */
    public void setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
    }

    /**
     * 读取系统触摸阈值，初始化拖动和惯性滑动判定
     *
     * @param context 用于读取 ViewConfiguration
     */
    public CupcakeGestureDetector(Context context) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    /**
     * 速度追踪，用于松手时判断是否触发惯性滑动
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 本次手势是否已经超过拖动阈值
     */
    private boolean mIsDragging;

    /**
     * 当前参与拖动的触点 X 坐标
     *
     * @param ev 触摸事件
     */
    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    /**
     * 当前参与拖动的触点 Y 坐标
     *
     * @param ev 触摸事件
     */
    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }

    /**
     * 当前是否正在双指缩放。基类不处理缩放，固定返回 false
     */
    public boolean isScaling() {
        return false;
    }

    /**
     * 处理按下、移动、抬起和取消。超过触摸阈值后回调拖动，速度足够时回调惯性滑动
     *
     * @param ev 触摸事件
     * @return 事件已被消费时返回 true
     */
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                } else {
                    LogUtil.logger(PreviewPhotoDialog.TAG, "Velocity tracker is null");
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    mListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX,
                                    -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
        return true;
    }

}
