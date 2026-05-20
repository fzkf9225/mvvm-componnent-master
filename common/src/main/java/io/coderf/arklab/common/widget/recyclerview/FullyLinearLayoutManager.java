package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 用于「外层可滚动 + 内层 RecyclerView 不滚动」场景的 {@link LinearLayoutManager} 扩展。
 * <p>
 * <b>解决的问题</b>
 * <ol>
 *   <li><b>显示不全</b>：RecyclerView 放在 ScrollView / NestedScrollView 内时，父布局往往对高度
 *       传入 AT_MOST，系统默认只测量约一屏高度，底部 item 被裁切。</li>
 *   <li><b>性能</b>：旧实现每次 onMeasure 对全部 item 执行 getViewForPosition，列表稍长即卡顿。
 *       本类在嵌套场景下默认仅测量第一个 item 并按条目数推算（等高列表）。</li>
 * </ol>
 * <p>
 * <b>与原生行为的关系</b>
 * <ul>
 *   <li>主轴已被 EXACTLY 约束（独立占满父布局、自行滚动）时：直接 {@code super.onMeasure()}，
 *       布局、回收、预加载与 {@link LinearLayoutManager} 完全一致。</li>
 *   <li>主轴为 UNSPECIFIED / AT_MOST 时：展开为「所有 item 高度之和 + padding」，供外层滚动容器使用。</li>
 * </ul>
 * <p>
 * <b>推荐用法</b>（嵌套滚动时务必关闭 RecyclerView 自身纵向滚动，由外层承担）：
 * <pre>{@code
 * recyclerView.setLayoutManager(new FullyLinearLayoutManager(context) {
 *     @Override
 *     public boolean canScrollVertically() {
 *         return false;
 *     }
 * });
 * recyclerView.setNestedScrollingEnabled(false);
 * }</pre>
 * <p>
 * <b>限制</b>
 * <ul>
 *   <li>默认 {@link #assumeUniformItemSize} 为 true，适用于表单附件、固定行高列表；不等高需设为 false。</li>
 *   <li>不等高且条目数 &gt; {@link NestedScrollLayoutMeasureHelper#MAX_FULL_MEASURE_ITEM_COUNT}
 *       时会回退系统测量，嵌套下可能仍显示不全，建议改为单 RecyclerView 多类型或分页。</li>
 *   <li>{@link RecyclerView.ItemDecoration} 间距未计入总高度，divider 较大时可能出现少量裁切。</li>
 * </ul>
 *
 * @see FullyGridLayoutManager
 * @see NestedScrollLayoutMeasureHelper
 */
public class FullyLinearLayoutManager extends LinearLayoutManager {

    /** measureChild 输出复用，避免 onMeasure 频繁 new 数组 */
    private final int[] mMeasuredDimension = new int[2];

    /**
     * 是否假定所有 item 主轴方向尺寸一致。
     * true：只测量 position 0，再 × itemCount（O(1) bind，适合等高 item）。
     * false：逐项测量（≤80 条），更高精度，适合高度不一的列表。
     */
    private boolean assumeUniformItemSize = true;

    /** 以下字段组成一次 layout 内的测量结果缓存，避免重复 getViewForPosition */
    private int measureCacheWidthSpec;
    private int measureCacheHeightSpec;
    private int measureCacheItemCount = -1;
    private int measureCacheWidth;
    private int measureCacheHeight;
    private boolean measureCacheAssumeUniform;

    public FullyLinearLayoutManager(Context context) {
        super(context);
    }

    public FullyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    /**
     * 设置是否按「等高 item」快速测量。
     *
     * @param assumeUniformItemSize {@code true} 仅测首项（默认）；{@code false} 逐项测量（有上限）
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
    public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        invalidateMeasureCache();
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        // 非嵌套场景：与 LinearLayoutManager 完全一致
        if (!NestedScrollLayoutMeasureHelper.needsCustomMeasure(getOrientation(), widthSpec, heightSpec)) {
            invalidateMeasureCache();
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        final int paddingH = getPaddingLeft() + getPaddingRight();
        final int paddingV = getPaddingTop() + getPaddingBottom();
        final int itemCount = state.getItemCount();

        if (itemCount == 0) {
            setMeasuredDimension(
                    NestedScrollLayoutMeasureHelper.emptyWidth(widthSpec, paddingH),
                    NestedScrollLayoutMeasureHelper.emptyHeight(heightSpec, paddingV));
            return;
        }

        if (tryApplyMeasureCache(widthSpec, heightSpec, itemCount)) {
            return;
        }

        final int childWidthSpec = NestedScrollLayoutMeasureHelper.makeChildWidthSpec(widthSpec, paddingH);
        final int childHeightSpec = NestedScrollLayoutMeasureHelper.makeChildHeightSpec(heightSpec, paddingV);

        int contentWidth = 0;
        int contentHeight = 0;

        if (assumeUniformItemSize) {
            // 快速路径：测 1 个 item × N
            NestedScrollLayoutMeasureHelper.measureChild(this, recycler, state, 0,
                    childWidthSpec, childHeightSpec, mMeasuredDimension);
            if (getOrientation() == HORIZONTAL) {
                contentWidth = mMeasuredDimension[0] * itemCount;
                contentHeight = mMeasuredDimension[1];
            } else {
                contentWidth = mMeasuredDimension[0];
                contentHeight = mMeasuredDimension[1] * itemCount;
            }
        } else if (itemCount <= NestedScrollLayoutMeasureHelper.MAX_FULL_MEASURE_ITEM_COUNT) {
            // 精确路径：不等高，逐项累加（有条目上限）
            for (int i = 0; i < itemCount; i++) {
                NestedScrollLayoutMeasureHelper.measureChild(this, recycler, state, i,
                        childWidthSpec, childHeightSpec, mMeasuredDimension);
                if (getOrientation() == HORIZONTAL) {
                    contentWidth += mMeasuredDimension[0];
                    if (i == 0) {
                        contentHeight = mMeasuredDimension[1];
                    }
                } else {
                    contentHeight += mMeasuredDimension[1];
                    if (i == 0) {
                        contentWidth = mMeasuredDimension[0];
                    }
                }
            }
        } else {
            // 条目过多且不等高：避免 ANR，回退系统测量
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        int width = contentWidth + paddingH;
        int height = contentHeight + paddingV;
        width = NestedScrollLayoutMeasureHelper.resolveWidth(widthSpec, width);
        height = NestedScrollLayoutMeasureHelper.resolveHeight(heightSpec, height);

        saveMeasureCache(widthSpec, heightSpec, itemCount, width, height);
        setMeasuredDimension(width, height);
    }

    /**
     * 若 itemCount、MeasureSpec、等高策略未变，直接复用上次测量结果。
     */
    private boolean tryApplyMeasureCache(int widthSpec, int heightSpec, int itemCount) {
        if (measureCacheItemCount != itemCount
                || measureCacheWidthSpec != widthSpec
                || measureCacheHeightSpec != heightSpec
                || measureCacheAssumeUniform != assumeUniformItemSize) {
            return false;
        }
        setMeasuredDimension(measureCacheWidth, measureCacheHeight);
        return true;
    }

    private void saveMeasureCache(int widthSpec, int heightSpec, int itemCount, int width, int height) {
        measureCacheWidthSpec = widthSpec;
        measureCacheHeightSpec = heightSpec;
        measureCacheItemCount = itemCount;
        measureCacheAssumeUniform = assumeUniformItemSize;
        measureCacheWidth = width;
        measureCacheHeight = height;
    }

    /** 数据或策略变化后清空缓存，下次 onMeasure 重新计算 */
    private void invalidateMeasureCache() {
        measureCacheItemCount = -1;
    }
}
