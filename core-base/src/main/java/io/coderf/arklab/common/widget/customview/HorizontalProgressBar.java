package io.coderf.arklab.common.widget.customview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
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
 * 自定义水平横向进度条。
 * <p>
 * 支持通过 XML 属性或 setter 设置颜色、圆角、文字样式等；进度更新支持直接设置
 * （{@link #postProgress(float)}）与动画（{@link #setProgress(float)}）两种方式。
 * <p>
 * 注意：所有进度更新方法均需在主线程调用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/11/2 9:20
 */
public class HorizontalProgressBar extends View {
    private final String TAG = HorizontalProgressBar.class.getSimpleName();
    /**
     * 默认背景颜色
     */
    private final int DEFAULT_BG_COLOR = Color.GRAY;
    /**
     * 默认进度颜色
     */
    private final int DEFAULT_PROGRESS_COLOR = Color.GREEN;
    /**
     * 默认圆角半径（dp）
     */
    private final float DEFAULT_RADIUS = 10;
    /**
     * 默认最大进度
     */
    private final float DEFAULT_MAX_PROGRESS = 100;
    /**
     * wrap_content 时的默认宽度（dp）
     */
    private final int DEFAULT_WIDTH = 200;
    /**
     * wrap_content 时的默认高度（dp）
     */
    private final int DEFAULT_HEIGHT = 20;
    /**
     * 默认进度动画时长（毫秒）
     */
    private static final long DEFAULT_ANIM_DURATION = 3000L;

    /**
     * 背景颜色
     */
    private int bgColor = DEFAULT_BG_COLOR;
    /**
     * 进度颜色
     */
    private int progressColor = DEFAULT_PROGRESS_COLOR;
    /**
     * 是否显示百分比文字
     */
    private boolean showText = true;
    /**
     * 文字大小（px）
     */
    private float fontSize = 14;
    /**
     * 文字颜色
     */
    private int fontColor;
    /**
     * 文字画笔
     */
    private Paint paintText;
    /**
     * 百分比文字保留的小数位数，为 0 时只保留整数
     */
    private int fontPercent = 2;
    /**
     * 文字测量区域
     */
    private Rect textBounds;
    /**
     * 背景圆角半径（px）
     */
    private float bgRadius = DEFAULT_RADIUS;
    /**
     * 当前进度值，范围 [0, maxProgress]
     */
    private float progress;
    /**
     * 最大进度值，必须大于 0
     */
    private float maxProgress = DEFAULT_MAX_PROGRESS;
    /**
     * 背景画笔
     */
    private Paint backgroundPaint;
    /**
     * 进度条画笔
     */
    private Paint progressBarPaint;
    /**
     * 背景区域
     */
    private RectF backgroundRect;
    /**
     * 进度条区域
     */
    private RectF progressRect;
    /**
     * 背景圆角裁剪路径
     */
    private Path mBgPath;
    /**
     * 进度条圆角裁剪路径
     */
    private Path mProgressPath;
    /**
     * 进度动画结束回调
     */
    private OnProgressEndListener onProgressEndListener;
    /**
     * 进度动画；重复调用 setProgress 前会先取消旧动画，避免并发动画导致进度抖动
     */
    private ValueAnimator animator;
    /**
     * 百分比格式化器；onDraw 中复用，避免每帧创建产生 GC 抖动
     */
    private final DecimalFormat percentFormat = createPercentFormat();

    /**
     * 代码中直接创建控件时使用。
     *
     * @param context 上下文
     */
    public HorizontalProgressBar(Context context) {
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
    public HorizontalProgressBar(Context context, AttributeSet attrs) {
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
    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            fontColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_textColor,
                    io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext()));
            fontSize = typedArray.getDimension(R.styleable.Custom_Progress_Bar_textSize, DensityUtil.dp2px(getContext(), 14));
            showText = typedArray.getBoolean(R.styleable.Custom_Progress_Bar_showText, true);
            // 小数位数是整数语义，不做 dp 换算
            fontPercent = typedArray.getInt(R.styleable.Custom_Progress_Bar_fontPercent, 2);
            bgRadius = typedArray.getDimension(R.styleable.Custom_Progress_Bar_bgRadius, DensityUtil.dp2px(getContext(), DEFAULT_RADIUS));
            maxProgress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_maxProgress, DEFAULT_MAX_PROGRESS);
            progress = clampProgress(typedArray.getFloat(R.styleable.Custom_Progress_Bar_progress, 0));
            typedArray.recycle();
        } else {
            bgColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.outlineVariant(getContext());
            progressColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.primary(getContext());
            fontColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext());
            fontSize = DensityUtil.sp2px(getContext(), 14);
            bgRadius = DensityUtil.dp2px(getContext(), DEFAULT_RADIUS);
        }
        updatePercentFormat();
    }

    /**
     * 初始化画笔与绘制对象。
     * <p>注意：修改样式请优先使用各 setter（会同步更新画笔并重绘）；
     * 本方法会整体重建画笔，仅供需要整体重置的场景调用。</p>
     */
    public void initPaint() {
        // 初始化文本的画笔
        paintText = new Paint();
        paintText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(fontColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(fontSize);

        // 初始化背景的画笔
        backgroundPaint = new Paint();
        backgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(bgColor);
        backgroundRect = new RectF();
        mBgPath = new Path();
        // 初始化进度条的画笔
        progressBarPaint = new Paint();
        progressBarPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressBarPaint.setAntiAlias(true);
        progressBarPaint.setColor(progressColor);
        progressRect = new RectF();
        mProgressPath = new Path();
        // 文字测量区域
        textBounds = new Rect();
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
     * 设置进度动画结束回调。
     *
     * @param onProgressEndListener 回调，传 null 可取消回调
     */
    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        this.onProgressEndListener = onProgressEndListener;
    }

    /**
     * 设置背景颜色，立即生效并重绘。
     *
     * @param bgColor 背景颜色
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        backgroundPaint.setColor(bgColor);
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
     * 设置是否显示百分比文字，立即生效并重绘。
     *
     * @param showText true 显示
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
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
        progressBarPaint.setColor(progressColor);
        invalidate();
    }

    /**
     * 获取当前进度值。
     *
     * @return 当前进度值
     */
    public float getProgress() {
        return progress;
    }

    /**
     * 设置最大进度值。
     * <p>当前进度值会被重新钳制，进度比例随之重新计算；传入非法值（&lt;= 0）时忽略本次设置。</p>
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
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        mBgPath.reset();
        mProgressPath.reset();
        backgroundRect.set(0, 0, getWidth(), getHeight());
        // 保存未裁剪的画布状态，文字需要在完整区域内绘制
        canvas.save();
        // 用圆角矩形路径裁剪画布，保证背景只显示在圆角范围内
        mBgPath.addRoundRect(backgroundRect, bgRadius, bgRadius, Path.Direction.CW);
        canvas.clipPath(mBgPath);
        // 重建圆角轮廓路径用于绘制（开放路径以 FILL 填充时等价于整个圆角矩形）
        mBgPath.moveTo(bgRadius, 0);
        mBgPath.lineTo(width - bgRadius, 0);
        mBgPath.quadTo(width, 0, width, bgRadius);

        mBgPath.lineTo(width, height - bgRadius);
        mBgPath.quadTo(width, height, width - bgRadius, height);

        mBgPath.lineTo(bgRadius, height);
        mBgPath.quadTo(0, height, 0, height - bgRadius);

        mBgPath.lineTo(0, bgRadius);
        mBgPath.quadTo(0, 0, bgRadius, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipPath(mBgPath);
        } else {
            canvas.clipPath(mBgPath, Region.Op.INTERSECT);
        }
        canvas.drawPath(mBgPath, backgroundPaint);
        // 计算进度比例并钳制到 [0, 1]，防止进度越界导致绘制异常
        float progressRatio = maxProgress > 0 ? clampProgress(progress) / maxProgress : 0f;
        float progressWidth = width * progressRatio;
        progressRect.set(0, 0, progressWidth, getHeight());
        // 用进度条圆角矩形裁剪画布，保证进度只显示在其区域内
        mProgressPath.addRoundRect(progressRect, bgRadius, bgRadius, Path.Direction.CW);
        canvas.clipPath(mProgressPath);
        // 重建圆角轮廓路径用于绘制（开放路径以 FILL 填充时等价于整个圆角矩形）
        mProgressPath.moveTo(bgRadius, 0);
        mProgressPath.lineTo(width - bgRadius, 0);
        mProgressPath.quadTo(width, 0, width, bgRadius);

        mProgressPath.lineTo(width, height - bgRadius);
        mProgressPath.quadTo(width, height, width - bgRadius, height);

        mProgressPath.lineTo(bgRadius, height);
        mProgressPath.quadTo(0, height, 0, height - bgRadius);

        mProgressPath.lineTo(0, bgRadius);
        mProgressPath.quadTo(0, 0, bgRadius, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipPath(mProgressPath);
        } else {
            canvas.clipPath(mProgressPath, Region.Op.INTERSECT);
        }
        canvas.drawPath(mProgressPath, progressBarPaint);
        // 还原画布裁剪状态，否则文字会被限制在进度条区域内部而显示不全
        canvas.restore();
        if (!showText) {
            return;
        }
        // 复用缓存的格式化器，避免每帧创建
        String text = percentFormat.format(progressRatio);
        // 计算文字的宽度和高度
        paintText.getTextBounds(text, 0, text.length(), textBounds);
        int textWidth = textBounds.width();
        // 文字中心默认放在进度末端左侧，避免文字右缘超过进度末端
        int textX = (int) (progressWidth - textWidth);
        // 以文字中心为基准夹取到控件可视范围内，避免小进度时文字被左边界裁掉
        int minX = textWidth / 2;
        int maxX = (int) width - textWidth / 2;
        if (maxX < minX) {
            // 控件过窄放不下文字时不绘制
            return;
        }
        textX = Math.max(minX, Math.min(textX, maxX));
        // 计算基线偏移，使文字垂直居中
        int centerY = getHeight() / 2;
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = centerY + distance;
        canvas.drawText(text, textX, baseline, paintText);
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
     * 直接更新进度，无动画。需在主线程调用。
     * <p>若进度达到最大值会触发 {@link #onProgressEndListener}；若存在进行中的动画会先取消。</p>
     *
     * @param progress 进度数值，超出 [0, maxProgress] 范围会被钳制
     */
    public void postProgress(float progress) {
        cancelAnimation();
        this.progress = clampProgress(progress);
        invalidate();
        if (onProgressEndListener != null && maxProgress > 0 && this.progress >= maxProgress) {
            onProgressEndListener.onEnd();
        }
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
        int defaultWidth = DensityUtil.dp2px(getContext(), DEFAULT_WIDTH);
        int defaultHeight = DensityUtil.dp2px(getContext(), DEFAULT_HEIGHT);
        setMeasuredDimension(measureDimension(defaultWidth, widthMeasureSpec),
                measureDimension(defaultHeight, heightMeasureSpec));
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
