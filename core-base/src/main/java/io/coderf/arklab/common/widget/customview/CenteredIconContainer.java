package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;

import com.google.android.material.imageview.ShapeableImageView;

import io.coderf.arklab.common.R;

/**
 * 居中图标容器：外层继承 {@link CornerConstraintLayout}（圆角 / 背景 / 描边），
 * 内部自动居中放置 {@link ShapeableImageView}。
 * <p>
 * 图标尺寸支持两种方式（与历史 Guideline 0.33~0.67 对齐，默认百分比 0.34）：
 * <ul>
 *   <li>百分比：相对父容器，属性 {@code app:iconSizePercent}（内部用 Guideline 实现，与旧 XML 一致）</li>
 *   <li>固定宽高：{@code app:iconWidth} / {@code app:iconHeight}，任一大于 0 时优先于百分比</li>
 * </ul>
 * 图标资源：{@code app:iconSrc}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/22
 * @updated 2026/10/22 用 Guideline 百分比约束修复图标铺满问题
 */
public class CenteredIconContainer extends CornerConstraintLayout {

    /**
     * 与历史 layout Guideline percent 0.33~0.67 对齐（中间约占 34%）
     */
    private static final float DEFAULT_ICON_SIZE_PERCENT = 0.34f;

    /**
     * 中间图标
     */
    protected ShapeableImageView iconView;

    private Guideline guideLeft;
    private Guideline guideRight;
    private Guideline guideTop;
    private Guideline guideBottom;

    /**
     * 图标相对父容器的比例，默认 {@link #DEFAULT_ICON_SIZE_PERCENT}
     */
    protected float iconSizePercent = DEFAULT_ICON_SIZE_PERCENT;

    /**
     * 固定宽度（px），&gt; 0 时与 {@link #iconHeight} 一起启用固定尺寸
     */
    protected int iconWidth;

    /**
     * 固定高度（px），&gt; 0 时与 {@link #iconWidth} 一起启用固定尺寸
     */
    protected int iconHeight;

    /**
     * 是否使用固定宽高（由 iconWidth / iconHeight 推导）
     */
    protected boolean useFixedSize;

    public CenteredIconContainer(@NonNull Context context) {
        super(context);
        initIcon(context, null);
    }

    public CenteredIconContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initIcon(context, attrs);
    }

    public CenteredIconContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initIcon(context, attrs);
    }

    private void initIcon(@NonNull Context context, @Nullable AttributeSet attrs) {
        int srcRes = 0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CenteredIconContainer);
            srcRes = a.getResourceId(R.styleable.CenteredIconContainer_iconSrc, 0);
            iconSizePercent = a.getFloat(R.styleable.CenteredIconContainer_iconSizePercent,
                    DEFAULT_ICON_SIZE_PERCENT);
            iconWidth = a.getDimensionPixelSize(R.styleable.CenteredIconContainer_iconWidth, 0);
            iconHeight = a.getDimensionPixelSize(R.styleable.CenteredIconContainer_iconHeight, 0);
            a.recycle();
        }
        useFixedSize = iconWidth > 0 || iconHeight > 0;

        iconView = new ShapeableImageView(context);
        iconView.setId(R.id.centered_icon_view);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (srcRes != 0) {
            iconView.setImageResource(srcRes);
        }

        if (useFixedSize) {
            setupFixedSizeIcon();
        } else {
            setupPercentIcon();
        }
    }

    /**
     * 百分比模式：四条 Guideline 夹住图标，与旧 XML 0.33/0.67 写法一致
     */
    private void setupPercentIcon() {
        float percent = clampPercent(iconSizePercent);
        float start = (1f - percent) / 2f;
        float end = (1f + percent) / 2f;

        guideLeft = createGuideline(VERTICAL_GUIDELINE, start);
        guideRight = createGuideline(VERTICAL_GUIDELINE, end);
        guideTop = createGuideline(HORIZONTAL_GUIDELINE, start);
        guideBottom = createGuideline(HORIZONTAL_GUIDELINE, end);

        addView(guideLeft);
        addView(guideRight);
        addView(guideTop);
        addView(guideBottom);

        LayoutParams lp = new LayoutParams(0, 0);
        lp.startToStart = guideLeft.getId();
        lp.endToEnd = guideRight.getId();
        lp.topToTop = guideTop.getId();
        lp.bottomToBottom = guideBottom.getId();
        addView(iconView, lp);
    }

    /**
     * 固定尺寸模式：明确宽高 + 四边贴父容器并 bias 居中
     */
    private void setupFixedSizeIcon() {
        int w = iconWidth > 0 ? iconWidth : iconHeight;
        int h = iconHeight > 0 ? iconHeight : iconWidth;
        LayoutParams lp = new LayoutParams(w, h);
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.startToStart = LayoutParams.PARENT_ID;
        lp.endToEnd = LayoutParams.PARENT_ID;
        lp.horizontalBias = 0.5f;
        lp.verticalBias = 0.5f;
        addView(iconView, lp);
    }

    private static final int VERTICAL_GUIDELINE = 0;
    private static final int HORIZONTAL_GUIDELINE = 1;

    @NonNull
    private Guideline createGuideline(int orientation, float guidePercent) {
        Guideline guideline = new Guideline(getContext());
        guideline.setId(generateViewId());
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (orientation == VERTICAL_GUIDELINE) {
            lp.orientation = LayoutParams.VERTICAL;
        } else {
            lp.orientation = LayoutParams.HORIZONTAL;
        }
        lp.guidePercent = guidePercent;
        guideline.setLayoutParams(lp);
        return guideline;
    }

    /**
     * 更新百分比 Guideline 位置（运行时改 percent 时用）
     */
    private void updateGuidelinePercents() {
        if (guideLeft == null) {
            return;
        }
        float percent = clampPercent(iconSizePercent);
        float start = (1f - percent) / 2f;
        float end = (1f + percent) / 2f;
        setGuidelinePercent(guideLeft, start);
        setGuidelinePercent(guideRight, end);
        setGuidelinePercent(guideTop, start);
        setGuidelinePercent(guideBottom, end);
    }

    private static void setGuidelinePercent(@NonNull Guideline guideline, float percent) {
        LayoutParams lp = (LayoutParams) guideline.getLayoutParams();
        if (lp != null) {
            lp.guidePercent = percent;
            guideline.setLayoutParams(lp);
        }
    }

    private static float clampPercent(float percent) {
        if (percent <= 0f) {
            return DEFAULT_ICON_SIZE_PERCENT;
        }
        if (percent > 1f) {
            return 1f;
        }
        return percent;
    }

    /**
     * 设置图标资源
     */
    public void setIconResource(@DrawableRes int resId) {
        if (iconView != null) {
            iconView.setImageResource(resId);
        }
    }

    /**
     * 设置图标百分比尺寸（相对父容器），并切换到百分比模式
     *
     * @param percent 0~1，超出范围会夹紧
     */
    public void setIconSizePercent(float percent) {
        this.iconSizePercent = percent;
        this.useFixedSize = false;
        if (guideLeft != null) {
            updateGuidelinePercents();
        } else {
            // 之前是固定尺寸，需要重建子 View 结构
            removeAllViews();
            guideLeft = guideRight = guideTop = guideBottom = null;
            setupPercentIcon();
        }
    }

    /**
     * 设置图标固定宽高（像素），并切换到固定尺寸模式
     */
    public void setIconSize(int widthPx, int heightPx) {
        this.iconWidth = widthPx;
        this.iconHeight = heightPx;
        this.useFixedSize = widthPx > 0 || heightPx > 0;
        if (useFixedSize && guideLeft == null && iconView.getParent() == this) {
            int w = iconWidth > 0 ? iconWidth : iconHeight;
            int h = iconHeight > 0 ? iconHeight : iconWidth;
            LayoutParams lp = (LayoutParams) iconView.getLayoutParams();
            if (lp != null) {
                lp.width = w;
                lp.height = h;
                lp.leftToLeft = LayoutParams.UNSET;
                lp.rightToRight = LayoutParams.UNSET;
                lp.topToTop = LayoutParams.PARENT_ID;
                lp.bottomToBottom = LayoutParams.PARENT_ID;
                lp.startToStart = LayoutParams.PARENT_ID;
                lp.endToEnd = LayoutParams.PARENT_ID;
                lp.horizontalBias = 0.5f;
                lp.verticalBias = 0.5f;
                iconView.setLayoutParams(lp);
            }
        } else if (useFixedSize) {
            removeAllViews();
            guideLeft = guideRight = guideTop = guideBottom = null;
            setupFixedSizeIcon();
        }
    }

    /**
     * 当前图标百分比（固定尺寸模式下仍保留上次百分比值）
     */
    public float getIconSizePercent() {
        return iconSizePercent;
    }

    /**
     * 内部图标 View，便于个别场景微调（scaleType、tint 等）
     */
    @NonNull
    public ShapeableImageView getIconView() {
        return iconView;
    }
}
