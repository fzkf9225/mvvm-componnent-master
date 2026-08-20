package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;

/**
 * 自动换行
 * 修复：对齐逻辑错误，新增清除子View等API
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @update 2026/6/22 23:09
 */
public class AutoNextLineLinearlayout extends ViewGroup {

    /**
     * 对齐方式常量
     */
    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_CENTER = 2;
    public static final int GRAVITY_RIGHT = 0;

    private final Type mType;
    private final List<WarpLine> mWarpLineGroup = new ArrayList<>();
    private final WarpLine warpLine = new WarpLine();

    public AutoNextLineLinearlayout(Context context) {
        this(context, null);
    }

    public AutoNextLineLinearlayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.WarpLinearLayoutDefault);
    }

    public AutoNextLineLinearlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mType = new Type(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int withMode = MeasureSpec.getMode(widthMeasureSpec);
        int withSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int with = 0;
        int height = 0;
        int childCount = getChildCount();

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // 计算宽度
        switch (withMode) {
            case MeasureSpec.AT_MOST:
                for (int i = 0; i < childCount; i++) {
                    if (i != 0) {
                        with += (int) mType.horizontalSpace;
                    }
                    with += getChildAt(i).getMeasuredWidth();
                }
                with += getPaddingLeft() + getPaddingRight();
                with = Math.min(with, withSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                for (int i = 0; i < childCount; i++) {
                    if (i != 0) {
                        with += (int) mType.horizontalSpace;
                    }
                    with += getChildAt(i).getMeasuredWidth();
                }
                with += getPaddingLeft() + getPaddingRight();
                break;
            default:
                with = withSize;
                break;
        }

        // 重新计算换行
        mWarpLineGroup.clear();
        warpLine.lineView.clear();
        warpLine.lineWidth = getPaddingLeft() + getPaddingRight();
        warpLine.height = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 判断是否需要换行：当前行宽度 + 间隔 + 子View宽度 > 总宽度
            if (warpLine.lineWidth + mType.horizontalSpace + childWidth > with) {
                // 如果当前行没有子View，强制放入（处理单个View宽度超过容器宽度的情况）
                if (warpLine.lineView.isEmpty()) {
                    warpLine.addView(child);
                    mWarpLineGroup.add(warpLine);
                    // 重置当前行
                    warpLine.lineView.clear();
                    warpLine.lineWidth = getPaddingLeft() + getPaddingRight();
                    warpLine.height = 0;
                } else {
                    // 保存当前行，开启新行
                    mWarpLineGroup.add(warpLine);
                    // 重置当前行
                    warpLine.lineView.clear();
                    warpLine.lineWidth = getPaddingLeft() + getPaddingRight();
                    warpLine.height = 0;
                    warpLine.addView(child);
                }
            } else {
                warpLine.addView(child);
            }
        }

        // 添加最后一行
        if (!warpLine.lineView.isEmpty()) {
            mWarpLineGroup.add(warpLine);
        }

        // 计算高度
        height = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < mWarpLineGroup.size(); i++) {
            if (i != 0) {
                height += (int) mType.verticalSpace;
            }
            height += mWarpLineGroup.get(i).height;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(height, heightSize);
                break;
            default:
                break;
        }

        setMeasuredDimension(with, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();
        int gravity = getGravity();

        for (int i = 0; i < mWarpLineGroup.size(); i++) {
            WarpLine warpLine = mWarpLineGroup.get(i);
            List<View> lineViews = warpLine.lineView;

            if (lineViews.isEmpty()) {
                continue;
            }

            // 计算当前行剩余空间
            int remainWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - warpLine.lineWidth;

            // 如果是充满模式，每个子View平均分配剩余空间
            if (isFull()) {
                int extraSpace = remainWidth / lineViews.size();
                int left = getPaddingLeft();
                for (int j = 0; j < lineViews.size(); j++) {
                    View view = lineViews.get(j);
                    int width = view.getMeasuredWidth() + extraSpace;
                    view.layout(left, top, left + width, top + view.getMeasuredHeight());
                    left += width + (int) mType.horizontalSpace;
                }
            } else {
                // 根据对齐方式计算起始偏移
                int startOffset = 0;
                if (gravity == GRAVITY_RIGHT) {
                    startOffset = remainWidth;
                } else if (gravity == GRAVITY_CENTER) {
                    startOffset = remainWidth / 2;
                }
                // GRAVITY_LEFT: startOffset = 0

                int left = getPaddingLeft() + startOffset;
                for (int j = 0; j < lineViews.size(); j++) {
                    View view = lineViews.get(j);
                    view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
                    left += view.getMeasuredWidth() + (int) mType.horizontalSpace;
                }
            }

            top += warpLine.height + (int) mType.verticalSpace;
        }
    }

    /**
     * 存放一行子View
     */
    private final class WarpLine {
        private final List<View> lineView = new ArrayList<>();
        private int lineWidth = getPaddingLeft() + getPaddingRight();
        private int height = 0;

        private void addView(View view) {
            if (!lineView.isEmpty()) {
                lineWidth += (int) mType.horizontalSpace;
            }
            height = Math.max(height, view.getMeasuredHeight());
            lineWidth += view.getMeasuredWidth();
            lineView.add(view);
        }
    }

    /**
     * 样式配置
     */
    private static final class Type {
        private int gravity = GRAVITY_LEFT;
        private float horizontalSpace = 0;
        private float verticalSpace = 0;
        private boolean isFull = false;

        Type(Context context, AttributeSet attrs) {
            if (attrs == null) {
                return;
            }
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WarpLinearLayout);
            gravity = typedArray.getInt(R.styleable.WarpLinearLayout_gravity, GRAVITY_LEFT);
            horizontalSpace = typedArray.getDimension(R.styleable.WarpLinearLayout_horizontalSpace, horizontalSpace);
            verticalSpace = typedArray.getDimension(R.styleable.WarpLinearLayout_verticalSpace, verticalSpace);
            isFull = typedArray.getBoolean(R.styleable.WarpLinearLayout_isFull, isFull);
            typedArray.recycle();
        }
    }

    // ==================== Getter / Setter ====================

    public int getGravity() {
        return mType.gravity;
    }

    public float getHorizontalSpace() {
        return mType.horizontalSpace;
    }

    public float getVerticalSpace() {
        return mType.verticalSpace;
    }

    public boolean isFull() {
        return mType.isFull;
    }

    public void setGravity(int gravity) {
        if (mType.gravity != gravity) {
            mType.gravity = gravity;
            requestLayout();
        }
    }

    public void setHorizontalSpace(float horizontalSpace) {
        if (mType.horizontalSpace != horizontalSpace) {
            mType.horizontalSpace = horizontalSpace;
            requestLayout();
        }
    }

    public void setVerticalSpace(float verticalSpace) {
        if (mType.verticalSpace != verticalSpace) {
            mType.verticalSpace = verticalSpace;
            requestLayout();
        }
    }

    public void setIsFull(boolean isFull) {
        if (mType.isFull != isFull) {
            mType.isFull = isFull;
            requestLayout();
        }
    }

    // ==================== 新增API ====================

    /**
     * 清除所有子View
     */
    public void removeAllChildren() {
        removeAllViews();
        mWarpLineGroup.clear();
        warpLine.lineView.clear();
        warpLine.lineWidth = getPaddingLeft() + getPaddingRight();
        warpLine.height = 0;
        requestLayout();
    }

    /**
     * 移除指定位置的子View
     * @param index 位置索引
     * @return 被移除的View
     */
    public View removeChildAt(int index) {
        if (index < 0 || index >= getChildCount()) {
            return null;
        }
        View view = getChildAt(index);
        removeViewAt(index);
        requestLayout();
        return view;
    }

    /**
     * 移除指定的子View
     * @param view 要移除的View
     * @return 是否移除成功
     */
    public boolean removeChild(View view) {
        if (view == null || view.getParent() != this) {
            return false;
        }
        removeView(view);
        requestLayout();
        return true;
    }

    /**
     * 移除所有可见的子View（保留GONE状态的View）
     */
    public void removeVisibleChildren() {
        List<View> toRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                toRemove.add(child);
            }
        }
        for (View view : toRemove) {
            removeView(view);
        }
        requestLayout();
    }

    /**
     * 移除所有不可见的子View（GONE状态的View）
     */
    public void removeGoneChildren() {
        List<View> toRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                toRemove.add(child);
            }
        }
        for (View view : toRemove) {
            removeView(view);
        }
        requestLayout();
    }

    /**
     * 获取当前所有子View（包括GONE）
     */
    public List<View> getAllChildren() {
        List<View> children = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            children.add(getChildAt(i));
        }
        return children;
    }

    /**
     * 获取可见的子View列表（不包括GONE）
     */
    public List<View> getVisibleChildren() {
        List<View> visible = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                visible.add(child);
            }
        }
        return visible;
    }

    /**
     * 获取当前行数
     */
    public int getLineCount() {
        return mWarpLineGroup.size();
    }

    /**
     * 获取指定行的子View列表
     * @param lineIndex 行索引
     * @return 该行的子View列表，如果索引无效返回空列表
     */
    public List<View> getLineChildren(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= mWarpLineGroup.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mWarpLineGroup.get(lineIndex).lineView);
    }

    /**
     * 刷新布局（当子View内容变化时调用）
     */
    public void refreshLayout() {
        requestLayout();
    }

    /**
     * 批量添加子View
     * @param views 要添加的View列表
     */
    public void addChildren(List<View> views) {
        if (views == null || views.isEmpty()) {
            return;
        }
        for (View view : views) {
            if (view != null) {
                addView(view);
            }
        }
        requestLayout();
    }

    /**
     * 在指定位置插入子View
     * @param index 插入位置
     * @param view 要插入的View
     */
    public void addChildAt(int index, View view) {
        if (view == null) {
            return;
        }
        if (index < 0 || index > getChildCount()) {
            addView(view);
        } else {
            addView(view, index);
        }
        requestLayout();
    }

    /**
     * 交换两个子View的位置
     * @param index1 位置1
     * @param index2 位置2
     * @return 是否交换成功
     */
    public boolean swapChildren(int index1, int index2) {
        int childCount = getChildCount();
        if (index1 < 0 || index1 >= childCount || index2 < 0 || index2 >= childCount || index1 == index2) {
            return false;
        }
        View view1 = getChildAt(index1);
        View view2 = getChildAt(index2);
        removeViewAt(Math.max(index1, index2));
        removeViewAt(Math.min(index1, index2));
        addView(view2, Math.min(index1, index2));
        addView(view1, Math.max(index1, index2));
        requestLayout();
        return true;
    }

    /**
     * 获取所有子View的数量（包括GONE）
     */
    public int getChildrenCount() {
        return getChildCount();
    }

    /**
     * 获取可见子View的数量（不包括GONE）
     */
    public int getVisibleChildrenCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getVisibility() != GONE) {
                count++;
            }
        }
        return count;
    }
}