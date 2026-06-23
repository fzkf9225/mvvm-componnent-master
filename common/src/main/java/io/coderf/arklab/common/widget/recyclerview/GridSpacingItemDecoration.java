package io.coderf.arklab.common.widget.recyclerview;

import android.graphics.Canvas;
import android.graphics.Color;
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
 * 「无需传颜色」的 API 仅通过 {@link #getItemOffsets} 占位，不绘制可见分割线，也不创建 Paint；
 * 历史写法传 {@code 0x00000000} 仍兼容，但会跳过 onDraw，性能不如直接使用无颜色构造。
 * </p>
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * // 1. 仅占位间距，不绘制分割线（无需传颜色，推荐）
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(context, 8)));
 *
 * // 2. 横纵不同间距，仅占位（无需传颜色）
 * recyclerView.addItemDecoration(GridSpacingItemDecoration.spacingOnly(columnGap, rowGap));
 *
 * // 3. 间距 + 可见分割线（历史 API）
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(gap, Color.parseColor("#EEEEEE")));
 *
 * // 4. 横纵不同 + 可见分割线（历史 API；透明色仅占位、跳过绘制）
 * recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnGap, rowGap, dividerColor));
 * </pre>
 * </p>
 *
 * @author fz
 * @since 2018/9/25
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * 按 item 位置判断是否选中，用于绘制选中/未选中两种间距颜色。
     */
    public interface SelectionProvider {
        boolean isSelected(int position);
    }

    /** 列与列之间的间距（影响 left / right offset 及垂直分割线宽度） */
    private final int mHorizontalSpacing;

    /** 行与行之间的间距（影响 bottom offset 及水平分割线高度） */
    private final int mVerticalSpacing;

    /** 是否绘制可见分割线；为 false 时仅通过 getItemOffsets 保留间距 */
    private final boolean mDrawDivider;

    private final Paint mPaint;

    /** 未选中时间距颜色；null 表示不绘制该状态的分割线 */
    @ColorInt
    private final Integer mUnselectedGapColor;

    /** 选中时间距颜色；null 表示不绘制该状态的分割线 */
    @ColorInt
    private final Integer mSelectedGapColor;

    private final SelectionProvider mSelectionProvider;

    /**
     * 仅设置 item 间距，不绘制可见分割线（无需传颜色）。
     * <p>
     * 仅占位、不绘制；比 {@code new GridSpacingItemDecoration(dividerWidth, Color.TRANSPARENT)} 更省性能。
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
     * @param color        分割线颜色；传透明色时仅占位、跳过绘制（兼容历史写法）
     */
    public GridSpacingItemDecoration(int dividerWidth, @ColorInt int color) {
        this(dividerWidth, dividerWidth, color);
    }

    /**
     * 分别指定横向、纵向间距，并按颜色决定是否绘制分割线（历史 API）。
     *
     * @param horizontalSpacing 列间距（px），对应 item 的 left / right offset
     * @param verticalSpacing   行间距（px），对应 item 的 bottom offset
     * @param color             分割线颜色；传透明色时仅占位、跳过绘制（兼容历史写法）
     */
    public GridSpacingItemDecoration(int horizontalSpacing, int verticalSpacing, @ColorInt int color) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
        mDrawDivider = Color.alpha(color) != 0;
        if (mDrawDivider) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(color);
            mPaint.setStyle(Paint.Style.FILL);
        } else {
            mPaint = null;
        }
        mUnselectedGapColor = null;
        mSelectedGapColor = null;
        mSelectionProvider = null;
    }

    /**
     * 内部构造：仅间距模式，不初始化绘制用的 Paint。
     */
    private GridSpacingItemDecoration(int horizontalSpacing, int verticalSpacing, boolean drawDivider) {
        mHorizontalSpacing = horizontalSpacing;
        mVerticalSpacing = verticalSpacing;
        mDrawDivider = drawDivider;
        mPaint = null;
        mUnselectedGapColor = null;
        mSelectedGapColor = null;
        mSelectionProvider = null;
    }

    private GridSpacingItemDecoration(Builder builder) {
        mHorizontalSpacing = builder.horizontalSpacing;
        mVerticalSpacing = builder.verticalSpacing;
        mUnselectedGapColor = builder.unselectedGapColor;
        mSelectedGapColor = builder.selectedGapColor;
        mSelectionProvider = builder.selectionProvider;
        boolean hasVisibleLegacyColor = builder.legacyGapColor != null
                && Color.alpha(builder.legacyGapColor) != 0;
        boolean hasVisibleUnselected = builder.unselectedGapColor != null
                && Color.alpha(builder.unselectedGapColor) != 0;
        boolean hasVisibleSelected = builder.selectedGapColor != null
                && Color.alpha(builder.selectedGapColor) != 0;
        mDrawDivider = hasVisibleLegacyColor || hasVisibleUnselected || hasVisibleSelected;
        if (mDrawDivider) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            if (hasVisibleLegacyColor) {
                mPaint.setColor(builder.legacyGapColor);
            }
        } else {
            mPaint = null;
        }
    }

    /**
     * 构建支持横/纵独立间距，以及选中/未选中两种间距颜色的装饰器。
     */
    public static class Builder {
        private int horizontalSpacing;
        private int verticalSpacing;
        @ColorInt
        private Integer unselectedGapColor;
        @ColorInt
        private Integer selectedGapColor;
        @ColorInt
        private Integer legacyGapColor;
        private SelectionProvider selectionProvider;

        public Builder horizontalSpacing(int horizontalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
            return this;
        }

        public Builder verticalSpacing(int verticalSpacing) {
            this.verticalSpacing = verticalSpacing;
            return this;
        }

        public Builder spacing(int horizontalSpacing, int verticalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
            this.verticalSpacing = verticalSpacing;
            return this;
        }

        public Builder unselectedGapColor(@ColorInt Integer unselectedGapColor) {
            this.unselectedGapColor = unselectedGapColor;
            return this;
        }

        public Builder selectedGapColor(@ColorInt Integer selectedGapColor) {
            this.selectedGapColor = selectedGapColor;
            return this;
        }

        public Builder gapColor(@ColorInt int gapColor) {
            this.legacyGapColor = gapColor;
            return this;
        }

        public Builder selectionProvider(SelectionProvider selectionProvider) {
            this.selectionProvider = selectionProvider;
            return this;
        }

        public GridSpacingItemDecoration build() {
            return new GridSpacingItemDecoration(this);
        }
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
                Integer gapColor = resolveGapColor(pos, pos + spanCount, adapterCount);
                if (gapColor != null) {
                    mPaint.setColor(gapColor);
                    int left = child.getLeft();
                    int right = child.getRight();
                    int top = child.getBottom() + layoutParams.bottomMargin;
                    int bottom = top + mVerticalSpacing;
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }

            // 垂直分割线：最后一列不绘制，避免与 offset 语义不一致
            if (!isLastColumn(parent, pos, spanCount, adapterCount)) {
                Integer gapColor = resolveGapColor(pos, pos + 1, adapterCount);
                if (gapColor != null) {
                    mPaint.setColor(gapColor);
                    int top2 = child.getTop();
                    // 向下延伸 mVerticalSpacing，与水平分割线在交叉处对齐
                    int bottom2 = child.getBottom() + mVerticalSpacing;
                    int left2 = child.getRight() + layoutParams.rightMargin;
                    int right2 = left2 + mHorizontalSpacing;
                    canvas.drawRect(left2, top2, right2, bottom2, mPaint);
                }
            }
        }
    }

    @ColorInt
    private Integer resolveGapColor(int currentPos, int adjacentPos, int adapterCount) {
        if (mSelectionProvider != null) {
            boolean selected = isPositionSelected(currentPos, adapterCount)
                    || isPositionSelected(adjacentPos, adapterCount);
            if (selected) {
                return isVisibleColor(mSelectedGapColor) ? mSelectedGapColor : null;
            }
            return isVisibleColor(mUnselectedGapColor) ? mUnselectedGapColor : null;
        }
        if (mPaint != null) {
            return mPaint.getColor();
        }
        return null;
    }

    private static boolean isVisibleColor(@ColorInt Integer color) {
        return color != null && Color.alpha(color) != 0;
    }

    private boolean isPositionSelected(int position, int adapterCount) {
        if (mSelectionProvider == null || position < 0 || position >= adapterCount) {
            return false;
        }
        return mSelectionProvider.isSelected(position);
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
