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
 * Created by fz on 2018/9/25.
 */

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint mPaint;
    private final int mDividerWidth;

    public GridSpacingItemDecoration(int mDividerWidth, @ColorInt int color) {
        this.mDividerWidth = mDividerWidth;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
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

        int top = 0;
        int left;
        int right;
        int eachWidth = (spanCount - 1) * mDividerWidth / spanCount;
        int dl = mDividerWidth - eachWidth;

        left = itemPosition % spanCount * dl;
        right = eachWidth - left;
        int bottom = mDividerWidth;
        if (isLastRow) {
            bottom = 0;
        }
        outRect.set(left, top, right, bottom);

    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        draw(canvas, parent);
    }

    //绘制item分割线
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

            //画水平分隔线（与 getItemOffsets 一致：最后一行不再向下占位则不绘制底部分割线）
            if (!isLastRow(parent, pos, spanCount, adapterCount)) {
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getBottom() + layoutParams.bottomMargin;
                int bottom = top + mDividerWidth;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            //画垂直分割线（最后一列不绘制右侧分割线，避免与 offsets 语义不一致）
            if (!isLastColumn(parent, pos, spanCount, adapterCount)) {
                int top2 = child.getTop();
                int bottom2 = child.getBottom() + mDividerWidth;
                int left2 = child.getRight() + layoutParams.rightMargin;
                int right2 = left2 + mDividerWidth;
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

    //获取列数
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
