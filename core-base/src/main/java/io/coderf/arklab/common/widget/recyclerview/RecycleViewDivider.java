package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 线性列表（{@link LinearLayoutManager}）分割线装饰器。
 * <p>
 * 「无需传颜色」的 API 仅通过 {@link #getItemOffsets} 占位，不绘制可见分割线，也不创建 Paint。
 * </p>
 * <p>
 * <b>左右缩进（仅绘制模式）：</b>默认分割线仍与 item 同宽。可通过
 * {@link #setHorizontalInset(int, int)} / {@link #setVerticalInset(int, int)} 让中间色条左右（或上下）空出一段；
 * 空出区域用 {@link #setSideColor(int)} / {@link #setSideColors(int, int)} 填充，避免透出列表背景。
 * 未设置缩进时行为与历史版本一致。
 * </p>
 * <pre>
 * // 全宽分割线（默认）
 * recyclerView.addItemDecoration(
 *     new RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 1, color));
 *
 * // 左右各空 16px，两侧填 surface，中间为分割线色
 * recyclerView.addItemDecoration(
 *     new RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 1, dividerColor)
 *         .setHorizontalInset(16, 16)
 *         .setSideColor(ThemeAttrs.surface(context)));
 * </pre>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/21
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mDividerHeight = 1;
    private final int mOrientation;
    private boolean isShowLastDivider = true;

    /** 是否执行 onDraw；仅占位模式为 false */
    private final boolean mDrawDivider;

    // ---------- 缩进与两侧填充（默认 0 / 透明 = 与历史全宽一致） ----------
    /** 垂直列表时：分割线左侧缩进；水平列表时：上侧缩进 */
    private int mInsetStartPx = 0;
    /** 垂直列表时：分割线右侧缩进；水平列表时：下侧缩进 */
    private int mInsetEndPx = 0;
    @ColorInt
    private int mSideStartColor = Color.TRANSPARENT;
    @ColorInt
    private int mSideEndColor = Color.TRANSPARENT;
    private final Paint mSidePaint;

    /**
     * 默认分割线：高度 1px，颜色为主题 outlineVariant（会绘制）。
     */
    public RecycleViewDivider(Context context, int orientation) {
        this(context, orientation, 1, ThemeAttrs.outlineVariant(context));
    }

    /**
     * 默认分割线 + 控制最后一项是否绘制分割线。
     */
    public RecycleViewDivider(Context context, int orientation, boolean isShowLastDivider) {
        this(context, orientation);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 仅保留 item 间距，不绘制可见分割线（无需传颜色）。
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight) {
        validateOrientation(orientation);
        mOrientation = orientation;
        mDividerHeight = dividerHeight;
        mDrawDivider = false;
        mPaint = null;
        mSidePaint = null;
    }

    /**
     * 仅保留 item 间距，不绘制可见分割线，并可控制最后一项是否占位。
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 自定义分割线高度与颜色。
     * 传透明色时仅占位、跳过绘制。
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor) {
        validateOrientation(orientation);
        mOrientation = orientation;
        mDividerHeight = dividerHeight;
        mDrawDivider = Color.alpha(dividerColor) != 0;
        if (mDrawDivider) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(dividerColor);
            mPaint.setStyle(Paint.Style.FILL);
            mSidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSidePaint.setStyle(Paint.Style.FILL);
        } else {
            mPaint = null;
            mSidePaint = null;
        }
    }

    /**
     * 自定义分割线高度、颜色，并控制最后一项是否绘制/占位。
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight,
                              @ColorInt int dividerColor, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, dividerColor);
        this.isShowLastDivider = isShowLastDivider;
    }

    // ==================== 扩展：缩进与两侧色 ====================

    /**
     * 垂直列表（画横线）时的左右缩进（px）。
     * 默认 0,0 即全宽。缩进区域内请配合 {@link #setSideColor(int)} 填充，否则透出列表背景。
     */
    @NonNull
    public RecycleViewDivider setHorizontalInset(int leftPx, int rightPx) {
        this.mInsetStartPx = Math.max(0, leftPx);
        this.mInsetEndPx = Math.max(0, rightPx);
        return this;
    }

    /**
     * 水平列表（画竖线）时的上下缩进（px）。
     */
    @NonNull
    public RecycleViewDivider setVerticalInset(int topPx, int bottomPx) {
        this.mInsetStartPx = Math.max(0, topPx);
        this.mInsetEndPx = Math.max(0, bottomPx);
        return this;
    }

    /**
     * 两侧缩进区域使用同一填充色（不透明色才会绘制）。
     */
    @NonNull
    public RecycleViewDivider setSideColor(@ColorInt int color) {
        this.mSideStartColor = color;
        this.mSideEndColor = color;
        return this;
    }

    /**
     * 分别设置左（上）侧、右（下）侧缩进区域填充色。
     */
    @NonNull
    public RecycleViewDivider setSideColors(@ColorInt int startColor, @ColorInt int endColor) {
        this.mSideStartColor = startColor;
        this.mSideEndColor = endColor;
        return this;
    }

    private static void validateOrientation(int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int adapterPos = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        boolean isLastItem = itemCount > 0 && adapterPos == itemCount - 1;
        boolean skipInsetForLast = !isShowLastDivider && isLastItem;
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            int bottom = skipInsetForLast ? 0 : mDividerHeight;
            outRect.set(0, 0, 0, bottom);
        } else {
            int right = skipInsetForLast ? 0 : mDividerHeight;
            outRect.set(0, 0, right, 0);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (!mDrawDivider || mPaint == null) {
            return;
        }
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawHorizontal(c, parent);
        } else {
            drawVertical(c, parent);
        }
    }

    /** 垂直列表：在 item 下方画横线 */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int contentLeft = parent.getPaddingLeft();
        final int contentRight = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (shouldSkipDividerForChild(parent, child)) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            drawSegmentedRect(canvas, contentLeft, top, contentRight, bottom, true);
        }
    }

    /** 水平列表：在 item 右侧画竖线 */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int contentTop = parent.getPaddingTop();
        final int contentBottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (shouldSkipDividerForChild(parent, child)) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            drawSegmentedRect(canvas, left, contentTop, right, contentBottom, false);
        }
    }

    /**
     * @param horizontalTrack true：横条（沿 X 分左/中/右）；false：竖条（沿 Y 分上/中/下）
     */
    private void drawSegmentedRect(Canvas canvas, int left, int top, int right, int bottom,
                                   boolean horizontalTrack) {
        int startInset = mInsetStartPx;
        int endInset = mInsetEndPx;
        if (horizontalTrack) {
            int width = right - left;
            if (startInset + endInset >= width) {
                // 缩进过大时退回全宽分割线色，避免无中间条
                canvas.drawRect(left, top, right, bottom, mPaint);
                return;
            }
            int midLeft = left + startInset;
            int midRight = right - endInset;
            if (startInset > 0 && Color.alpha(mSideStartColor) != 0 && mSidePaint != null) {
                mSidePaint.setColor(mSideStartColor);
                canvas.drawRect(left, top, midLeft, bottom, mSidePaint);
            }
            canvas.drawRect(midLeft, top, midRight, bottom, mPaint);
            if (endInset > 0 && Color.alpha(mSideEndColor) != 0 && mSidePaint != null) {
                mSidePaint.setColor(mSideEndColor);
                canvas.drawRect(midRight, top, right, bottom, mSidePaint);
            }
        } else {
            int height = bottom - top;
            if (startInset + endInset >= height) {
                canvas.drawRect(left, top, right, bottom, mPaint);
                return;
            }
            int midTop = top + startInset;
            int midBottom = bottom - endInset;
            if (startInset > 0 && Color.alpha(mSideStartColor) != 0 && mSidePaint != null) {
                mSidePaint.setColor(mSideStartColor);
                canvas.drawRect(left, top, right, midTop, mSidePaint);
            }
            canvas.drawRect(left, midTop, right, midBottom, mPaint);
            if (endInset > 0 && Color.alpha(mSideEndColor) != 0 && mSidePaint != null) {
                mSidePaint.setColor(mSideEndColor);
                canvas.drawRect(left, midBottom, right, bottom, mSidePaint);
            }
        }
    }

    private boolean shouldSkipDividerForChild(RecyclerView parent, View child) {
        if (isShowLastDivider) {
            return false;
        }
        int pos = parent.getChildAdapterPosition(child);
        if (pos == RecyclerView.NO_POSITION) {
            return true;
        }
        int count = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        return count > 0 && pos == count - 1;
    }
}
