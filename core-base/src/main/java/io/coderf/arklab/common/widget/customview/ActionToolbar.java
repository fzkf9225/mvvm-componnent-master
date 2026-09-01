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

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 支持自定义内容区高度的 MaterialToolbar。
 * 自定义标题按整条 Toolbar 水平居中（不被返回键把视觉中心挤偏），过长时在两侧控件之间省略。
 */
public class ActionToolbar extends MaterialToolbar {

    private int contentHeightPx = -1;
    private final int titleGapPx;

    public ActionToolbar(@NonNull Context context) {
        this(context, null);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.toolbarStyle);
    }

    public ActionToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        titleGapPx = DensityUtil.dp2px(context, 8f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (findViewById(R.id.tv_title) == null) {
            return;
        }
        setContentInsetsRelative(0, 0);
        setContentInsetStartWithNavigation(0);
        setContentInsetEndWithActions(0);
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
        View title = findViewById(R.id.tv_title);
        if (title != null && title.getVisibility() != GONE) {
            int maxTitleWidth = Math.max(0, getMeasuredWidth() - resolveSideReserve() - titleGapPx * 2);
            if (maxTitleWidth > 0 && title.getMeasuredWidth() > maxTitleWidth) {
                int heightSpec = MeasureSpec.makeMeasureSpec(title.getMeasuredHeight(), MeasureSpec.EXACTLY);
                title.measure(MeasureSpec.makeMeasureSpec(maxTitleWidth, MeasureSpec.EXACTLY), heightSpec);
            }
        }
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutTitle();
    }

    /**
     * 标题相对 Toolbar 全宽居中；START/END 则贴在返回键/菜单内侧。
     */
    private void layoutTitle() {
        View title = findViewById(R.id.tv_title);
        if (title == null || title.getVisibility() == GONE) {
            return;
        }
        int leftLimit = getPaddingLeft() + titleGapPx;
        int rightLimit = getWidth() - getPaddingRight() - titleGapPx;
        int mid = getWidth() / 2;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == title || child.getVisibility() == GONE) {
                continue;
            }
            int childMid = (child.getLeft() + child.getRight()) / 2;
            if (childMid < mid) {
                leftLimit = Math.max(leftLimit, child.getRight() + titleGapPx);
            } else {
                rightLimit = Math.min(rightLimit, child.getLeft() - titleGapPx);
            }
        }
        int titleWidth = title.getMeasuredWidth();
        int titleHeight = title.getMeasuredHeight();
        int gravity = Gravity.CENTER;
        ViewGroup.LayoutParams lp = title.getLayoutParams();
        if (lp instanceof LayoutParams toolbarLp) {
            gravity = toolbarLp.gravity;
        }
        int hgrav = Gravity.getAbsoluteGravity(gravity, getLayoutDirection())
                & Gravity.HORIZONTAL_GRAVITY_MASK;
        int titleLeft;
        if (hgrav == Gravity.LEFT) {
            titleLeft = leftLimit;
        } else if (hgrav == Gravity.RIGHT) {
            titleLeft = rightLimit - titleWidth;
        } else {
            titleLeft = (getWidth() - titleWidth) / 2;
            if (titleLeft < leftLimit) {
                titleLeft = leftLimit;
            }
            if (titleLeft + titleWidth > rightLimit) {
                titleLeft = Math.max(leftLimit, rightLimit - titleWidth);
            }
        }
        int titleTop = title.getTop();
        title.layout(titleLeft, titleTop, titleLeft + titleWidth, titleTop + titleHeight);
    }

    /** 测量阶段预估左右系统控件占用，给标题省略号留宽。 */
    private int resolveSideReserve() {
        int leftReserve = getPaddingLeft();
        int rightReserve = getPaddingRight();
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.tv_title || child.getVisibility() == GONE) {
                continue;
            }
            int w = child.getMeasuredWidth();
            if (child instanceof ActionMenuView) {
                if (rtl) {
                    leftReserve += w;
                } else {
                    rightReserve += w;
                }
                continue;
            }
            if (!isSystemButtonChild(child)) {
                continue;
            }
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int hgrav = Gravity.getAbsoluteGravity(
                    lp instanceof LayoutParams toolbarLp ? toolbarLp.gravity : Gravity.NO_GRAVITY,
                    getLayoutDirection()
            ) & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (hgrav == Gravity.LEFT) {
                leftReserve += w;
            } else if (hgrav == Gravity.RIGHT) {
                rightReserve += w;
            }
        }
        return leftReserve + rightReserve;
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
