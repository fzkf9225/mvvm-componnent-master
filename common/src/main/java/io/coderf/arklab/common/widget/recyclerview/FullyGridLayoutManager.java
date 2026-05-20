package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 用于「外层可滚动 + 内层 RecyclerView 不滚动」场景的 {@link GridLayoutManager} 扩展。
 * <p>
 * 行为说明、性能策略、推荐用法与 {@link FullyLinearLayoutManager} 相同，差异在于：
 * <ul>
 *   <li>按 {@link #getSpanCount()} 分列/分行测量；纵向 Grid 每行高度取该行 cell 的 <b>最大值</b>
 *       （与系统 Grid 行高规则一致）。</li>
 *   <li>纵向 Grid 子项宽度使用 {@code (父宽 - padding) / span}，避免按整行宽度误测 cell。</li>
 *   <li>等高快速路径下仅测量<b>首行</b>共 {@code span} 个 item，再 {@code 行高 × 行数} 推算总高。</li>
 * </ul>
 * <p>
 * <b>推荐用法</b>：
 * <pre>{@code
 * recyclerView.setLayoutManager(new FullyGridLayoutManager(context, spanCount) {
 *     @Override
 *     public boolean canScrollVertically() {
 *         return false;
 *     }
 * });
 * }</pre>
 * <p>
 * <b>限制</b>：未处理 {@link androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup}
 * 跨列合并；含跨列 item 的网格请设 {@link #setAssumeUniformItemSize(boolean)} 为 false 并控制条目数。
 *
 * @see FullyLinearLayoutManager
 * @see NestedScrollLayoutMeasureHelper
 */
public class FullyGridLayoutManager extends GridLayoutManager {

    private final int[] mMeasuredDimension = new int[2];

    /**
     * 是否假定每个 cell 等宽等高（如相册缩略图宫格）。
     * true：只测首行（默认）；false：按行取 max 高度逐项测（≤80 条）。
     */
    private boolean assumeUniformItemSize = true;

    private int measureCacheWidthSpec;
    private int measureCacheHeightSpec;
    private int measureCacheItemCount = -1;
    private int measureCacheSpanCount = -1;
    private int measureCacheWidth;
    private int measureCacheHeight;
    private boolean measureCacheAssumeUniform;

    public FullyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public FullyGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    /**
     * @param assumeUniformItemSize {@code true} 仅测首行（默认）；{@code false} 按行精确测量
     */
    public void setAssumeUniformItemSize(boolean assumeUniformItemSize) {
        if (this.assumeUniformItemSize != assumeUniformItemSize) {
            this.assumeUniformItemSize = assumeUniformItemSize;
            invalidateMeasureCache();
        }
    }

    public boolean isAssumeUniformItemSize() {
        return assumeUniformItemSize;
    }

    @Override
    public void setSpanCount(int spanCount) {
        if (getSpanCount() != spanCount) {
            invalidateMeasureCache();
        }
        super.setSpanCount(spanCount);
    }

    @Override
    public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        invalidateMeasureCache();
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        if (!NestedScrollLayoutMeasureHelper.needsCustomMeasure(getOrientation(), widthSpec, heightSpec)) {
            invalidateMeasureCache();
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        final int paddingH = getPaddingLeft() + getPaddingRight();
        final int paddingV = getPaddingTop() + getPaddingBottom();
        final int itemCount = state.getItemCount();
        final int span = getSpanCount();

        if (itemCount == 0) {
            setMeasuredDimension(
                    NestedScrollLayoutMeasureHelper.emptyWidth(widthSpec, paddingH),
                    NestedScrollLayoutMeasureHelper.emptyHeight(heightSpec, paddingV));
            return;
        }

        if (tryApplyMeasureCache(widthSpec, heightSpec, itemCount, span)) {
            return;
        }

        // 纵向 Grid 用单列宽；横向 Grid 用单行高，与 layout 时分摊规则一致
        final int childWidthSpec = getOrientation() == VERTICAL
                ? NestedScrollLayoutMeasureHelper.makeGridChildWidthSpec(widthSpec, paddingH, span)
                : NestedScrollLayoutMeasureHelper.makeChildWidthSpec(widthSpec, paddingH);
        final int childHeightSpec = getOrientation() == HORIZONTAL
                ? NestedScrollLayoutMeasureHelper.makeGridChildHeightSpec(heightSpec, paddingV, span)
                : NestedScrollLayoutMeasureHelper.makeChildHeightSpec(heightSpec, paddingV);

        int contentWidth = 0;
        int contentHeight = 0;

        if (assumeUniformItemSize) {
            // 快速路径：测首行 span 个 cell，行高取 max，总高 = 行高 × 行数
            int rowHeight = 0;
            int rowWidth = 0;
            final int firstRowCount = Math.min(span, itemCount);
            for (int i = 0; i < firstRowCount; i++) {
                NestedScrollLayoutMeasureHelper.measureChild(this, recycler, state, i,
                        childWidthSpec, childHeightSpec, mMeasuredDimension);
                rowHeight = Math.max(rowHeight, mMeasuredDimension[1]);
                rowWidth = Math.max(rowWidth, mMeasuredDimension[0]);
            }
            final int rowCount = (itemCount + span - 1) / span;
            if (getOrientation() == HORIZONTAL) {
                contentWidth = rowWidth * rowCount;
                contentHeight = rowHeight;
            } else {
                int widthMode = View.MeasureSpec.getMode(widthSpec);
                if (widthMode == View.MeasureSpec.EXACTLY || widthMode == View.MeasureSpec.AT_MOST) {
                    contentWidth = Math.max(0, View.MeasureSpec.getSize(widthSpec) - paddingH);
                } else {
                    contentWidth = rowWidth * span;
                }
                contentHeight = rowHeight * rowCount;
            }
        } else if (itemCount <= NestedScrollLayoutMeasureHelper.MAX_FULL_MEASURE_ITEM_COUNT) {
            // 精确路径：按行遍历，每行高度 = 该行各 cell 高度最大值
            if (getOrientation() == HORIZONTAL) {
                for (int rowStart = 0; rowStart < itemCount; rowStart += span) {
                    int colWidth = 0;
                    int colHeight = 0;
                    for (int j = 0; j < span && rowStart + j < itemCount; j++) {
                        NestedScrollLayoutMeasureHelper.measureChild(this, recycler, state, rowStart + j,
                                childWidthSpec, childHeightSpec, mMeasuredDimension);
                        colWidth = Math.max(colWidth, mMeasuredDimension[0]);
                        colHeight = Math.max(colHeight, mMeasuredDimension[1]);
                    }
                    contentWidth += colWidth;
                    if (rowStart == 0) {
                        contentHeight = colHeight;
                    }
                }
            } else {
                for (int rowStart = 0; rowStart < itemCount; rowStart += span) {
                    int rowHeight = 0;
                    for (int j = 0; j < span && rowStart + j < itemCount; j++) {
                        NestedScrollLayoutMeasureHelper.measureChild(this, recycler, state, rowStart + j,
                                childWidthSpec, childHeightSpec, mMeasuredDimension);
                        rowHeight = Math.max(rowHeight, mMeasuredDimension[1]);
                    }
                    contentHeight += rowHeight;
                }
                int widthMode = View.MeasureSpec.getMode(widthSpec);
                if (widthMode == View.MeasureSpec.EXACTLY || widthMode == View.MeasureSpec.AT_MOST) {
                    contentWidth = Math.max(0, View.MeasureSpec.getSize(widthSpec) - paddingH);
                }
            }
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        int width = contentWidth + paddingH;
        int height = contentHeight + paddingV;
        width = NestedScrollLayoutMeasureHelper.resolveWidth(widthSpec, width);
        height = NestedScrollLayoutMeasureHelper.resolveHeight(heightSpec, height);

        saveMeasureCache(widthSpec, heightSpec, itemCount, span, width, height);
        setMeasuredDimension(width, height);
    }

    private boolean tryApplyMeasureCache(int widthSpec, int heightSpec, int itemCount, int span) {
        if (measureCacheItemCount != itemCount
                || measureCacheSpanCount != span
                || measureCacheWidthSpec != widthSpec
                || measureCacheHeightSpec != heightSpec
                || measureCacheAssumeUniform != assumeUniformItemSize) {
            return false;
        }
        setMeasuredDimension(measureCacheWidth, measureCacheHeight);
        return true;
    }

    private void saveMeasureCache(int widthSpec, int heightSpec, int itemCount, int span,
                                  int width, int height) {
        measureCacheWidthSpec = widthSpec;
        measureCacheHeightSpec = heightSpec;
        measureCacheItemCount = itemCount;
        measureCacheSpanCount = span;
        measureCacheAssumeUniform = assumeUniformItemSize;
        measureCacheWidth = width;
        measureCacheHeight = height;
    }

    private void invalidateMeasureCache() {
        measureCacheItemCount = -1;
        measureCacheSpanCount = -1;
    }
}
