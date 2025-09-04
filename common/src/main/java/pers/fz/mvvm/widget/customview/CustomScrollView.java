package pers.fz.mvvm.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by fz on 2017/12/28.
 * 下拉回弹
 */

public class CustomScrollView extends ScrollView {

    private View inner;
    private float y;
    private final Rect normal = new Rect();
    private static final int SIZE = 3;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 获得第一个view
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (inner == null) {
            return super.onTouchEvent(ev);
        } else {
            commOnTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 添加手势响应事件
     * @param ev
     */
    public void commOnTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN -> y = ev.getY();
            case MotionEvent.ACTION_UP -> {
                if (isNeedAnimation()) {
                    animation();
                }
            }
            case MotionEvent.ACTION_MOVE -> {
                final float preY = y;
                float nowY = ev.getY();
                int deltaY = (int) (preY - nowY) / SIZE;
                y = nowY;
                // 当滚动到最上或者最下时就不会再滚动，这时移动布局
                if (isNeedMove()) {
                    if (normal.isEmpty()) {
                        // 保存正常的布局位置
                        normal.set(inner.getLeft(), inner.getTop(),
                                inner.getRight(), inner.getBottom());
                        return;
                    }
                    //这里移动布局
                    inner.layout(inner.getLeft(), inner.getTop() - deltaY, inner.getRight(),
                            inner.getBottom() - deltaY);
                }
            }
            default -> {}
        }
    }

    // 开启动画移动
    public void animation() {
        // 开启移动动画
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop() - normal.top, 0);
        ta.setDuration(200);
        inner.startAnimation(ta);
        // 设置回到正常的布局位置
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        normal.setEmpty();
    }

    // 是否需要开启动画
    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    // 是否需要移动布局
    public boolean isNeedMove() {
        int offset = inner.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        return scrollY == 0 || scrollY == offset;
    }
}
