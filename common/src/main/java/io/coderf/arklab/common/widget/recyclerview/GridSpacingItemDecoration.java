package io.coderf.arklab.common.widget.recyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 网格布局 Item 间距装饰器，支持绘制分割线并分配 item 间距。
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * // 1. 仅设置间距，不绘制分割线（无需传颜色，推荐替代 0x00000000 写法）
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(context, 8)));
 *
 * // 2. 横纵间距不同，且不绘制分割线（静态工厂，避免与两参数 color 构造冲突）
 * recyclerView.addItemDecoration(GridSpacingItemDecoration.spacingOnly(columnGap, rowGap));
 *
 * // 3. 间距 + 可见分割线（兼容历史用法）
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(gap, Color.parseColor("#EEEEEE")));
 *
 * // 4. 横纵间距不同 + 可见分割线
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnGap, rowGap, dividerColor));
 * </pre>
 * </p>
 *
 * @author fz
 * @since 2018/9/25
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    /** 列与列之间的间距（影响 left / right offset 及垂直分割线宽度） */
    private final int mHorizontalSpacing;

    /** 行与行之间的间距（影响 bottom offset 及水平分割线高度） */
    private final int mVerticalSpacing;

    /** 是否绘制可见分割线；为 false 时仅通过 getItemOffsets 保留间距 */
    private final boolean mDrawDivider;

    private final Paint mPaint;

    /**
     * 仅设置 item 间距，不绘制可见分割线（无需传颜色）。
     * <p>
     * 等价于 {@code new GridSpacingItemDecoration(dividerWidth, 0x00000000)}，但语义更清晰。
     * </p>
     *
     * @param dividerWidth 横向、纵向共用的间距（px）
     */
    public GridSpacingItemDecoration(int dividerWidth) {
        this(dividerWidth, dividerWidth, false);
    }

    /**
     * 分别指定横向、纵向间距，不绘制可见分割线（无需传颜色）。
     * <p>
     * 使用静态工厂而非双参数构造，是为了与 {@link #GridSpacingItemDecoration(int, int)}（间距 + 颜色）
     * 的签名区分，避免编译歧义。
     * </p>
     *
     * @param horizontalSpacing 列间距（px）
     * @param verticalSpacing   行间距（px）
     */
    public static GridSpacingItemDecoration spacingOnly(int horizontalSpacing, int verticalSpacing) {
        return new GridSpacingItemDecoration(horizontalSpacing, verticalSpacing, false);
    }

    /**
     * 使用相同横纵间距创建装饰器，并绘制可见分割线（兼容旧版 API）。
     *
     * @param dividerWidth 横向、纵向共用的间距（px）
     * @param color        分割线颜色；传透明色时仅保留间距、不显示分割线
     */
    public GridSpacingItemDecoration(int dividerWidth, @ColorInt int color) {
        this(dividerWidth, dividerWidth, color);
    }

    /**
     * 分别指定横向、纵向间距，并绘制可见分割线。
     *
     * @param horizontalSpacing 列间距（px），对应 item 的 left / right offset
     * @param verticalSpacing   行间距（px），对应 item 的 bottom offset
     * @param color             分割线颜色；传透明色时仅保留间距、不显示分割线
     */
    public GridSpacingItemDecoration(int horizontalSpacing, int verticalSpacing, @ColorInt int color) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
        mDrawDivider = true;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 内部构造：仅间距模式，不初始化绘制用的 Paint。
     */
    private GridSpacingItemDecoration(int horizontalSpacing, int verticalSpacing, boolean drawDivider) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
        mDrawDivider = drawDivider;
        mPaint = null;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        int spanCount = getSpanCount(parent);
        if (spanCount <= 0) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        int childCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();

        boolean isLastRow = isLastRow(parent, itemPosition, spanCount, childCount);

        // 横向：将列间距均分到相邻 item 的 left / right，保证每列宽度一致
        int top = 0;
        int left;
        int right;
        int eachWidth = (spanCount - 1) * mHorizontalSpacing / spanCount;
        int dl = mHorizontalSpacing - eachWidth;

        left = itemPosition % spanCount * dl;
        right = eachWidth - left;

        // 纵向：非最后一行保留行间距
        int bottom = mVerticalSpacing;
        if (isLastRow) {
            bottom = 0;
        }
        outRect.set(left, top, right, bottom);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        if (mDrawDivider && mPaint != null) {
            draw(canvas, parent);
        }
    }

    /**
     * 绘制 item 之间的分割线；仅在使用带 color 参数的构造方法时调用。
     */
    private void draw(Canvas canvas, RecyclerView parent) {
        int spanCount = getSpanCount(parent);
        if (spanCount <= 0) {
            return;
        }
        int adapterCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos == RecyclerView.NO_POSITION) {
                continue;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            // 水平分割线：与 getItemOffsets 一致，最后一行不绘制
            if (!isLastRow(parent, pos, spanCount, adapterCount)) {
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getBottom() + layoutParams.bottomMargin;
                int bottom = top + mVerticalSpacing;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

            // 垂直分割线：最后一列不绘制，避免与 offset 语义不一致
            if (!isLastColumn(parent, pos, spanCount, adapterCount)) {
                int top2 = child.getTop();
                // 向下延伸 mVerticalSpacing，与水平分割线在交叉处对齐
                int bottom2 = child.getBottom() + mVerticalSpacing;
                int left2 = child.getRight() + layoutParams.rightMargin;
                int right2 = left2 + mHorizontalSpacing;
                canvas.drawRect(left2, top2, right2, bottom2, mPaint);
            }
        }
    }

    private boolean isLastColumn(RecyclerView parent, int pos, int spanCount,
                                 int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) {// 如果是最后一列，则不需要绘制右边
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一列，则不需要绘制右边
                return (pos + 1) % spanCount == 0;
            } else {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一列，则不需要绘制右边
                return pos >= childCount;
            }
        }
        return false;
    }

    private boolean isLastRow(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int lines = childCount % spanCount == 0 ? childCount / spanCount : childCount / spanCount + 1;
            return lines == pos / spanCount + 1;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                return pos >= childCount;
            } else {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFirstRow(RecyclerView parent, int pos, int spanCount,
                               int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return pos < spanCount;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            // 瀑布流位置与「行」无严格一一对应，仅作近似：首行下标通常小于 spanCount
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                return pos < spanCount;
            } else {
                return pos < spanCount;
            }
        }
        return false;
    }

    /** 获取列数（GridLayoutManager / StaggeredGridLayoutManager） */
    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                    .getSpanCount();
        }
        return spanCount;
    }
}
