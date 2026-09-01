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

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 支持自定义内容区高度的 MaterialToolbar。
 * 标题走控件自带 title / titleCentered，与官方 Top App Bar 一致；
 * 自定义高度时把返回键和菜单按钮限制在内容区内垂直居中。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class ActionToolbar extends MaterialToolbar {

    private int contentHeightPx = -1;

    public ActionToolbar(@NonNull Context context) {
        this(context, null);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置 MaterialToolbar 内容区高度（不含状态栏 padding）。
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
