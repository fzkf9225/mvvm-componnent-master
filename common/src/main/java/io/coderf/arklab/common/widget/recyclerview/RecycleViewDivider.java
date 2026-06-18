package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
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
 * <b>使用示例：</b>
 * <pre>
 * // 1. 默认高度 1px + 主题分割线颜色（无需传颜色）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.VERTICAL));
 *
 * // 2. 自定义高度 + 主题分割线颜色（无需传颜色，推荐替代手动取 R.color.h_line_color）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.VERTICAL, dividerHeight));
 *
 * // 3. 自定义高度 + 是否显示最后一项分割线 + 主题颜色（无需传颜色）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, orientation, dividerHeight, false));
 *
 * // 4. 自定义高度与颜色（兼容历史用法）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, orientation, dividerHeight, dividerColor));
 *
 * // 5. 自定义高度、颜色及最后一项分割线（兼容历史用法）
 * recyclerView.addItemDecoration(new RecycleViewDivider(context, orientation, dividerHeight, dividerColor, false));
 * </pre>
 * </p>
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mDividerHeight = 1; // 分割线高度，默认为1px
    private final int mOrientation; // 列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private boolean isShowLastDivider = true; // 是否展示最后一行的分隔符

    /**
     * 默认分割线：高度为 1px，颜色为主题色 {@link R.color#h_line_color}（无需传颜色）。
     *
     * @param context     上下文，用于读取主题分割线颜色
     * @param orientation 列表方向，{@link LinearLayoutManager#VERTICAL} 或 {@link LinearLayoutManager#HORIZONTAL}
     */
    public RecycleViewDivider(Context context, int orientation) {
        this(context, orientation, 1, ContextCompat.getColor(context, R.color.h_line_color));
    }

    /**
     * 默认分割线 + 控制最后一项是否绘制分割线（无需传颜色）。
     *
     * @param context             上下文
     * @param orientation         列表方向
     * @param isShowLastDivider     是否在最后一项之后绘制分割线
     */
    public RecycleViewDivider(Context context, int orientation, boolean isShowLastDivider) {
        this(context, orientation);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 自定义分割线高度，颜色使用主题色 {@link R.color#h_line_color}（无需传颜色）。
     *
     * @param context       上下文
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度（px）
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight) {
        this(context, orientation, dividerHeight, ContextCompat.getColor(context, R.color.h_line_color));
    }

    /**
     * 自定义分割线高度，颜色使用主题色，并可控制最后一项是否绘制分割线（无需传颜色）。
     *
     * @param context             上下文
     * @param orientation         列表方向
     * @param dividerHeight       分割线高度（px）
     * @param isShowLastDivider   是否在最后一项之后绘制分割线
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, ContextCompat.getColor(context, R.color.h_line_color), isShowLastDivider);
    }

    /**
     * 自定义分割线高度与颜色（兼容历史 API）。
     *
     * @param context       上下文
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度（px）
     * @param dividerColor  分割线颜色
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 自定义分割线高度、颜色，并控制最后一项是否绘制分割线（兼容历史 API）。
     *
     * @param context             上下文
     * @param orientation         列表方向
     * @param dividerHeight       分割线高度（px）
     * @param dividerColor        分割线颜色
     * @param isShowLastDivider   是否在最后一项之后绘制分割线
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, dividerColor);
        this.isShowLastDivider = isShowLastDivider;
    }

    // 获取分割线尺寸
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

    // 绘制分割线
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        // 与 LinearLayoutManager 方向一致：纵向列表在 item 底边画「横线」；横向列表在 item 右侧画「竖线」
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawHorizontal(c, parent);
        } else {
            drawVertical(c, parent);
        }
    }

    // 绘制横向 item 分割线
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

    // 绘制纵向 item 分割线
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
