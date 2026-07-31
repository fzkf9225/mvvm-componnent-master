package io.coderf.arklab.media.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 裁剪遮罩层：半透明蒙版 + 圆形/矩形裁剪框描边。
 * 本身不拦截触摸；由 {@link GestureCropImageView} 长按边缘后调用缩放 API。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public class CropOverlayView extends View {

    /**
     * 触控热点：无
     */
    public static final int TOUCH_NONE = 0;

    /**
     * 触控热点：左上角
     */
    public static final int TOUCH_LEFT_TOP = 1;

    /**
     * 触控热点：右上角
     */
    public static final int TOUCH_RIGHT_TOP = 2;

    /**
     * 触控热点：左下角
     */
    public static final int TOUCH_LEFT_BOTTOM = 3;

    /**
     * 触控热点：右下角
     */
    public static final int TOUCH_RIGHT_BOTTOM = 4;

    /**
     * 触控热点：左边（自由裁剪）
     */
    public static final int TOUCH_LEFT = 5;

    /**
     * 触控热点：右边（自由裁剪）
     */
    public static final int TOUCH_RIGHT = 6;

    /**
     * 触控热点：上边（自由裁剪）
     */
    public static final int TOUCH_TOP = 7;

    /**
     * 触控热点：下边（自由裁剪）
     */
    public static final int TOUCH_BOTTOM = 8;

    /**
     * 裁剪框形状
     */
    private CropShapeEnum cropShape = CropShapeEnum.SQUARE;

    /**
     * 宽高比-宽
     */
    private int aspectX = 1;

    /**
     * 宽高比-高
     */
    private int aspectY = 1;

    /**
     * 是否自由裁剪（不锁定宽高比）
     */
    private boolean freeCrop = false;

    /**
     * 初始裁剪框相对可用区域比例（0~1），默认 0.7
     */
    private float initialCropScale = 0.7f;

    /**
     * 裁剪框相对屏幕边缘的边距（px）
     */
    private float cropMargin;

    /**
     * 边缘命中厚度（px），长按判定用
     */
    private float edgeHitThickness;

    /**
     * 裁剪框最小边长（px）
     */
    private float minCropSize;

    /**
     * 是否允许缩放裁剪框，默认 true
     */
    private boolean cropFrameScalable = true;

    /**
     * 当前是否处于缩放中（长按激活后）
     */
    private boolean resizing;

    /**
     * 当前缩放角点模式
     */
    private int resizeMode = TOUCH_NONE;

    /**
     * 当前裁剪框区域（View 坐标）
     */
    private final RectF cropRect = new RectF();

    /**
     * 半透明蒙版画笔
     */
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 裁剪框描边画笔
     */
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 网格辅助线画笔
     */
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 缩放激活时加粗描边画笔
     */
    private final Paint activeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 挖空路径
     */
    private final Path clipPath = new Path();

    /**
     * 裁剪框变化监听
     */
    @Nullable
    private OnCropRectChangeListener onCropRectChangeListener;

    /**
     * 裁剪框变化回调
     */
    public interface OnCropRectChangeListener {
        /**
         * 裁剪框发生变化
         *
         * @param cropRect 最新裁剪框
         */
        void onCropRectChanged(@NonNull RectF cropRect);
    }

    /**
     * 构造
     *
     * @param context 上下文
     */
    public CropOverlayView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 构造
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 构造
     *
     * @param context      上下文
     * @param attrs        属性
     * @param defStyleAttr 默认样式
     */
    public CropOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化画笔与尺寸常量
     *
     * @param context 上下文
     */
    private void init(Context context) {
        setClickable(false);
        setFocusable(false);
        cropMargin = dp(context, ImageCropBuilder.DEFAULT_CROP_EDGE_INSET_DP);
        initialCropScale = ImageCropBuilder.DEFAULT_INITIAL_CROP_SCALE;
        edgeHitThickness = dp(context, 28f);
        minCropSize = dp(context, 80f);
        dimPaint.setColor(Color.parseColor("#99000000"));
        dimPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(context, 2f));
        activeBorderPaint.setColor(Color.WHITE);
        activeBorderPaint.setStyle(Paint.Style.STROKE);
        activeBorderPaint.setStrokeWidth(dp(context, 3.5f));
        gridPaint.setColor(Color.parseColor("#66FFFFFF"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(context, 0.8f));
        setWillNotDraw(false);
    }

    /**
     * dp 转 px
     *
     * @param context 上下文
     * @param dp      dp 值
     * @return px
     */
    private static float dp(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 设置裁剪形状、宽高比与是否自由裁剪
     *
     * @param cropShape 形状
     * @param aspectX   宽比例
     * @param aspectY   高比例
     * @param freeCrop  是否自由裁剪（不锁比例，仅长方形有效）
     */
    public void setCropConfig(@NonNull CropShapeEnum cropShape, int aspectX, int aspectY, boolean freeCrop) {
        this.cropShape = cropShape;
        this.aspectX = Math.max(1, aspectX);
        this.aspectY = Math.max(1, aspectY);
        this.freeCrop = freeCrop && cropShape == CropShapeEnum.RECTANGLE;
        if (getWidth() > 0 && getHeight() > 0) {
            recalculateCropRect(getWidth(), getHeight());
            notifyCropRectChanged();
        }
        invalidate();
    }

    /**
     * 设置裁剪框距屏幕边缘的最小间距（px）
     *
     * @param cropEdgeInsetPx 间距像素
     */
    public void setCropEdgeInsetPx(float cropEdgeInsetPx) {
        this.cropMargin = Math.max(0f, cropEdgeInsetPx);
        if (getWidth() > 0 && getHeight() > 0) {
            recalculateCropRect(getWidth(), getHeight());
            notifyCropRectChanged();
        }
        invalidate();
    }

    /**
     * 设置初始裁剪框相对可用区域比例（0~1）
     *
     * @param initialCropScale 比例
     */
    public void setInitialCropScale(float initialCropScale) {
        if (initialCropScale <= 0f) {
            this.initialCropScale = ImageCropBuilder.DEFAULT_INITIAL_CROP_SCALE;
        } else {
            this.initialCropScale = Math.min(1f, initialCropScale);
        }
        if (getWidth() > 0 && getHeight() > 0) {
            recalculateCropRect(getWidth(), getHeight());
            notifyCropRectChanged();
        }
        invalidate();
    }

    /**
     * 获取裁剪框距边缘间距（px）
     *
     * @return 间距
     */
    public float getCropEdgeInsetPx() {
        return cropMargin;
    }

    /**
     * 设置裁剪形状与宽高比（非自由裁剪）
     *
     * @param cropShape 形状
     * @param aspectX   宽比例
     * @param aspectY   高比例
     */
    public void setCropConfig(@NonNull CropShapeEnum cropShape, int aspectX, int aspectY) {
        setCropConfig(cropShape, aspectX, aspectY, false);
    }

    /**
     * 是否自由裁剪
     *
     * @return true 不锁定宽高比
     */
    public boolean isFreeCrop() {
        return freeCrop;
    }

    /**
     * 设置是否允许用户缩放裁剪框
     *
     * @param cropFrameScalable true 允许
     */
    public void setCropFrameScalable(boolean cropFrameScalable) {
        this.cropFrameScalable = cropFrameScalable;
        if (!cropFrameScalable) {
            endResize();
        }
        invalidate();
    }

    /**
     * 是否允许缩放裁剪框
     *
     * @return true 允许
     */
    public boolean isCropFrameScalable() {
        return cropFrameScalable;
    }

    /**
     * 设置裁剪框描边颜色
     *
     * @param borderColor 颜色
     */
    public void setBorderColor(@ColorInt int borderColor) {
        borderPaint.setColor(borderColor);
        activeBorderPaint.setColor(borderColor);
        int alpha = Color.alpha(borderColor);
        int grid = Color.argb(Math.max(60, alpha / 2),
                Color.red(borderColor), Color.green(borderColor), Color.blue(borderColor));
        gridPaint.setColor(grid);
        invalidate();
    }

    /**
     * 设置九宫格辅助线颜色
     *
     * @param gridColor 颜色
     */
    public void setGridColor(@ColorInt int gridColor) {
        gridPaint.setColor(gridColor);
        invalidate();
    }

    /**
     * 设置蒙版颜色
     *
     * @param dimColor 颜色，建议带透明度
     */
    public void setDimColor(@ColorInt int dimColor) {
        dimPaint.setColor(dimColor);
        invalidate();
    }

    /**
     * 设置裁剪框变化监听
     *
     * @param listener 监听
     */
    public void setOnCropRectChangeListener(@Nullable OnCropRectChangeListener listener) {
        this.onCropRectChangeListener = listener;
    }

    /**
     * 获取当前裁剪框（View 坐标系）
     *
     * @return 裁剪矩形
     */
    @NonNull
    public RectF getCropRect() {
        return cropRect;
    }

    /**
     * 裁剪框是否已就绪（非空且控件已布局）
     *
     * @return true 已就绪
     */
    public boolean isCropRectReady() {
        return getWidth() > 0 && getHeight() > 0 && !cropRect.isEmpty();
    }

    /**
     * 获取裁剪形状
     *
     * @return CropShapeEnum
     */
    @NonNull
    public CropShapeEnum getCropShape() {
        return cropShape;
    }

    /**
     * 是否正在缩放裁剪框
     *
     * @return true 缩放中
     */
    public boolean isResizing() {
        return resizing;
    }

    /**
     * 判断坐标是否落在裁剪框边缘（用于长按触发缩放）
     *
     * @param x View 坐标 X
     * @param y View 坐标 Y
     * @return true 在边缘带内
     */
    public boolean isOnCropEdge(float x, float y) {
        return cropFrameScalable && hitEdgeMode(x, y) != TOUCH_NONE;
    }

    /**
     * 命中裁剪框边缘时，返回对应缩放模式
     *
     * @param x View 坐标 X
     * @param y View 坐标 Y
     * @return 模式，未命中返回 {@link #TOUCH_NONE}
     */
    public int hitEdgeMode(float x, float y) {
        if (!cropFrameScalable || cropRect.isEmpty()) {
            return TOUCH_NONE;
        }
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;
        boolean nearLeft = Math.abs(x - left) <= edgeHitThickness && y >= top - edgeHitThickness && y <= bottom + edgeHitThickness;
        boolean nearRight = Math.abs(x - right) <= edgeHitThickness && y >= top - edgeHitThickness && y <= bottom + edgeHitThickness;
        boolean nearTop = Math.abs(y - top) <= edgeHitThickness && x >= left - edgeHitThickness && x <= right + edgeHitThickness;
        boolean nearBottom = Math.abs(y - bottom) <= edgeHitThickness && x >= left - edgeHitThickness && x <= right + edgeHitThickness;
        if (!nearLeft && !nearRight && !nearTop && !nearBottom) {
            return TOUCH_NONE;
        }
        // 角点优先
        if (nearLeft && nearTop) {
            return TOUCH_LEFT_TOP;
        }
        if (nearRight && nearTop) {
            return TOUCH_RIGHT_TOP;
        }
        if (nearLeft && nearBottom) {
            return TOUCH_LEFT_BOTTOM;
        }
        if (nearRight && nearBottom) {
            return TOUCH_RIGHT_BOTTOM;
        }
        // 自由裁剪：单边缩放；锁定比例：映射到最近角点
        if (freeCrop) {
            if (nearLeft) {
                return TOUCH_LEFT;
            }
            if (nearRight) {
                return TOUCH_RIGHT;
            }
            if (nearTop) {
                return TOUCH_TOP;
            }
            return TOUCH_BOTTOM;
        }
        boolean preferLeft = Math.abs(x - left) <= Math.abs(x - right);
        boolean preferTop = Math.abs(y - top) <= Math.abs(y - bottom);
        if (preferLeft && preferTop) {
            return TOUCH_LEFT_TOP;
        }
        if (!preferLeft && preferTop) {
            return TOUCH_RIGHT_TOP;
        }
        if (preferLeft) {
            return TOUCH_LEFT_BOTTOM;
        }
        return TOUCH_RIGHT_BOTTOM;
    }

    /**
     * 开始边缘缩放（长按边缘成功后调用）
     *
     * @param mode 角点模式
     * @return true 启动成功
     */
    public boolean beginResize(int mode) {
        if (!cropFrameScalable || mode == TOUCH_NONE || cropRect.isEmpty()) {
            return false;
        }
        resizing = true;
        resizeMode = mode;
        invalidate();
        return true;
    }

    /**
     * 按位移缩放裁剪框
     *
     * @param dx X 偏移
     * @param dy Y 偏移
     */
    public void applyResizeDelta(float dx, float dy) {
        if (!resizing || resizeMode == TOUCH_NONE) {
            return;
        }
        if (freeCrop) {
            resizeCropRectFree(resizeMode, dx, dy);
        } else {
            resizeCropRectLocked(resizeMode, dx, dy);
        }
        invalidate();
        notifyCropRectChanged();
    }

    /**
     * 结束缩放
     */
    public void endResize() {
        resizing = false;
        resizeMode = TOUCH_NONE;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateCropRect(w, h);
        notifyCropRectChanged();
    }

    /**
     * 计算初始裁剪框：不铺满，按 initialCropScale 居中；最大不超过边缘间距内的可用区域
     *
     * @param viewWidth  控件宽
     * @param viewHeight 控件高
     */
    private void recalculateCropRect(int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }
        float availableW = viewWidth - cropMargin * 2f;
        float availableH = viewHeight - cropMargin * 2f;
        if (availableW <= 0 || availableH <= 0) {
            return;
        }
        float scale = initialCropScale <= 0f ? ImageCropBuilder.DEFAULT_INITIAL_CROP_SCALE : Math.min(1f, initialCropScale);
        float cropW;
        float cropH;
        if (freeCrop) {
            cropW = availableW * scale;
            cropH = availableH * scale;
        } else {
            float ratio = aspectX * 1f / aspectY;
            float maxW = availableW * scale;
            float maxH = availableH * scale;
            if (maxW / maxH > ratio) {
                cropH = maxH;
                cropW = cropH * ratio;
            } else {
                cropW = maxW;
                cropH = cropW / ratio;
            }
        }
        cropW = Math.max(minCropSize, Math.min(cropW, availableW));
        cropH = Math.max(minCropSize, Math.min(cropH, availableH));
        float left = (viewWidth - cropW) / 2f;
        float top = (viewHeight - cropH) / 2f;
        // 保证不越过边缘间距
        left = Math.max(cropMargin, Math.min(left, viewWidth - cropMargin - cropW));
        top = Math.max(cropMargin, Math.min(top, viewHeight - cropMargin - cropH));
        cropRect.set(left, top, left + cropW, top + cropH);
    }

    /**
     * 通知裁剪框变化
     */
    private void notifyCropRectChanged() {
        if (onCropRectChangeListener != null && !cropRect.isEmpty()) {
            onCropRectChangeListener.onCropRectChanged(new RectF(cropRect));
        }
    }

    /**
     * 自由裁剪：单边/角点独立缩放，不锁定宽高比
     *
     * @param mode 触控模式
     * @param dx   X 偏移
     * @param dy   Y 偏移
     */
    private void resizeCropRectFree(int mode, float dx, float dy) {
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;
        float maxRight = getWidth() - cropMargin;
        float maxBottom = getHeight() - cropMargin;
        switch (mode) {
            case TOUCH_LEFT:
            case TOUCH_LEFT_TOP:
            case TOUCH_LEFT_BOTTOM:
                left = Math.min(right - minCropSize, Math.max(cropMargin, left + dx));
                break;
            case TOUCH_RIGHT:
            case TOUCH_RIGHT_TOP:
            case TOUCH_RIGHT_BOTTOM:
                right = Math.max(left + minCropSize, Math.min(maxRight, right + dx));
                break;
            default:
                break;
        }
        switch (mode) {
            case TOUCH_TOP:
            case TOUCH_LEFT_TOP:
            case TOUCH_RIGHT_TOP:
                top = Math.min(bottom - minCropSize, Math.max(cropMargin, top + dy));
                break;
            case TOUCH_BOTTOM:
            case TOUCH_LEFT_BOTTOM:
            case TOUCH_RIGHT_BOTTOM:
                bottom = Math.max(top + minCropSize, Math.min(maxBottom, bottom + dy));
                break;
            default:
                break;
        }
        // 纯左右边不改高度；纯上下边不改宽度（上面 switch 已处理）
        cropRect.set(left, top, right, bottom);
    }

    /**
     * 锁定宽高比缩放裁剪框
     *
     * @param mode 角点模式
     * @param dx   X 偏移
     * @param dy   Y 偏移
     */
    private void resizeCropRectLocked(int mode, float dx, float dy) {
        float ratio = aspectX * 1f / aspectY;
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;
        float minW = minCropSize;
        float minH = minCropSize / ratio;
        if (minH < minCropSize) {
            minH = minCropSize;
            minW = minH * ratio;
        }
        float maxRight = getWidth() - cropMargin;
        float maxBottom = getHeight() - cropMargin;

        switch (mode) {
            case TOUCH_LEFT_TOP: {
                float widthByX = right - (left + dx);
                float heightByY = bottom - (top + dy);
                float newW = Math.min(widthByX, heightByY * ratio);
                newW = Math.max(minW, newW);
                float newH = newW / ratio;
                float newLeft = right - newW;
                float newTop = bottom - newH;
                if (newLeft < cropMargin) {
                    newLeft = cropMargin;
                    newW = right - newLeft;
                    newH = newW / ratio;
                    newTop = bottom - newH;
                }
                if (newTop < cropMargin) {
                    newTop = cropMargin;
                    newH = bottom - newTop;
                    newW = newH * ratio;
                    newLeft = right - newW;
                }
                left = newLeft;
                top = newTop;
                break;
            }
            case TOUCH_RIGHT_TOP: {
                float widthByX = (right + dx) - left;
                float heightByY = bottom - (top + dy);
                float newW = Math.min(widthByX, heightByY * ratio);
                newW = Math.max(minW, newW);
                float newH = newW / ratio;
                float newRight = left + newW;
                float newTop = bottom - newH;
                if (newRight > maxRight) {
                    newRight = maxRight;
                    newW = newRight - left;
                    newH = newW / ratio;
                    newTop = bottom - newH;
                }
                if (newTop < cropMargin) {
                    newTop = cropMargin;
                    newH = bottom - newTop;
                    newW = newH * ratio;
                    newRight = left + newW;
                }
                right = newRight;
                top = newTop;
                break;
            }
            case TOUCH_LEFT_BOTTOM: {
                float widthByX = right - (left + dx);
                float heightByY = (bottom + dy) - top;
                float newW = Math.min(widthByX, heightByY * ratio);
                newW = Math.max(minW, newW);
                float newH = newW / ratio;
                float newLeft = right - newW;
                float newBottom = top + newH;
                if (newLeft < cropMargin) {
                    newLeft = cropMargin;
                    newW = right - newLeft;
                    newH = newW / ratio;
                    newBottom = top + newH;
                }
                if (newBottom > maxBottom) {
                    newBottom = maxBottom;
                    newH = newBottom - top;
                    newW = newH * ratio;
                    newLeft = right - newW;
                }
                left = newLeft;
                bottom = newBottom;
                break;
            }
            case TOUCH_RIGHT_BOTTOM:
            default: {
                float widthByX = (right + dx) - left;
                float heightByY = (bottom + dy) - top;
                float newW = Math.min(widthByX, heightByY * ratio);
                newW = Math.max(minW, newW);
                float newH = newW / ratio;
                float newRight = left + newW;
                float newBottom = top + newH;
                if (newRight > maxRight) {
                    newRight = maxRight;
                    newW = newRight - left;
                    newH = newW / ratio;
                    newBottom = top + newH;
                }
                if (newBottom > maxBottom) {
                    newBottom = maxBottom;
                    newH = newBottom - top;
                    newW = newH * ratio;
                    newRight = left + newW;
                }
                right = newRight;
                bottom = newBottom;
                break;
            }
        }
        cropRect.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (cropRect.isEmpty()) {
            return;
        }
        int save = canvas.save();
        clipPath.reset();
        if (cropShape == CropShapeEnum.CIRCLE) {
            float cx = cropRect.centerX();
            float cy = cropRect.centerY();
            float radius = Math.min(cropRect.width(), cropRect.height()) / 2f;
            clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
        } else {
            clipPath.addRect(cropRect, Path.Direction.CW);
        }
        canvas.clipOutPath(clipPath);
        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);
        canvas.restoreToCount(save);

        Paint stroke = resizing ? activeBorderPaint : borderPaint;
        if (cropShape == CropShapeEnum.CIRCLE) {
            float cx = cropRect.centerX();
            float cy = cropRect.centerY();
            float radius = Math.min(cropRect.width(), cropRect.height()) / 2f;
            canvas.drawCircle(cx, cy, radius, stroke);
        } else {
            canvas.drawRect(cropRect, stroke);
            float thirdW = cropRect.width() / 3f;
            float thirdH = cropRect.height() / 3f;
            canvas.drawLine(cropRect.left + thirdW, cropRect.top,
                    cropRect.left + thirdW, cropRect.bottom, gridPaint);
            canvas.drawLine(cropRect.left + thirdW * 2, cropRect.top,
                    cropRect.left + thirdW * 2, cropRect.bottom, gridPaint);
            canvas.drawLine(cropRect.left, cropRect.top + thirdH,
                    cropRect.right, cropRect.top + thirdH, gridPaint);
            canvas.drawLine(cropRect.left, cropRect.top + thirdH * 2,
                    cropRect.right, cropRect.top + thirdH * 2, gridPaint);
        }
    }
}
