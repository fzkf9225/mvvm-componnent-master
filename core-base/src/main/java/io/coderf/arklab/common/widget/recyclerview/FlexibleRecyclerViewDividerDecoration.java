package io.coderf.arklab.common.widget.recyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 功能较全的「线性列表」分割线 {@link RecyclerView.ItemDecoration}（适用于 {@link LinearLayoutManager}）。
 * <p>
 * 与 {@link RecycleViewDivider} 的关系：本类在 API 上更灵活（Builder、自定义是否绘制、边距），
 * 绘制/占位语义与修正后的 {@link RecycleViewDivider} 一致：纵向列表在 item 底边画横线并占底部 outRect，
 * 横向列表在 item 右侧画竖线并占右侧 outRect。
 * </p>
 * <p>
 * 网格/瀑布流请继续使用 {@link GridSpacingItemDecoration} 等专用实现。
 * </p>
 */
public final class FlexibleRecyclerViewDividerDecoration extends RecyclerView.ItemDecoration {

    /**
     * 判断在 adapter 位置为 {@code afterAdapterPosition} 的 item 的「末端」是否绘制分割线
     * （即该线位于 item[afterAdapterPosition] 与 item[afterAdapterPosition+1] 之间；若 afterAdapterPosition 为最后一项，
     * 则表示列表末尾是否再画一条线）。
     */
    public interface BetweenItemsPredicate {
        boolean shouldDrawDividerAfter(int afterAdapterPosition, int itemCount);
    }

    private final Paint paint;
    private final int orientation;
    private final int thicknessPx;
    private final BetweenItemsPredicate predicate;
    /** 在 RecyclerView padding 基础上，分割线起点再向内缩进（纵向：左侧；横向：上侧） */
    private final int drawInsetStartPx;
    /** 在 RecyclerView padding 基础上，分割线终点再向内缩进（纵向：右侧；横向：下侧） */
    private final int drawInsetEndPx;

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        if (pos == RecyclerView.NO_POSITION || itemCount <= 0) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        boolean drawAfterThisItem = predicate.shouldDrawDividerAfter(pos, itemCount);
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, drawAfterThisItem ? thicknessPx : 0);
        } else {
            outRect.set(0, 0, drawAfterThisItem ? thicknessPx : 0, 0);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (orientation == LinearLayoutManager.VERTICAL) {
            drawUnderEachChild(c, parent);
        } else {
            drawRightOfEachChild(c, parent);
        }
    }

    /** 纵向列表：在 child 底边绘制横线（占满内容区宽度，可配置左右内缩进） */
    private void drawUnderEachChild(Canvas canvas, RecyclerView parent) {
        int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        int leftBase = parent.getPaddingLeft() + drawInsetStartPx;
        int rightBase = parent.getWidth() - parent.getPaddingRight() - drawInsetEndPx;
        int n = parent.getChildCount();
        for (int i = 0; i < n; i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos == RecyclerView.NO_POSITION) {
                continue;
            }
            if (!predicate.shouldDrawDividerAfter(pos, itemCount)) {
                continue;
            }
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + lp.bottomMargin;
            int bottom = top + thicknessPx;
            canvas.drawRect(leftBase, top, rightBase, bottom, paint);
        }
    }

    /** 横向列表：在 child 右侧绘制竖线（占满内容区高度，可配置上下内缩进） */
    private void drawRightOfEachChild(Canvas canvas, RecyclerView parent) {
        int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
        int topBase = parent.getPaddingTop() + drawInsetStartPx;
        int bottomBase = parent.getHeight() - parent.getPaddingBottom() - drawInsetEndPx;
        int n = parent.getChildCount();
        for (int i = 0; i < n; i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos == RecyclerView.NO_POSITION) {
                continue;
            }
            if (!predicate.shouldDrawDividerAfter(pos, itemCount)) {
                continue;
            }
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getRight() + lp.rightMargin;
            int right = left + thicknessPx;
            canvas.drawRect(left, topBase, right, bottomBase, paint);
        }
    }

    /**
     * 构建器：链式配置后 {@link #build()}。
     */
    public static final class Builder {
        private int orientation = LinearLayoutManager.VERTICAL;
        private int thicknessPx = 1;
        @ColorInt
        private int color = 0xFFCCCCCC;
        private boolean drawDividerAfterLastItem = true;
        @Nullable
        private BetweenItemsPredicate predicate;
        private int drawInsetStartPx;
        private int drawInsetEndPx;

        public Builder() {
        }

        /** 列表方向：{@link LinearLayoutManager#VERTICAL} 或 {@link LinearLayoutManager#HORIZONTAL} */
        @NonNull
        public Builder setOrientation(int orientation) {
            if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
                throw new IllegalArgumentException("orientation 必须为 LinearLayoutManager.VERTICAL 或 HORIZONTAL");
            }
            this.orientation = orientation;
            return this;
        }

        /** 分割线厚度：纵向列表为「高度」，横向列表为「宽度」，单位 px */
        @NonNull
        public Builder setThicknessPx(int thicknessPx) {
            this.thicknessPx = Math.max(0, thicknessPx);
            return this;
        }

        @NonNull
        public Builder setColor(@ColorInt int color) {
            this.color = color;
            return this;
        }

        /**
         * 是否在「最后一个 adapter item」之后仍绘制/预留分割线。
         * 若为 false，最后一项的 outRect 为 0，且不会在列表末尾画线。
         */
        @NonNull
        public Builder setDrawDividerAfterLastItem(boolean drawDividerAfterLastItem) {
            this.drawDividerAfterLastItem = drawDividerAfterLastItem;
            return this;
        }

        /**
         * 完全自定义「在哪些 adapter 位置之后画线」；若设置，则忽略 {@link #setDrawDividerAfterLastItem(boolean)}。
         */
        @NonNull
        public Builder setBetweenItemsPredicate(@NonNull BetweenItemsPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        /**
         * 绘制时在 padding 内侧再缩进，便于与列表左右/上下边距对齐（单位 px，非负）。
         * 纵向列表：start=左、end=右；横向列表：start=上、end=下。
         */
        @NonNull
        public Builder setDrawInsetsPx(int startPx, int endPx) {
            this.drawInsetStartPx = startPx;
            this.drawInsetEndPx = endPx;
            return this;
        }

        @NonNull
        public FlexibleRecyclerViewDividerDecoration build() {
            BetweenItemsPredicate p = predicate;
            if (p == null) {
                final boolean afterLast = drawDividerAfterLastItem;
                p = (afterAdapterPosition, itemCount) -> {
                    if (itemCount <= 0 || afterAdapterPosition < 0 || afterAdapterPosition >= itemCount) {
                        return false;
                    }
                    if (afterAdapterPosition == itemCount - 1) {
                        return afterLast;
                    }
                    return true;
                };
            }
            return new FlexibleRecyclerViewDividerDecoration(this, p);
        }
    }

    private FlexibleRecyclerViewDividerDecoration(Builder b, BetweenItemsPredicate p) {
        this.orientation = b.orientation;
        this.thicknessPx = b.thicknessPx;
        this.predicate = p;
        this.drawInsetStartPx = Math.max(0, b.drawInsetStartPx);
        this.drawInsetEndPx = Math.max(0, b.drawInsetEndPx);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(b.color);
        paint.setStyle(Paint.Style.FILL);
    }
}
