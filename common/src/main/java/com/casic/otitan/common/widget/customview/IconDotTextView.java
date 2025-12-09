package com.casic.otitan.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.casic.otitan.common.R;
import com.casic.otitan.common.utils.common.DensityUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 图标+文字+数字角标的自定义View
 * 支持多种布局方向和角标自定义
 * Created by fz on 2017/4/24.
 */
public class IconDotTextView extends ConstraintLayout {

    // 布局方向定义
    public static final int DIRECTION_ICON_TOP_TEXT_BOTTOM = 0;
    public static final int DIRECTION_ICON_BOTTOM_TEXT_TOP = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DIRECTION_ICON_TOP_TEXT_BOTTOM, DIRECTION_ICON_BOTTOM_TEXT_TOP})
    public @interface LayoutDirection {
    }
    /**
     * label是否加粗，默认不加粗
     */
    protected boolean isBold = false;
    // 默认值
    private static final int DEFAULT_DOT_MAX_COUNT = 99;
    private static final String DEFAULT_MORE_TEXT = "99+";
    private int DEFAULT_DOT_SIZE;
    private int DEFAULT_DOT_TEXT_SIZE;

    // View组件
    private AppCompatImageView mIconView;
    private AppCompatTextView mTitleView;
    private AppCompatTextView mDotView;

    // 属性值
    private int mImageWidth;
    private int mImageHeight;
    private Drawable mDrawableImage;
    private String mLabel;
    // 文字相关属性
    private float labelTextSize;
    private int labelTextColor;
    // 角标相关属性
    private int mDotTextSize;
    private int mDotTextColor;
    private int mDotBackgroundColor;
    private Drawable mDotBackgroundDrawable;
    private int mDotWidth;
    private int mDotHeight;
    private int mDotPadding;
    private boolean mShowDot;
    private int line;
    private int mDotMaxCount;
    // 角标偏移量
    private int mDotOffsetX;
    private int mDotOffsetY;
    // 布局相关属性
    private int mLayoutDirection;
    private int mIconTextSpacing;

    // 当前角标数量
    private int mCurrentCount = 0;

    public IconDotTextView(Context context) {
        super(context);
        init(null);
    }

    public IconDotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView));
    }

    public IconDotTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView, defStyleAttr, 0));
    }

    public IconDotTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context.obtainStyledAttributes(attrs, R.styleable.IconDotTextView, defStyleAttr, defStyleRes));
    }

    /**
     * 初始化View，动态创建所有子View
     */
    private void init(TypedArray a) {
        setClipChildren(false);
        setClipToPadding(false);
        DEFAULT_DOT_SIZE = DensityUtil.dp2px(getContext(), 16);
        DEFAULT_DOT_TEXT_SIZE = DensityUtil.sp2px(getContext(), 10);
        // 获取所有属性值
        parseAttributes(a);

        // 动态创建子View
        createSubViews();
        // 应用布局方向
        applyLayoutDirection();
    }

    /**
     * 解析XML中设置的属性
     */
    private void parseAttributes(TypedArray a) {
        Context context = getContext();
        if (a == null) {
            labelTextSize = DensityUtil.sp2px(context, 12);
            labelTextColor = ContextCompat.getColor(context, R.color.autoColor);
            mImageWidth = 0;
            mImageHeight = 0;
            mDotTextSize = DEFAULT_DOT_TEXT_SIZE;
            mDotTextColor = ContextCompat.getColor(context, R.color.white);
            mDotBackgroundColor = ContextCompat.getColor(context, R.color.theme_red);
            mDotWidth = DEFAULT_DOT_SIZE;
            mDotHeight = DEFAULT_DOT_SIZE;
            mShowDot = false;
            line = -1;
            isBold = false;
            mDotMaxCount = DEFAULT_DOT_MAX_COUNT;
            mDotPadding = DensityUtil.dp2px(context, 2f);
            mLayoutDirection = DIRECTION_ICON_TOP_TEXT_BOTTOM;
            mIconTextSpacing = DensityUtil.dp2px(context, 12f);
            // ... 默认值 ...
            mDotOffsetX = DensityUtil.dp2px(context, DEFAULT_DOT_SIZE/3.0f); // 默认向右超出8dp
            mDotOffsetY = DensityUtil.dp2px(context, DEFAULT_DOT_SIZE/3.0f); // 默认向上超出8dp
        } else {
            // 原有属性
            labelTextSize = a.getDimension(R.styleable.IconDotTextView_textSize,
                    DensityUtil.sp2px(context, 12));
            labelTextColor = a.getColor(R.styleable.IconDotTextView_textColor,
                    ContextCompat.getColor(context, R.color.autoColor));
            mImageWidth = a.getDimensionPixelSize(R.styleable.IconDotTextView_imageWidth, 0);
            mImageHeight = a.getDimensionPixelSize(R.styleable.IconDotTextView_imageHeight, 0);
            mDrawableImage = a.getDrawable(R.styleable.IconDotTextView_imageSrc);
            mLabel = a.getString(R.styleable.IconDotTextView_label);

            // 角标属性（新增）
            mDotTextSize = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotTextSize,
                    DEFAULT_DOT_TEXT_SIZE);
            mDotTextColor = a.getColor(R.styleable.IconDotTextView_dotTextColor,
                    ContextCompat.getColor(context, R.color.white));
            mDotBackgroundDrawable = a.getDrawable(R.styleable.IconDotTextView_dotBackground);
            mDotBackgroundColor = a.getColor(R.styleable.IconDotTextView_dotBackgroundColor,
                    ContextCompat.getColor(context, R.color.theme_red));
            mDotWidth = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotWidth, DEFAULT_DOT_SIZE);
            mDotHeight = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotHeight, DEFAULT_DOT_SIZE);
            mDotPadding = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotPadding,
                    DensityUtil.dp2px(context, 2f));
            line = a.getInteger(R.styleable.IconDotTextView_line, 1);
            mShowDot = a.getBoolean(R.styleable.IconDotTextView_dotShow, false);
            mDotMaxCount = a.getInt(R.styleable.IconDotTextView_dotMaxCount, DEFAULT_DOT_MAX_COUNT);
            // 布局方向属性（新增）
            mLayoutDirection = a.getInt(R.styleable.IconDotTextView_layoutDirection,
                    DIRECTION_ICON_TOP_TEXT_BOTTOM);
            mDotOffsetX = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotOffsetX,
                    mDotWidth/3);
            mDotOffsetY = a.getDimensionPixelSize(R.styleable.IconDotTextView_dotOffsetY,
                    mDotHeight/3);
            isBold = a.getBoolean(R.styleable.IconDotTextView_isBold,
                    false);
            mIconTextSpacing = a.getDimensionPixelSize(R.styleable.IconDotTextView_iconTextSpacing,
                    DensityUtil.dp2px(context, 12f));

            a.recycle();
        }

        // 如果没有设置角标背景drawable，创建默认的圆形背景
        if (mDotBackgroundDrawable == null) {
            mDotBackgroundDrawable = ContextCompat.getDrawable(getContext(),R.drawable.ic_red_dot);
        }
    }

    /**
     * 动态创建所有子View
     */
    private void createSubViews() {
        // 创建图标View
        mIconView = new AppCompatImageView(getContext());
        mIconView.setId(View.generateViewId());
        // 设置图标尺寸
        ConstraintLayout.LayoutParams iconParams =
                new ConstraintLayout.LayoutParams(
                        mImageWidth > 0 ? mImageWidth : LayoutParams.WRAP_CONTENT,
                        mImageHeight > 0 ? mImageHeight : LayoutParams.WRAP_CONTENT);
        if (mDrawableImage != null) {
            mIconView.setImageDrawable(mDrawableImage);
        }
        // 创建标题View
        mTitleView = new AppCompatTextView(getContext());
        mTitleView.setId(View.generateViewId());
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setIncludeFontPadding(false);
        ConstraintLayout.LayoutParams labelParams = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
        mTitleView.setTextColor(labelTextColor);
        mTitleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        mTitleView.setText(mLabel);
        if (line > 0) {
            mTitleView.setMaxLines(line);
        } else {
            mTitleView.setMaxLines(Integer.MAX_VALUE);
        }
        if (isBold) {
            // 设置为加粗
            mTitleView.setTypeface(mTitleView.getTypeface(), Typeface.BOLD);
        } else {
            // 取消加粗（恢复正常）
            mTitleView.setTypeface(mTitleView.getTypeface(), Typeface.NORMAL);
        }
        // 创建角标View
        mDotView = new AppCompatTextView(getContext());
        mDotView.setId(View.generateViewId());
        mDotView.setGravity(Gravity.CENTER);
        mDotView.setIncludeFontPadding(false);
        mDotView.setVisibility(mShowDot ? VISIBLE : GONE);
        // 设置角标尺寸
        ConstraintLayout.LayoutParams dotParams = new ConstraintLayout.LayoutParams(mDotWidth, mDotHeight);
        // 设置角标背景
        if (mDotBackgroundDrawable != null) {
            mDotView.setBackground(mDotBackgroundDrawable);
        } else {
            mDotView.setBackgroundColor(mDotBackgroundColor);
        }
        // 设置角标文字样式
        mDotView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDotTextSize);
        mDotView.setTextColor(mDotTextColor);
        mDotView.setPadding(mDotPadding, mDotPadding, mDotPadding, mDotPadding);
        mDotView.setEllipsize(TextUtils.TruncateAt.END);
        mDotView.setLines(1);
        // 添加到父布局
        addView(mIconView, iconParams);
        addView(mTitleView, labelParams);
        addView(mDotView, dotParams);
    }

    /**
     * 应用布局方向
     */
    private void applyLayoutDirection() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        // 根据方向设置约束
        switch (mLayoutDirection) {
            case DIRECTION_ICON_BOTTOM_TEXT_TOP:
                setupIconBottomTextTop(constraintSet);
                break;
            case DIRECTION_ICON_TOP_TEXT_BOTTOM:
            default:
                setupIconTopTextBottom(constraintSet);
                break;
        }

        // 设置角标约束（始终在图标右上角）
        setupDotConstraints(constraintSet);
        // 应用约束
        constraintSet.applyTo(this);
    }

    /**
     * 设置图标在上，文字在下的布局约束
     */
    private void setupIconTopTextBottom(ConstraintSet constraintSet) {
        // 图标约束
        constraintSet.connect(mIconView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mIconView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mIconView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.setMargin(mIconView.getId(), ConstraintSet.TOP, mDotOffsetY);
        constraintSet.setMargin(mIconView.getId(), ConstraintSet.END, mDotOffsetX);
        // 文字约束
        constraintSet.connect(mTitleView.getId(), ConstraintSet.TOP, mIconView.getId(), ConstraintSet.BOTTOM, mIconTextSpacing);
        constraintSet.connect(mTitleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mTitleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    }

    /**
     * 设置图标在下，文字在上的布局约束
     */
    private void setupIconBottomTextTop(ConstraintSet constraintSet) {
        // 文字约束
        constraintSet.connect(mTitleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mTitleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mTitleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.setMargin(mTitleView.getId(), ConstraintSet.TOP, mDotOffsetY);
        constraintSet.setMargin(mTitleView.getId(), ConstraintSet.END, mDotOffsetX);
        // 图标约束
        constraintSet.connect(mIconView.getId(), ConstraintSet.TOP, mTitleView.getId(), ConstraintSet.BOTTOM, mIconTextSpacing);
        constraintSet.connect(mIconView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mIconView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    }

    /**
     * 设置角标约束（始终在图标右上角）
     */
    private void setupDotConstraints(ConstraintSet constraintSet) {
        // 修正：角标应该约束到图标上，而不是父容器
        constraintSet.connect(mDotView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(mDotView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 强制角标View的绘制顺序，确保它在最上层
        mDotView.bringToFront();
    }
    // ==================== 公共方法 ====================
    /**
     * 修正setDotOffset方法
     */
    public void setDotOffset(int offsetX, int offsetY) {
        mDotOffsetX = offsetX;
        mDotOffsetY = offsetY;
        // 重新应用约束
        applyLayoutDirection();
    }

    public void setDotOffsetDp(float offsetXDp, float offsetYDp) {
        mDotOffsetX = DensityUtil.dp2px(getContext(), offsetXDp);
        mDotOffsetY = DensityUtil.dp2px(getContext(), offsetYDp);
        applyLayoutDirection();
    }
    /**
     * 显示角标数字
     * @param count 角标数量，小于等于0时隐藏角标
     */
    public void showRedDot(int count) {
        mCurrentCount = count;
        mDotView.setVisibility(count > 0 ? VISIBLE : GONE);
        if (count > mDotMaxCount) {
            mDotView.setText(DEFAULT_MORE_TEXT);
        } else if (count > 0) {
            mDotView.setText(String.valueOf(count));
        }
    }

    /**
     * 设置文字是否加粗
     * @param isBold 加粗label
     */
    public void setTextStyle(boolean isBold){
        this.isBold = isBold;
        if (isBold) {
            // 设置为加粗
            mTitleView.setTypeface(mTitleView.getTypeface(), Typeface.BOLD);
        } else {
            // 取消加粗（恢复正常）
            mTitleView.setTypeface(mTitleView.getTypeface(), Typeface.NORMAL);
        }
    }

    /**
     * 设置角标文字
     * @param text 角标文字（如"New"、"Hot"等）
     */
    public void setDotText(String text) {
        mDotView.setText(text);
        mDotView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    /**
     * 隐藏角标
     */
    public void hideDot() {
        mDotView.setVisibility(GONE);
    }

    /**
     * 显示角标
     */
    public void showDot() {
        mDotView.setVisibility(VISIBLE);
    }

    /**
     * 设置角标背景颜色
     * @param color 颜色资源ID
     */
    public void setDotBackgroundColor(int color) {
        mDotBackgroundColor = ContextCompat.getColor(getContext(), color);
        if (mDotView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) mDotView.getBackground()).setColor(mDotBackgroundColor);
        }
    }

    /**
     * 设置角标背景Drawable
     * @param drawable Drawable资源
     */
    public void setDotBackgroundDrawable(Drawable drawable) {
        mDotBackgroundDrawable = drawable;
        mDotView.setBackground(drawable);
    }

    /**
     * 设置角标文字颜色
     * @param color 颜色资源ID
     */
    public void setDotTextColor(int color) {
        mDotTextColor = ContextCompat.getColor(getContext(), color);
        mDotView.setTextColor(mDotTextColor);
    }

    /**
     * 设置角标文字大小
     * @param sizeSp 文字大小（单位：sp）
     */
    public void setDotTextSize(float sizeSp) {
        mDotTextSize = DensityUtil.sp2px(getContext(), sizeSp);
        mDotView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDotTextSize);
    }

    /**
     * 设置角标尺寸
     * @param width 宽度（像素）
     * @param height 高度（像素）
     */
    public void setDotSize(int width, int height) {
        mDotWidth = width;
        mDotHeight = height;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mDotView.getLayoutParams();
        params.width = width;
        params.height = height;
        mDotView.setLayoutParams(params);
    }

    /**
     * 设置角标内边距
     * @param padding 内边距（像素）
     */
    public void setDotPadding(int padding) {
        mDotPadding = padding;
        mDotView.setPadding(padding, padding, padding, padding);
    }

    /**
     * 设置布局方向
     * @param direction 布局方向，使用预定义的DIRECTION常量
     */
    public void setLayoutDirection(@LayoutDirection int direction) {
        mLayoutDirection = direction;
        applyLayoutDirection();
    }

    /**
     * 设置图标与文字的间距
     * @param spacing 间距（像素）
     */
    public void setIconTextSpacing(int spacing) {
        mIconTextSpacing = spacing;
        applyLayoutDirection(); // 重新应用布局
    }

    /**
     * 设置角标最大显示数字
     * @param maxCount 最大数字，超过此数字显示"99+"（或其他默认文本）
     */
    public void setDotMaxCount(int maxCount) {
        mDotMaxCount = maxCount;
        showRedDot(mCurrentCount); // 重新应用当前计数
    }

    /**
     * 使用Glide加载网络图片
     * @param src 图片URL
     */
    public void setGlideImage(String src) {
        Glide.with(getContext())
                .load(src)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.ic_default_image)
                        .error(R.mipmap.ic_default_image))
                .into(mIconView);
    }

    /**
     * 设置图标Drawable
     * @param drawable Drawable资源
     */
    public void setDrawableImage(Drawable drawable) {
        mDrawableImage = drawable;
        mIconView.setImageDrawable(drawable);
    }

    /**
     * 设置标题文字
     * @param label 标题文字
     */
    public void setLabel(String label) {
        mLabel = label;
        mTitleView.setText(label);
    }

    /**
     * 初始化View
     * @param resId 图标资源ID
     * @param strId 文字资源ID
     * @param tag 标签
     */
    public void init(@DrawableRes int resId, @StringRes int strId, String tag) {
        mIconView.setImageResource(resId);
        mTitleView.setText(strId);
        mDotView.setTag(getResources().getString(strId));
        mTitleView.setTag(tag);
    }

    /**
     * 获取角标文字
     * @return 角标文字
     */
    public String getDotText() {
        return mDotView.getText().toString();
    }

    /**
     * 获取标题文字
     * @return 标题文字
     */
    public String getLabelText() {
        return mTitleView.getText().toString();
    }

    /**
     * 获取当前角标数量
     * @return 当前角标数量
     */
    public int getCurrentDotCount() {
        return mCurrentCount;
    }

    /**
     * 获取图标View，用于进一步自定义
     * @return 图标ImageView
     */
    public AppCompatImageView getIconView() {
        return mIconView;
    }

    /**
     * 获取标题View，用于进一步自定义
     * @return 标题TextView
     */
    public AppCompatTextView getTitleView() {
        return mTitleView;
    }

    /**
     * 获取角标View，用于进一步自定义
     * @return 角标TextView
     */
    public AppCompatTextView getDotView() {
        return mDotView;
    }
}