package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.UpdateDialogBinding;
import io.coderf.arklab.common.utils.network.NetworkStateUtil;
import io.coderf.arklab.common.widget.customview.CornerShapeHelper;

/**
 * 应用更新提示弹窗。
 * <p>
 * 纯 UI 组件：白底圆角卡片 + 主题色顶栏，无业务图资源依赖，适合作为框架通用更新入口。
 * 更新说明支持：
 * <ul>
 *   <li>纯文本 + 自动识别 URL（可点击跳转）</li>
 *   <li>HTML 富文本（{@code <b>/<i>/<a href> } 等），链接可点击</li>
 * </ul>
 * 下载逻辑由 {@link OnUpdateListener} 交给调用方（如 {@code UpdateManager}）处理。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/12/9
 */
public class UpdateMessageDialog extends BaseDialog {

    private String updateMsgString;
    private String versionName;
    private OnUpdateListener onUpdateListener;
    private boolean canCancel = false;
    private String buttonText;
    private Drawable drawable;
    private @ColorInt int strokeColor;
    private int strokeWidth;
    private @ColorInt int bgColor;
    private @ColorInt int buttonTextColor;
    private @ColorInt int titleColor;
    private @ColorInt int updateMsgTextColor;
    private float titleTextSize;
    private float updateMsgTextSize;
    private float buttonTextSize;
    private float radius;
    private UpdateDialogBinding binding;

    public UpdateMessageDialog(@NonNull Context context) {
        super(context,R.style.DialogSoftHighlightStyle);
        buttonTextColor = ContextCompat.getColor(context, R.color.onPrimary);
        titleColor = ContextCompat.getColor(context, R.color.autoColor);
        updateMsgTextColor = ContextCompat.getColor(context, R.color.gray);
        bgColor = ContextCompat.getColor(context, R.color.themeColor);
        strokeColor = ContextCompat.getColor(context, R.color.themeColor);
        strokeWidth = 0;
        radius = context.getResources().getDimension(R.dimen.radius_xxl);
        titleTextSize = context.getResources().getDimension(R.dimen.font_size_xxl);
        updateMsgTextSize = context.getResources().getDimension(R.dimen.font_size_xl);
        buttonTextSize = context.getResources().getDimension(R.dimen.font_size_xxl);
    }

    public UpdateMessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        buttonTextColor = ContextCompat.getColor(context, R.color.onPrimary);
        titleColor = ContextCompat.getColor(context, R.color.autoColor);
        updateMsgTextColor = ContextCompat.getColor(context, R.color.gray);
        bgColor = ContextCompat.getColor(context, R.color.themeColor);
        strokeColor = ContextCompat.getColor(context, R.color.themeColor);
        strokeWidth = 0;
        radius = context.getResources().getDimension(R.dimen.radius_xxl);
        titleTextSize = context.getResources().getDimension(R.dimen.font_size_xxl);
        updateMsgTextSize = context.getResources().getDimension(R.dimen.font_size_xl);
        buttonTextSize = context.getResources().getDimension(R.dimen.font_size_xxl);
    }

    public UpdateMessageDialog setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
        return this;
    }

    public UpdateMessageDialog setButtonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
    }

    public UpdateMessageDialog setDrawable(Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgString(String updateMsgString) {
        this.updateMsgString = updateMsgString;
        return this;
    }

    public UpdateMessageDialog setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public UpdateMessageDialog setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public UpdateMessageDialog setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public UpdateMessageDialog setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public UpdateMessageDialog setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
        return this;
    }

    public UpdateMessageDialog setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public UpdateMessageDialog setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgTextColor(int updateMsgTextColor) {
        this.updateMsgTextColor = updateMsgTextColor;
        return this;
    }

    public UpdateMessageDialog setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgTextSize(float updateMsgTextSize) {
        this.updateMsgTextSize = updateMsgTextSize;
        return this;
    }

    public UpdateMessageDialog setButtonTextSize(float buttonTextSize) {
        this.buttonTextSize = buttonTextSize;
        return this;
    }

    public UpdateMessageDialog setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
        return this;
    }

    public UpdateMessageDialog builder() {
        initView();
        return this;
    }

    public UpdateDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = UpdateDialogBinding.inflate(getLayoutInflater(), null, false);

        bindTitle();
        bindVersionChip();
        bindUpdateMessage();
        bindUpdateButton();
        bindCloseButton();

        setContentView(binding.getRoot());
        applyCancelableOutside(canCancel);
        // 窗口背景透明，圆角与白底由布局 bg_update_dialog 承担
        setBgDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        applyCenterWindow();
    }

    private void bindTitle() {
        binding.updateTitle.setText(getContext().getString(R.string.checked_new_version));
        binding.updateTitle.setTextColor(titleColor);
        binding.updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
    }

    private void bindVersionChip() {
        if (TextUtils.isEmpty(versionName)) {
            binding.tvVersionChip.setVisibility(View.GONE);
            return;
        }
        String label = versionName.trim();
        if (!label.regionMatches(true, 0, "v", 0, 1)) {
            label = "V" + label;
        } else {
            // 统一成大写 V 前缀展示
            label = "V" + label.substring(1);
        }
        binding.tvVersionChip.setText(label);
        binding.tvVersionChip.setTextColor(bgColor);
        // 使用当前按钮主题色的浅色底，避免写死色值
        android.graphics.drawable.GradientDrawable chipBg = new android.graphics.drawable.GradientDrawable();
        chipBg.setColor((bgColor & 0x00FFFFFF) | 0x1A000000);
        chipBg.setCornerRadius(getContext().getResources().getDimension(R.dimen.radius_xxl));
        binding.tvVersionChip.setBackground(chipBg);
        binding.tvVersionChip.setVisibility(View.VISIBLE);
    }

    /**
     * 更新说明：优先按 HTML 解析（支持 {@code <a href>} 等），再对纯文本 URL 做 Linkify。
     */
    private void bindUpdateMessage() {
        CharSequence content = buildUpdateContent();
        binding.updateMsg.setText(content);
        binding.updateMsg.setTextColor(updateMsgTextColor);
        binding.updateMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, updateMsgTextSize);
        binding.updateMsg.setMovementMethod(LinkMovementMethod.getInstance());
        // 去掉点击链接后的高亮背景残影
        binding.updateMsg.setHighlightColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
    }

    @NonNull
    private CharSequence buildUpdateContent() {
        String raw = updateMsgString;
        if (TextUtils.isEmpty(raw)) {
            return getContext().getString(R.string.no_upgrade_info);
        }
        // 含 HTML 标签时走 HtmlCompat，便于服务端下发带链接的富文本
        if (looksLikeHtml(raw)) {
            Spanned spanned = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_COMPACT);
            // 对未写成 <a> 的裸 URL 再补一层可点击
            SpannableString spannable = new SpannableString(spanned);
            Linkify.addLinks(spannable, Linkify.WEB_URLS);
            return spannable;
        }
        SpannableString spannable = new SpannableString(raw);
        Linkify.addLinks(spannable, Linkify.WEB_URLS);
        return spannable;
    }

    private static boolean looksLikeHtml(@NonNull String text) {
        String lower = text.toLowerCase();
        return lower.contains("<a ")
                || lower.contains("<br")
                || lower.contains("<p")
                || lower.contains("<b>")
                || lower.contains("<i>")
                || lower.contains("<ul")
                || lower.contains("<li")
                || lower.contains("<div")
                || lower.contains("<span")
                || lower.contains("&lt;")
                || lower.contains("</");
    }

    private void bindUpdateButton() {
        if (TextUtils.isEmpty(buttonText)) {
            binding.updateBtn.setText(ContextCompat.getString(getContext(), R.string.upgrade));
        } else {
            binding.updateBtn.setText(buttonText);
        }
        binding.updateBtn.setTextColor(buttonTextColor);
        binding.updateBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);

        if (drawable != null) {
            binding.updateBtn.setBackground(drawable);
        } else {
            CornerShapeHelper.apply(binding.updateBtn, radius, bgColor, strokeWidth, strokeColor);
        }

        binding.updateBtn.setOnClickListener(v -> {
            if (isShowing()) {
                dismiss();
            }
            if (NetworkStateUtil.isMobile(v.getContext())) {
                new ConfirmDialog(v.getContext(),R.style.DialogSoftHighlightStyle)
                        .setMessage("您正在使用数据流量，确定继续下载吗？")
                        .setOnPositiveClickListener(dialog -> {
                            if (onUpdateListener != null) {
                                onUpdateListener.onUpdate(v);
                            }
                        })
                        .builder()
                        .show();
            } else if (onUpdateListener != null) {
                onUpdateListener.onUpdate(v);
            }
        });
    }

    private void bindCloseButton() {
        if (!canCancel) {
            binding.ivClose.setVisibility(View.GONE);
            return;
        }
        binding.ivClose.setVisibility(View.VISIBLE);
        binding.ivClose.setOnClickListener(v -> dismiss());
    }

    public interface OnUpdateListener {
        void onUpdate(View v);
    }
}
