package io.coderf.arklab.common.widget.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 嵌套在外层可滚动容器（ScrollView / NestedScrollView）时的测量工具类。
 * <p>
 * <b>背景</b>：系统默认的 {@link androidx.recyclerview.widget.LinearLayoutManager} /
 * {@link androidx.recyclerview.widget.GridLayoutManager} 在 {@code onMeasure} 阶段通常只按
 * 「一屏」估算尺寸。当父布局传入的主轴 MeasureSpec 为 {@link View.MeasureSpec#AT_MOST} 或
 * {@link View.MeasureSpec#UNSPECIFIED} 时，RecyclerView 高度/宽度会被压扁，导致列表显示不全。
 * <p>
 * <b>职责</b>：本类抽取 {@link FullyLinearLayoutManager} 与 {@link FullyGridLayoutManager}
 * 共用的测量逻辑，包括：判断是否需要自定义展开测量、构造子 View 的 MeasureSpec、安全测量
 * Scrap 子项、按父级 MeasureSpec 解析最终宽高。
 * <p>
 * <b>注意</b>：不负责 {@link RecyclerView.LayoutManager#canScrollVertically()} 等滚动冲突处理；
 * 嵌套场景下请在 LayoutManager 子类中重写并返回 {@code false}，由外层容器承担滚动。
 *
 * @see FullyLinearLayoutManager
 * @see FullyGridLayoutManager
 */
final class NestedScrollLayoutMeasureHelper {

    /**
     * 在「不等高 / 不等宽」模式下，允许逐项 {@code getViewForPosition} 测量的最大条目数。
     * 超过该值将回退 {@code super.onMeasure()}，避免主线程 O(n) 绑定导致卡顿。
     * 表单、相册宫格等场景请保持 {@link FullyLinearLayoutManager#setAssumeUniformItemSize(boolean)}
     * 为 {@code true}，仅测量首项或首行即可。
     */
    static final int MAX_FULL_MEASURE_ITEM_COUNT = 80;

    private NestedScrollLayoutMeasureHelper() {
    }

    /**
     * 判断当前是否需要走「展开主轴」的自定义测量，而非系统默认逻辑。
     * <ul>
     *   <li>纵向列表：看 <b>heightSpec</b> 是否为 UNSPECIFIED / AT_MOST</li>
     *   <li>横向列表：看 <b>widthSpec</b> 是否为 UNSPECIFIED / AT_MOST</li>
     *   <li>主轴为 EXACTLY（如 RecyclerView 高度 match_parent 且父布局高度已固定）时返回 {@code false}，
     *       调用方应使用 {@code super.onMeasure()}，行为与原生 LayoutManager 一致</li>
     * </ul>
     *
     * @param orientation {@link RecyclerView#VERTICAL} 或 {@link RecyclerView#HORIZONTAL}
     * @param widthSpec   RecyclerView 收到的宽度 MeasureSpec
     * @param heightSpec  RecyclerView 收到的高度 MeasureSpec
     * @return {@code true} 表示需要自定义展开测量
     */
    static boolean needsCustomMeasure(int orientation, int widthSpec, int heightSpec) {
        if (orientation == RecyclerView.VERTICAL) {
            int heightMode = View.MeasureSpec.getMode(heightSpec);
            return heightMode == View.MeasureSpec.UNSPECIFIED
                    || heightMode == View.MeasureSpec.AT_MOST;
        }
        int widthMode = View.MeasureSpec.getMode(widthSpec);
        return widthMode == View.MeasureSpec.UNSPECIFIED
                || widthMode == View.MeasureSpec.AT_MOST;
    }

    /**
     * 为 Linear 或横向 Grid 的子 item 构造宽度 MeasureSpec。
     * 当父级宽度为 EXACTLY / AT_MOST 时，使用「父宽 − padding」作为子项宽度上限，更接近真实 layout。
     *
     * @param widthSpec          RecyclerView 的 widthSpec
     * @param paddingHorizontal  {@code paddingLeft + paddingRight}
     */
    static int makeChildWidthSpec(int widthSpec, int paddingHorizontal) {
        int mode = View.MeasureSpec.getMode(widthSpec);
        int size = View.MeasureSpec.getSize(widthSpec);
        if (mode == View.MeasureSpec.EXACTLY || mode == View.MeasureSpec.AT_MOST) {
            int childWidth = Math.max(0, size - paddingHorizontal);
            return View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY);
        }
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    /**
     * 为 Linear 或纵向 Grid 的子 item 构造高度 MeasureSpec（逻辑同 {@link #makeChildWidthSpec}）。
     */
    static int makeChildHeightSpec(int heightSpec, int paddingVertical) {
        int mode = View.MeasureSpec.getMode(heightSpec);
        int size = View.MeasureSpec.getSize(heightSpec);
        if (mode == View.MeasureSpec.EXACTLY || mode == View.MeasureSpec.AT_MOST) {
            int childHeight = Math.max(0, size - paddingVertical);
            return View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY);
        }
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    /**
     * 纵向 Grid 单列宽度：{@code (父宽 - padding) / spanCount}，避免按整行宽度测量 cell 导致偏高。
     *
     * @param spanCount 列数，须 &gt; 0
     */
    static int makeGridChildWidthSpec(int widthSpec, int paddingHorizontal, int spanCount) {
        int mode = View.MeasureSpec.getMode(widthSpec);
        int size = View.MeasureSpec.getSize(widthSpec);
        if (spanCount > 0 && (mode == View.MeasureSpec.EXACTLY || mode == View.MeasureSpec.AT_MOST)) {
            int cellWidth = Math.max(0, (size - paddingHorizontal) / spanCount);
            return View.MeasureSpec.makeMeasureSpec(cellWidth, View.MeasureSpec.EXACTLY);
        }
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    /**
     * 横向 Grid 单行高度：{@code (父高 - padding) / spanCount}。
     */
    static int makeGridChildHeightSpec(int heightSpec, int paddingVertical, int spanCount) {
        int mode = View.MeasureSpec.getMode(heightSpec);
        int size = View.MeasureSpec.getSize(heightSpec);
        if (spanCount > 0 && (mode == View.MeasureSpec.EXACTLY || mode == View.MeasureSpec.AT_MOST)) {
            int cellHeight = Math.max(0, (size - paddingVertical) / spanCount);
            return View.MeasureSpec.makeMeasureSpec(cellHeight, View.MeasureSpec.EXACTLY);
        }
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    /**
     * 从 Recycler 池取出指定 position 的 View 进行测量，结果写入 {@code outDimen}，并立即回收。
     * <p>
     * {@code outDimen[0]} = 宽（含 margin），{@code outDimen[1]} = 高（含 margin）。
     * 使用 {@link RecyclerView.State#getItemCount()} 做边界判断，与 layout 阶段数据更一致。
     *
     * @param outDimen 长度至少为 2 的数组，由调用方复用，减少 GC
     */
    static void measureChild(@NonNull RecyclerView.LayoutManager layoutManager,
                             @NonNull RecyclerView.Recycler recycler,
                             @NonNull RecyclerView.State state,
                             int position,
                             int widthSpec,
                             int heightSpec,
                             @NonNull int[] outDimen) {
        outDimen[0] = 0;
        outDimen[1] = 0;
        if (position < 0 || position >= state.getItemCount()) {
            return;
        }
        View view = null;
        try {
            view = recycler.getViewForPosition(position);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    layoutManager.getPaddingLeft() + layoutManager.getPaddingRight(), lp.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    layoutManager.getPaddingTop() + layoutManager.getPaddingBottom(), lp.height);
            view.measure(childWidthSpec, childHeightSpec);
            outDimen[0] = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            outDimen[1] = view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        } catch (IndexOutOfBoundsException ignored) {
            // DiffUtil / notify 与 onMeasure 不同步时可能越界，跳过该 position
        } finally {
            if (view != null) {
                recycler.recycleView(view);
            }
        }
    }

    /**
     * 根据父级 widthSpec 解析最终宽度：EXACTLY 用父值，AT_MOST 取 min，UNSPECIFIED 用测量值。
     */
    static int resolveWidth(int widthSpec, int measured) {
        int mode = View.MeasureSpec.getMode(widthSpec);
        int size = View.MeasureSpec.getSize(widthSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            return size;
        }
        if (mode == View.MeasureSpec.AT_MOST) {
            return Math.min(measured, size);
        }
        return measured;
    }

    /**
     * 根据父级 heightSpec 解析最终高度（规则同 {@link #resolveWidth}）。
     */
    static int resolveHeight(int heightSpec, int measured) {
        int mode = View.MeasureSpec.getMode(heightSpec);
        int size = View.MeasureSpec.getSize(heightSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            return size;
        }
        if (mode == View.MeasureSpec.AT_MOST) {
            return Math.min(measured, size);
        }
        return measured;
    }

    /** adapter 无数据时，仅返回 padding 与 MeasureSpec 约束后的尺寸 */
    static int emptyWidth(int widthSpec, int paddingHorizontal) {
        return resolveWidth(widthSpec, paddingHorizontal);
    }

    static int emptyHeight(int heightSpec, int paddingVertical) {
        return resolveHeight(heightSpec, paddingVertical);
    }
}
