package io.coderf.arklab.common.widget.customview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.listener.OnProgressEndListener;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 自定义圆形进度条。
 * <p>
 * 支持通过 XML 属性或 setter 设置颜色、环宽、文字样式等；进度更新支持直接设置
 * （{@link #postProgress(float)}）与动画（{@link #setProgress(float)}）两种方式。
 * <p>
 * 注意：所有进度更新方法均需在主线程调用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/11/2 9:20
 */
public class CircleProgressBar extends View {
    protected final String TAG = CircleProgressBar.class.getSimpleName();
    /**
     * 默认背景（底环）颜色
     */
    protected final int DEFAULT_BG_COLOR = Color.GRAY;
    /**
     * 默认进度（弧）颜色
     */
    protected final int DEFAULT_PROGRESS_COLOR = Color.GREEN;
    /**
     * 默认进度条宽度（dp）
     */
    protected final int DEFAULT_STROKE_WIDTH = 3;
    /**
     * wrap_content 时的默认控件尺寸（dp）
     */
    protected final int DEFAULT_SIZE = 100;
    /**
     * 默认进度动画时长（毫秒）
     */
    protected static final long DEFAULT_ANIM_DURATION = 3000L;
    /**
     * 背景（底环）颜色
     */
    protected int bgColor = DEFAULT_BG_COLOR;
    /**
     * 进度（弧）颜色
     */
    protected int progressColor = DEFAULT_PROGRESS_COLOR;
    /**
     * 进度条宽度（px）
     */
    protected float strokeWidth = DEFAULT_STROKE_WIDTH;
    /**
     * 是否显示百分比文字
     */
    protected boolean showText = true;
    /**
     * 文字大小（px）
     */
    protected float fontSize = 14;
    /**
     * 文字颜色
     */
    protected int fontColor = Color.BLACK;
    /**
     * 底层圆环画笔
     */
    protected final Paint paintCircleBottom = new Paint();
    /**
     * 上层进度弧画笔
     */
    protected final Paint paintArcTop = new Paint();
    /**
     * 文字画笔
     */
    protected final Paint paintText = new Paint();
    /**
     * 百分比文字保留的小数位数，为 0 时只保留整数
     */
    protected int fontPercent = 2;
    /**
     * 当前进度值，范围 [0, maxProgress]
     */
    protected float progress;
    /**
     * 最大进度值，必须大于 0
     */
    protected float maxProgress = 100;
    /**
     * 进度弧对应的扫过角度（0~360）
     */
    protected float angle;
    /**
     * 进度动画；重复调用 setProgress 前会先取消旧动画，避免并发动画导致进度抖动
     */
    private ValueAnimator animator;
    /**
     * 百分比格式化器；onDraw 中复用，避免每帧创建产生 GC 抖动
     */
    private final DecimalFormat percentFormat = createPercentFormat();
    /**
     * 进度动画结束回调
     */
    protected OnProgressEndListener onProgressEndListener;

    /**
     * 代码中直接创建控件时使用。
     *
     * @param context 上下文
     */
    public CircleProgressBar(Context context) {
        super(context);
        init(null);
        initPaint();
    }

    /**
     * XML 布局中 inflate 时使用。
     *
     * @param context 上下文
     * @param attrs   布局属性
     */
    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        initPaint();
    }

    /**
     * XML 布局中 inflate 且指定默认 style 属性时使用。
     *
     * @param context      上下文
     * @param attrs        布局属性
     * @param defStyleAttr 默认 style 属性
     */
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        initPaint();
    }

    /**
     * XML 布局中 inflate 且指定默认 style 资源时使用。
     *
     * @param context      上下文
     * @param attrs        布局属性
     * @param defStyleAttr 默认 style 属性
     * @param defStyleRes  默认 style 资源
     */
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
        initPaint();
    }

    /**
     * 从 XML 属性或主题默认值读取样式。
     *
     * @param attrs 布局属性，代码创建时为 null
     */
    @SuppressLint("CustomViewStyleable")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Custom_Progress_Bar);
            bgColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_bgColor,
                    io.coderf.arklab.common.utils.theme.ThemeAttrs.outlineVariant(getContext()));
            progressColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_progressColor,
                    io.coderf.arklab.common.utils.theme.ThemeAttrs.primary(getContext()));
            strokeWidth = typedArray.getDimension(R.styleable.Custom_Progress_Bar_strokeWidth,
                    DensityUtil.dp2px(getContext(), DEFAULT_STROKE_WIDTH));
            fontColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_textColor,
                    io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext()));
            fontSize = typedArray.getDimension(R.styleable.Custom_Progress_Bar_textSize, DensityUtil.sp2px(getContext(), 12));
            showText = typedArray.getBoolean(R.styleable.Custom_Progress_Bar_showText, true);
            // 小数位数是整数语义，不做 dp 换算
            fontPercent = typedArray.getInt(R.styleable.Custom_Progress_Bar_fontPercent, 2);
            maxProgress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_maxProgress, 100f);
            progress = clampProgress(typedArray.getFloat(R.styleable.Custom_Progress_Bar_progress, 0));
            typedArray.recycle();
        } else {
            bgColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.outlineVariant(getContext());
            progressColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.primary(getContext());
            fontColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext());
            fontSize = DensityUtil.sp2px(getContext(), 14);
            strokeWidth = DensityUtil.dp2px(getContext(), DEFAULT_STROKE_WIDTH);
        }
        updatePercentFormat();
        updateAngle();
    }

    /**
     * 初始化画笔配置。
     * <p>注意：修改样式请优先使用各 setter（会同步更新画笔并重绘）；
     * 本方法会把三个画笔整体重建为当前字段值，仅供需要整体重置的场景调用。</p>
     */
    public void initPaint() {
        // 初始化文本的画笔
        paintText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(fontColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(fontSize);

        // 初始化底层圆形的画笔
        paintCircleBottom.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintCircleBottom.setColor(bgColor);
        paintCircleBottom.setStrokeWidth(strokeWidth);
        paintCircleBottom.setStrokeCap(Paint.Cap.ROUND);
        paintCircleBottom.setStyle(Paint.Style.STROKE);

        // 初始化弧形的画笔
        paintArcTop.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintArcTop.setColor(progressColor);
        paintArcTop.setStrokeWidth(strokeWidth);
        paintArcTop.setStrokeCap(Paint.Cap.ROUND);
        paintArcTop.setStyle(Paint.Style.STROKE);
    }

    /**
     * 创建固定语言环境的百分比格式化器。
     * <p>不使用 NumberFormat.getPercentInstance()，避免输出依赖系统 locale
     * （如阿拉伯语环境）导致进度文字异常。</p>
     */
    private static DecimalFormat createPercentFormat() {
        // "0%" 模式会把数值乘以 100 并在末尾追加 % 符号
        return new DecimalFormat("0%", new DecimalFormatSymbols(Locale.CHINA));
    }

    /**
     * 按当前小数位数同步格式化器配置。
     */
    private void updatePercentFormat() {
        percentFormat.setMinimumFractionDigits(fontPercent);
        percentFormat.setMaximumFractionDigits(fontPercent);
    }

    /**
     * 按当前进度与最大进度重新计算弧角度。
     */
    private void updateAngle() {
        if (maxProgress <= 0) {
            angle = 0;
        } else {
            angle = progress / maxProgress * 360;
        }
    }

    /**
     * 设置进度动画结束回调。
     *
     * @param onProgressEndListener 回调，传 null 可取消回调
     */
    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        this.onProgressEndListener = onProgressEndListener;
    }

    /**
     * 设置背景（底环）颜色，立即生效并重绘。
     *
     * @param bgColor 背景颜色
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        paintCircleBottom.setColor(bgColor);
        invalidate();
    }

    /**
     * 设置文字颜色，立即生效并重绘。
     *
     * @param fontColor 文字颜色
     */
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        paintText.setColor(fontColor);
        invalidate();
    }

    /**
     * 设置最大进度值。
     * <p>当前进度值会被重新钳制，弧角度按新上限重新计算；传入非法值（&lt;= 0）时忽略本次设置。</p>
     *
     * @param maxProgress 最大进度值，必须大于 0
     */
    public void setMaxProgress(float maxProgress) {
        if (maxProgress <= 0) {
            Log.w(TAG, "setMaxProgress: 最大进度值必须大于 0，忽略非法值 " + maxProgress);
            return;
        }
        this.maxProgress = maxProgress;
        this.progress = clampProgress(this.progress);
        updateAngle();
        invalidate();
    }

    /**
     * 设置是否显示百分比文字，立即生效并重绘。
     *
     * @param showText true 显示
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
        invalidate();
    }

    /**
     * 设置进度条宽度，立即生效并重绘。
     *
     * @param strokeWidth 进度条宽度（px）
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        paintCircleBottom.setStrokeWidth(strokeWidth);
        paintArcTop.setStrokeWidth(strokeWidth);
        invalidate();
    }

    /**
     * 设置百分比文字的小数位数，立即生效并重绘。
     *
     * @param fontPercent 小数位数，0 表示只保留整数
     */
    public void setFontPercent(int fontPercent) {
        this.fontPercent = fontPercent;
        updatePercentFormat();
        invalidate();
    }

    /**
     * 设置文字大小，立即生效并重绘。
     *
     * @param fontSize 文字大小（px）
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        paintText.setTextSize(fontSize);
        invalidate();
    }

    /**
     * 设置进度颜色，立即生效并重绘。
     *
     * @param progressColor 进度颜色
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        paintArcTop.setColor(progressColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 圆心坐标
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        // 半径 = 内切半径 - 画笔宽度：STROKE 绘制时笔迹会向外延伸半个笔宽，
        // 减掉整个笔宽后圆环外缘不会超出控件边界
        float radius = Math.min(centerX, centerY) - paintCircleBottom.getStrokeWidth();
        // 防御：控件过小或画笔过粗时半径可能为负，直接放弃绘制
        if (radius <= 0) {
            return;
        }
        // 绘制底层圆形
        canvas.drawCircle(centerX, centerY, radius, paintCircleBottom);
        // 绘制上层弧形：从顶部（-90°）开始顺时针扫过 angle 度
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                270, angle, false, paintArcTop);
        if (!showText) {
            return;
        }
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        // 计算基线偏移，使文字中心和圆心保持一致
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = centerY + distance;
        // 复用缓存的格式化器，避免每帧创建
        canvas.drawText(percentFormat.format(angle / 360), centerX, baseline, paintText);
    }

    /**
     * 直接更新进度，无动画。需在主线程调用。
     * <p>若进度达到最大值会触发 {@link #onProgressEndListener}；若存在进行中的动画会先取消。</p>
     *
     * @param progress 进度数值，超出 [0, maxProgress] 范围会被钳制
     */
    public void postProgress(float progress) {
        cancelAnimation();
        this.progress = clampProgress(progress);
        updateAngle();
        invalidate();
        if (onProgressEndListener != null && maxProgress > 0 && this.progress >= maxProgress) {
            onProgressEndListener.onEnd();
        }
    }

    /**
     * 设置进度并展示默认时长（3 秒）的加速动画。需在主线程调用。
     * <p>动画从当前进度插值到目标进度，重复调用会先取消上一次动画。</p>
     *
     * @param progress 目标进度数值，超出 [0, maxProgress] 范围会被钳制
     */
    public void setProgress(float progress) {
        setProgress(progress, DEFAULT_ANIM_DURATION);
    }

    /**
     * 设置进度并展示指定时长的加速动画。需在主线程调用。
     * <p>动画从当前进度插值到目标进度，重复调用会先取消上一次动画，
     * 避免多个动画并发导致进度抖动。</p>
     *
     * @param progress 目标进度数值，超出 [0, maxProgress] 范围会被钳制
     * @param duration 动画时长（毫秒）
     */
    public void setProgress(float progress, long duration) {
        final float start = this.progress;
        final float target = clampProgress(progress);
        cancelAnimation();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(animation -> {
            // fraction 表示动画完成度（0~1），据此在当前进度与目标进度间插值
            float fraction = (float) animation.getAnimatedValue();
            this.progress = start + (target - start) * fraction;
            updateAngle();
            invalidate();
            if (onProgressEndListener != null && fraction >= 1f) {
                onProgressEndListener.onEnd();
            }
        });
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    /**
     * 取消正在进行的进度动画（无动画时为空操作）。
     */
    private void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // 控件移除时取消动画，避免动画继续持有控件引用并向已销毁的宿主回调
        cancelAnimation();
        super.onDetachedFromWindow();
    }

    /**
     * 将进度值钳制在 [0, maxProgress] 范围内。
     *
     * @param value 原始进度值
     * @return 钳制后的进度值；maxProgress 非法时返回 0
     */
    private float clampProgress(float value) {
        if (maxProgress <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(value, maxProgress));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = DensityUtil.dp2px(getContext(), DEFAULT_SIZE);
        int width = measureDimension(defaultSize, widthMeasureSpec);
        int height = measureDimension(defaultSize, heightMeasureSpec);
        // 圆形控件宽高保持一致，取两者较小值
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    /**
     * 解析单个测量维度。
     * <p>EXACTLY 以父容器给定尺寸为准；AT_MOST（wrap_content）时取默认尺寸兜底，
     * 避免未指定尺寸时控件塌陷为 0。</p>
     *
     * @param defaultSize 默认尺寸（px）
     * @param measureSpec 测量规格
     * @return 解析后的尺寸（px）
     */
    private int measureDimension(int defaultSize, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = defaultSize;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        }
        return result;
    }

}
