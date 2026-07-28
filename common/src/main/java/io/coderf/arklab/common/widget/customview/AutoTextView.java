package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 带 3D 上下翻页动画的文字切换控件，基于 {@link TextSwitcher}。
 * 支持圆角背景、描边、自动轮播、翻页动画时长配置，并在 View 脱离窗口时释放定时任务以避免内存泄漏。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/28 15:10
 */
public class AutoTextView extends TextSwitcher implements ViewSwitcher.ViewFactory {

    /** 默认翻页动画时长（毫秒） */
    private static final long DEFAULT_ANIM_DURATION = 1000L;
    /** 默认自动翻页间隔（毫秒） */
    private static final long DEFAULT_FLIP_INTERVAL = 3000L;

    /** 文字字号，单位 px */
    private float textSizePx;
    /** 文字颜色 */
    @ColorInt
    private int textColor;
    /** 初始 / 当前展示文字 */
    @Nullable
    private String textStr;
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
    /** 翻页动画时长（毫秒） */
    private long animDuration = DEFAULT_ANIM_DURATION;
    /** 自动翻页间隔（毫秒） */
    private long flipInterval = DEFAULT_FLIP_INTERVAL;
    /** 是否自动翻页 */
    private boolean autoFlip;
    /** 文字水平对齐方式对应的 Gravity */
    private int textGravity = Gravity.CENTER;
    /** 轮播文案列表 */
    private final List<String> textList = new ArrayList<>();
    /** 当前轮播下标 */
    private int currentIndex;
    /** 是否正在自动翻页 */
    private boolean flipping;
    /** 是否向上翻页方向（true 向上，false 向下） */
    private boolean flipUp = true;
    /** 圆角背景 Drawable */
    private final GradientDrawable gradientDrawable = new GradientDrawable();
    /** 主线程 Handler，用于自动翻页 */
    private final Handler flipHandler = new Handler(Looper.getMainLooper());
    /** 自动翻页任务 */
    private final Runnable flipRunnable = new Runnable() {
        @Override
        public void run() {
            if (!flipping || textList.size() <= 1) {
                return;
            }
            showNextText();
            flipHandler.postDelayed(this, flipInterval);
        }
    };

    /** 向上翻页进入动画 */
    private Rotate3dAnimation inUp;
    /** 向上翻页退出动画 */
    private Rotate3dAnimation outUp;
    /** 向下翻页进入动画 */
    private Rotate3dAnimation inDown;
    /** 向下翻页退出动画 */
    private Rotate3dAnimation outDown;

    /**
     * 代码创建构造
     *
     * @param context 上下文
     */
    public AutoTextView(Context context) {
        this(context, null);
    }

    /**
     * XML 布局构造
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    public AutoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
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
        if (attrs == null) {
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoTextView);
        textSizePx = ta.getDimensionPixelSize(R.styleable.AutoTextView_textSize, (int) textSizePx);
        textColor = ta.getColor(R.styleable.AutoTextView_textColor, textColor);
        textStr = ta.getString(R.styleable.AutoTextView_text);
        animDuration = ta.getInt(R.styleable.AutoTextView_animDuration, (int) DEFAULT_ANIM_DURATION);
        flipInterval = ta.getInt(R.styleable.AutoTextView_flipInterval, (int) DEFAULT_FLIP_INTERVAL);
        autoFlip = ta.getBoolean(R.styleable.AutoTextView_autoFlip, false);

        boolean hasStrokeColor = ta.hasValue(R.styleable.AutoTextView_strokeColor);
        boolean hasStrokeWidthAttr = ta.hasValue(R.styleable.AutoTextView_strokeWidth);
        hasBgColor = ta.hasValue(R.styleable.AutoTextView_bgColor);
        boolean hasRadiusAttr = ta.hasValue(R.styleable.AutoTextView_radius);
        boolean hasLeftTop = ta.hasValue(R.styleable.AutoTextView_leftTopRadius);
        boolean hasRightTop = ta.hasValue(R.styleable.AutoTextView_rightTopRadius);
        boolean hasRightBottom = ta.hasValue(R.styleable.AutoTextView_rightBottomRadius);
        boolean hasLeftBottom = ta.hasValue(R.styleable.AutoTextView_leftBottomRadius);

        if (hasBgColor) {
            bgColor = ta.getColor(R.styleable.AutoTextView_bgColor, Color.TRANSPARENT);
        }
        if (hasStrokeColor) {
            strokeColor = ta.getColor(R.styleable.AutoTextView_strokeColor, Color.TRANSPARENT);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = ta.getDimension(R.styleable.AutoTextView_strokeWidth, 0f);
        }
        radius = hasRadiusAttr ? ta.getDimension(R.styleable.AutoTextView_radius, 0f) : 0f;
        leftTopRadius = hasLeftTop ? ta.getDimension(R.styleable.AutoTextView_leftTopRadius, 0f) : radius;
        rightTopRadius = hasRightTop ? ta.getDimension(R.styleable.AutoTextView_rightTopRadius, 0f) : radius;
        rightBottomRadius = hasRightBottom ? ta.getDimension(R.styleable.AutoTextView_rightBottomRadius, 0f) : radius;
        leftBottomRadius = hasLeftBottom ? ta.getDimension(R.styleable.AutoTextView_leftBottomRadius, 0f) : radius;

        int gravityEnum = ta.getInt(R.styleable.AutoTextView_textGravity, 1);
        textGravity = resolveTextGravity(gravityEnum);

        customBackgroundEnabled = hasBgColor || hasRadiusAttr || hasLeftTop || hasRightTop
                || hasRightBottom || hasLeftBottom || hasStrokeWidthAttr || hasStrokeColor;
        ta.recycle();
    }

    /**
     * 将枚举对齐值转换为 Gravity
     *
     * @param gravityEnum 0 左 / 1 中 / 2 右
     * @return Gravity 常量
     */
    private int resolveTextGravity(int gravityEnum) {
        if (gravityEnum == 0) {
            return Gravity.START | Gravity.CENTER_VERTICAL;
        }
        if (gravityEnum == 2) {
            return Gravity.END | Gravity.CENTER_VERTICAL;
        }
        return Gravity.CENTER;
    }

    /**
     * 初始化 Factory、动画与背景
     */
    private void init() {
        setFactory(this);
        createAnimations();
        setInAnimation(inUp);
        setOutAnimation(outUp);
        if (customBackgroundEnabled) {
            applyBackground();
        }
        if (!TextUtils.isEmpty(textStr)) {
            setTextList(Collections.singletonList(textStr));
        }
    }

    /**
     * 创建四套进出翻页动画
     */
    private void createAnimations() {
        inUp = createAnim(-90f, 0f, true, true);
        outUp = createAnim(0f, 90f, false, true);
        inDown = createAnim(90f, 0f, true, false);
        outDown = createAnim(0f, -90f, false, false);
    }

    /**
     * 创建单个 3D 旋转动画
     *
     * @param start  起始角度
     * @param end    结束角度
     * @param turnIn 是否为进入动画
     * @param turnUp 是否向上方向
     * @return 动画实例
     */
    private Rotate3dAnimation createAnim(float start, float end, boolean turnIn, boolean turnUp) {
        Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, turnIn, turnUp);
        rotation.setDuration(animDuration);
        rotation.setFillAfter(false);
        rotation.setInterpolator(new AccelerateInterpolator());
        return rotation;
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
     * 同步更新已创建的子 TextView 样式
     */
    private void applyChildTextStyle() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                tv.setTextColor(textColor);
                LayoutParams lp = (LayoutParams) tv.getLayoutParams();
                if (lp == null) {
                    lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                lp.gravity = textGravity;
                tv.setLayoutParams(lp);
                tv.setGravity(textGravity);
            }
        }
    }

    /**
     * 将当前动画时长同步到四套动画
     */
    private void applyAnimDuration() {
        if (inUp != null) {
            inUp.setDuration(animDuration);
        }
        if (outUp != null) {
            outUp.setDuration(animDuration);
        }
        if (inDown != null) {
            inDown.setDuration(animDuration);
        }
        if (outDown != null) {
            outDown.setDuration(animDuration);
        }
    }

    @Override
    public View makeView() {
        TextView t = new TextView(getContext());
        t.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = textGravity;
        t.setLayoutParams(lp);
        t.setGravity(textGravity);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        t.setTextColor(textColor);
        t.setMaxLines(1);
        t.setSingleLine(true);
        if (!TextUtils.isEmpty(textStr)) {
            t.setText(textStr);
        }
        return t;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (autoFlip && textList.size() > 1) {
            startAutoFlip();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAutoFlip();
        flipHandler.removeCallbacksAndMessages(null);
        clearAnimation();
        if (inUp != null) {
            inUp.cancel();
        }
        if (outUp != null) {
            outUp.cancel();
        }
        if (inDown != null) {
            inDown.cancel();
        }
        if (outDown != null) {
            outDown.cancel();
        }
        super.onDetachedFromWindow();
    }

    /**
     * 切换为向下翻页动画方向（不切换文案）
     */
    public void previous() {
        flipUp = false;
        if (getInAnimation() != inDown) {
            setInAnimation(inDown);
        }
        if (getOutAnimation() != outDown) {
            setOutAnimation(outDown);
        }
    }

    /**
     * 切换为向上翻页动画方向（不切换文案）
     */
    public void next() {
        flipUp = true;
        if (getInAnimation() != inUp) {
            setInAnimation(inUp);
        }
        if (getOutAnimation() != outUp) {
            setOutAnimation(outUp);
        }
    }

    /**
     * 向上翻页并展示指定文案
     *
     * @param text 目标文案
     */
    public void next(@Nullable CharSequence text) {
        next();
        setText(text == null ? "" : text);
    }

    /**
     * 向下翻页并展示指定文案
     *
     * @param text 目标文案
     */
    public void previous(@Nullable CharSequence text) {
        previous();
        setText(text == null ? "" : text);
    }

    /**
     * 按当前方向切换到下一条轮播文案
     */
    public void showNextText() {
        if (textList.isEmpty()) {
            return;
        }
        if (flipUp) {
            next();
            currentIndex = (currentIndex + 1) % textList.size();
        } else {
            previous();
            currentIndex = (currentIndex - 1 + textList.size()) % textList.size();
        }
        textStr = textList.get(currentIndex);
        setText(textStr);
    }

    /**
     * 设置轮播文案列表
     *
     * @param texts 文案集合，可为 null
     * @return this
     */
    public AutoTextView setTextList(@Nullable List<String> texts) {
        textList.clear();
        if (texts != null) {
            for (String item : texts) {
                if (!TextUtils.isEmpty(item)) {
                    textList.add(item);
                }
            }
        }
        currentIndex = 0;
        if (!textList.isEmpty()) {
            textStr = textList.get(0);
            setCurrentText(textStr);
        } else {
            textStr = "";
            setCurrentText("");
        }
        if (autoFlip && isAttachedToWindow() && textList.size() > 1) {
            startAutoFlip();
        } else if (textList.size() <= 1) {
            stopAutoFlip();
        }
        return this;
    }

    /**
     * 设置轮播文案数组
     *
     * @param texts 文案数组
     * @return this
     */
    public AutoTextView setTextList(@Nullable String... texts) {
        if (texts == null || texts.length == 0) {
            return setTextList((List<String>) null);
        }
        return setTextList(Arrays.asList(texts));
    }

    /**
     * 获取当前轮播文案列表（只读副本）
     *
     * @return 文案列表副本
     */
    @NonNull
    public List<String> getTextList() {
        return new ArrayList<>(textList);
    }

    /**
     * 开始自动翻页
     *
     * @return this
     */
    public AutoTextView startAutoFlip() {
        if (textList.size() <= 1) {
            return this;
        }
        flipping = true;
        autoFlip = true;
        flipHandler.removeCallbacks(flipRunnable);
        flipHandler.postDelayed(flipRunnable, flipInterval);
        return this;
    }

    /**
     * 停止自动翻页
     *
     * @return this
     */
    public AutoTextView stopAutoFlip() {
        flipping = false;
        flipHandler.removeCallbacks(flipRunnable);
        return this;
    }

    /**
     * 是否正在自动翻页
     *
     * @return true 表示正在自动翻页
     */
    public boolean isFlipping() {
        return flipping;
    }

    /**
     * 设置翻页动画时长
     *
     * @param durationMs 动画时长，单位毫秒，最小 0
     * @return this
     */
    public AutoTextView setAnimDuration(long durationMs) {
        this.animDuration = Math.max(0L, durationMs);
        applyAnimDuration();
        return this;
    }

    /**
     * 获取翻页动画时长
     *
     * @return 动画时长（毫秒）
     */
    public long getAnimDuration() {
        return animDuration;
    }

    /**
     * 设置自动翻页间隔
     *
     * @param intervalMs 间隔毫秒，建议大于动画时长
     * @return this
     */
    public AutoTextView setFlipInterval(long intervalMs) {
        this.flipInterval = Math.max(animDuration, intervalMs);
        if (flipping) {
            startAutoFlip();
        }
        return this;
    }

    /**
     * 获取自动翻页间隔
     *
     * @return 间隔毫秒
     */
    public long getFlipInterval() {
        return flipInterval;
    }

    /**
     * 设置是否在附着到窗口后自动翻页
     *
     * @param autoFlip true 自动翻页
     * @return this
     */
    public AutoTextView setAutoFlip(boolean autoFlip) {
        this.autoFlip = autoFlip;
        if (autoFlip) {
            if (isAttachedToWindow()) {
                startAutoFlip();
            }
        } else {
            stopAutoFlip();
        }
        return this;
    }

    /**
     * 是否开启自动翻页标记
     *
     * @return true 表示开启
     */
    public boolean isAutoFlip() {
        return autoFlip;
    }

    /**
     * 设置翻页方向
     *
     * @param flipUp true 向上翻页，false 向下翻页
     * @return this
     */
    public AutoTextView setFlipUp(boolean flipUp) {
        this.flipUp = flipUp;
        if (flipUp) {
            next();
        } else {
            previous();
        }
        return this;
    }

    /**
     * 当前是否向上翻页
     *
     * @return true 向上
     */
    public boolean isFlipUp() {
        return flipUp;
    }

    /**
     * 设置文字字号（单位 sp）
     *
     * @param sp 字号
     * @return this
     */
    public AutoTextView setAutoTextSizeSp(float sp) {
        this.textSizePx = DensityUtil.sp2px(getContext(), sp);
        applyChildTextStyle();
        return this;
    }

    /**
     * 设置文字字号（单位 px）
     *
     * @param px 字号像素值
     * @return this
     */
    public AutoTextView setAutoTextSizePx(float px) {
        this.textSizePx = px;
        applyChildTextStyle();
        return this;
    }

    /**
     * 获取文字字号（px）
     *
     * @return 字号像素值
     */
    public float getAutoTextSizePx() {
        return textSizePx;
    }

    /**
     * 设置文字颜色
     *
     * @param color 颜色值
     * @return this
     */
    public AutoTextView setAutoTextColor(@ColorInt int color) {
        this.textColor = color;
        applyChildTextStyle();
        return this;
    }

    /**
     * 获取文字颜色
     *
     * @return 颜色值
     */
    @ColorInt
    public int getAutoTextColor() {
        return textColor;
    }

    /**
     * 设置文字水平对齐
     *
     * @param gravity Gravity 常量，建议带垂直居中
     * @return this
     */
    public AutoTextView setTextGravity(int gravity) {
        this.textGravity = gravity;
        applyChildTextStyle();
        return this;
    }

    /**
     * 获取文字对齐方式
     *
     * @return Gravity
     */
    public int getTextGravity() {
        return textGravity;
    }

    /**
     * 设置统一圆角半径
     *
     * @param radius 圆角半径（px）
     * @return this
     */
    public AutoTextView setRadius(float radius) {
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
     * @param leftTop      左上角
     * @param rightTop     右上角
     * @param rightBottom  右下角
     * @param leftBottom   左下角
     * @return this
     */
    public AutoTextView setCornerRadii(float leftTop, float rightTop, float rightBottom, float leftBottom) {
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
    public AutoTextView setBgColor(@ColorInt int color) {
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
    public AutoTextView setBgColorAndRadius(@ColorInt int color, float radius) {
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
    public AutoTextView setStroke(float strokeWidthPx, @ColorInt int color) {
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
    public float getStrokeWidth() {
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

    /**
     * 获取当前展示文案
     *
     * @return 文案，可能为 null
     */
    @Nullable
    public String getCurrentDisplayText() {
        return textStr;
    }

    /**
     * 获取当前轮播下标
     *
     * @return 下标，从 0 开始
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 静态 3D 旋转动画，避免持有外部类引用造成泄漏
     */
    private static class Rotate3dAnimation extends Animation {
        /** 起始角度 */
        private final float fromDegrees;
        /** 结束角度 */
        private final float toDegrees;
        /** 旋转中心 X */
        private float centerX;
        /** 旋转中心 Y */
        private float centerY;
        /** 是否进入动画 */
        private final boolean turnIn;
        /** 是否向上方向 */
        private final boolean turnUp;
        /** Camera，在 initialize 中创建 */
        private Camera camera;

        /**
         * @param fromDegrees 起始角度
         * @param toDegrees   结束角度
         * @param turnIn      是否进入
         * @param turnUp      是否向上
         */
        Rotate3dAnimation(float fromDegrees, float toDegrees, boolean turnIn, boolean turnUp) {
            this.fromDegrees = fromDegrees;
            this.toDegrees = toDegrees;
            this.turnIn = turnIn;
            this.turnUp = turnUp;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            camera = new Camera();
            centerY = height / 2f;
            centerX = width / 2f;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (camera == null) {
                return;
            }
            float degrees = fromDegrees + ((toDegrees - fromDegrees) * interpolatedTime);
            int direction = turnUp ? 1 : -1;
            Matrix matrix = t.getMatrix();
            camera.save();
            if (turnIn) {
                camera.translate(0.0f, direction * centerY * (interpolatedTime - 1.0f), 0.0f);
            } else {
                camera.translate(0.0f, direction * centerY * interpolatedTime, 0.0f);
            }
            camera.rotateX(degrees);
            camera.getMatrix(matrix);
            camera.restore();
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }
}
