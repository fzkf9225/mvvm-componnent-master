package io.coderf.arklab.common.widget.gallery.scrollerproxy;

import android.content.Context;

/**
 * 惯性滑动计算器，屏蔽不同系统版本的 Scroller 差异。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public abstract class ScrollerProxy {

    /**
     * 创建当前系统可用的惯性滑动实现
     *
     * @param context 用于创建 OverScroller
     */
    public static ScrollerProxy getScroller(Context context) {
        return new GingerScroller(context);
    }

    /**
     * 推进一帧惯性滑动
     *
     * @return 仍在滑动时返回 true
     */
    public abstract boolean computeScrollOffset();

    /**
     * 从起点按速度开始惯性滑动，并限制在给定边界内
     *
     * @param startX    起点 X
     * @param startY    起点 Y
     * @param velocityX 水平速度
     * @param velocityY 垂直速度
     * @param minX      X 方向最小边界
     * @param maxX      X 方向最大边界
     * @param minY      Y 方向最小边界
     * @param maxY      Y 方向最大边界
     * @param overX     X 方向允许越过边界的距离
     * @param overY     Y 方向允许越过边界的距离
     */
    public abstract void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY,
                               int maxY, int overX, int overY);

    /**
     * 立即结束惯性滑动
     *
     * @param finished true 表示标记为已结束
     */
    public abstract void forceFinished(boolean finished);

    /**
     * 惯性滑动是否已经结束
     */
    public abstract boolean isFinished();

    /**
     * 当前惯性滑动的 X 坐标
     */
    public abstract int getCurrX();

    /**
     * 当前惯性滑动的 Y 坐标
     */
    public abstract int getCurrY();


}
