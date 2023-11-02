package pers.fz.mvvm.wight.customlayout;

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
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import java.text.NumberFormat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.listener.OnProgressEndListener;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/11/2 9:20
 * describe :自定义圆形进度条
 */
public class HorizontalProgressBar extends View {
    private final String TAG = HorizontalProgressBar.class.getSimpleName();
    private final int DEFAULT_BG_COLOR = Color.GRAY;
    private final int DEFAULT_PROGRESS_COLOR = Color.GREEN;

    private int bgColor = DEFAULT_BG_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;
    private boolean showText = true;
    private float fontSize = 14;
    private int fontColor = Color.BLACK;
    private Paint paintText;
    /**
     * 数字显示的小数点位置，为0时则保留整数
     */
    private int fontPercent = 2;
    private Rect textBounds;
    private final float DEFAULT_RADIUS = 10;
    private float bgRadius = DEFAULT_RADIUS;
    /**
     * 进度
     */
    private float progress;
    private final float DEFAULT_MAX_PROGRESS = 100;
    /**
     * 最大进度
     */
    private float maxProgress = DEFAULT_MAX_PROGRESS;
    private Paint backgroundPaint;
    private Paint progressBarPaint;
    private RectF backgroundRect;
    private RectF progressRect;
    private Path mBgPath;
    private Path mProgressPath;
    private OnProgressEndListener onProgressEndListener;

    public HorizontalProgressBar(Context context) {
        super(context);
        init(null);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        initPaint();
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        initPaint();
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
            fontColor = typedArray.getColor(R.styleable.Custom_Progress_Bar_fontColor, Color.BLACK);
            fontSize = typedArray.getDimension(R.styleable.Custom_Progress_Bar_fontSize, 14);
            showText = typedArray.getBoolean(R.styleable.Custom_Progress_Bar_showText, true);
            fontPercent = typedArray.getInt(R.styleable.Custom_Progress_Bar_fontPercent, 2);
            bgRadius = typedArray.getDimension(R.styleable.Custom_Progress_Bar_bgRadius, DEFAULT_RADIUS);
            maxProgress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_maxProgress, DEFAULT_MAX_PROGRESS);
            progress = typedArray.getFloat(R.styleable.Custom_Progress_Bar_progress, 0);
            typedArray.recycle();
        }
    }

    public void initPaint() {
        //初始化文本的画笔
        paintText = new Paint();
        paintText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(fontColor);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTextSize(fontSize);

        //初始化背景的画笔
        backgroundPaint = new Paint();
        backgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(bgColor);
        backgroundRect = new RectF();
        mBgPath = new Path();
        //初始化进度条的画笔
        progressBarPaint = new Paint();
        progressBarPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressBarPaint.setAntiAlias(true);
        progressBarPaint.setColor(progressColor);
        progressRect = new RectF();
        mProgressPath = new Path();
        //文字
        textBounds = new Rect();
    }

    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        this.onProgressEndListener = onProgressEndListener;
    }

    /**
     * 背景色
     *
     * @param bgColor
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * 文字颜色
     *
     * @param fontColor
     */
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    /**
     * 是否显示文字
     *
     * @param showText true默认显示
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    /**
     * 文字小数位数
     *
     * @param fontPercent 默认2位，如果为0则为整数
     */
    public void setFontPercent(int fontPercent) {
        this.fontPercent = fontPercent;
    }

    /**
     * 文字大小
     *
     * @param fontSize 文字大小
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * 进度颜色
     *
     * @param progressColor 颜色
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // 绘制背景
        float width = getWidth();
        float height = getHeight();
        mBgPath.reset();
        mProgressPath.reset();
        backgroundRect.set(0, 0, getWidth(), getHeight());
        mBgPath.addRoundRect(backgroundRect, bgRadius, bgRadius, Path.Direction.CW);
        canvas.clipPath(mBgPath);
        //四个角：右上，右下，左下，左上
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
        super.onDraw(canvas);
        float progressWidth = width * progress / maxProgress;
        progressRect.set(0, 0, progressWidth, getHeight());
        mProgressPath.addRoundRect(progressRect, bgRadius, bgRadius, Path.Direction.CW);
        canvas.clipPath(mProgressPath);
        //四个角：右上，右下，左下，左上
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
        if (!showText) {
            return;
        }
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        // 设置小数位数
        percentFormat.setMinimumFractionDigits(fontPercent);
        percentFormat.setMaximumFractionDigits(fontPercent);
        // 获取进度百分比文字
        String text = percentFormat.format(progress / maxProgress);
        // 计算文字的宽度和高度
        paintText.getTextBounds(text, 0, text.length(), textBounds);
        int textWidth = textBounds.width();

        // 计算文字的位置
        int textX = (int) ((width / maxProgress) * progress) - textWidth;

        // 如果文字显示区域不够，则不绘制文字
        if (textX + textWidth > width) {
            return;
        }
        // 圆心坐标是(centerX,centerY)
        int centerY = getHeight() / 2;
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = centerY + distance;
        //文字中心和圆心一致
        canvas.drawText(text, textX, baseline, paintText);
    }

    /**
     * 设置进度，展现动画
     */
    public void setProgress(float progress) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 100);
        animator.addUpdateListener(animation -> {
            float cur = (float) animation.getAnimatedValue();
            this.progress = cur / 100 * maxProgress * progress / maxProgress;
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
            this.progress = cur / 100 * maxProgress * progress / maxProgress;
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

    public void postProgress(float progress) {
        this.progress = progress;
        invalidate();
        if (onProgressEndListener == null) {
            return;
        }
        if (progress == maxProgress) {
            onProgressEndListener.onEnd();
        }
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

}
