package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 单行横向滚动文本控件。文字超出控件宽度时自动左右滚动，可配置速度、方向、圆角背景与描边。
 * 在 View 脱离窗口时停止滚动任务，避免 Handler/帧回调导致的内存泄漏。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/28 15:10
 */
public class MarqueeTextView extends View {

    /** 默认滚动速度（dp/秒） */
    private static final float DEFAULT_SCROLL_SPEED_DP = 40f;
    /** 滚动方向：从右向左 */
    public static final int DIRECTION_LEFT = 0;
    /** 滚动方向：从左向右 */
    public static final int DIRECTION_RIGHT = 1;

    /** 展示文案 */
    @Nullable
    private String text = "";
    /** 文字画笔 */
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    /** 文字字号（px） */
    private float textSizePx;
    /** 文字颜色 */
    @ColorInt
    private int textColor;
    /** 统一圆角半径 */
    private float radius;
    /** 左上角圆角半径 */
    private float leftTopRadius;
    /** 右上角圆角半径 */
    private float rightTopRadius;
    /** 右下角圆角半径 */
    private float rightBottomRadius;
    /** 左下角圆角半径 */
    private float leftBottomRadius;
    /** 背景色 */
    @ColorInt
    private int bgColor = Color.TRANSPARENT;
    /** 描边颜色 */
    @ColorInt
    private int strokeColor = Color.TRANSPARENT;
    /** 描边宽度 */
    private float strokeWidth;
    /** 是否已指定背景色 */
    private boolean hasBgColor;
    /** 是否启用自定义圆角背景 */
    private boolean customBackgroundEnabled;
    /** 滚动速度（px/秒） */
    private float scrollSpeedPx;
    /** 循环滚动首尾间距（px） */
    private float marqueeGapPx;
    /** 是否自动开始滚动 */
    private boolean autoStart = true;
    /** 滚动方向 */
    private int scrollDirection = DIRECTION_LEFT;
    /** 当前横向偏移量 */
    private float scrollOffset;
    /** 文字测量宽度 */
    private float textWidth;
    /** 是否正在滚动 */
    private boolean scrolling;
    /** 上一帧时间戳 */
    private long lastFrameTime;
    /** 圆角背景 */
    private final GradientDrawable gradientDrawable = new GradientDrawable();
    /** 裁剪区域 */
    private final RectF clipRect = new RectF();

    /** 逐帧滚动任务 */
    private final Runnable scrollTicker = new Runnable() {
        @Override
        public void run() {
            if (!scrolling) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            if (lastFrameTime == 0L) {
                lastFrameTime = now;
            }
            float deltaSec = (now - lastFrameTime) / 1000f;
            lastFrameTime = now;
            float step = scrollSpeedPx * deltaSec;
            if (scrollDirection == DIRECTION_RIGHT) {
                scrollOffset -= step;
            } else {
                scrollOffset += step;
            }
            float loopWidth = textWidth + marqueeGapPx;
            if (loopWidth > 0f) {
                if (scrollOffset >= loopWidth) {
                    scrollOffset %= loopWidth;
                } else if (scrollOffset < 0f) {
                    scrollOffset = (scrollOffset % loopWidth + loopWidth) % loopWidth;
                }
            }
            invalidate();
            postOnAnimation(this);
        }
    };

    /**
     * 代码创建构造
     *
     * @param context 上下文
     */
    public MarqueeTextView(Context context) {
        this(context, null);
    }

    /**
     * XML 布局构造
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 完整构造
     *
     * @param context      上下文
     * @param attrs        属性集合
     * @param defStyleAttr 默认样式
     */
    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
        if (customBackgroundEnabled) {
            applyBackground();
        }
    }

    /**
     * 解析自定义属性
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    private void initAttrs(Context context, @Nullable AttributeSet attrs) {
        textSizePx = DensityUtil.sp2px(context, 14f);
        textColor = ContextCompat.getColor(context, R.color.autoColor);
        scrollSpeedPx = DensityUtil.dp2px(context, DEFAULT_SCROLL_SPEED_DP);
        marqueeGapPx = DensityUtil.dp2px(context, 48f);
        if (attrs == null) {
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView);
        textSizePx = ta.getDimensionPixelSize(R.styleable.MarqueeTextView_textSize, (int) textSizePx);
        textColor = ta.getColor(R.styleable.MarqueeTextView_textColor, textColor);
        text = ta.getString(R.styleable.MarqueeTextView_text);
        if (text == null) {
            text = "";
        }
        float speedDp = ta.getFloat(R.styleable.MarqueeTextView_scrollSpeed, DEFAULT_SCROLL_SPEED_DP);
        scrollSpeedPx = DensityUtil.dp2px(context, speedDp);
        marqueeGapPx = ta.getDimension(R.styleable.MarqueeTextView_marqueeGap, marqueeGapPx);
        autoStart = ta.getBoolean(R.styleable.MarqueeTextView_autoStart, true);
        scrollDirection = ta.getInt(R.styleable.MarqueeTextView_scrollDirection, DIRECTION_LEFT);

        boolean hasStrokeColor = ta.hasValue(R.styleable.MarqueeTextView_strokeColor);
        boolean hasStrokeWidthAttr = ta.hasValue(R.styleable.MarqueeTextView_strokeWidth);
        hasBgColor = ta.hasValue(R.styleable.MarqueeTextView_bgColor);
        boolean hasRadiusAttr = ta.hasValue(R.styleable.MarqueeTextView_radius);
        boolean hasLeftTop = ta.hasValue(R.styleable.MarqueeTextView_leftTopRadius);
        boolean hasRightTop = ta.hasValue(R.styleable.MarqueeTextView_rightTopRadius);
        boolean hasRightBottom = ta.hasValue(R.styleable.MarqueeTextView_rightBottomRadius);
        boolean hasLeftBottom = ta.hasValue(R.styleable.MarqueeTextView_leftBottomRadius);

        if (hasBgColor) {
            bgColor = ta.getColor(R.styleable.MarqueeTextView_bgColor, Color.TRANSPARENT);
        }
        if (hasStrokeColor) {
            strokeColor = ta.getColor(R.styleable.MarqueeTextView_strokeColor, Color.TRANSPARENT);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = ta.getDimension(R.styleable.MarqueeTextView_strokeWidth, 0f);
        }
        radius = hasRadiusAttr ? ta.getDimension(R.styleable.MarqueeTextView_radius, 0f) : 0f;
        leftTopRadius = hasLeftTop ? ta.getDimension(R.styleable.MarqueeTextView_leftTopRadius, 0f) : radius;
        rightTopRadius = hasRightTop ? ta.getDimension(R.styleable.MarqueeTextView_rightTopRadius, 0f) : radius;
        rightBottomRadius = hasRightBottom ? ta.getDimension(R.styleable.MarqueeTextView_rightBottomRadius, 0f) : radius;
        leftBottomRadius = hasLeftBottom ? ta.getDimension(R.styleable.MarqueeTextView_leftBottomRadius, 0f) : radius;

        customBackgroundEnabled = hasBgColor || hasRadiusAttr || hasLeftTop || hasRightTop
                || hasRightBottom || hasLeftBottom || hasStrokeWidthAttr || hasStrokeColor;
        ta.recycle();
    }

    /**
     * 初始化文字画笔
     */
    private void initPaint() {
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        measureTextWidth();
    }

    /**
     * 重新测量文字宽度
     */
    private void measureTextWidth() {
        textWidth = TextUtils.isEmpty(text) ? 0f : textPaint.measureText(text);
    }

    /**
     * 应用圆角背景与描边
     */
    private void applyBackground() {
        customBackgroundEnabled = true;
        if (hasBgColor) {
            gradientDrawable.setColor(bgColor);
        } else {
            gradientDrawable.setColor(Color.TRANSPARENT);
        }
        if (leftTopRadius == radius && rightTopRadius == radius
                && rightBottomRadius == radius && leftBottomRadius == radius) {
            gradientDrawable.setCornerRadius(radius);
        } else {
            float[] radii = new float[]{
                    leftTopRadius, leftTopRadius,
                    rightTopRadius, rightTopRadius,
                    rightBottomRadius, rightBottomRadius,
                    leftBottomRadius, leftBottomRadius
            };
            gradientDrawable.setCornerRadii(radii);
        }
        if (strokeWidth > 0f) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        } else {
            gradientDrawable.setStroke(0, Color.TRANSPARENT);
        }
        setBackground(gradientDrawable);
    }

    /**
     * 判断文字是否超出可视区域，需要滚动
     *
     * @return true 需要滚动
     */
    private boolean needScroll() {
        float contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        return contentWidth > 0f && textWidth > contentWidth;
    }

    /**
     * 根据状态尝试启动或停止滚动
     */
    private void updateScrollState() {
        if (!isAttachedToWindow()) {
            return;
        }
        if (autoStart && needScroll()) {
            startScroll();
        } else {
            stopScroll();
            scrollOffset = 0f;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            int desired = (int) Math.ceil(textWidth) + getPaddingLeft() + getPaddingRight();
            width = widthMode == MeasureSpec.AT_MOST ? Math.min(desired, width) : desired;
        }

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        int textHeight = (int) Math.ceil(fm.descent - fm.ascent);
        int height = textHeight + getPaddingTop() + getPaddingBottom();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(height, heightSize);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollState();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateScrollState();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopScroll();
        removeCallbacks(scrollTicker);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float top = getPaddingTop();
        float bottom = getHeight() - getPaddingBottom();
        clipRect.set(left, top, right, bottom);
        canvas.save();
        canvas.clipRect(clipRect);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float baseline = top + (bottom - top - fm.bottom + fm.ascent) / 2f - fm.ascent;

        if (!needScroll()) {
            canvas.drawText(text, left, baseline, textPaint);
            canvas.restore();
            return;
        }

        float loopWidth = textWidth + marqueeGapPx;
        float startX = left - scrollOffset;
        canvas.drawText(text, startX, baseline, textPaint);
        // 绘制第二段，保证无缝循环
        canvas.drawText(text, startX + loopWidth, baseline, textPaint);
        canvas.restore();
    }

    /**
     * 开始横向滚动
     *
     * @return this
     */
    public MarqueeTextView startScroll() {
        if (!needScroll()) {
            scrolling = false;
            return this;
        }
        if (scrolling) {
            return this;
        }
        scrolling = true;
        lastFrameTime = 0L;
        removeCallbacks(scrollTicker);
        postOnAnimation(scrollTicker);
        return this;
    }

    /**
     * 停止横向滚动
     *
     * @return this
     */
    public MarqueeTextView stopScroll() {
        scrolling = false;
        lastFrameTime = 0L;
        removeCallbacks(scrollTicker);
        return this;
    }

    /**
     * 重置偏移并按需重新开始滚动
     *
     * @return this
     */
    public MarqueeTextView resetScroll() {
        scrollOffset = 0f;
        stopScroll();
        updateScrollState();
        invalidate();
        return this;
    }

    /**
     * 是否正在滚动
     *
     * @return true 正在滚动
     */
    public boolean isScrolling() {
        return scrolling;
    }

    /**
     * 设置展示文案
     *
     * @param text 文案，可为 null
     * @return this
     */
    public MarqueeTextView setMarqueeText(@Nullable CharSequence text) {
        this.text = text == null ? "" : text.toString();
        measureTextWidth();
        scrollOffset = 0f;
        requestLayout();
        updateScrollState();
        invalidate();
        return this;
    }

    /**
     * 获取展示文案
     *
     * @return 文案
     */
    @NonNull
    public String getMarqueeText() {
        return text == null ? "" : text;
    }

    /**
     * 设置文字字号（sp）
     *
     * @param sp 字号
     * @return this
     */
    public MarqueeTextView setMarqueeTextSizeSp(float sp) {
        this.textSizePx = DensityUtil.sp2px(getContext(), sp);
        textPaint.setTextSize(textSizePx);
        measureTextWidth();
        requestLayout();
        updateScrollState();
        return this;
    }

    /**
     * 设置文字字号（px）
     *
     * @param px 字号像素
     * @return this
     */
    public MarqueeTextView setMarqueeTextSizePx(float px) {
        this.textSizePx = px;
        textPaint.setTextSize(textSizePx);
        measureTextWidth();
        requestLayout();
        updateScrollState();
        return this;
    }

    /**
     * 获取文字字号（px）
     *
     * @return 字号像素
     */
    public float getMarqueeTextSizePx() {
        return textSizePx;
    }

    /**
     * 设置文字颜色
     *
     * @param color 颜色值
     * @return this
     */
    public MarqueeTextView setMarqueeTextColor(@ColorInt int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
        return this;
    }

    /**
     * 获取文字颜色
     *
     * @return 颜色值
     */
    @ColorInt
    public int getMarqueeTextColor() {
        return textColor;
    }

    /**
     * 设置滚动速度
     *
     * @param speedDpPerSecond 速度，单位 dp/秒
     * @return this
     */
    public MarqueeTextView setScrollSpeed(float speedDpPerSecond) {
        this.scrollSpeedPx = DensityUtil.dp2px(getContext(), Math.max(0f, speedDpPerSecond));
        return this;
    }

    /**
     * 设置滚动速度（px/秒）
     *
     * @param speedPxPerSecond 速度像素每秒
     * @return this
     */
    public MarqueeTextView setScrollSpeedPx(float speedPxPerSecond) {
        this.scrollSpeedPx = Math.max(0f, speedPxPerSecond);
        return this;
    }

    /**
     * 获取滚动速度（px/秒）
     *
     * @return 速度
     */
    public float getScrollSpeedPx() {
        return scrollSpeedPx;
    }

    /**
     * 设置循环间距
     *
     * @param gapPx 间距像素
     * @return this
     */
    public MarqueeTextView setMarqueeGap(float gapPx) {
        this.marqueeGapPx = Math.max(0f, gapPx);
        invalidate();
        return this;
    }

    /**
     * 获取循环间距
     *
     * @return 间距像素
     */
    public float getMarqueeGap() {
        return marqueeGapPx;
    }

    /**
     * 设置是否自动开始滚动
     *
     * @param autoStart true 自动开始
     * @return this
     */
    public MarqueeTextView setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        updateScrollState();
        return this;
    }

    /**
     * 是否自动开始滚动
     *
     * @return true 自动开始
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * 设置滚动方向
     *
     * @param direction {@link #DIRECTION_LEFT} 或 {@link #DIRECTION_RIGHT}
     * @return this
     */
    public MarqueeTextView setScrollDirection(int direction) {
        this.scrollDirection = direction == DIRECTION_RIGHT ? DIRECTION_RIGHT : DIRECTION_LEFT;
        return this;
    }

    /**
     * 获取滚动方向
     *
     * @return 方向常量
     */
    public int getScrollDirection() {
        return scrollDirection;
    }

    /**
     * 设置统一圆角半径
     *
     * @param radius 圆角半径（px）
     * @return this
     */
    public MarqueeTextView setRadius(float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        applyBackground();
        return this;
    }

    /**
     * 分别设置四个角圆角半径
     *
     * @param leftTop     左上角
     * @param rightTop    右上角
     * @param rightBottom 右下角
     * @param leftBottom  左下角
     * @return this
     */
    public MarqueeTextView setCornerRadii(float leftTop, float rightTop, float rightBottom, float leftBottom) {
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyBackground();
        return this;
    }

    /**
     * 设置背景色
     *
     * @param color 颜色值
     * @return this
     */
    public MarqueeTextView setBgColor(@ColorInt int color) {
        this.bgColor = color;
        this.hasBgColor = true;
        applyBackground();
        return this;
    }

    /**
     * 设置背景色与统一圆角
     *
     * @param color  背景色
     * @param radius 圆角半径（px）
     * @return this
     */
    public MarqueeTextView setBgColorAndRadius(@ColorInt int color, float radius) {
        this.bgColor = color;
        this.hasBgColor = true;
        return setRadius(radius);
    }

    /**
     * 设置描边
     *
     * @param strokeWidthPx 描边宽度（px）
     * @param color         描边颜色
     * @return this
     */
    public MarqueeTextView setStroke(float strokeWidthPx, @ColorInt int color) {
        this.strokeWidth = strokeWidthPx;
        this.strokeColor = color;
        applyBackground();
        return this;
    }

    /**
     * 获取统一圆角半径
     *
     * @return 圆角半径（px）
     */
    public float getRadius() {
        return radius;
    }

    /**
     * 获取左上角圆角半径
     *
     * @return 圆角半径（px）
     */
    public float getLeftTopRadius() {
        return leftTopRadius;
    }

    /**
     * 获取右上角圆角半径
     *
     * @return 圆角半径（px）
     */
    public float getRightTopRadius() {
        return rightTopRadius;
    }

    /**
     * 获取右下角圆角半径
     *
     * @return 圆角半径（px）
     */
    public float getRightBottomRadius() {
        return rightBottomRadius;
    }

    /**
     * 获取左下角圆角半径
     *
     * @return 圆角半径（px）
     */
    public float getLeftBottomRadius() {
        return leftBottomRadius;
    }

    /**
     * 获取背景色
     *
     * @return 颜色值
     */
    @ColorInt
    public int getBgColor() {
        return bgColor;
    }

    /**
     * 获取描边宽度
     *
     * @return 描边宽度（px）
     */
    public float getStrokeWidthValue() {
        return strokeWidth;
    }

    /**
     * 获取描边颜色
     *
     * @return 颜色值
     */
    @ColorInt
    public int getStrokeColor() {
        return strokeColor;
    }
}
