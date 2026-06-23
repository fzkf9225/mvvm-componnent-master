package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.coderf.arklab.common.R;

/**
 * 线性列表（{@link LinearLayoutManager}）分割线装饰器。
 * <p>
 * 「无需传颜色」的 API 仅通过 {@link #getItemOffsets} 占位，不绘制可见分割线，也不创建 Paint，
 * 性能优于传 {@code 0x00000000} 后仍走绘制逻辑的旧写法。
 * </p>
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * // 1. 仅占位间距，不绘制分割线（无需传颜色，推荐）
 * recyclerView.addItemDecoration(
 *     new RecycleViewDivider(context, LinearLayoutManager.VERTICAL, dividerHeight));
 *
 * // 2. 仅占位 + 控制最后一项是否保留间距（无需传颜色）
 * recyclerView.addItemDecoration(
 *     new RecycleViewDivider(context, orientation, dividerHeight, false));
 *
 * // 3. 默认 1px + 主题分割线颜色（历史 API，会绘制）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.VERTICAL));
 *
 * // 4. 自定义高度与颜色（历史 API；传透明色时仅占位、跳过绘制）
 * recyclerView.addItemDecoration(
 *     new RecycleViewDivider(context, orientation, dividerHeight, dividerColor));
 * </pre>
 * </p>
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mDividerHeight = 1;
    private final int mOrientation;
    private boolean isShowLastDivider = true;

    /** 是否执行 onDraw；仅占位模式为 false，跳过 Canvas 绘制以提升性能 */
    private final boolean mDrawDivider;

    /**
     * 默认分割线：高度 1px，颜色为主题色 {@link R.color#h_line_color}（历史 API，会绘制）。
     */
    public RecycleViewDivider(Context context, int orientation) {
        this(context, orientation, 1, ContextCompat.getColor(context, R.color.h_line_color));
    }

    /**
     * 默认分割线 + 控制最后一项是否绘制分割线（历史 API，会绘制）。
     */
    public RecycleViewDivider(Context context, int orientation, boolean isShowLastDivider) {
        this(context, orientation);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 仅保留 item 间距，不绘制可见分割线（无需传颜色）。
     * <p>
     * 等价于 {@code new RecycleViewDivider(context, orientation, dividerHeight, Color.TRANSPARENT)}，
     * 但不创建 Paint、不进入 onDraw，性能更好。
     * </p>
     *
     * @param context       上下文（与历史 API 签名保持一致，本构造中不使用）
     * @param orientation   列表方向
     * @param dividerHeight 占位高度（px）
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight) {
        validateOrientation(orientation);
        mOrientation = orientation;
        mDividerHeight = dividerHeight;
        mDrawDivider = false;
        mPaint = null;
    }

    /**
     * 仅保留 item 间距，不绘制可见分割线，并可控制最后一项是否占位（无需传颜色）。
     *
     * @param context             上下文（与历史 API 签名保持一致，本构造中不使用）
     * @param orientation         列表方向
     * @param dividerHeight       占位高度（px）
     * @param isShowLastDivider   最后一项之后是否保留占位
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 自定义分割线高度与颜色（历史 API）。
     * <p>
     * 传透明色（如 {@code 0x00000000}）时行为与仅占位一致，但会跳过 onDraw，兼容旧代码。
     * </p>
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
        } else {
            mPaint = null;
        }
    }

    /**
     * 自定义分割线高度、颜色，并控制最后一项是否绘制/占位（历史 API）。
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, dividerColor);
        this.isShowLastDivider = isShowLastDivider;
    }

    private static void validateOrientation(int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
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

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (shouldSkipDividerForChild(parent, child)) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (shouldSkipDividerForChild(parent, child)) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /** 与 {@link #isShowLastDivider} 一致：最后一项之后不绘制分割线 */
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
