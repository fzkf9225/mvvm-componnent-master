package pers.fz.mvvm.wight.customlayout;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.wight.customlayout.round.EasyVerticalCenterSpan;
import pers.fz.mvvm.wight.customlayout.round.ShapeBuilder;
import pers.fz.mvvm.wight.customlayout.round.SpanContainer;

/**
 * Created by fz on 2023/5/23 16:29
 * describe :
 */
public class RoundTextView extends AppCompatTextView {

    private static final String EMPTY_SPACE = "\u3000";
    private final Context mContext;
    private int type = RECTANGLE;
    private float mRadius;
    private float mTopLeftRadius, mTopRightRadius, mBottomLeftRadius, mBottomRightRadius;
    private int mStrokeColor;
    private int mStrokeWidth;
    private int mSoild;
    private float mTextPadding;
    private CharSequence mTextLeft;
    private CharSequence mTextRight;
    private ColorStateList mIconColor = null;
    private int mCurIconColor;
    private CharSequence iconString;
    private ColorStateList mLeftColor = null;
    private int mCurLeftColor;
    private ColorStateList mRightColor = null;
    private int mCurRightColor;
    private float mLeftSize;
    private float mRightSize;
    private List<SpanContainer> leftContainer;
    private List<SpanContainer> rightContainer;
    private int mTextLeftStyle;
    private int mTextRightStyle;
    private int mTextCenterStyle;
    //icon的index
    private int iconIndex = 0;
    //是否开启计算文字边界，开启后会以最大文字大小为View高度，并且会增加部分文字高度，防止部分英文类型y,g由于基线的原因无法显示完全
    private boolean autoMaxHeight;
    //渐变色
    private ColorStateList startColor = null;
    private ColorStateList centerColor = null;
    private ColorStateList endColor = null;
    //渐变方向
    GradientDrawable.Orientation orientation;

    public RoundTextView(Context context) {
        this(context, null);
    }

    public RoundTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttr(context, attrs);
        init();
    }

    private void init() {
        initIconFont();
        initShape();
    }

    private void initIconFont() {
        iconString = getText().toString();
        int centerSize = iconString.length();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(getText());
        if (!TextUtils.isEmpty(mTextLeft) || !TextUtils.isEmpty(mTextRight)) {
            //增加空格
            if (!TextUtils.isEmpty(mTextLeft)) {
                if (mTextPadding != 0) {
                    stringBuilder.insert(0, EMPTY_SPACE);
                    iconIndex++;
                }
                stringBuilder.insert(0, mTextLeft);
                iconIndex += mTextLeft.length();
            }

            if (!TextUtils.isEmpty(mTextRight)) {
                if (mTextPadding != 0) {
                    stringBuilder.append(EMPTY_SPACE);
                }
                stringBuilder.append(mTextRight);
            }
            /*
             * ==============
             * 设置字和icon间距
             * ==============
             */
            if (mTextPadding != 0) {
                //设置字和icon间距
                if (!TextUtils.isEmpty(mTextLeft)) {
                    AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan((int) mTextPadding);
                    stringBuilder.setSpan(sizeSpan, iconIndex - 1, iconIndex, Spanned
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (!TextUtils.isEmpty(mTextRight)) {
                    AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan((int) mTextPadding);
                    stringBuilder.setSpan(sizeSpan, iconIndex + centerSize, iconIndex +
                            centerSize + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }
            /*
             * ==============
             * 设置左边文字样式
             * ==============
             */
            setLeftTextAttr(stringBuilder);
            /*
             * ==============
             * 设置右边文字样式
             * ==============
             */
            setRightTextAttr(centerSize, stringBuilder);
        }
        /*
         * ==============
         * 设置icon和字的颜色
         * ==============
         */
        if (mIconColor != null) {
            int color = mIconColor.getColorForState(getDrawableState(), 0);
            if (color != mCurIconColor) {
                mCurIconColor = color;
            }
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mCurIconColor);
            stringBuilder.setSpan(foregroundColorSpan, iconIndex, iconIndex + centerSize, Spanned
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            mCurIconColor = getCurrentTextColor();
        }
        /*
         * ==============
         * 设置icon的字的样式
         * ==============
         */
        initTextStyle(mTextCenterStyle, stringBuilder, iconIndex, iconIndex + centerSize);
        /*
         * ==============
         * 设置左右Span，记得调用前在**所有方法**前先clearSpan(),不然直接build，上一次的span任然保留着
         * ==============
         */
        if (leftContainer != null) {
            for (SpanContainer container : leftContainer) {
                for (Object o : container.spans) {
                    try {
                        stringBuilder.setSpan(o, container.start, container.end, container.flag);
                    } catch (Exception e) {
                        //please check invoke clearSpan() method first
                    }
                }
            }
        }
        if (rightContainer != null) {
            int start = mTextPadding == 0 ? iconIndex + centerSize : iconIndex + centerSize + 1;
            for (SpanContainer container : rightContainer) {
                for (Object o : container.spans) {
                    try {
                        stringBuilder.setSpan(o, start + container.start, start + container.end,
                                container.flag);
                    } catch (Exception e) {
                        //please check invoke clearSpan() method first
                    }
                }
            }
        }

        setText(stringBuilder);
    }

    private void setRightTextAttr(int centerSize, SpannableStringBuilder stringBuilder) {
        if (!TextUtils.isEmpty(mTextRight)) {
            int start = mTextPadding == 0 ? iconIndex + centerSize : iconIndex + centerSize + 1;
            /*
             * ==============
             * 设置右边字的粗体和斜体
             * ==============
             */
            initTextStyle(mTextRightStyle, stringBuilder, start, stringBuilder.length());
            /*
             * ==============
             * 设置右边字的颜色
             * ==============
             */
            initTextRightColor(stringBuilder, start);
            /*
             * ==============
             * 设置右边字的大小
             * ==============
             */
            initTextSize(stringBuilder, start, stringBuilder.length(), mRightSize, mCurRightColor);
        }
    }

    private void initTextRightColor(SpannableStringBuilder stringBuilder, int start) {
        if (mRightColor != null) {
            int color = mRightColor.getColorForState(getDrawableState(), 0);
            if (color != mCurRightColor) {
                mCurRightColor = color;
            }
            ForegroundColorSpan foregroundRightColor = new ForegroundColorSpan(mCurRightColor);
            stringBuilder.setSpan(foregroundRightColor, start, stringBuilder.length()
                    , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            mCurRightColor = getCurrentTextColor();
        }
    }

    private void setLeftTextAttr(SpannableStringBuilder stringBuilder) {
        if (!TextUtils.isEmpty(mTextLeft)) {
            int end = mTextPadding == 0 ? iconIndex : iconIndex - 1;
            /*
             * ==============
             * 设置左边字的粗体和斜体
             * ==============
             */
            initTextStyle(mTextLeftStyle, stringBuilder, 0, end);
            /*
             * ==============
             * 设置左边字的颜色
             * ==============
             */
            initTextLeftColor(stringBuilder, end);
            /*
             * ==============
             * 设置左边字的大小
             * ==============
             */
            initTextSize(stringBuilder, 0, end, mLeftSize, mCurLeftColor);
        }
    }

    private void initTextSize(SpannableStringBuilder stringBuilder, int start, int end, float
            textSize, int mCurColor) {
        if (textSize != 0) {
            CharacterStyle sizeSpan;
            final int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            if (gravity == Gravity.CENTER_VERTICAL) {
                sizeSpan = new EasyVerticalCenterSpan(textSize, mCurColor);
            } else {
                sizeSpan = new AbsoluteSizeSpan((int) textSize);
            }
            stringBuilder.setSpan(sizeSpan, start, end, Spanned
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void initTextLeftColor(SpannableStringBuilder stringBuilder, int end) {
        if (mLeftColor != null) {
            int color = mLeftColor.getColorForState(getDrawableState(), 0);
            if (color != mCurLeftColor) {
                mCurLeftColor = color;
            }
            ForegroundColorSpan foregroundLeftColor = new ForegroundColorSpan(mCurLeftColor);
            stringBuilder.setSpan(foregroundLeftColor, 0, end, Spanned
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            mCurLeftColor = getCurrentTextColor();
        }
    }

    private void initTextStyle(int textStyle, SpannableStringBuilder stringBuilder, int start,
                               int end) {
        StyleSpan span;
        if (textStyle != Typeface.NORMAL) {
            span = new StyleSpan(textStyle);
            stringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void initShape() {
        if (mRadius == -0 && mStrokeColor == -1 && mStrokeWidth == 0 && mSoild ==
                -1 && mTopLeftRadius == 0 && mTopRightRadius == 0 && mBottomLeftRadius == 0 &&
                mBottomRightRadius == 0 ) {
        } else {
            setShape();
        }
    }

    private void setShape() {
        ShapeBuilder shapeBuilder;
        if (mRadius != 0) {
            shapeBuilder = ShapeBuilder.create().Type(type).Radius(mRadius).Stroke
                    (mStrokeWidth, mStrokeColor);
        } else {
            if (mTopRightRadius != 0 || mTopLeftRadius != 0 || mBottomRightRadius != 0 || mBottomLeftRadius != 0) {
                shapeBuilder = ShapeBuilder.create().Type(type).RoundRadius(mTopLeftRadius,
                        mTopRightRadius, mBottomLeftRadius, mBottomRightRadius).Stroke
                        (mStrokeWidth, mStrokeColor);
            } else {
                shapeBuilder = ShapeBuilder.create().Type(type).Radius(mTopLeftRadius,
                        mTopRightRadius, mBottomLeftRadius, mBottomRightRadius).Stroke
                        (mStrokeWidth, mStrokeColor);
            }
        }
        if (orientation != null && startColor != null && endColor != null) {
            //渐变
            if (centerColor != null) {
                shapeBuilder.Gradient(orientation, getColor(startColor), getColor(centerColor),
                        getColor(endColor));
            } else {
                shapeBuilder.GradientInit(orientation, getColor(startColor), getColor(endColor));
            }
        } else {
            shapeBuilder.Soild(mSoild);
        }
        shapeBuilder.build(this);
    }

    private int getColor(ColorStateList color) {
        return color.getColorForState(getDrawableState(), 0);
    }

    private GradientDrawable.Orientation switchEnumToOrientation(int orientation) {
        switch (orientation) {
            case 0:
                return GradientDrawable.Orientation.TOP_BOTTOM;
            case 1:
                return GradientDrawable.Orientation.TR_BL;
            case 2:
                return GradientDrawable.Orientation.RIGHT_LEFT;
            case 3:
                return GradientDrawable.Orientation.BR_TL;
            case 4:
                return GradientDrawable.Orientation.BOTTOM_TOP;
            case 5:
                return GradientDrawable.Orientation.BL_TR;
            case 6:
                return GradientDrawable.Orientation.LEFT_RIGHT;
            case 7:
                return GradientDrawable.Orientation.TL_BR;
        }
        return GradientDrawable.Orientation.LEFT_RIGHT;
    }

    private void clearText() {
        setText(iconString);
        iconIndex = 0;
    }

    private void initAttr(Context context, AttributeSet attrs) {
        //左右文字支持xml中设置iconFont
        TypedValue textValue = new TypedValue();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RectgleTextView);
        type = array.getInteger(R.styleable.RectgleTextView_shapeType, 0);
        mRadius = array.getDimensionPixelOffset(R.styleable.RectgleTextView_totalRadius, 0);
        mTopLeftRadius = array.getDimensionPixelSize(R.styleable.RectgleTextView_topLeft, 0);
        mTopRightRadius = array.getDimensionPixelSize(R.styleable.RectgleTextView_topRight, 0);
        mBottomLeftRadius = array.getDimensionPixelSize(R.styleable.RectgleTextView_bottomLeft, 0);
        mBottomRightRadius = array.getDimensionPixelSize(R.styleable.RectgleTextView_bottomRight, 0);

        mStrokeColor = array.getColor(R.styleable.RectgleTextView_strColor, -1);
        mStrokeWidth = array.getDimensionPixelOffset(R.styleable.RectgleTextView_strWidth, 0);
        mSoild = array.getColor(R.styleable.RectgleTextView_solidBac, -1);
        mTextPadding = array.getDimensionPixelOffset(R.styleable.RectgleTextView_textPadding, 0);
        boolean has = array.getValue(R.styleable.RectgleTextView_textLeft, textValue);
        if (has) {
            if (textValue.type == TypedValue.TYPE_REFERENCE) {
                //文字引用
                mTextLeft = mContext.getResources().getText(textValue.resourceId);
            } else {
                //纯文字
                mTextLeft = textValue.string;
            }
        }
        has = array.getValue(R.styleable.RectgleTextView_textRight, textValue);
        if (has) {
            if (textValue.type == TypedValue.TYPE_REFERENCE) {
                //文字引用
                mTextRight = mContext.getResources().getText(textValue.resourceId);
            } else {
                //纯文字
                mTextRight = textValue.string;
            }
        }
        mIconColor = array.getColorStateList(R.styleable.RectgleTextView_iconColor);
        mLeftColor = array.getColorStateList(R.styleable.RectgleTextView_textLeftColor);
        mRightColor = array.getColorStateList(R.styleable.RectgleTextView_textRightColor);
        mLeftSize = array.getDimension(R.styleable.RectgleTextView_textLeftSize, 0);
        mRightSize = array.getDimension(R.styleable.RectgleTextView_textRightSize, 0);
        mTextLeftStyle = array.getInt(R.styleable.RectgleTextView_textLeftStyle, Typeface.NORMAL);
        mTextRightStyle = array.getInt(R.styleable.RectgleTextView_textRightStyle, Typeface.NORMAL);
        mTextCenterStyle = array.getInt(R.styleable.RectgleTextView_textCenterStyle, Typeface.NORMAL);
        autoMaxHeight = array.getBoolean(R.styleable.RectgleTextView_autoMaxHeight, false);
        orientation = switchEnumToOrientation(array.getInt(R.styleable
                .RectgleTextView_gradientOrientation, 0));
        startColor = array.getColorStateList(R.styleable.RectgleTextView_startSolid);
        centerColor = array.getColorStateList(R.styleable.RectgleTextView_centerSolid);
        endColor = array.getColorStateList(R.styleable.RectgleTextView_endSolid);
        array.recycle();
    }

    @Override
    protected void drawableStateChanged() {
        if (mIconColor != null && mIconColor.isStateful()
                || mLeftColor != null && mLeftColor.isStateful()
                || mRightColor != null && mRightColor.isStateful()) {
            clearText();
            initIconFont();
        }
        super.drawableStateChanged();
    }

    /**
     * 设置Shape Type
     */
    public void setType(int type) {
        this.type = type;
        setShape();
    }

    /**
     * 设置边线的宽度
     */
    public void setStrokeWidth(int value) {
        this.mStrokeWidth = value;
        setShape();
    }

    /**
     * 设置边线的颜色
     */
    public void setStrokeColor(@ColorInt int color) {
        this.mStrokeColor = color;
        setShape();
    }

    /**
     * 设置shape背景颜色
     */
    public void setSolid(int soild) {
        this.mSoild = soild;
        setShape();
    }

    /**
     * 设置radius
     */
    public void setRadius(int radius) {
        this.mRadius = radius;
        setShape();
    }

    /**
     * 设置icon颜色
     */
    public void setIconColor(int color) {
        this.mIconColor = ColorStateList.valueOf(color);
        build();
    }

    /**
     * 设置左文案
     */
    public void setTextLeft(CharSequence textLeft) {
        this.mTextLeft = textLeft;
        build();
    }

    /**
     * 设置左文案
     */
    public void setTextLeft(@StringRes int textLeft) {
        this.mTextLeft = mContext.getString(textLeft);
        build();
    }

    /**
     * 设置右文案
     */
    public void setTextRight(CharSequence textRight) {
        this.mTextRight = textRight;
        build();
    }

    /**
     * 设置右文案
     */
    public void setTextRight(@StringRes int textRight) {
        this.mTextRight = mContext.getString(textRight);
        build();
    }

    /**
     * 设置左文案颜色
     */
    public void setTextLeftColor(int color) {
        this.mLeftColor = ColorStateList.valueOf(color);
        build();
    }

    /**
     * 设置右文案颜色
     */
    public void setTextRightColor(int color) {
        this.mRightColor = ColorStateList.valueOf(color);
        build();
    }

    /**
     * 设置左文案字号大小
     */
    public void setTextLeftSize(float leftSize) {
        this.mLeftSize = leftSize;
        build();
    }

    /**
     * 设置右文案字号大小
     */
    public void setTextRightSize(float rightSize) {
        this.mRightSize = rightSize;
        build();
    }

    /**
     * 设置Icon
     */
    public void setIcon(String iconText) {
        this.iconString = iconText;
        build();
    }

    /**
     * 设置Icon
     */
    public void setIcon(CharSequence iconText) {
        this.iconString = iconText;
        build();
    }

    /**
     * 设置Icon
     */
    public void setIcon(@StringRes int iconText) {
        this.iconString = mContext.getString(iconText);
        build();
    }

    /**
     * 设置左文案样式
     */
    public void setTextLeftStyle(int textLeftStyle) {
        this.mTextLeftStyle = textLeftStyle;
        build();
    }

    /**
     * 设置右文案样式
     */
    public void setTextRightStyle(int textRightStyle) {
        this.mTextRightStyle = textRightStyle;
        build();
    }

    /**
     * 设置中间文案样式
     */
    public void setTextCenterStyle(int textCenterStyle) {
        this.mTextCenterStyle = textCenterStyle;
        build();
    }


    /**
     * span之前需要首先clear
     */
    public void clearSpan() {
        if (leftContainer != null) {
            leftContainer.clear();
        }
        if (rightContainer != null) {
            rightContainer.clear();
        }
    }

    /**
     * 设置左边文字为多个span
     */
    public void addSpanLeft(List<Object> objects, int start, int end, int flags) {
        spanLeft(objects, start, end, flags);
        build();
    }

    /**
     * 设置左边文字为span
     */
    public void addSpanLeft(Object object, int start, int end, int flags) {
        spanLeft(object, start, end, flags);
        build();
    }

    /**
     * 设置右边文字为多个span
     */
    public void addSpanRight(List<Object> objects, int start, int end, int flags) {
        spanRight(objects, start, end, flags);
        build();
    }

    /**
     * 设置右边文字为span
     */
    public void addSpanRight(Object object, int start, int end, int flags) {
        spanRight(object, start, end, flags);
        build();
    }

    /**
     * 设置文字padding
     */
    public void setTextPadding(float textPadding) {
        this.mTextPadding = textPadding;
        build();
    }

    /**
     * 设置三段文字颜色
     */
    public void setAllTextColor(@ColorInt int color) {
        allTextColor(color);
        build();
    }

    //=================================链式调用##需要最后调用build()==================================

    /**
     * 设置Shape type
     */
    public RoundTextView type(int type) {
        this.type = type;
        return this;
    }

    /**
     * 设置边线的宽度
     */
    public RoundTextView strokeWidth(int width) {
        this.mStrokeWidth = width;
        return this;
    }

    /**
     * 设置边线的宽度
     */
    public RoundTextView strokeColor(@ColorInt int color) {
        this.mStrokeColor = color;
        return this;
    }

    /**
     * 设置填充的颜色
     */
    public RoundTextView solid(@ColorInt int color) {
        this.mSoild = color;
        return this;
    }

    /**
     * 设置radius
     */
    public RoundTextView radius(int radius) {
        this.mRadius = radius;
        return this;
    }


    /**
     * 设置icon颜色
     */
    public RoundTextView iconColor(int color) {
        this.mIconColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * 设置左文案
     */
    public RoundTextView textLeft(String textLeft) {
        this.mTextLeft = textLeft;
        return this;
    }

    /**
     * 设置左文案
     */
    public RoundTextView textLeft(@StringRes int textLeft) {
        this.mTextLeft = mContext.getString(textLeft);
        return this;
    }

    /**
     * 设置右文案
     */
    public RoundTextView textRight(String textRight) {
        this.mTextRight = textRight;
        return this;
    }

    /**
     * 设置右文案
     */
    public RoundTextView textRight(@StringRes int textRight) {
        this.mTextRight = mContext.getString(textRight);
        return this;
    }

    /**
     * 设置左文案颜色
     */
    public RoundTextView textLeftColor(int color) {
        this.mLeftColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * 设置右文案颜色
     */
    public RoundTextView textRightColor(int color) {
        this.mRightColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * 设置左文案字号大小
     */
    public RoundTextView textLeftSize(float leftSize) {
        this.mLeftSize = leftSize;
        return this;
    }

    /**
     * 设置右文案字号大小
     */
    public RoundTextView textRightSize(float rightSize) {
        this.mRightSize = rightSize;
        return this;
    }

    /**
     * 设置Icon
     */
    public RoundTextView icon(String iconText) {
        this.iconString = iconText;
        return this;
    }

    /**
     * 设置Icon
     */
    public RoundTextView icon(@StringRes int iconText) {
        this.iconString = mContext.getString(iconText);
        return this;
    }

    /**
     * 设置左文案样式
     */
    public RoundTextView textLeftStyle(int textLeftStyle) {
        this.mTextLeftStyle = textLeftStyle;
        return this;
    }

    /**
     * 设置右文案样式
     */
    public RoundTextView textRightStyle(int textRightStyle) {
        this.mTextRightStyle = textRightStyle;
        return this;
    }

    /**
     * 设置中间文案样式
     */
    public RoundTextView textCenterStyle(int textCenterStyle) {
        this.mTextCenterStyle = textCenterStyle;
        return this;
    }

    /**
     * 设置右边文字为多个span
     */
    public RoundTextView spanRight(List<Object> objects, int start, int end, int flags) {
        if (rightContainer == null) {
            rightContainer = new ArrayList<>();
        }
        this.rightContainer.add(new SpanContainer(objects, start, end, flags));
        return this;
    }

    /**
     * 设置右边文字为span
     */
    public RoundTextView spanRight(Object object, int start, int end, int flags) {
        if (rightContainer == null) {
            rightContainer = new ArrayList<>();
        }
        this.rightContainer.add(new SpanContainer(object, start, end, flags));
        return this;
    }

    /**
     * 设置左边文字为多个span
     */
    public RoundTextView spanLeft(List<Object> objects, int start, int end, int flags) {
        if (leftContainer == null) {
            leftContainer = new ArrayList<>();
        }
        this.leftContainer.add(new SpanContainer(objects, start, end, flags));
        return this;
    }

    /**
     * 设置左边文字为span
     */
    public RoundTextView spanLeft(Object object, int start, int end, int flags) {
        if (leftContainer == null) {
            leftContainer = new ArrayList<>();
        }
        this.leftContainer.add(new SpanContainer(object, start, end, flags));
        return this;
    }

    /**
     * 设置文字padding
     */
    public RoundTextView textPadding(float textPadding) {
        this.mTextPadding = textPadding;
        return this;
    }

    /**
     * 设置三段文字颜色
     */
    public RoundTextView allTextColor(@ColorInt int color) {
        ColorStateList temp = ColorStateList.valueOf(color);
        this.mIconColor = temp;
        this.mLeftColor = temp;
        this.mRightColor = temp;
        return this;
    }

    /**
     * 获取中间的文案
     */
    public CharSequence getIconStr() {
        return iconString;
    }

    /**
     * 防止重复初始化，最后调用build
     */
    public RoundTextView build() {
        clearText();
        //initIconFont();
        init();
        return this;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {

        }
        if (this.autoMaxHeight) {
            int lead = 0;
            if (this.getPaint() != null) {
                lead = this.getPaint().getFontMetricsInt().leading * 3;
            }
            this.setMeasuredDimension(this.getMeasuredWidth(), (int) (Math.max((float) this
                    .getMeasuredHeight(), Math.max(this.mLeftSize, this.mRightSize)) + (float)
                    lead));
        }

    }
}
