package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by fz on 2018/7/30.
 * 解决scrollView嵌套RecyclerView显示不全和焦点冲突的问题
 */
public class FullyLinearLayoutManager extends LinearLayoutManager {

    public FullyLinearLayoutManager(Context context) {
        super(context);
    }

    public FullyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    private final int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state,
                          int widthSpec, int heightSpec) {

        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width = 0;
        int height = 0;

        // 核心修复 1: 增加数据量校验，state.getItemCount() 比 getItemCount() 更准确
        int itemCount = state.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            // 核心修复 2: 传入正确的 position i
            measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                width = width + mMeasuredDimension[0];
                if (i == 0) {
                    height = mMeasuredDimension[1];
                }
            } else {
                height = height + mMeasuredDimension[1];
                if (i == 0) {
                    width = mMeasuredDimension[0];
                }
            }
        }

        // 测量模式修正
        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize;
        }

        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize;
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        try {
            // 核心修复 3: 检查 position 是否在有效范围内
            if (position < 0 || position >= getItemCount()) {
                return;
            }

            // 获取对应位置的 View 而不是硬编码 0
            View view = recycler.getViewForPosition(position);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);

            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    getPaddingTop() + getPaddingBottom(), p.height);

            view.measure(childWidthSpec, childHeightSpec);
            measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
            measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;

            // 记得回收 View，避免测量内存泄漏
            recycler.recycleView(view);
        } catch (Exception e) {
            // 捕获潜在的越界或空指针，防止应用闪退
            e.printStackTrace();
        }
    }
}