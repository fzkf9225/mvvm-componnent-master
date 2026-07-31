package io.coderf.arklab.media.crop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;

import io.coderf.arklab.media.R;

/**
 * 图片裁剪页面：支持圆形 / 正方形 / 长方形裁剪框，底部取消与确定按钮。
 * 裁剪框可缩放拖动，按钮文案/颜色/字号及裁剪框颜色均可配置。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public class ImageCropActivity extends AppCompatActivity {

    /**
     * 可缩放拖动的图片预览
     */
    private GestureCropImageView cropImageView;

    /**
     * 裁剪遮罩层
     */
    private CropOverlayView cropOverlayView;

    /**
     * 取消按钮
     */
    private AppCompatTextView btnCancel;

    /**
     * 确定按钮
     */
    private AppCompatTextView btnConfirm;

    /**
     * 源图 Bitmap
     */
    @Nullable
    private Bitmap sourceBitmap;

    /**
     * 裁剪形状
     */
    private CropShapeEnum cropShape = CropShapeEnum.SQUARE;

    /**
     * 输出目录
     */
    private String outputDir;

    /**
     * 输出文件名
     */
    @Nullable
    private String outputFileName;

    /**
     * 最长边限制
     */
    private int maxOutputSize;

    /**
     * 是否已回调结果，避免重复回调
     */
    private boolean resultDelivered;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImmersiveStatusBar();
        setContentView(R.layout.activity_image_crop);
        initViews();
        parseIntentAndLoad();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                deliverCancel();
                finish();
            }
        });
    }

    /**
     * 沉浸式深色状态栏，适配裁剪页黑底风格
     */
    private void setupImmersiveStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }
    }

    /**
     * 初始化控件与点击事件
     */
    private void initViews() {
        cropImageView = findViewById(R.id.crop_image_view);
        cropOverlayView = findViewById(R.id.crop_overlay_view);
        btnCancel = findViewById(R.id.btn_crop_cancel);
        btnConfirm = findViewById(R.id.btn_crop_confirm);
        cropImageView.setCropOverlayView(cropOverlayView);
        btnCancel.setOnClickListener(v -> onCancelClick());
        btnConfirm.setOnClickListener(v -> onConfirmClick());
    }

    /**
     * 解析 Intent 参数并加载图片
     */
    private void parseIntentAndLoad() {
        Uri sourceUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sourceUri = getIntent().getParcelableExtra(ImageCropHelper.EXTRA_SOURCE_URI, Uri.class);
        } else {
            sourceUri = getIntent().getParcelableExtra(ImageCropHelper.EXTRA_SOURCE_URI);
        }
        String sourcePath = getIntent().getStringExtra(ImageCropHelper.EXTRA_SOURCE_PATH);
        int shapeOrdinal = getIntent().getIntExtra(ImageCropHelper.EXTRA_CROP_SHAPE,
                CropShapeEnum.SQUARE.ordinal());
        CropShapeEnum[] values = CropShapeEnum.values();
        if (shapeOrdinal >= 0 && shapeOrdinal < values.length) {
            cropShape = values[shapeOrdinal];
        }
        int aspectX = getIntent().getIntExtra(ImageCropHelper.EXTRA_ASPECT_X, 1);
        int aspectY = getIntent().getIntExtra(ImageCropHelper.EXTRA_ASPECT_Y, 1);
        outputDir = getIntent().getStringExtra(ImageCropHelper.EXTRA_OUTPUT_DIR);
        outputFileName = getIntent().getStringExtra(ImageCropHelper.EXTRA_OUTPUT_FILE_NAME);
        maxOutputSize = getIntent().getIntExtra(ImageCropHelper.EXTRA_MAX_OUTPUT_SIZE,
                ImageCropBuilder.DEFAULT_MAX_OUTPUT_SIZE);

        boolean scalable = getIntent().getBooleanExtra(ImageCropHelper.EXTRA_CROP_FRAME_SCALABLE, true);
        boolean freeCrop = getIntent().getBooleanExtra(ImageCropHelper.EXTRA_FREE_CROP, false);
        float edgeInsetDp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CROP_EDGE_INSET_DP,
                ImageCropBuilder.DEFAULT_CROP_EDGE_INSET_DP);
        float initialCropScale = getIntent().getFloatExtra(ImageCropHelper.EXTRA_INITIAL_CROP_SCALE,
                ImageCropBuilder.DEFAULT_INITIAL_CROP_SCALE);
        int borderColor = getIntent().getIntExtra(ImageCropHelper.EXTRA_BORDER_COLOR,
                ImageCropBuilder.DEFAULT_BORDER_COLOR);
        int dimColor = getIntent().getIntExtra(ImageCropHelper.EXTRA_DIM_COLOR,
                ImageCropBuilder.DEFAULT_DIM_COLOR);
        int gridColor = getIntent().getIntExtra(ImageCropHelper.EXTRA_GRID_COLOR, 0);

        float edgeInsetPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, edgeInsetDp,
                getResources().getDisplayMetrics());
        cropOverlayView.setCropEdgeInsetPx(edgeInsetPx);
        cropOverlayView.setInitialCropScale(initialCropScale);
        cropOverlayView.setCropConfig(cropShape, aspectX, aspectY, freeCrop);
        cropOverlayView.setCropFrameScalable(scalable);
        cropOverlayView.setBorderColor(borderColor);
        cropOverlayView.setDimColor(dimColor);
        if (gridColor != 0) {
            cropOverlayView.setGridColor(gridColor);
        }
        applyButtonStyles();

        sourceBitmap = CropUtil.loadBitmap(this, sourceUri, sourcePath);
        if (sourceBitmap == null) {
            deliverError(getString(R.string.media_crop_load_failed), null);
            finish();
            return;
        }
        cropImageView.setImageBitmap(sourceBitmap);
        // 等待裁剪区域与遮罩同尺寸布局完成后再居中，避免初始化偏移
        View container = findViewById(R.id.crop_content_container);
        container.post(cropImageView::scheduleFitWhenReady);
    }

    /**
     * 应用取消/确定按钮的文案、颜色、字号、背景色与宽高配置
     */
    private void applyButtonStyles() {
        String cancelText = getIntent().getStringExtra(ImageCropHelper.EXTRA_CANCEL_TEXT);
        String confirmText = getIntent().getStringExtra(ImageCropHelper.EXTRA_CONFIRM_TEXT);
        int cancelTextColor = getIntent().getIntExtra(ImageCropHelper.EXTRA_CANCEL_TEXT_COLOR,
                ImageCropBuilder.DEFAULT_BUTTON_TEXT_COLOR);
        int confirmTextColor = getIntent().getIntExtra(ImageCropHelper.EXTRA_CONFIRM_TEXT_COLOR,
                ImageCropBuilder.DEFAULT_BUTTON_TEXT_COLOR);
        float cancelTextSizeSp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CANCEL_TEXT_SIZE_SP,
                ImageCropBuilder.DEFAULT_BUTTON_TEXT_SIZE_SP);
        float confirmTextSizeSp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CONFIRM_TEXT_SIZE_SP,
                ImageCropBuilder.DEFAULT_BUTTON_TEXT_SIZE_SP);
        int cancelBg = getIntent().getIntExtra(ImageCropHelper.EXTRA_CANCEL_BG_COLOR,
                ImageCropBuilder.DEFAULT_CANCEL_BG_COLOR);
        int confirmBg = getIntent().getIntExtra(ImageCropHelper.EXTRA_CONFIRM_BG_COLOR,
                ImageCropBuilder.DEFAULT_CONFIRM_BG_COLOR);
        float cancelWidthDp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CANCEL_BUTTON_WIDTH_DP,
                ImageCropBuilder.DEFAULT_BUTTON_WIDTH_DP);
        float cancelHeightDp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CANCEL_BUTTON_HEIGHT_DP,
                ImageCropBuilder.DEFAULT_BUTTON_HEIGHT_DP);
        float confirmWidthDp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CONFIRM_BUTTON_WIDTH_DP,
                ImageCropBuilder.DEFAULT_BUTTON_WIDTH_DP);
        float confirmHeightDp = getIntent().getFloatExtra(ImageCropHelper.EXTRA_CONFIRM_BUTTON_HEIGHT_DP,
                ImageCropBuilder.DEFAULT_BUTTON_HEIGHT_DP);

        if (!TextUtils.isEmpty(cancelText)) {
            btnCancel.setText(cancelText);
        }
        if (!TextUtils.isEmpty(confirmText)) {
            btnConfirm.setText(confirmText);
        }
        btnCancel.setTextColor(cancelTextColor);
        btnConfirm.setTextColor(confirmTextColor);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, cancelTextSizeSp);
        btnConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, confirmTextSizeSp);
        btnCancel.setBackground(createRoundRippleBackground(cancelBg));
        btnConfirm.setBackground(createRoundRippleBackground(confirmBg));
        applyButtonSize(btnCancel, cancelWidthDp, cancelHeightDp);
        applyButtonSize(btnConfirm, confirmWidthDp, confirmHeightDp);
    }

    /**
     * 应用按钮宽高（dp）。宽度 &lt;=0 保持均分；高度 &lt;=0 使用默认 48dp。
     *
     * @param button   按钮
     * @param widthDp  宽度 dp
     * @param heightDp 高度 dp
     */
    private void applyButtonSize(AppCompatTextView button, float widthDp, float heightDp) {
        android.view.ViewGroup.LayoutParams lp = button.getLayoutParams();
        if (!(lp instanceof android.widget.LinearLayout.LayoutParams)) {
            return;
        }
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) lp;
        if (widthDp > 0) {
            params.width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp,
                    getResources().getDisplayMetrics()));
            params.weight = 0f;
        } else {
            params.width = 0;
            params.weight = 1f;
        }
        float resolvedHeight = heightDp > 0 ? heightDp : ImageCropBuilder.DEFAULT_BUTTON_HEIGHT_DP;
        params.height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resolvedHeight,
                getResources().getDisplayMetrics()));
        button.setLayoutParams(params);
    }

    /**
     * 创建圆角按钮背景（含按压缩放波纹）
     *
     * @param solidColor 实心背景色
     * @return Drawable
     */
    private RippleDrawable createRoundRippleBackground(@ColorInt int solidColor) {
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f,
                getResources().getDisplayMetrics());
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(solidColor);
        // 取消按钮默认半透明时补描边，避免过淡看不清
        if (Color.alpha(solidColor) < 0xCC) {
            shape.setStroke((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f,
                    getResources().getDisplayMetrics()), 0xCCFFFFFF);
        }
        return new RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x40FFFFFF),
                shape,
                null
        );
    }

    /**
     * 点击取消
     */
    private void onCancelClick() {
        deliverCancel();
        finish();
    }

    /**
     * 点击确定，执行裁剪并保存
     */
    private void onConfirmClick() {
        if (sourceBitmap == null) {
            deliverError(getString(R.string.media_crop_load_failed), null);
            finish();
            return;
        }
        RectF cropRect = new RectF(cropOverlayView.getCropRect());
        Bitmap cropped = CropUtil.cropBitmap(sourceBitmap, cropImageView.getCurrentImageMatrix(),
                cropRect, cropShape, maxOutputSize);
        if (cropped == null) {
            deliverError(getString(R.string.media_crop_failed), null);
            Toast.makeText(this, R.string.media_crop_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        File outFile = CropUtil.resolveOutputFile(outputDir, outputFileName, cropShape);
        boolean isPng = cropShape == CropShapeEnum.CIRCLE
                || (outputFileName != null && outputFileName.toLowerCase().endsWith(".png"));
        boolean saved = CropUtil.saveBitmap(cropped, outFile, isPng);
        if (cropped != sourceBitmap) {
            cropped.recycle();
        }
        if (!saved) {
            deliverError(getString(R.string.media_crop_save_failed), null);
            Toast.makeText(this, R.string.media_crop_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri outUri = ImageCropHelper.fileToUri(this, outFile);
        deliverSuccess(outFile, outUri);
        finish();
    }

    /**
     * 分发成功回调
     *
     * @param file 输出文件
     * @param uri  输出 Uri
     */
    private void deliverSuccess(File file, Uri uri) {
        if (resultDelivered) {
            return;
        }
        resultDelivered = true;
        OnImageCropListener listener = ImageCropHelper.drainListener();
        if (listener != null) {
            listener.onCropSuccess(file, uri);
        }
    }

    /**
     * 分发取消回调
     */
    private void deliverCancel() {
        if (resultDelivered) {
            return;
        }
        resultDelivered = true;
        OnImageCropListener listener = ImageCropHelper.drainListener();
        if (listener != null) {
            listener.onCropCancel();
        }
    }

    /**
     * 分发失败回调
     *
     * @param message   错误信息
     * @param throwable 异常
     */
    private void deliverError(@Nullable String message, @Nullable Throwable throwable) {
        if (resultDelivered) {
            return;
        }
        resultDelivered = true;
        OnImageCropListener listener = ImageCropHelper.drainListener();
        if (listener != null) {
            listener.onCropError(message, throwable);
        }
    }

    @Override
    protected void onDestroy() {
        if (!resultDelivered) {
            // 异常退出时清理静态回调，避免泄漏
            ImageCropHelper.drainListener();
        }
        if (sourceBitmap != null && !sourceBitmap.isRecycled()) {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }
        super.onDestroy();
    }
}
