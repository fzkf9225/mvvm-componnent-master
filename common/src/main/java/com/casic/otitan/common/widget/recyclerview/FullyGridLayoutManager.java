package com.casic.otitan.common.widget.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by fz on 2018/7/30.
 * 解决scrollView嵌套RecyclerView显示不全和焦点冲突的问题
 */
public class FullyGridLayoutManager extends GridLayoutManager {
    public FullyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public FullyGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    private final int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width = 0;
        int height = 0;
        int count = getItemCount();

        // 处理空数据情况
        if (count == 0) {
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        int span = getSpanCount();
        for (int i = 0; i < count; i++) {
            // 修复：添加状态检查，确保位置有效
            if (state.isMeasuring() && i >= state.getItemCount()) {
                break;
            }

            measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                if (i % span == 0) {
                    width += mMeasuredDimension[0];
                }
                if (i == 0) {
                    height = mMeasuredDimension[1];
                }
            } else {
                if (i % span == 0) {
                    height += mMeasuredDimension[1];
                }
                if (i == 0) {
                    width = mMeasuredDimension[0];
                }
            }
        }

        // 根据测量模式决定最终尺寸
        width = (widthMode == View.MeasureSpec.EXACTLY) ? widthSize : Math.min(width, widthSize);
        height = (heightMode == View.MeasureSpec.EXACTLY) ? heightSize : Math.min(height, heightSize);

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        // 重置测量结果
        measuredDimension[0] = 0;
        measuredDimension[1] = 0;

        // 修复：添加额外的安全检查
        if (position < 0 || position >= getItemCount()) {
            return;
        }

        View view = null;
        try {
            // 修复：使用更安全的方式获取视图
            view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
            }
        } catch (IndexOutOfBoundsException e) {
            // 捕获并忽略索引越界异常
            // 这种情况通常发生在数据变化和测量不同步时
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 修复：确保视图被回收，但要避免重复回收
            if (view != null && recycler != null) {
                try {
                    recycler.recycleView(view);
                } catch (Exception e) {
                    // 忽略回收时的异常
                }
            }
        }
    }
}