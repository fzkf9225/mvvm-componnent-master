package io.coderf.arklab.common.widget.customview;

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

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 短信验证码输入框。
 * <p>
 * 以多个独立方格展示验证码，支持光标闪烁、输入类型限制（数字 / 字母 / 数字+字母）
 * 及边框、光标、背景等样式自定义。输入达到最大长度后自动收起软键盘并回调完成监听。
 * </p>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/7/2 9:00
 */
public class VerificationCodeInputView extends AppCompatEditText implements View.OnFocusChangeListener {

    /** 光标闪烁间隔（毫秒） */
    private static final long CURSOR_BLINK_INTERVAL_MS = 500;

    /** 输入类型：纯数字 */
    public static final int CODE_TYPE_NUMBER = 0;
    /** 输入类型：纯字母 */
    public static final int CODE_TYPE_CHARACTER = 1;
    /** 输入类型：数字 + 字母 */
    public static final int CODE_TYPE_ALL = 2;

    private static final int[] STATE_FOCUSED = new int[]{android.R.attr.state_focused};
    private static final int[] STATE_UNFOCUSED = new int[]{-android.R.attr.state_focused};

    /** 仅允许输入数字 */
    private static final InputFilter FILTER_NUMBER = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isDigit(source.charAt(i))) {
                return "";
            }
        }
        return null;
    };

    /** 仅允许输入字母 */
    private static final InputFilter FILTER_CHARACTER = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isLetter(source.charAt(i))) {
                return "";
            }
        }
        return null;
    };

    /** 仅允许输入数字和字母 */
    private static final InputFilter FILTER_NUMBER_AND_CHARACTER = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isLetterOrDigit(source.charAt(i))) {
                return "";
            }
        }
        return null;
    };

    /**
     * 验证码输入完成监听。
     */
    public interface OnTextFinishListener {

        /**
         * 输入达到最大长度时回调。
         *
         * @param text   完整验证码文本
         * @param length 最大长度（与 {@link #getMaxLength()} 一致）
         */
        void onTextFinish(CharSequence text, int length);
    }

    /** 文本绘制颜色（onDraw 时临时设为透明，绘制完方格后再恢复） */
    private int mTextColor;
    /** 允许输入的最大字符数 */
    private int mMaxLength;
    /** 单个验证码方格的宽度（px） */
    private int mStrokeWidth;
    /** 单个验证码方格的高度（px） */
    private int mStrokeHeight;
    /** 相邻方格之间的间距（px） */
    private int mStrokePadding;
    /** 自定义光标的宽度（px） */
    private int mCursorWidth;
    /** 自定义光标的高度（px） */
    private int mCursorHeight;
    /** 方格边框的状态列表 Drawable */
    private StateListDrawable mStrokeDrawable;
    /** 光标的状态列表 Drawable */
    private StateListDrawable mCursorDrawable;
    /** 输入完成回调 */
    private OnTextFinishListener mOnInputFinishListener;
    /** 光标当前是否处于“亮”态（用于闪烁） */
    private boolean mCursorVisible = true;
    /** 允许的输入类型，见 {@link #CODE_TYPE_NUMBER} 等常量 */
    private int mCodeType = CODE_TYPE_NUMBER;
    /** 方格获取焦点时的边框颜色 */
    private int mStrokeFocusedColor;
    /** 方格圆角半径（px） */
    private int mRadius;
    /** 方格边框线宽（px） */
    private int mBorderWidth;
    /** 方格获取焦点时的背景颜色 */
    private int mStrokeFocusedBgColor;
    /** 光标处于“亮”态时的颜色 */
    private int mCursorFocusedBgColor;
    /** 方格未获取焦点时的边框颜色 */
    private int mStrokeDefaultColor;
    /** 方格未获取焦点时的背景颜色 */
    private int mStrokeDefaultBgColor;
    /** 光标处于“暗”态时的颜色 */
    private int mCursorDefaultBgColor;
    /** 标记构造是否完成，避免 super() 期间回调访问未初始化的成员 */
    private boolean mInitialized;

    /** 主线程 Handler，用于光标闪烁与光标位置修正 */
    private Handler mMainHandler;
    /** 绘制时复用的矩形区域，避免 onDraw 中频繁分配对象 */
    private final Rect mDrawRect = new Rect();
    /** 光标闪烁任务 */
    private final Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!shouldBlinkCursor()) {
                return;
            }
            mCursorVisible = !mCursorVisible;
            invalidate();
            mMainHandler.postDelayed(this, CURSOR_BLINK_INTERVAL_MS);
        }
    };

    /**
     * 构造方法。
     *
     * @param context 上下文
     */
    public VerificationCodeInputView(Context context) {
        this(context, null);
    }

    /**
     * 构造方法，支持从 XML 读取 {@link R.styleable#verification_code} 属性。
     *
     * @param context 上下文
     * @param attrs   布局属性，可为 null
     */
    public VerificationCodeInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs == null) {
            initDefaultValue();
        } else {
            initAttrs(attrs);
        }

        rebuildDrawables();
        applyInputFilters();
        setLongClickable(false);
        setBackgroundColor(Color.TRANSPARENT);
        setCursorVisible(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnFocusChangeListener(this);
        mMainHandler = new Handler(Looper.getMainLooper());
        mInitialized = true;
    }

    /**
     * 使用内置默认值初始化样式（无 XML 属性时）。
     */
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
        mCodeType = CODE_TYPE_NUMBER;
    }

    /**
     * 从 XML 属性初始化样式。
     *
     * @param attrs 布局属性
     */
    private void initAttrs(AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.verification_code, 0, 0);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.verification_code_strokeWidth,
                DensityUtil.dp2px(getContext(), 30));
        mStrokeHeight = ta.getDimensionPixelSize(R.styleable.verification_code_strokeHeight,
                DensityUtil.dp2px(getContext(), 30));
        mStrokePadding = ta.getDimensionPixelSize(R.styleable.verification_code_codeStrokePadding,
                DensityUtil.dp2px(getContext(), 10));
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.verification_code_codeStrokeBorderWidth,
                DensityUtil.dp2px(getContext(), 2));
        mRadius = ta.getDimensionPixelSize(R.styleable.verification_code_radius,
                DensityUtil.dp2px(getContext(), 4));
        mStrokeFocusedColor = ta.getColor(R.styleable.verification_code_codeStrokeColorStateFocusedTrue,
                ContextCompat.getColor(getContext(), R.color.themeColor));
        mStrokeDefaultColor = ta.getColor(R.styleable.verification_code_codeStrokeColorStateFocusedFalse,
                ContextCompat.getColor(getContext(), R.color.white));
        mStrokeFocusedBgColor = ta.getColor(R.styleable.verification_code_codeBgColorStateFocusedTrue,
                ContextCompat.getColor(getContext(), R.color.white));
        mStrokeDefaultBgColor = ta.getColor(R.styleable.verification_code_codeBgColorStateFocusedFalse,
                ContextCompat.getColor(getContext(), R.color.transparent));
        mCursorWidth = ta.getDimensionPixelSize(R.styleable.verification_code_cursorWidth,
                DensityUtil.dp2px(getContext(), 2));
        mCursorHeight = ta.getDimensionPixelSize(R.styleable.verification_code_cursorHeight,
                DensityUtil.dp2px(getContext(), 15));
        mCursorFocusedBgColor = ta.getColor(R.styleable.verification_code_cursorBgColorStateFocusedTrue,
                ContextCompat.getColor(getContext(), R.color.themeColor));
        mCursorDefaultBgColor = ta.getColor(R.styleable.verification_code_cursorBgColorStateFocusedFalse,
                ContextCompat.getColor(getContext(), R.color.transparent));
        mMaxLength = ta.getInt(R.styleable.verification_code_codeMaxLength, 6);
        mCodeType = ta.getInt(R.styleable.verification_code_codeType, CODE_TYPE_NUMBER);
        ta.recycle();
    }

    /**
     * 根据当前颜色、尺寸配置重建方格与光标 Drawable。
     */
    private void rebuildDrawables() {
        mStrokeDrawable = new StateListDrawable();

        GradientDrawable bgDefaultDrawable = new GradientDrawable();
        bgDefaultDrawable.setStroke(mBorderWidth, mStrokeDefaultColor);
        bgDefaultDrawable.setColor(mStrokeDefaultBgColor);
        bgDefaultDrawable.setCornerRadius(mRadius);
        mStrokeDrawable.addState(STATE_UNFOCUSED, bgDefaultDrawable);

        GradientDrawable bgFocusedDrawable = new GradientDrawable();
        bgFocusedDrawable.setStroke(mBorderWidth, mStrokeFocusedColor);
        bgFocusedDrawable.setColor(mStrokeFocusedBgColor);
        bgFocusedDrawable.setCornerRadius(mRadius);
        mStrokeDrawable.addState(STATE_FOCUSED, bgFocusedDrawable);

        mCursorDrawable = new StateListDrawable();

        GradientDrawable defaultDrawable = new GradientDrawable();
        defaultDrawable.setColor(mCursorDefaultBgColor);
        defaultDrawable.setSize(mCursorWidth, mCursorHeight);
        mCursorDrawable.addState(STATE_UNFOCUSED, defaultDrawable);

        GradientDrawable focusedDrawable = new GradientDrawable();
        focusedDrawable.setColor(mCursorFocusedBgColor);
        focusedDrawable.setSize(mCursorWidth, mCursorHeight);
        mCursorDrawable.addState(STATE_FOCUSED, focusedDrawable);
    }

    /**
     * 根据 {@link #mMaxLength} 与 {@link #mCodeType} 更新输入过滤器。
     */
    private void applyInputFilters() {
        if (mMaxLength < 0) {
            setFilters(new InputFilter[0]);
            return;
        }
        InputFilter typeFilter = getTypeFilter();
        if (typeFilter != null) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), typeFilter});
        } else {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
        }
    }

    /**
     * 根据当前输入类型返回对应的字符过滤器。
     *
     * @return 类型过滤器，未知类型时返回 null
     */
    private InputFilter getTypeFilter() {
        switch (mCodeType) {
            case CODE_TYPE_NUMBER:
                return FILTER_NUMBER;
            case CODE_TYPE_CHARACTER:
                return FILTER_CHARACTER;
            case CODE_TYPE_ALL:
                return FILTER_NUMBER_AND_CHARACTER;
            default:
                return null;
        }
    }

    /**
     * 当前是否应显示并闪烁光标（有焦点且尚未输满）。
     */
    private boolean shouldBlinkCursor() {
        return hasFocus() && getEditableText().length() < mMaxLength;
    }

    /**
     * 启动光标闪烁动画。
     */
    private void startCursorBlink() {
        if (mMainHandler == null) {
            return;
        }
        mMainHandler.removeCallbacks(mBlinkRunnable);
        mCursorVisible = true;
        mMainHandler.postDelayed(mBlinkRunnable, CURSOR_BLINK_INTERVAL_MS);
    }

    /**
     * 停止光标闪烁动画。
     */
    private void stopCursorBlink() {
        if (mMainHandler == null) {
            return;
        }
        mMainHandler.removeCallbacks(mBlinkRunnable);
        mCursorVisible = true;
    }

    /**
     * 计算第 index 个方格在 View 中的左边界 x 坐标。
     *
     * @param index 方格索引，从 0 开始
     * @return 左边界 x（px）
     */
    private int getCellLeft(int index) {
        return index * (mStrokeWidth + mStrokePadding);
    }

    /**
     * 当前待输入方格的索引；无焦点或已全部输入时返回 -1。
     */
    private int getActiveCellIndex() {
        if (!hasFocus()) {
            return -1;
        }
        int length = getEditableText().length();
        return length < mMaxLength ? length : -1;
    }

    /**
     * 设置方格边框线宽。
     *
     * @param borderWidth 线宽（px）
     */
    public void setBorderWidth(int borderWidth) {
        this.mBorderWidth = borderWidth;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格圆角半径。
     *
     * @param radius 圆角（px）
     */
    public void setRadius(int radius) {
        this.mRadius = radius;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格获取焦点时的边框颜色。
     *
     * @param strokeFocusedColor 颜色值
     */
    public void setStrokeFocusedColor(int strokeFocusedColor) {
        this.mStrokeFocusedColor = strokeFocusedColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格未获取焦点时的边框颜色。
     *
     * @param strokeDefaultColor 颜色值
     */
    public void setStrokeDefaultColor(int strokeDefaultColor) {
        this.mStrokeDefaultColor = strokeDefaultColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格未获取焦点时的背景颜色。
     *
     * @param strokeDefaultBgColor 颜色值
     */
    public void setStrokeDefaultBgColor(int strokeDefaultBgColor) {
        this.mStrokeDefaultBgColor = strokeDefaultBgColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格获取焦点时的背景颜色。
     *
     * @param strokeFocusedBgColor 颜色值
     */
    public void setStrokeFocusedBgColor(int strokeFocusedBgColor) {
        this.mStrokeFocusedBgColor = strokeFocusedBgColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置光标“暗”态颜色。
     *
     * @param cursorDefaultBgColor 颜色值
     */
    public void setCursorDefaultBgColor(int cursorDefaultBgColor) {
        this.mCursorDefaultBgColor = cursorDefaultBgColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置光标“亮”态颜色。
     *
     * @param cursorFocusedBgColor 颜色值
     */
    public void setCursorFocusedBgColor(int cursorFocusedBgColor) {
        this.mCursorFocusedBgColor = cursorFocusedBgColor;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置允许的输入类型。
     *
     * @param codeType {@link #CODE_TYPE_NUMBER}、{@link #CODE_TYPE_CHARACTER} 或 {@link #CODE_TYPE_ALL}
     */
    public void setCodeType(int codeType) {
        this.mCodeType = codeType;
        applyInputFilters();
    }

    /**
     * 设置单个方格宽度。
     *
     * @param strokeWidth 宽度（px）
     */
    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        requestLayout();
        invalidate();
    }

    /**
     * 设置单个方格高度。
     *
     * @param strokeHeight 高度（px）
     */
    public void setStrokeHeight(int strokeHeight) {
        mStrokeHeight = strokeHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 设置相邻方格间距。
     *
     * @param strokePadding 间距（px）
     */
    public void setStrokePadding(int strokePadding) {
        mStrokePadding = strokePadding;
        requestLayout();
        invalidate();
    }

    /**
     * 设置自定义光标宽度。
     *
     * @param cursorWidth 宽度（px）
     */
    public void setCursorWidth(int cursorWidth) {
        mCursorWidth = cursorWidth;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置自定义光标高度。
     *
     * @param cursorHeight 高度（px）
     */
    public void setCursorHeight(int cursorHeight) {
        mCursorHeight = cursorHeight;
        rebuildDrawables();
        invalidate();
    }

    /**
     * 设置方格边框 Drawable（覆盖默认样式）。
     *
     * @param strokeDrawable 状态列表 Drawable
     */
    public void setStrokeDrawable(StateListDrawable strokeDrawable) {
        mStrokeDrawable = strokeDrawable;
        invalidate();
    }

    /**
     * 设置光标 Drawable（覆盖默认样式）。
     *
     * @param cursorDrawable 状态列表 Drawable
     */
    public void setCursorDrawable(StateListDrawable cursorDrawable) {
        mCursorDrawable = cursorDrawable;
        invalidate();
    }

    /**
     * 设置验证码最大长度，并同步更新输入过滤器。
     *
     * @param maxLength 最大字符数，小于 0 表示不限制
     */
    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        applyInputFilters();
        requestLayout();
        invalidate();
    }

    /**
     * 获取验证码最大长度。
     *
     * @return 最大字符数
     */
    public int getMaxLength() {
        return mMaxLength;
    }

    /**
     * 设置自定义输入过滤器（仍会保留长度限制）。
     * <p>
     * 适用于需要比 {@link #setCodeType(int)} 更细粒度过滤规则的场景。
     * </p>
     *
     * @param customFilter 自定义过滤器
     */
    public void setCustomInputFilter(InputFilter customFilter) {
        if (mMaxLength < 0) {
            setFilters(customFilter == null ? new InputFilter[0] : new InputFilter[]{customFilter});
            return;
        }
        if (customFilter == null) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
        } else {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength), customFilter});
        }
    }

    /**
     * @deprecated 请使用 {@link #setCustomInputFilter(InputFilter)}
     */
    @Deprecated
    public void setInputFilters(InputFilter filter) {
        setCustomInputFilter(filter);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (getText() == null) {
            return;
        }
        if (hasFocus) {
            setSelection(getText().length());
            startCursorBlink();
        } else {
            stopCursorBlink();
        }
        invalidate();
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        // 禁用复制、粘贴等上下文菜单，避免破坏分格展示逻辑
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (height < mStrokeHeight) {
            height = mStrokeHeight;
        }

        int recommendWidth = mStrokeWidth * mMaxLength + mStrokePadding * Math.max(0, mMaxLength - 1);
        if (width < recommendWidth) {
            width = recommendWidth;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTextColor = getCurrentTextColor();
        setTextColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        setTextColor(mTextColor);
        drawStrokeBackground(canvas);
        drawCursorBackground(canvas);
        drawText(canvas);
    }

    /**
     * 绘制所有验证码方格背景，当前待输入方格高亮显示。
     *
     * @param canvas 画布
     */
    private void drawStrokeBackground(Canvas canvas) {
        if (mStrokeDrawable == null) {
            return;
        }
        int activeIndex = getActiveCellIndex();
        for (int i = 0; i < mMaxLength; i++) {
            int left = getCellLeft(i);
            mDrawRect.set(left, 0, left + mStrokeWidth, mStrokeHeight);
            mStrokeDrawable.setBounds(mDrawRect);
            mStrokeDrawable.setState(i == activeIndex ? STATE_FOCUSED : STATE_UNFOCUSED);
            mStrokeDrawable.draw(canvas);
        }
    }

    /**
     * 绘制光标；仅在当前待输入方格且处于“亮”态时显示。
     *
     * @param canvas 画布
     */
    private void drawCursorBackground(Canvas canvas) {
        if (mCursorDrawable == null) {
            return;
        }
        int activeIndex = getActiveCellIndex();
        if (activeIndex < 0 || !mCursorVisible) {
            return;
        }
        int left = getCellLeft(activeIndex) + (mStrokeWidth - mCursorWidth) / 2;
        int top = (mStrokeHeight - mCursorHeight) / 2;
        mDrawRect.set(left, top, left + mCursorWidth, top + mCursorHeight);
        mCursorDrawable.setBounds(mDrawRect);
        mCursorDrawable.setState(STATE_FOCUSED);
        mCursorDrawable.draw(canvas);
    }

    /**
     * 在每个方格中心绘制已输入字符。
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas) {
        int length = getEditableText().length();
        if (length <= 0) {
            return;
        }
        TextPaint textPaint = getPaint();
        textPaint.setColor(mTextColor);
        for (int i = 0; i < length; i++) {
            String text = String.valueOf(getEditableText().charAt(i));
            textPaint.getTextBounds(text, 0, 1, mDrawRect);
            int x = getCellLeft(i) + mStrokeWidth / 2 - mDrawRect.centerX();
            int y = mStrokeHeight / 2 - mDrawRect.centerY();
            canvas.drawText(text, x, y, textPaint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (!mInitialized || mMaxLength <= 0) {
            return;
        }
        int textLength = getEditableText().length();
        if (textLength == mMaxLength) {
            stopCursorBlink();
            clearFocus();
            hideSoftInput();
            if (mOnInputFinishListener != null) {
                mOnInputFinishListener.onTextFinish(getEditableText().toString(), mMaxLength);
            }
        } else if (shouldBlinkCursor()) {
            startCursorBlink();
        }
        invalidate();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (!mInitialized || mMainHandler == null || getText() == null) {
            return;
        }
        int end = getText().length();
        if (selStart == end && selEnd == end) {
            return;
        }
        // 强制光标始终在末尾，保证逐格输入体验
        mMainHandler.post(() -> setSelection(end));
    }

    @Override
    protected void onDetachedFromWindow() {
        stopCursorBlink();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        super.onDetachedFromWindow();
    }

    /**
     * 隐藏软键盘。
     */
    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    /**
     * 设置输入完成监听。
     *
     * @param onInputFinishListener 监听实例，可为 null 表示移除
     */
    public void setOnTextFinishListener(OnTextFinishListener onInputFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener;
    }

}
