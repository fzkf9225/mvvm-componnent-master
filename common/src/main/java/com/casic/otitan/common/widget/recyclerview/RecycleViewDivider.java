package com.casic.otitan.common.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleViewDivider extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mDividerHeight = 1; // 分割线高度，默认为1px
    private final int mOrientation; // 列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private boolean isShowLastDivider = true; // 是否展示最后一行的分隔符

    /**
     * 默认分割线：高度为1px，颜色为灰色
     *
     * @param context
     * @param orientation 列表方向
     */
    public RecycleViewDivider(Context context, int orientation) {
        this(context, orientation, 1, context.getResources().getColor(android.R.color.darker_gray));
    }

    public RecycleViewDivider(Context context, int orientation, boolean isShowLastDivider) {
        this(context, orientation);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
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

    public RecycleViewDivider(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, dividerColor);
        this.isShowLastDivider = isShowLastDivider;
    }

    // 获取分割线尺寸
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
    }

    // 绘制分割线
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    // 绘制横向 item 分割线
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        int itemCount = isShowLastDivider ? childSize : (childSize - 1);
        for (int i = 0; i < (Math.max(itemCount, 0)); i++) {
            final View child = parent.getChildAt(i);
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
        int itemCount = isShowLastDivider ? childSize : (childSize - 1);
        for (int i = 0; i < Math.max(itemCount, 0); i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }
}
