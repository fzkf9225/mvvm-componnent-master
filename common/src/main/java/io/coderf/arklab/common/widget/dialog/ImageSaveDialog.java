package io.coderf.arklab.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.ImageSaveDialogBinding;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.gallery.PreviewGalleryConfig;

/**
 * 保存图片到本地 dialog
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/5/20 17:13
 */
public class ImageSaveDialog extends BaseDialog {

    private OnImageSaveListener onImageSaveListener;
    private ImageSaveDialogBinding binding;
    @Nullable
    private ImageSaveDialogConfig config;

    public ImageSaveDialog setOnImageSaveListener(OnImageSaveListener onImageSaveListener) {
        this.onImageSaveListener = onImageSaveListener;
        return this;
    }

    /**
     * 设置样式配置；未设置字段将保持内置默认样式。
     */
    public ImageSaveDialog setConfig(@Nullable ImageSaveDialogConfig config) {
        this.config = config;
        return this;
    }

    public ImageSaveDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle_No_Bg);
    }

    public ImageSaveDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public ImageSaveDialogBinding getBinding() {
        return binding;
    }

    public ImageSaveDialog build() {
        binding = ImageSaveDialogBinding.inflate(getLayoutInflater(), null, false);
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        binding.saveLocal.setOnClickListener(v -> {
            if (onImageSaveListener != null) {
                onImageSaveListener.saveSuccess(this);
            }
        });
        applyBottomSheetStyle(getEffectiveConfig());
        setContentView(binding.getRoot());
        applyBottomWindowLayout();
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return this;
        }
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 0;
        ImageSaveDialogConfig cfg = getEffectiveConfig();
        if (isDimBehindEnabled(cfg)) {
            dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialogWindow.setDimAmount(resolveDimAmount(cfg));
        } else {
            dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        dialogWindow.setAttributes(lp);
        return this;
    }

    private ImageSaveDialogConfig getEffectiveConfig() {
        if (config != null) {
            return config;
        }
        ImageSaveDialogConfig global = PreviewGalleryConfig.getGlobalImageSaveDialogConfig();
        return global != null ? global : ImageSaveDialogConfig.empty();
    }

    private float resolveDimAmount(ImageSaveDialogConfig cfg) {
        return cfg.getDimAmount() != null ? cfg.getDimAmount() : 0.45f;
    }

    private boolean isDimBehindEnabled(ImageSaveDialogConfig cfg) {
        return cfg.getDimBehindEnabled() != null && cfg.getDimBehindEnabled();
    }

    /**
     * 动态美化底部操作栏，不修改 xml 中的 rounded_white（避免影响其他页面）。
     */
    private void applyBottomSheetStyle(ImageSaveDialogConfig cfg) {
        Context ctx = getContext();
        int horizontalMargin = cfg.getHorizontalMarginPx() != null
                ? cfg.getHorizontalMarginPx() : DensityUtil.dp2px(ctx, 12f);
        int bottomMargin = cfg.getBottomMarginPx() != null
                ? cfg.getBottomMarginPx() : DensityUtil.dp2px(ctx, 12f);
        int buttonGap = cfg.getButtonGapPx() != null
                ? cfg.getButtonGapPx() : DensityUtil.dp2px(ctx, 10f);
        float cornerRadius = cfg.getCornerRadiusPx() != null
                ? cfg.getCornerRadiusPx() : DensityUtil.dp2px(ctx, 12f);
        int white = cfg.getButtonBackgroundColor() != null
                ? cfg.getButtonBackgroundColor() : ContextCompat.getColor(ctx, R.color.white);
        int saveTextColor = cfg.getSaveTextColor() != null
                ? cfg.getSaveTextColor() : ContextCompat.getColor(ctx, R.color.autoColor);
        int cancelTextColor = cfg.getCancelTextColor() != null
                ? cfg.getCancelTextColor() : ContextCompat.getColor(ctx, R.color.gray);
        int rippleMask = cfg.getRippleColor() != null
                ? cfg.getRippleColor() : ContextCompat.getColor(ctx, R.color.h_line_color);
        boolean saveBold = cfg.getSaveTextBold() != null ? cfg.getSaveTextBold() : true;
        float saveTextSizePx = cfg.getSaveTextSizePx() != null
                ? cfg.getSaveTextSizePx()
                : ctx.getResources().getDimension(R.dimen.font_size_xxl);
        float cancelTextSizePx = cfg.getCancelTextSizePx() != null
                ? cfg.getCancelTextSizePx()
                : ctx.getResources().getDimension(R.dimen.font_size_xl);
        int buttonHeight = cfg.getButtonHeightPx() != null
                ? cfg.getButtonHeightPx()
                : ctx.getResources().getDimensionPixelSize(R.dimen.height_xl);

        String saveText = cfg.getSaveText() != null
                ? cfg.getSaveText() : ctx.getString(R.string.save_to_local);
        String cancelText = cfg.getCancelText() != null
                ? cfg.getCancelText() : ctx.getString(R.string.cancel);

        binding.saveLocal.setText(saveText);
        binding.buttonCancel.setText(cancelText);

        LinearLayout root = (LinearLayout) binding.getRoot();
        root.setPadding(horizontalMargin, 0, horizontalMargin, bottomMargin);
        root.setBackgroundColor(ContextCompat.getColor(ctx, R.color.transparent));

        styleActionButton(binding.saveLocal,
                createRoundedBackground(white, cornerRadius, rippleMask),
                saveTextColor, saveTextSizePx, saveBold, buttonHeight);
        styleActionButton(binding.buttonCancel,
                createRoundedBackground(white, cornerRadius, rippleMask),
                cancelTextColor, cancelTextSizePx, false, buttonHeight);

        ViewGroup.MarginLayoutParams saveLp =
                (ViewGroup.MarginLayoutParams) binding.saveLocal.getLayoutParams();
        if (saveLp != null) {
            saveLp.height = buttonHeight;
            binding.saveLocal.setLayoutParams(saveLp);
        }
        ViewGroup.MarginLayoutParams cancelLp =
                (ViewGroup.MarginLayoutParams) binding.buttonCancel.getLayoutParams();
        if (cancelLp != null) {
            cancelLp.topMargin = buttonGap;
            cancelLp.height = buttonHeight;
            binding.buttonCancel.setLayoutParams(cancelLp);
        }
    }

    private void styleActionButton(AppCompatButton button, Drawable background,
                                   int textColor, float textSizePx, boolean emphasize,
                                   int buttonHeight) {
        button.setBackground(background);
        button.setTextColor(textColor);
        button.setAllCaps(false);
        button.setTypeface(emphasize ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        button.setElevation(DensityUtil.dp2px(button.getContext(), emphasize ? 2f : 1f));
        button.setStateListAnimator(null);
        int verticalPadding = DensityUtil.dp2px(button.getContext(), 4f);
        button.setPadding(button.getPaddingLeft(), verticalPadding,
                button.getPaddingRight(), verticalPadding);
        ViewGroup.LayoutParams lp = button.getLayoutParams();
        if (lp != null) {
            lp.height = buttonHeight;
            button.setLayoutParams(lp);
        }
    }

    private Drawable createRoundedBackground(int fillColor, float cornerRadius, int rippleColor) {
        GradientDrawable content = new GradientDrawable();
        content.setColor(fillColor);
        content.setCornerRadius(cornerRadius);
        return DrawableUtil.createRippleDrawableCompat(content, rippleColor);
    }

    public interface OnImageSaveListener {
        void saveSuccess(Dialog dialog);
    }
}