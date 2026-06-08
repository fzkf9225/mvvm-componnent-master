package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

/**
 * 支持自定义内容区高度的 Toolbar，用于 Edge-to-Edge 场景下让返回键/菜单在任意高度内垂直居中。
 */
public class ActionToolbar extends Toolbar {

    private int contentHeightPx = -1;

    public ActionToolbar(@NonNull Context context) {
        super(context);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置 Toolbar 内容区高度（不含状态栏 padding）。
     * 传入 {@code <= 0} 时恢复系统默认按钮尺寸。
     */
    public void setContentHeightPx(int heightPx) {
        if (contentHeightPx == heightPx) {
            return;
        }
        contentHeightPx = heightPx;
        setMinimumHeight(Math.max(heightPx, 0));
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (contentHeightPx <= 0) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!isSystemButtonChild(child) || child.getMeasuredHeight() <= contentHeightPx) {
                continue;
            }
            int widthSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(contentHeightPx, MeasureSpec.EXACTLY);
            child.measure(widthSpec, heightSpec);
        }
    }

    @SuppressLint("RtlHardcoded")
    private boolean isSystemButtonChild(View child) {
        if (child instanceof ActionMenuView) {
            return true;
        }
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (!(lp instanceof LayoutParams)) {
            return false;
        }
        int horizontalGravity = Gravity.getAbsoluteGravity(
                ((LayoutParams) lp).gravity,
                getLayoutDirection()
        ) & Gravity.HORIZONTAL_GRAVITY_MASK;
        return horizontalGravity == Gravity.LEFT || horizontalGravity == Gravity.RIGHT;
    }
}
