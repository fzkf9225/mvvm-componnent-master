package io.coderf.arklab.common.widget.feedback;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 骨架屏容器：在内容加载前展示占位块，并附带 Shimmer 扫光动画。
 * 可通过 {@link #setRowCount(int)}、{@link #setRowHeight(int)} 配置占位行。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:30
 */
public class SkeletonLayout extends LinearLayout {

    private int rowCount = 4;
    private int rowHeightPx;
    private int rowSpacingPx;
    private int rowRadiusPx;
    @ColorInt
    private int baseColor;
    @ColorInt
    private int highlightColor;
    private boolean shimmerEnabled = true;
    private long shimmerDurationMs = 1200L;

    private ValueAnimator shimmerAnimator;
    private float shimmerTranslate;
    private final Paint shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix shimmerMatrix = new Matrix();

    public SkeletonLayout(@NonNull Context context) {
        this(context, null);
    }

    public SkeletonLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkeletonLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        rowHeightPx = DensityUtil.dp2px(context, 16f);
        rowSpacingPx = DensityUtil.dp2px(context, 12f);
        rowRadiusPx = DensityUtil.dp2px(context, 4f);
        baseColor = ContextCompat.getColor(context, R.color.default_background);
        highlightColor = Color.parseColor("#ECECEC");

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SkeletonLayout);
            rowCount = ta.getInt(R.styleable.SkeletonLayout_skeletonRowCount, rowCount);
            rowHeightPx = (int) ta.getDimension(R.styleable.SkeletonLayout_skeletonRowHeight, rowHeightPx);
            rowSpacingPx = (int) ta.getDimension(R.styleable.SkeletonLayout_skeletonRowSpacing, rowSpacingPx);
            rowRadiusPx = (int) ta.getDimension(R.styleable.SkeletonLayout_skeletonRowRadius, rowRadiusPx);
            baseColor = ta.getColor(R.styleable.SkeletonLayout_skeletonBaseColor, baseColor);
            highlightColor = ta.getColor(R.styleable.SkeletonLayout_skeletonHighlightColor, highlightColor);
            shimmerEnabled = ta.getBoolean(R.styleable.SkeletonLayout_skeletonShimmerEnabled, shimmerEnabled);
            shimmerDurationMs = ta.getInt(R.styleable.SkeletonLayout_skeletonShimmerDuration, (int) shimmerDurationMs);
            ta.recycle();
        }

        setWillNotDraw(false);
        rebuildRows();
    }

    /**
     * 显示骨架屏并开始动画。
     */
    public void showSkeleton() {
        setVisibility(VISIBLE);
        startShimmer();
    }

    /**
     * 隐藏骨架屏并停止动画。
     */
    public void hideSkeleton() {
        stopShimmer();
        setVisibility(GONE);
    }

    public SkeletonLayout setRowCount(int rowCount) {
        this.rowCount = Math.max(1, rowCount);
        rebuildRows();
        return this;
    }

    public SkeletonLayout setRowHeight(int rowHeightPx) {
        this.rowHeightPx = rowHeightPx;
        rebuildRows();
        return this;
    }

    public SkeletonLayout setShimmerEnabled(boolean shimmerEnabled) {
        this.shimmerEnabled = shimmerEnabled;
        if (!shimmerEnabled) {
            stopShimmer();
        } else if (getVisibility() == VISIBLE) {
            startShimmer();
        }
        return this;
    }

    private void rebuildRows() {
        removeAllViews();
        for (int i = 0; i < rowCount; i++) {
            View row = new SkeletonRowView(getContext(), baseColor, rowRadiusPx);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    rowHeightPx);
            if (i > 0) {
                lp.topMargin = rowSpacingPx;
            }
            addView(row, lp);
        }
        requestLayout();
    }

    private void startShimmer() {
        if (!shimmerEnabled) {
            return;
        }
        stopShimmer();
        shimmerAnimator = ValueAnimator.ofFloat(-1f, 2f);
        shimmerAnimator.setDuration(shimmerDurationMs);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        shimmerAnimator.addUpdateListener(animation -> {
            shimmerTranslate = (float) animation.getAnimatedValue();
            invalidate();
        });
        shimmerAnimator.start();
    }

    private void stopShimmer() {
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!shimmerEnabled || shimmerAnimator == null || getWidth() <= 0) {
            return;
        }
        LinearGradient gradient = new LinearGradient(
                0, 0, getWidth(), 0,
                new int[]{Color.TRANSPARENT, highlightColor, Color.TRANSPARENT},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP);
        shimmerMatrix.setTranslate(getWidth() * shimmerTranslate, 0);
        gradient.setLocalMatrix(shimmerMatrix);
        shimmerPaint.setShader(gradient);
        canvas.drawRect(0, 0, getWidth(), getHeight(), shimmerPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE && shimmerEnabled) {
            startShimmer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopShimmer();
        super.onDetachedFromWindow();
    }

    private static class SkeletonRowView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final float radius;

        SkeletonRowView(Context context, @ColorInt int color, float radiusPx) {
            super(context);
            this.radius = radiusPx;
            paint.setColor(color);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, radius, radius, paint);
        }
    }
}
