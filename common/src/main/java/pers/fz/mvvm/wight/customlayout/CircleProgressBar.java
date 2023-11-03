package pers.fz.mvvm.wight.customlayout;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.databinding.BindingAdapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.listener.OnProgressEndListener;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/11/2 9:20
 * describe :自定义圆形进度条
 */
public class CircleProgressBar extends View {
    private final String TAG = CircleProgressBar.class.getSimpleName();
    private final int DEFAULT_BG_COLOR = Color.GRAY;
    private final int DEFAULT_PROGRESS_COLOR = Color.GREEN;
    private final int DEFAULT_STROKE_WIDTH = 3;

    private int bgColor = DEFAULT_BG_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;
    private float strokeWidth = DEFAULT_STROKE_WIDTH;
    private boolean showText = true;
    private float fontSize = 14;
    private int fontColor = Color.BLACK;
    private final Paint paintCircleBottom = new Paint();
    private final Paint paintArcTop = new Paint();
    private final Paint paintText = new Paint();
    /**
     * 数字显示的小数点位置，为0时则保留整数
     */
    private int fontPercent = 2;
    private float maxProgress = 100;
    /**
     * 弧形的角度
     */
    private float angle;
    private OnProgressEndListener onProgressEndListener;
    public CircleProgressBar(Context context) {
        super(context);
        init(null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        initPaint();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        initPaint();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
        initPaint();
    }

    /**
     * 初始化
     *
     * @param attrs AttributeSet
     */
    @SuppressLint("CustomViewStyleable")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Custom_Progress_Bar);
            bgColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_bgColor, DEFAULT_BG_COLOR);
            progressColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_progressColor, DEFAULT_PROGRESS_COLOR);
            strokeWidth = typedArray.getDimension(R.styleable.Custom_Progress_Bar_strokeWidth, DEFAULT_STROKE_WIDTH);
            fontColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_fontColor, Color.BLACK);
            fontSize = typedArray.getDimension(R.styleable.Custom_Progress_Bar_fontSize, 14);
            showText = typedArray.getBoolean(R.styleable.Custom_Progress_Bar_showText, true);
            fontPercent = typedArray.getInt(R.styleable.Custom_Progress_Bar_fontPercent, 2);
            maxProgress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_maxProgress, 100f);
            float progress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_progress, 0);
            angle = progress / maxProgress * 360;
            typedArray.recycle();
        }
    }

    public void initPaint() {
        //初始化文本的画笔
        paintText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(fontColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(fontSize);

        //初始化底层圆形的画笔
        paintCircleBottom.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintCircleBottom.setColor(bgColor);
        paintCircleBottom.setStrokeWidth(strokeWidth);
        paintCircleBottom.setStrokeCap(Paint.Cap.ROUND);
        paintCircleBottom.setStyle(Paint.Style.STROKE);

        //初始化弧形的画笔
        paintArcTop.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintArcTop.setColor(progressColor);
        paintArcTop.setStrokeWidth(strokeWidth);
        paintArcTop.setStrokeCap(Paint.Cap.ROUND);
        paintArcTop.setStyle(Paint.Style.STROKE);
    }

    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        this.onProgressEndListener = onProgressEndListener;
    }

    /**
     * 背景色
     * @param bgColor
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * 文字颜色
     * @param fontColor
     */
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * 是否显示文字
     * @param showText true默认显示
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    /**
     * 进度宽度
     * @param strokeWidth
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * 文字小数位数
     * @param fontPercent 默认2位，如果为0则为整数
     */
    public void setFontPercent(int fontPercent) {
        this.fontPercent = fontPercent;
    }

    /**
     * 文字大小
     * @param fontSize 文字大小
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * 进度颜色
     * @param progressColor 颜色
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 圆心坐标是(centerX,centerY)
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        //确定半径
        float radius = Math.min(centerX, centerY) - paintCircleBottom.getStrokeWidth();

        //绘制底层圆形
        canvas.drawCircle(centerX, centerY, radius, paintCircleBottom);

        //绘制上层弧形,从顶部开始，顺时针走90°
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 270, angle, false, paintArcTop);
        if (!showText) {
            return;
        }
        //绘制文本,文字中心和圆心保持一致
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        // 设置小数位数
        percentFormat.setMinimumFractionDigits(fontPercent);
        percentFormat.setMaximumFractionDigits(fontPercent);

        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = centerY + distance;
        //文字中心和圆心一致
        canvas.drawText(percentFormat.format(angle / 360), centerX, baseline, paintText);
    }

    /**
     * 更新进度
     *
     * @param progress 进度数值
     */
    public void postProgress(float progress) {
        angle = progress / maxProgress * 360;
        invalidate();
        if (onProgressEndListener == null) {
            return;
        }
        if (progress == maxProgress) {
            onProgressEndListener.onEnd();
        }
    }

    /**
     * 设置进度，展现动画
     */
    public void setProgress(float progress) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 100f);
        animator.addUpdateListener(animation -> {
            float cur = (float) animation.getAnimatedValue();
            angle = cur / maxProgress * 360 * progress / 100;
            invalidate();
            if (onProgressEndListener == null) {
                return;
            }
            if (cur < 100) {
                return;
            }
            onProgressEndListener.onEnd();
        });
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    /**
     * 设置进度，展现动画
     *
     * @param progress 进度值
     * @param duration 动画时间
     */
    public void setProgress(float progress, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 100f);
        animator.addUpdateListener(animation -> {
            float cur = (float) animation.getAnimatedValue();
            angle = cur / 100 * 360 * progress / maxProgress;
            invalidate();
            if (onProgressEndListener == null) {
                return;
            }
            if (cur < 100) {
                return;
            }
            onProgressEndListener.onEnd();
        });
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

}
