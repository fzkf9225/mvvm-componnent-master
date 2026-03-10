package com.casic.otitan.common.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.casic.otitan.common.R;

/**
 * 箭头分割线
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/10 15:35
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration{
    private Paint mPaint;
    private int mDividerWidth = 1;  // 分割线宽度（水平方向时使用）
    private int mDividerHeight = 1; // 分割线高度（垂直方向时使用）
    private final int mOrientation; // 列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private boolean isShowLastDivider = true; // 是否展示最后一行的分隔符
    private Drawable mDividerDrawable; // 分割线形状
    private Shape mShape; // 自定义形状
    private boolean isShapeDivider = false; // 是否使用形状分割线

    // 分割线绘制位置常量
    public static final int DIVIDER_CENTER = 0;      // 居中
    public static final int DIVIDER_START = 1;       // 靠开始
    public static final int DIVIDER_END = 2;         // 靠结束
    private int mDividerGravity = DIVIDER_CENTER;    // 分割线绘制位置

    // 形状类型
    public static final int SHAPE_CIRCLE = 0;        // 圆形
    public static final int SHAPE_RECTANGLE = 1;     // 矩形
    public static final int SHAPE_OVAL = 2;          // 椭圆形
    public static final int SHAPE_LINE = 3;          // 线条（默认）

    /**
     * 默认分割线：高度为1px，颜色为灰色
     *
     * @param context
     * @param orientation 列表方向
     */
    public DividerItemDecoration(Context context, int orientation) {
        this(context, orientation, 1, ContextCompat.getColor(context, R.color.h_line_color));
    }

    public DividerItemDecoration(Context context, int orientation, boolean isShowLastDivider) {
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
    public DividerItemDecoration(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        mDividerHeight = dividerHeight;
        mDividerWidth = dividerHeight; // 默认宽度等于高度
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
        isShapeDivider = false;
    }

    public DividerItemDecoration(Context context, int orientation, int dividerHeight, @ColorInt int dividerColor, boolean isShowLastDivider) {
        this(context, orientation, dividerHeight, dividerColor);
        this.isShowLastDivider = isShowLastDivider;
    }

    /**
     * 使用形状作为分割线
     *
     * @param context
     * @param orientation   列表方向
     * @param shapeType     形状类型 (SHAPE_CIRCLE, SHAPE_RECTANGLE, SHAPE_OVAL)
     * @param shapeSize     形状大小 (宽高相同)
     * @param shapeColor    形状颜色
     */
    public DividerItemDecoration(Context context, int orientation, int shapeType, int shapeSize, @ColorInt int shapeColor) {
        this(context, orientation, shapeType, shapeSize, shapeSize, shapeColor, DIVIDER_CENTER);
    }

    /**
     * 使用形状作为分割线，可自定义宽高
     *
     * @param context
     * @param orientation   列表方向
     * @param shapeType     形状类型 (SHAPE_CIRCLE, SHAPE_RECTANGLE, SHAPE_OVAL)
     * @param shapeWidth    形状宽度
     * @param shapeHeight   形状高度
     * @param shapeColor    形状颜色
     */
    public DividerItemDecoration(Context context, int orientation, int shapeType, int shapeWidth, int shapeHeight, @ColorInt int shapeColor) {
        this(context, orientation, shapeType, shapeWidth, shapeHeight, shapeColor, DIVIDER_CENTER);
    }

    /**
     * 使用形状作为分割线，可自定义位置
     *
     * @param context
     * @param orientation    列表方向
     * @param shapeType      形状类型 (SHAPE_CIRCLE, SHAPE_RECTANGLE, SHAPE_OVAL)
     * @param shapeWidth     形状宽度
     * @param shapeHeight    形状高度
     * @param shapeColor     形状颜色
     * @param dividerGravity 分割线位置 (DIVIDER_CENTER, DIVIDER_START, DIVIDER_END)
     */
    public DividerItemDecoration(Context context, int orientation, int shapeType, int shapeWidth, int shapeHeight,
                                 @ColorInt int shapeColor, int dividerGravity) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        mDividerWidth = shapeWidth;
        mDividerHeight = shapeHeight;
        mDividerGravity = dividerGravity;

        // 创建形状Drawable
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        Paint paint = shapeDrawable.getPaint();
        paint.setColor(shapeColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        // 根据类型设置形状
        switch (shapeType) {
            case SHAPE_CIRCLE:
                // 圆形特殊处理，因为ShapeDrawable没有直接的圆形，使用Oval但设置宽高相等
                shapeDrawable.setShape(new android.graphics.drawable.shapes.OvalShape());
                break;
            case SHAPE_RECTANGLE:
                shapeDrawable.setShape(new android.graphics.drawable.shapes.RectShape());
                break;
            case SHAPE_OVAL:
                shapeDrawable.setShape(new android.graphics.drawable.shapes.OvalShape());
                break;
            case SHAPE_LINE:
            default:
                // 线条模式，退回到普通分割线
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setColor(shapeColor);
                mPaint.setStyle(Paint.Style.FILL);
                isShapeDivider = false;
                return;
        }

        mDividerDrawable = shapeDrawable;
        isShapeDivider = true;
    }

    /**
     * 使用自定义Drawable作为分割线
     *
     * @param orientation 列表方向
     * @param drawable    自定义Drawable
     */
    public DividerItemDecoration(int orientation, Drawable drawable) {
        this(orientation, drawable, DIVIDER_CENTER);
    }

    /**
     * 使用自定义Drawable作为分割线，可指定位置
     *
     * @param orientation    列表方向
     * @param drawable       自定义Drawable
     * @param dividerGravity 分割线位置
     */
    public DividerItemDecoration(int orientation, Drawable drawable, int dividerGravity) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        mDividerDrawable = drawable;
        mDividerGravity = dividerGravity;
        mDividerWidth = drawable.getIntrinsicWidth();
        mDividerHeight = drawable.getIntrinsicHeight();
        isShapeDivider = true;
    }

    // 获取分割线尺寸
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();

        // 最后一项不添加分割线空间
        if (!isShowLastDivider && position == itemCount - 1) {
            return;
        }

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            // 垂直方向：在底部添加空间
            outRect.set(0, 0, 0, mDividerHeight);
        } else {
            // 水平方向：在右侧添加空间
            outRect.set(0, 0, mDividerWidth, 0);
        }
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

    // 绘制横向 item 分割线（水平列表，分割线在右侧）
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int childSize = parent.getChildCount();
        int itemCount = isShowLastDivider ? childSize : (childSize - 1);

        for (int i = 0; i < (Math.max(itemCount, 0)); i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            // 计算分割线的位置
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerWidth;

            if (isShapeDivider && mDividerDrawable != null) {
                // 绘制形状分割线
                int top, bottom;

                // 根据重力设置垂直位置
                if (mDividerGravity == DIVIDER_START) {
                    top = child.getTop() - layoutParams.topMargin;
                    bottom = top + mDividerHeight;
                } else if (mDividerGravity == DIVIDER_END) {
                    bottom = child.getBottom() + layoutParams.bottomMargin;
                    top = bottom - mDividerHeight;
                } else {
                    // 居中
                    int centerY = (child.getTop() - layoutParams.topMargin + child.getBottom() + layoutParams.bottomMargin) / 2;
                    top = centerY - mDividerHeight / 2;
                    bottom = centerY + mDividerHeight / 2;
                }

                mDividerDrawable.setBounds(left, top, right, bottom);
                mDividerDrawable.draw(canvas);
            } else if (mPaint != null) {
                // 绘制线条分割线
                final int top = parent.getPaddingTop();
                final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    // 绘制纵向 item 分割线（垂直列表，分割线在底部）
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int childSize = parent.getChildCount();
        int itemCount = isShowLastDivider ? childSize : (childSize - 1);

        for (int i = 0; i < Math.max(itemCount, 0); i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            // 计算分割线的位置
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;

            if (isShapeDivider && mDividerDrawable != null) {
                // 绘制形状分割线
                int left, right;

                // 根据重力设置水平位置
                if (mDividerGravity == DIVIDER_START) {
                    left = child.getLeft() - layoutParams.leftMargin;
                    right = left + mDividerWidth;
                } else if (mDividerGravity == DIVIDER_END) {
                    right = child.getRight() + layoutParams.rightMargin;
                    left = right - mDividerWidth;
                } else {
                    // 居中
                    int centerX = (child.getLeft() - layoutParams.leftMargin + child.getRight() + layoutParams.rightMargin) / 2;
                    left = centerX - mDividerWidth / 2;
                    right = centerX + mDividerWidth / 2;
                }

                mDividerDrawable.setBounds(left, top, right, bottom);
                mDividerDrawable.draw(canvas);
            } else if (mPaint != null) {
                // 绘制线条分割线
                final int left = parent.getPaddingLeft();
                final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    // 设置是否显示最后一个分割线
    public void setShowLastDivider(boolean showLastDivider) {
        isShowLastDivider = showLastDivider;
    }

    // 设置分割线重力位置
    public void setDividerGravity(int gravity) {
        this.mDividerGravity = gravity;
    }
}

