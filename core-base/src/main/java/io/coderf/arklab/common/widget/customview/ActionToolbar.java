package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import io.coderf.arklab.core.utils.DensityUtil;

/**
 * 支持自定义内容区高度的 MaterialToolbar。
 * 标题走控件自带 title / titleCentered，与官方 Top App Bar 一致。
 * 非居中时官方只把标题放在起始边，靠结束边要在 {@link #onLayout} 之后再平移。
 * 自定义高度时把返回键和菜单按钮限制在内容区内垂直居中。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class ActionToolbar extends MaterialToolbar {

    private int contentHeightPx = -1;

    /**
     * 标题要贴的水平边：{@link Gravity#LEFT}、{@link Gravity#RIGHT}，
     * 或 {@link Gravity#NO_GRAVITY} 表示不额外平移。
     */
    private int titleAbsoluteGravity = Gravity.NO_GRAVITY;

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

    /**
     * 指定标题贴左还是贴右。
     * <p>
     * {@link MaterialToolbar} 只提供 {@code titleCentered}。关掉居中后，
     * {@code onLayout} 一律把标题放在起始边，改 LayoutParams.gravity 不会生效。
     * 传 {@link Gravity#RIGHT} 时在布局结束后把标题平移到结束边（菜单左侧）。
     * 传 {@link Gravity#LEFT} 或 {@link Gravity#NO_GRAVITY} 则不再平移。
     * </p>
     */
    @SuppressLint("RtlHardcoded")
    public void setTitleAbsoluteHorizontalGravity(int absoluteHorizontalGravity) {
        int next = absoluteHorizontalGravity == Gravity.LEFT
                || absoluteHorizontalGravity == Gravity.RIGHT
                ? absoluteHorizontalGravity
                : Gravity.NO_GRAVITY;
        if (titleAbsoluteGravity == next) {
            return;
        }
        titleAbsoluteGravity = next;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (titleAbsoluteGravity == Gravity.RIGHT) {
            layoutTitleAgainstEdge(true);
        } else if (titleAbsoluteGravity == Gravity.LEFT
                && getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            layoutTitleAgainstEdge(false);
        }
    }

    /**
     * 把标题 TextView 贴到可用区域的右缘或左缘，避开返回键和菜单。
     *
     * @param toEnd {@code true} 贴右缘，{@code false} 贴左缘
     */
    private void layoutTitleAgainstEdge(boolean toEnd) {
        TextView titleView = findTitleView();
        if (titleView == null || titleView.getVisibility() == GONE || titleView.getWidth() <= 0) {
            return;
        }
        int leftBound = getPaddingLeft();
        int rightBound = getWidth() - getPaddingRight();
        int titleLeft = titleView.getLeft();
        int titleRight = titleView.getRight();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == titleView || child.getVisibility() == GONE || child.getWidth() <= 0) {
                continue;
            }
            if (child.getRight() <= titleLeft) {
                leftBound = Math.max(leftBound, child.getRight());
            } else if (child.getLeft() >= titleRight) {
                rightBound = Math.min(rightBound, child.getLeft());
            }
        }
        int gap = DensityUtil.dp2px(getContext(), 16f);
        int room = rightBound - leftBound;
        if (titleView.getWidth() + gap <= room) {
            if (toEnd) {
                rightBound -= gap;
            } else {
                leftBound += gap;
            }
        }
        int targetLeft = toEnd
                ? rightBound - titleView.getWidth()
                : leftBound;
        if (targetLeft < leftBound) {
            targetLeft = leftBound;
        }
        int dx = targetLeft - titleView.getLeft();
        if (dx != 0) {
            titleView.offsetLeftAndRight(dx);
        }
    }

    @Nullable
    private TextView findTitleView() {
        CharSequence title = getTitle();
        TextView fallback = null;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child instanceof TextView) || child instanceof MaterialButton) {
                continue;
            }
            TextView textView = (TextView) child;
            if (fallback == null) {
                fallback = textView;
            }
            if (title == null || title.equals(textView.getText())) {
                return textView;
            }
        }
        return fallback;
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
