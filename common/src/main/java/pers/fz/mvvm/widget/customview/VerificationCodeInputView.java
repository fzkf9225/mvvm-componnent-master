package pers.fz.mvvm.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/9/7 10:25
 * describe :
 */
public class VerificationCodeInputView extends AppCompatEditText implements View.OnFocusChangeListener {
    private final String TAG = VerificationCodeInputView.class.getSimpleName();

    public interface OnTextFinishListener {

        void onTextFinish(CharSequence text, int length);
    }

    private int mTextColor;
    // 输入的最大长度
    private int mMaxLength;
    // 边框宽度
    private int mStrokeWidth;
    // 边框高度
    private int mStrokeHeight;
    // 边框之间的距离
    private int mStrokePadding;
    // 光标宽度
    private int mCursorWidth;
    // 光标高度
    private int mCursorHeight;
    // 方框的背景
    private StateListDrawable mStrokeDrawable;
    // 光标的背景
    private StateListDrawable mCursorDrawable;
    // 输入结束监听
    private OnTextFinishListener mOnInputFinishListener;
    // 是否光标获取焦点
    private boolean mCursorStateFocused = true;
    // 记录上次光标获取焦点时间
    private long mLastCursorFocusedTimeMillis = System.currentTimeMillis();
    //纯数字
    private final int NUMBER = 0;
    //纯字母
    private final int CHARACTER = 1;
    //数字+字母
    private final int ALL = 2;
    //验证码框颜色，有焦点时边框颜色
    private int mStrokeFocusedColor;
    //验证码框圆角大小
    private int mRadius;
    //验证码框粗细
    private int mBorderWidth;
    //验证码框颜色，有焦点时背景颜色
    private int mStrokeFocusedBgColor;
    //验证码框颜色，有焦点时光标颜色
    private int mCursorFocusedBgColor;
    //验证码框颜色，无焦点时边框颜色
    private int mStrokeDefaultColor;
    //验证码框颜色，无焦点时背景色
    private int mStrokeDefaultBgColor;
    //输入类型
    private int mCodeType = NUMBER;
    //验证码框颜色，无焦点时光标颜色
    private int mCursorDefaultBgColor;

    /**
     * 构造方法
     */
    public VerificationCodeInputView(Context context) {
        this(context, null);
    }

    public VerificationCodeInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs == null) {
            initDefaultValue();
        } else {
            initAttrs(attrs);
        }

        mStrokeDrawable = new StateListDrawable();

        GradientDrawable bgDefaultDrawable = new GradientDrawable();
        bgDefaultDrawable.setStroke(mBorderWidth, mStrokeDefaultColor);
        bgDefaultDrawable.setColor(mStrokeDefaultBgColor);
        bgDefaultDrawable.setCornerRadius(mRadius);
        mStrokeDrawable.addState(new int[]{-android.R.attr.state_focused}, bgDefaultDrawable);

        GradientDrawable bgFocusedDrawable = new GradientDrawable();
        bgFocusedDrawable.setStroke(mBorderWidth, mStrokeFocusedColor);
        bgFocusedDrawable.setColor(mStrokeFocusedBgColor);
        bgFocusedDrawable.setCornerRadius(mRadius);
        mStrokeDrawable.addState(new int[]{android.R.attr.state_focused}, bgFocusedDrawable);


        mCursorDrawable = new StateListDrawable();
        // 创建默认状态的GradientDrawable
        GradientDrawable defaultDrawable = new GradientDrawable();
        defaultDrawable.setColor(mCursorDefaultBgColor);
        defaultDrawable.setSize(mCursorWidth, mCursorHeight);
        mCursorDrawable.addState(new int[]{-android.R.attr.state_focused}, defaultDrawable);

        // 创建焦点状态的GradientDrawable
        GradientDrawable focusedDrawable = new GradientDrawable();
        focusedDrawable.setColor(mCursorFocusedBgColor);
        focusedDrawable.setSize(mCursorWidth, mCursorHeight);
        mCursorDrawable.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);

        setCodeType(this.mCodeType);
        setLongClickable(false);
        // 去掉背景颜色
        setBackgroundColor(Color.TRANSPARENT);
        // 不显示光标
        setCursorVisible(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnFocusChangeListener(this);
    }

    private void initDefaultValue() {
        mStrokeFocusedColor = ContextCompat.getColor(getContext(), R.color.themeColor);
        mStrokeFocusedBgColor = ContextCompat.getColor(getContext(), R.color.white);
        mCursorFocusedBgColor = ContextCompat.getColor(getContext(), R.color.themeColor);
        mStrokeDefaultColor = ContextCompat.getColor(getContext(), R.color.white);
        mStrokeDefaultBgColor = ContextCompat.getColor(getContext(), R.color.transparent);
        mCursorDefaultBgColor = ContextCompat.getColor(getContext(), R.color.transparent);
        mStrokeWidth = DensityUtil.dp2px(getContext(), 30);
        mStrokeHeight = DensityUtil.dp2px(getContext(), 30);
        mBorderWidth = DensityUtil.dp2px(getContext(), 2);
        mRadius = DensityUtil.dp2px(getContext(), 4);
        mStrokePadding = DensityUtil.dp2px(getContext(), 10);
        mCursorWidth = DensityUtil.dp2px(getContext(), 2);
        mCursorHeight = DensityUtil.dp2px(getContext(), 15);
        mMaxLength = 6;
        mCodeType = NUMBER;
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        @SuppressLint("CustomViewStyleable")
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.verification_code, 0, 0);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.verification_code_strokeWidth, DensityUtil.dp2px(getContext(), 30));
        mStrokeHeight = ta.getDimensionPixelSize(R.styleable.verification_code_strokeWidth, DensityUtil.dp2px(getContext(), 30));
        mStrokePadding = ta.getDimensionPixelSize(R.styleable.verification_code_codeStrokePadding, DensityUtil.dp2px(getContext(), 10));
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.verification_code_codeStrokeBorderWidth, DensityUtil.dp2px(getContext(), 2));
        mRadius = ta.getDimensionPixelSize(R.styleable.verification_code_radius, DensityUtil.dp2px(getContext(), 4));
        mStrokeFocusedColor = ta.getColor(R.styleable.verification_code_codeStrokeColorStateFocusedTrue, ContextCompat.getColor(getContext(), R.color.themeColor));
        mStrokeDefaultColor = ta.getColor(R.styleable.verification_code_codeStrokeColorStateFocusedFalse, ContextCompat.getColor(getContext(), R.color.white));
        mStrokeFocusedBgColor = ta.getColor(R.styleable.verification_code_codeBgColorStateFocusedTrue, ContextCompat.getColor(getContext(), R.color.white));
        mStrokeDefaultBgColor = ta.getColor(R.styleable.verification_code_codeBgColorStateFocusedFalse, ContextCompat.getColor(getContext(), R.color.transparent));
        mCursorWidth = ta.getDimensionPixelSize(R.styleable.verification_code_cursorWidth, DensityUtil.dp2px(getContext(), 2));
        mCursorHeight = ta.getDimensionPixelSize(R.styleable.verification_code_cursorHeight, DensityUtil.dp2px(getContext(), 15));
        mCursorFocusedBgColor = ta.getColor(R.styleable.verification_code_cursorBgColorStateFocusedTrue, ContextCompat.getColor(getContext(), R.color.themeColor));
        mCursorDefaultBgColor = ta.getColor(R.styleable.verification_code_cursorBgColorStateFocusedFalse, ContextCompat.getColor(getContext(), R.color.transparent));
        mMaxLength = ta.getInt(R.styleable.verification_code_codeMaxLength, 6);
        mCodeType = ta.getInt(R.styleable.verification_code_codeType, NUMBER);
        ta.recycle();
    }

    public void setBorderWidth(int mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public void setStrokeFocusedColor(int mStrokeFocusedColor) {
        this.mStrokeFocusedColor = mStrokeFocusedColor;
    }

    public void setStrokeDefaultColor(int mStrokeDefaultColor) {
        this.mStrokeDefaultColor = mStrokeDefaultColor;
    }

    public void setStrokeDefaultBgColor(int mStrokeDefaultBgColor) {
        this.mStrokeDefaultBgColor = mStrokeDefaultBgColor;
    }

    public void setStrokeFocusedBgColor(int mStrokeFocusedBgColor) {
        this.mStrokeFocusedBgColor = mStrokeFocusedBgColor;
    }

    public void setCursorDefaultBgColor(int mCursorDefaultBgColor) {
        this.mCursorDefaultBgColor = mCursorDefaultBgColor;
    }

    public void setCursorFocusedBgColor(int mCursorFocusedBgColor) {
        this.mCursorFocusedBgColor = mCursorFocusedBgColor;
    }

    public void setCodeType(int mCodeType) {
        this.mCodeType = mCodeType;
        if (mMaxLength >= 0) {
            if (NUMBER == this.mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterNumber});
            } else if (CHARACTER == mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterCharacter});
            } else if (ALL == mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterNumberAndCharacter});
            } else {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
            }
        } else {
            setFilters(new InputFilter[0]);
        }
    }

    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    public void setStrokeHeight(int strokeHeight) {
        mStrokeHeight = strokeHeight;
    }

    public void setStrokePadding(int strokePadding) {
        mStrokePadding = strokePadding;
    }

    public void setCursorWidth(int cursorWidth) {
        mCursorWidth = cursorWidth;
    }

    public void setCursorHeight(int cursorHeight) {
        mCursorHeight = cursorHeight;
    }

    public void setStrokeDrawable(StateListDrawable strokeDrawable) {
        mStrokeDrawable = strokeDrawable;
    }

    public void setCursorDrawable(StateListDrawable cursorDrawable) {
        mCursorDrawable = cursorDrawable;
    }

    /**
     * 设置最大长度
     */
    private void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        if (mMaxLength >= 0) {
            if (NUMBER == this.mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterNumber});
            } else if (CHARACTER == mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterCharacter});
            } else if (ALL == mCodeType) {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterNumberAndCharacter});
            } else {
                setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
            }
        } else {
            setFilters(new InputFilter[0]);
        }
    }

    /**
     * 设置最大长度
     */
    public void setInputFilters(InputFilter filterNumber) {
        if (mMaxLength >= 0) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), filterNumber});
        } else {
            setFilters(new InputFilter[0]);
        }
    }

    //仅允许输入纯数字：
    InputFilter filterNumber = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };
    //仅允许输入纯字母：
    InputFilter filterCharacter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };
    // 仅允许输入数字和字符：
    InputFilter filterNumberAndCharacter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (getText() == null) {
            return;
        }
        if (hasFocus) {
            setSelection(getText().toString().length());
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 判断高度是否小于推荐高度
        if (height < mStrokeHeight) {
            height = mStrokeHeight;
        }

        // 判断高度是否小于推荐宽度
        int recommendWidth = mStrokeWidth * mMaxLength + mStrokePadding * (mMaxLength - 1);
        if (width < recommendWidth) {
            width = recommendWidth;
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTextColor = getCurrentTextColor();
        setTextColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        setTextColor(mTextColor);
        // 重绘背景颜色
        drawStrokeBackground(canvas);
        drawCursorBackground(canvas);
        // 重绘文本
        drawText(canvas);
    }

    /**
     * 重绘背景
     */
    private void drawStrokeBackground(Canvas canvas) {
        Rect mRect = new Rect();
        if (mStrokeDrawable != null) {
            // 绘制方框背景
            mRect.left = 0;
            mRect.top = 0;
            mRect.right = mStrokeWidth;
            mRect.bottom = mStrokeHeight;
            int count = canvas.getSaveCount();
            canvas.save();
            for (int i = 0; i < mMaxLength; i++) {
                mStrokeDrawable.setBounds(mRect);
                mStrokeDrawable.setState(new int[]{-android.R.attr.state_focused});
                mStrokeDrawable.draw(canvas);
                float dx = mRect.right + mStrokePadding;
                // 移动画布
                canvas.save();
                canvas.translate(dx, 0);
            }
            canvas.restoreToCount(count);
            canvas.translate(0, 0);

            // 绘制激活状态
            // 当前激活的索引
            int activatedIndex = Math.max(0, getEditableText().length());
            if (activatedIndex < mMaxLength) {
                mRect.left = mStrokeWidth * activatedIndex + mStrokePadding * activatedIndex;
                mRect.right = mRect.left + mStrokeWidth;
                mStrokeDrawable.setState(new int[]{android.R.attr.state_focused});
                mStrokeDrawable.setBounds(mRect);
                mStrokeDrawable.draw(canvas);
            }
        }
    }

    /**
     * 重绘光标
     */
    private void drawCursorBackground(Canvas canvas) {
        Rect mRect = new Rect();
        if (mCursorDrawable != null) {
            // 绘制光标
            mRect.left = (mStrokeWidth - mCursorWidth) / 2;
            mRect.top = (mStrokeHeight - mCursorHeight) / 2;
            mRect.right = mRect.left + mCursorWidth;
            mRect.bottom = mRect.top + mCursorHeight;
            int count = canvas.getSaveCount();
            canvas.save();
            for (int i = 0; i < mMaxLength; i++) {
                mCursorDrawable.setBounds(mRect);
                mCursorDrawable.setState(new int[]{-android.R.attr.state_focused});
                mCursorDrawable.draw(canvas);
                float dx = mRect.right + mStrokePadding;
                // 移动画布
                canvas.save();
                canvas.translate(dx, 0);
            }
            canvas.restoreToCount(count);
            canvas.translate(0, 0);

            // 绘制激活状态
            // 当前激活的索引
            int activatedIndex = Math.max(0, getEditableText().length());
            if (activatedIndex < mMaxLength) {
                mRect.left = mStrokeWidth * activatedIndex + mStrokePadding * activatedIndex + (mStrokeWidth - mCursorWidth) / 2;
                mRect.right = mRect.left + mCursorWidth;
                int[] state = new int[]{isFocusable() && isFocusableInTouchMode() && mCursorStateFocused ? android.R.attr.state_focused : -android.R.attr.state_focused};
                mCursorDrawable.setState(state);
                mCursorDrawable.setBounds(mRect);
                mCursorDrawable.draw(canvas);
                if ((System.currentTimeMillis() - mLastCursorFocusedTimeMillis) >= 800) {
                    mCursorStateFocused = !mCursorStateFocused;
                    mLastCursorFocusedTimeMillis = System.currentTimeMillis();
                }
            }
        }
    }


    /**
     * 重绘文本
     */
    private void drawText(Canvas canvas) {
        Rect mRect = new Rect();
        int count = canvas.getSaveCount();
        canvas.translate(0, 0);
        int length = getEditableText().length();
        for (int i = 0; i < length; i++) {
            String text = String.valueOf(getEditableText().charAt(i));
            TextPaint textPaint = getPaint();
            textPaint.setColor(mTextColor);
            // 获取文本大小
            textPaint.getTextBounds(text, 0, 1, mRect);
            // 计算(x,y) 坐标
            int x = mStrokeWidth / 2 + (mStrokeWidth + mStrokePadding) * i - (mRect.centerX());
            int y = mStrokeHeight / 2 - mRect.centerY();
            canvas.drawText(text, x, y, textPaint);
        }
        canvas.restoreToCount(count);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
                                 int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        // 当前文本长度
        int textLength = getEditableText().length();
        if (textLength == mMaxLength) {
            clearFocus();
            hideSoftInput();
            if (mOnInputFinishListener != null) {
                mOnInputFinishListener.onTextFinish(getEditableText().toString(), mMaxLength);
            }
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (getText() == null) {
            return;
        }
        if (selStart == getText().toString().length()) {
            return;
        }
        try {
            new Handler(Looper.getMainLooper()).post(() -> {
                setSelection(getText().toString().length());
                setFocusable(true);
                setFocusableInTouchMode(true);
                requestFocus();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 设置输入完成监听
     */
    public void setOnTextFinishListener(OnTextFinishListener onInputFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener;
    }

}
