package pers.fz.mvvm.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2017/10/30.
 * describe：自动换行
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AutoNextLineLinearlayout extends ViewGroup  {
    private final Type mType;
    private List<WarpLine> mWarpLineGroup;

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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int withMode = MeasureSpec.getMode(widthMeasureSpec);
        int withSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int with = 0;
        int height = 0;
        int childCount = getChildCount();
        /*
         * 在调用childView。getMeasure之前必须先调用该行代码，用于对子View大小的测量
         */
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        /*
         * 计算宽度
         */
        switch (withMode) {
            case MeasureSpec.EXACTLY:
                with = withSize;
                break;
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
        /*
         * 根据计算出的宽度，计算出所需要的行数
         */
        WarpLine warpLine = new WarpLine();
        /*
         * 不能够在定义属性时初始化，因为onMeasure方法会多次调用
         */
        mWarpLineGroup = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            if (warpLine.lineWidth + getChildAt(i).getMeasuredWidth() + mType.horizontalSpace > with) {
                if (warpLine.lineView.isEmpty()) {
                    warpLine.addView(getChildAt(i));
                    mWarpLineGroup.add(warpLine);
                    warpLine = new WarpLine();
                } else {
                    mWarpLineGroup.add(warpLine);
                    warpLine = new WarpLine();
                    warpLine.addView(getChildAt(i));
                }
            } else {
                warpLine.addView(getChildAt(i));
            }
        }
        /*
         * 添加最后一行
         */
        if (!warpLine.lineView.isEmpty() && !mWarpLineGroup.contains(warpLine)) {
            mWarpLineGroup.add(warpLine);
        }
        /* 
         * 计算宽度
         */
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
            case MeasureSpec.UNSPECIFIED:
                break;
            default:
                break;
        }
        setMeasuredDimension(with, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        t = getPaddingTop();
        for (int i = 0; i < mWarpLineGroup.size(); i++) {
            int left = getPaddingLeft();
            WarpLine warpLine = mWarpLineGroup.get(i);
            int lastWidth = getMeasuredWidth() - warpLine.lineWidth;
            for (int j = 0; j < warpLine.lineView.size(); j++) {
                View view = warpLine.lineView.get(j);
                if (isFull()) {//需要充满当前行时
                    view.layout(left, t, left + view.getMeasuredWidth() + lastWidth / warpLine.lineView.size(), t + view.getMeasuredHeight());
                    left += (int) (view.getMeasuredWidth() + mType.horizontalSpace + (float) lastWidth / warpLine.lineView.size());
                } else {
                    switch (getGravity()) {
                        case 0://右对齐
                            view.layout(left + lastWidth, t, left + lastWidth + view.getMeasuredWidth(), t + view.getMeasuredHeight());
                            break;
                        case 2://居中对齐
                            view.layout(left + lastWidth / 2, t, left + lastWidth / 2 + view.getMeasuredWidth(), t + view.getMeasuredHeight());
                            break;
                        default://左对齐
                            view.layout(left, t, left + view.getMeasuredWidth(), t + view.getMeasuredHeight());
                            break;
                    }
                    left += (int) (view.getMeasuredWidth() + mType.horizontalSpace);
                }
            }
            t += (int) (warpLine.height + mType.verticalSpace);
        }
    }

    /**
     * 用于存放一行子View
     */
    private final class WarpLine {
        private List<View> lineView = new ArrayList<View>();
        /**
         * 当前行中所需要占用的宽度
         */
        private int lineWidth = getPaddingLeft() + getPaddingRight();
        /**
         * 该行View中所需要占用的最大高度
         */
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
     * 对样式的初始化
     */
    private final static class Type {
        /*
         *对齐方式 right 0，left 1，center 2
        */
        private int gravity;
        /**
         * 水平间距,单位px
         */
        private float horizontalSpace;
        /**
         * 垂直间距,单位px
         */
        private float verticalSpace;
        /**
         * 是否自动填满
         */
        private boolean isFull;

        Type(Context context, AttributeSet attrs) {
            if (attrs == null) {
                return;
            }
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WarpLinearLayout);
            gravity = typedArray.getInt(R.styleable.WarpLinearLayout_gravity, gravity);
            horizontalSpace = typedArray.getDimension(R.styleable.WarpLinearLayout_horizontalSpace, horizontalSpace);
            verticalSpace = typedArray.getDimension(R.styleable.WarpLinearLayout_verticalSpace, verticalSpace);
            isFull = typedArray.getBoolean(R.styleable.WarpLinearLayout_isFull, isFull);
        }
    }

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
        mType.gravity = gravity;
    }

    public void setHorizontalSpace(float horizontalSpace) {
        mType.horizontalSpace = horizontalSpace;
    }

    public void setVerticalSpace(float verticalSpace) {
        mType.verticalSpace = verticalSpace;
    }

    public void setIsFull(boolean isFull) {
        mType.isFull = isFull;
    }
}
