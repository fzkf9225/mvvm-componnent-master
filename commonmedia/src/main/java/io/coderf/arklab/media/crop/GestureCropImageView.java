package io.coderf.arklab.media.crop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 支持双指缩放、单指拖动的裁剪预览 ImageView。
 * 图片拖动与裁剪框缩放互不抢占：默认拖图/双指缩放；
 * 长按裁剪框边缘后进入裁剪框缩放模式。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public class GestureCropImageView extends AppCompatImageView {

    /**
     * 图片变换矩阵
     */
    private final Matrix imageMatrixInternal = new Matrix();

    /**
     * 源图矩阵值缓存
     */
    private final float[] matrixValues = new float[9];

    /**
     * 双指缩放检测
     */
    private ScaleGestureDetector scaleGestureDetector;

    /**
     * 长按检测（用于激活裁剪框边缘缩放）
     */
    private GestureDetector gestureDetector;

    /**
     * 关联的裁剪遮罩
     */
    @Nullable
    private CropOverlayView cropOverlayView;

    /**
     * 上一次触摸 X
     */
    private float lastX;

    /**
     * 上一次触摸 Y
     */
    private float lastY;

    /**
     * 是否正在拖动图片
     */
    private boolean isDragging;

    /**
     * DOWN 时的坐标，用于长按后切换模式
     */
    private float downX;

    /**
     * DOWN 时的坐标
     */
    private float downY;

    /**
     * 手指按下后是否发生过明显移动（移动则取消长按缩放意图）
     */
    private boolean movedBeforeLongPress;

    /**
     * 系统触摸滑动阈值
     */
    private int touchSlop;

    /**
     * 最小缩放倍数
     */
    private float minScale = 1f;

    /**
     * 最大缩放倍数
     */
    private float maxScale = 5f;

    /**
     * 初始适配缩放值
     */
    private float baseScale = 1f;

    /**
     * 是否已完成首次居中适配，避免重复 reset 造成偏移
     */
    private boolean hasFittedOnce;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public GestureCropImageView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 构造
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public GestureCropImageView(Context context, @Nullable AttributeSet attrs) {
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
    public GestureCropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化手势
     *
     * @param context 上下文
     */
    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        setClickable(true);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                tryEnterCropResize(e.getX(), e.getY());
            }
        });
    }

    /**
     * 绑定裁剪遮罩
     *
     * @param cropOverlayView 遮罩层
     */
    public void setCropOverlayView(@Nullable CropOverlayView cropOverlayView) {
        this.cropOverlayView = cropOverlayView;
        hasFittedOnce = false;
    }

    /**
     * 设置源 Bitmap，布局就绪后居中适配
     *
     * @param bm 源图
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        hasFittedOnce = false;
        scheduleFitWhenReady();
    }

    /**
     * 布局与裁剪框都就绪后再做一次居中适配
     */
    public void scheduleFitWhenReady() {
        post(() -> {
            if (resetToFitCrop()) {
                return;
            }
            // 裁剪框可能尚未量完，再等一帧
            post(this::resetToFitCrop);
        });
    }

    /**
     * 将图片按原比例适配预览区域：宽或高其一贴边，另一侧按比例显示（不双向拉满）。
     *
     * @return true 适配成功
     */
    public boolean resetToFitCrop() {
        Bitmap bitmap = getBitmapSafe();
        if (bitmap == null || cropOverlayView == null) {
            return false;
        }
        if (!cropOverlayView.isCropRectReady() || getWidth() == 0 || getHeight() == 0) {
            return false;
        }
        // 两边宽高需一致，避免坐标系偏差
        if (getWidth() != cropOverlayView.getWidth() || getHeight() != cropOverlayView.getHeight()) {
            return false;
        }
        RectF crop = cropOverlayView.getCropRect();
        float fitX = getWidth() / (float) bitmap.getWidth();
        float fitY = getHeight() / (float) bitmap.getHeight();
        // 取较小值：保持原比例，宽或高任意一边充满即可
        float fitScale = Math.min(fitX, fitY);
        float coverCropX = crop.width() / (float) bitmap.getWidth();
        float coverCropY = crop.height() / (float) bitmap.getHeight();
        float coverCrop = Math.max(coverCropX, coverCropY);
        // 默认按原比例 fit；仅当裁剪框超出当前显示图时才放大到刚好覆盖裁剪框
        baseScale = Math.max(fitScale, coverCrop);
        minScale = coverCrop;
        maxScale = baseScale * 5f;
        imageMatrixInternal.reset();
        imageMatrixInternal.postScale(baseScale, baseScale);
        float scaledW = bitmap.getWidth() * baseScale;
        float scaledH = bitmap.getHeight() * baseScale;
        float dx = getWidth() / 2f - scaledW / 2f;
        float dy = getHeight() / 2f - scaledH / 2f;
        imageMatrixInternal.postTranslate(dx, dy);
        boundTranslation();
        setImageMatrix(imageMatrixInternal);
        hasFittedOnce = true;
        return true;
    }

    /**
     * 裁剪框变化后，保证图片仍覆盖裁剪区域
     */
    public void ensureImageCoversCrop() {
        if (!hasFittedOnce) {
            return;
        }
        Bitmap bitmap = getBitmapSafe();
        if (bitmap == null || cropOverlayView == null || !cropOverlayView.isCropRectReady()) {
            return;
        }
        RectF crop = cropOverlayView.getCropRect();
        float needScaleX = crop.width() / (float) bitmap.getWidth();
        float needScaleY = crop.height() / (float) bitmap.getHeight();
        float needScale = Math.max(needScaleX, needScaleY);
        minScale = needScale;
        maxScale = Math.max(needScale * 5f, maxScale);
        float current = getCurrentScale();
        if (current < needScale && current > 0f) {
            float factor = needScale / current;
            imageMatrixInternal.postScale(factor, factor, crop.centerX(), crop.centerY());
        }
        boundTranslation();
        setImageMatrix(imageMatrixInternal);
    }

    /**
     * 获取当前图片矩阵副本
     *
     * @return Matrix
     */
    @NonNull
    public Matrix getCurrentImageMatrix() {
        return new Matrix(imageMatrixInternal);
    }

    /**
     * 长按边缘时尝试进入裁剪框缩放
     *
     * @param x 触摸 X
     * @param y 触摸 Y
     */
    private void tryEnterCropResize(float x, float y) {
        if (movedBeforeLongPress || cropOverlayView == null || !cropOverlayView.isCropFrameScalable()) {
            return;
        }
        int mode = cropOverlayView.hitEdgeMode(x, y);
        if (mode == CropOverlayView.TOUCH_NONE) {
            return;
        }
        if (cropOverlayView.beginResize(mode)) {
            isDragging = false;
            lastX = x;
            lastY = y;
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    /**
     * 安全获取 Drawable Bitmap
     *
     * @return Bitmap 或 null
     */
    @Nullable
    private Bitmap getBitmapSafe() {
        if (getDrawable() == null) {
            return null;
        }
        if (getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
            return ((android.graphics.drawable.BitmapDrawable) getDrawable()).getBitmap();
        }
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 遮罩在上层且不消费事件时，事件会落到本控件；统一处理图片手势与长按缩放
        gestureDetector.onTouchEvent(event);
        boolean resizing = cropOverlayView != null && cropOverlayView.isResizing();
        if (!resizing) {
            scaleGestureDetector.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastX = downX;
                lastY = downY;
                movedBeforeLongPress = false;
                isDragging = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 双指开始时若在缩放裁剪框，先退出
                if (cropOverlayView != null && cropOverlayView.isResizing()) {
                    cropOverlayView.endResize();
                }
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (!movedBeforeLongPress) {
                    float slopX = x - downX;
                    float slopY = y - downY;
                    if (slopX * slopX + slopY * slopY > touchSlop * touchSlop) {
                        movedBeforeLongPress = true;
                    }
                }
                if (cropOverlayView != null && cropOverlayView.isResizing()) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    cropOverlayView.applyResizeDelta(dx, dy);
                    ensureImageCoversCrop();
                    lastX = x;
                    lastY = y;
                } else if (isDragging && !scaleGestureDetector.isInProgress() && event.getPointerCount() == 1) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    imageMatrixInternal.postTranslate(dx, dy);
                    boundTranslation();
                    setImageMatrix(imageMatrixInternal);
                    lastX = x;
                    lastY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (cropOverlayView != null && cropOverlayView.isResizing()) {
                    cropOverlayView.endResize();
                }
                isDragging = false;
                movedBeforeLongPress = false;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 限制平移，保证图片始终覆盖裁剪框
     */
    private void boundTranslation() {
        Bitmap bitmap = getBitmapSafe();
        if (bitmap == null || cropOverlayView == null || !cropOverlayView.isCropRectReady()) {
            return;
        }
        RectF crop = cropOverlayView.getCropRect();
        RectF imageRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        imageMatrixInternal.mapRect(imageRect);

        float dx = 0f;
        float dy = 0f;
        if (imageRect.width() + 0.5f >= crop.width()) {
            if (imageRect.left > crop.left) {
                dx = crop.left - imageRect.left;
            } else if (imageRect.right < crop.right) {
                dx = crop.right - imageRect.right;
            }
        } else {
            dx = crop.centerX() - imageRect.centerX();
        }
        if (imageRect.height() + 0.5f >= crop.height()) {
            if (imageRect.top > crop.top) {
                dy = crop.top - imageRect.top;
            } else if (imageRect.bottom < crop.bottom) {
                dy = crop.bottom - imageRect.bottom;
            }
        } else {
            dy = crop.centerY() - imageRect.centerY();
        }
        if (dx != 0f || dy != 0f) {
            imageMatrixInternal.postTranslate(dx, dy);
        }
    }

    /**
     * 获取当前缩放值（MSCALE_X）
     *
     * @return 缩放值
     */
    private float getCurrentScale() {
        imageMatrixInternal.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    /**
     * 双指缩放监听
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (cropOverlayView != null && cropOverlayView.isResizing()) {
                return false;
            }
            float scaleFactor = detector.getScaleFactor();
            float current = getCurrentScale();
            if (current <= 0f) {
                return false;
            }
            float target = current * scaleFactor;
            if (target < minScale) {
                scaleFactor = minScale / current;
            } else if (target > maxScale) {
                scaleFactor = maxScale / current;
            }
            imageMatrixInternal.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            boundTranslation();
            setImageMatrix(imageMatrixInternal);
            return true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && (w != oldw || h != oldh)) {
            hasFittedOnce = false;
            scheduleFitWhenReady();
        }
    }
}
