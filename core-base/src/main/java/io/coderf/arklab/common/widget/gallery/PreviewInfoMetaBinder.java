package io.coderf.arklab.common.widget.gallery;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;
import io.coderf.arklab.common.widget.gallery.config.PreviewInfoConfig;
import io.coderf.arklab.common.widget.gallery.inter.PreviewInfoMetaHost;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * 绑定信息大图预览 item 的元数据区（右上定位 + 底部三行文案）。
 * <p>
 * 约定 item 根布局（或 merge include）内存在：
 * <ul>
 *   <li>{@code R.id.iv_preview_location}</li>
 *   <li>{@code R.id.ll_preview_info} / {@code tv_preview_title} / {@code tv_preview_time} / {@code tv_preview_location}</li>
 * </ul>
 * 推荐 {@code <include layout="@layout/include_preview_info_meta"/>}。
 *
 * @author fz
 * @since 1.0
 * @created 2026/11/26
 */
public final class PreviewInfoMetaBinder {

    private static final int TAG_LOCATION_TOP = R.id.iv_preview_location;
    private static final int TAG_INFO_PAD_BOTTOM = R.id.ll_preview_info;

    private PreviewInfoMetaBinder() {
    }

    /**
     * 按 Host + 当前条目绑定元数据区；host/item 为空时全部隐藏。
     */
    public static void bind(@Nullable View itemRoot,
                            @Nullable PreviewInfoMetaHost host,
                            @Nullable PreviewInfoBean item,
                            int adapterPosition) {
        if (itemRoot == null) {
            return;
        }
        ShapeableImageView ivLocation = itemRoot.findViewById(R.id.iv_preview_location);
        View llInfo = itemRoot.findViewById(R.id.ll_preview_info);
        MaterialTextView tvTitle = itemRoot.findViewById(R.id.tv_preview_title);
        MaterialTextView tvTime = itemRoot.findViewById(R.id.tv_preview_time);
        MaterialTextView tvLocation = itemRoot.findViewById(R.id.tv_preview_location);

        if (host == null || item == null) {
            setGone(ivLocation);
            setGone(llInfo);
            return;
        }

        PreviewInfoConfig config = host.getPreviewInfoConfig();
        if (config == null) {
            config = PreviewInfoConfig.defaults();
        }

        bindLocationIcon(ivLocation, host, config, item, adapterPosition);
        bindInfoLines(llInfo, tvTitle, tvTime, tvLocation, host, config, item);
    }

    private static void bindLocationIcon(@Nullable ShapeableImageView ivLocation,
                                         @NonNull PreviewInfoMetaHost host,
                                         @NonNull PreviewInfoConfig config,
                                         @NonNull PreviewInfoBean item,
                                         int adapterPosition) {
        if (ivLocation == null) {
            return;
        }
        if (!config.isLocationEnabled()) {
            ivLocation.setVisibility(View.GONE);
            ivLocation.setOnClickListener(null);
            return;
        }
        ivLocation.setVisibility(View.VISIBLE);
        applyIconSize(ivLocation, config.getLocationIconSizePx());
        applyTopInset(ivLocation, host.getMetaTopInsetPx());

        boolean selected = config.isLocationCanSelected() && item.isLocationIconSelected();
        Drawable icon = selected ? host.getLocationSelectedIcon() : host.getLocationNormalIcon();
        if (icon == null) {
            icon = ContextCompat.getDrawable(ivLocation.getContext(), R.drawable.ic_preview_location);
        }
        if (icon != null) {
            ivLocation.setImageDrawable(icon);
        }

        ivLocation.setOnClickListener(v -> {
            boolean nextSelected = item.isLocationIconSelected();
            if (config.isLocationCanSelected()) {
                nextSelected = !nextSelected;
                item.setLocationIconSelected(nextSelected);
                Drawable updated = nextSelected
                        ? host.getLocationSelectedIcon()
                        : host.getLocationNormalIcon();
                if (updated == null) {
                    updated = ContextCompat.getDrawable(v.getContext(), R.drawable.ic_preview_location);
                }
                if (updated != null) {
                    ivLocation.setImageDrawable(updated);
                }
            }
            host.onLocationIconClick(item, adapterPosition, nextSelected);
        });
    }

    private static void bindInfoLines(@Nullable View llInfo,
                                      @Nullable MaterialTextView tvTitle,
                                      @Nullable MaterialTextView tvTime,
                                      @Nullable MaterialTextView tvLocation,
                                      @NonNull PreviewInfoMetaHost host,
                                      @NonNull PreviewInfoConfig config,
                                      @NonNull PreviewInfoBean item) {
        if (llInfo == null) {
            return;
        }
        applyInfoPadding(llInfo, host, config);

        float titleSize = config.getTitleTextSizePx() != null
                ? config.getTitleTextSizePx()
                : DensityUtil.sp2px(llInfo.getContext(), 16f);
        int titleColor = config.getTitleTextColor() != null
                ? config.getTitleTextColor()
                : PreviewInfoConfig.DEFAULT_TITLE_TEXT_COLOR;
        float timeSize = config.getTimeTextSizePx() != null
                ? config.getTimeTextSizePx()
                : DensityUtil.sp2px(llInfo.getContext(), 13f);
        int timeColor = config.getTimeTextColor() != null
                ? config.getTimeTextColor()
                : PreviewInfoConfig.DEFAULT_SUB_TEXT_COLOR;
        float locationSize = config.getLocationTextSizePx() != null
                ? config.getLocationTextSizePx()
                : DensityUtil.sp2px(llInfo.getContext(), 13f);
        int locationColor = config.getLocationTextColor() != null
                ? config.getLocationTextColor()
                : PreviewInfoConfig.DEFAULT_SUB_TEXT_COLOR;

        applyTextStyle(tvTitle, titleSize, titleColor);
        applyTextStyle(tvTime, timeSize, timeColor);
        applyTextStyle(tvLocation, locationSize, locationColor);

        int titleTimeSpacing = config.getTitleTimeSpacingPx() != null
                ? config.getTitleTimeSpacingPx()
                : DensityUtil.dp2px(llInfo.getContext(), 6f);
        int timeLocationSpacing = config.getTimeLocationSpacingPx() != null
                ? config.getTimeLocationSpacingPx()
                : DensityUtil.dp2px(llInfo.getContext(), 4f);
        applyTopMargin(tvTime, titleTimeSpacing);
        applyTopMargin(tvLocation, timeLocationSpacing);

        bindLine(tvTitle, config.isTitleEnabled() ? item.resolveTitle() : null);
        bindLine(tvTime, config.isTimeEnabled() ? item.resolveTimeText() : null);
        bindLine(tvLocation, config.isLocationTextEnabled() ? item.resolveLocationText() : null);

        boolean hasInfo = isVisible(tvTitle) || isVisible(tvTime) || isVisible(tvLocation);
        llInfo.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
    }

    private static void applyInfoPadding(@NonNull View llInfo,
                                         @NonNull PreviewInfoMetaHost host,
                                         @NonNull PreviewInfoConfig config) {
        int paddingStart = config.getInfoPaddingStartPx() != null
                ? config.getInfoPaddingStartPx()
                : DensityUtil.dp2px(llInfo.getContext(), 16f);
        int paddingTop = config.getInfoPaddingTopPx() != null
                ? config.getInfoPaddingTopPx()
                : DensityUtil.dp2px(llInfo.getContext(), 12f);
        int paddingEnd = config.getInfoPaddingEndPx() != null
                ? config.getInfoPaddingEndPx()
                : DensityUtil.dp2px(llInfo.getContext(), 72f);
        int baseBottom = config.getInfoPaddingBottomPx() != null
                ? config.getInfoPaddingBottomPx()
                : DensityUtil.dp2px(llInfo.getContext(), 24f);
        Object tag = llInfo.getTag(TAG_INFO_PAD_BOTTOM);
        if (!(tag instanceof Integer)) {
            llInfo.setTag(TAG_INFO_PAD_BOTTOM, baseBottom);
        } else {
            baseBottom = (Integer) tag;
            // 若 config 显式改了底边距，刷新基准
            if (config.getInfoPaddingBottomPx() != null) {
                baseBottom = config.getInfoPaddingBottomPx();
                llInfo.setTag(TAG_INFO_PAD_BOTTOM, baseBottom);
            }
        }
        int paddingBottom = baseBottom + Math.max(0, host.getMetaBottomInsetPx());
        llInfo.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
    }

    private static void applyTopInset(@NonNull View view, int extraTopPx) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (!(raw instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) raw;
        Object tag = view.getTag(TAG_LOCATION_TOP);
        int baseTop;
        if (tag instanceof Integer) {
            baseTop = (Integer) tag;
        } else {
            baseTop = lp.topMargin;
            view.setTag(TAG_LOCATION_TOP, baseTop);
        }
        int target = baseTop + Math.max(0, extraTopPx);
        if (lp.topMargin != target) {
            lp.topMargin = target;
            view.setLayoutParams(lp);
        }
    }

    private static void bindLine(@Nullable MaterialTextView textView, @Nullable String text) {
        if (textView == null) {
            return;
        }
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
            textView.setText("");
            return;
        }
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
    }

    private static void applyTextStyle(@Nullable MaterialTextView textView,
                                       float textSizePx,
                                       @ColorInt int textColor) {
        if (textView == null) {
            return;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        textView.setTextColor(textColor);
    }

    private static void applyIconSize(@NonNull View view, @Nullable Integer sizePx) {
        if (sizePx == null || sizePx <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            lp.width = sizePx;
            lp.height = sizePx;
            view.setLayoutParams(lp);
        }
    }

    private static void applyTopMargin(@Nullable View view, int topMarginPx) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (!(raw instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) raw;
        if (lp.topMargin != topMarginPx) {
            lp.topMargin = topMarginPx;
            view.setLayoutParams(lp);
        }
    }

    private static boolean isVisible(@Nullable View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    private static void setGone(@Nullable View v) {
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }
}
