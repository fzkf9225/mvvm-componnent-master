package io.coderf.arklab.common.widget.gallery.scrollerproxy;

import android.content.Context;
import android.widget.OverScroller;

/**
 * 基于 {@link OverScroller} 的惯性滑动实现。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class GingerScroller extends ScrollerProxy {

    /**
     * 系统惯性滑动计算器
     */
    protected final OverScroller mScroller;
    /**
     * 首次计算需要额外推进一帧，避免起点被跳过
     */
    private boolean mFirstScroll = false;

    /**
     * @param context 用于创建 OverScroller
     */
    public GingerScroller(Context context) {
        mScroller = new OverScroller(context);
    }

    /**
     * 推进一帧。第一次调用会多计算一次，保证当前位置从起点开始
     */
    @Override
    public boolean computeScrollOffset() {
        if (mFirstScroll) {
            mScroller.computeScrollOffset();
            mFirstScroll = false;
        }
        return mScroller.computeScrollOffset();
    }

    /**
     * 参数含义见 {@link ScrollerProxy#fling}
     */
    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY,
                      int overX, int overY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
    }

    @Override
    public void forceFinished(boolean finished) {
        mScroller.forceFinished(finished);
    }

    @Override
    public boolean isFinished() {
        return mScroller.isFinished();
    }

    @Override
    public int getCurrX() {
        return mScroller.getCurrX();
    }

    @Override
    public int getCurrY() {
        return mScroller.getCurrY();
    }

}