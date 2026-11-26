package io.coderf.arklab.common.widget.gallery;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.gallery.inter.PreviewItemActionHost;

/**
 * 绑定大图预览 item 右下角操作区（下载 / 查看全部）。
 * <p>
 * 约定 item 根布局内存在（可选）：
 * <ul>
 *   <li>{@code R.id.ll_preview_item_actions} 操作容器</li>
 *   <li>{@code R.id.iv_preview_download}</li>
 *   <li>{@code R.id.iv_preview_view_all}</li>
 * </ul>
 * 推荐直接 {@code <include layout="@layout/include_preview_item_actions"/>}。
 * 新 item 页面接入：include 布局 + 在 onBind 调用
 * {@link #bind(View, PreviewItemActionHost, int)} 即可。
 *
 * @author fz
 * @since 1.0
 * @created 2026/11/26
 */
public final class PreviewItemActionBinder {

    private PreviewItemActionBinder() {
    }

    /**
     * 按 Host 契约绑定当前 item 的操作区。
     *
     * @param itemRoot        item 根 View（Binding.getRoot()）
     * @param host            Dialog 等实现的契约，null 则隐藏全部操作
     * @param adapterPosition 当前数据下标
     */
    public static void bind(@Nullable View itemRoot,
                            @Nullable PreviewItemActionHost host,
                            int adapterPosition) {
        if (itemRoot == null) {
            return;
        }
        View actions = itemRoot.findViewById(R.id.ll_preview_item_actions);
        ShapeableImageView ivDownload = itemRoot.findViewById(R.id.iv_preview_download);
        ShapeableImageView ivViewAll = itemRoot.findViewById(R.id.iv_preview_view_all);

        if (host == null) {
            setGone(actions);
            setGone(ivDownload);
            setGone(ivViewAll);
            return;
        }

        boolean showDownload = host.isDownloadActionVisible() && host.canSaveCurrentMedia();
        boolean showViewAll = host.isViewAllActionVisible();

        if (actions != null) {
            actions.setVisibility(showDownload || showViewAll ? View.VISIBLE : View.GONE);
            applyBottomInset(actions, host.getActionBottomInsetPx());
        }

        bindActionButton(
                ivDownload,
                showDownload,
                host.getDownloadActionIcon(),
                host.getActionIconSizePx(),
                host.isActionCircleBackgroundEnabled(),
                host.getActionCircleBackground(),
                showDownload ? v -> host.onDownloadAction(adapterPosition) : null);

        bindActionButton(
                ivViewAll,
                showViewAll,
                host.getViewAllActionIcon(),
                host.getActionIconSizePx(),
                host.isActionCircleBackgroundEnabled(),
                host.getActionCircleBackground(),
                showViewAll ? v -> host.onViewAllAction(adapterPosition) : null);
    }

    private static void bindActionButton(@Nullable ShapeableImageView view,
                                         boolean visible,
                                         @Nullable Drawable icon,
                                         @Nullable Integer sizePx,
                                         boolean circleBg,
                                         @Nullable Drawable circleDrawable,
                                         @Nullable View.OnClickListener click) {
        if (view == null) {
            return;
        }
        if (!visible) {
            view.setVisibility(View.GONE);
            view.setOnClickListener(null);
            return;
        }
        view.setVisibility(View.VISIBLE);
        if (icon != null) {
            view.setImageDrawable(icon);
        }
        if (sizePx != null && sizePx > 0) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null) {
                lp.width = sizePx;
                lp.height = sizePx;
                view.setLayoutParams(lp);
            }
        }
        applyCircleBackground(view, circleBg, circleDrawable);
        view.setOnClickListener(click);
    }

    private static void applyCircleBackground(@NonNull ShapeableImageView view,
                                              boolean enabled,
                                              @Nullable Drawable custom) {
        if (!enabled) {
            view.setBackground(null);
            view.setClipToOutline(false);
            return;
        }
        if (custom != null && custom.getConstantState() != null) {
            view.setBackground(custom.getConstantState().newDrawable().mutate());
        } else if (custom != null) {
            view.setBackground(custom.mutate());
        } else {
            view.setBackgroundResource(R.drawable.bg_preview_nav_circle);
        }
        // setClipToOutline 自 API 21 可用；勿用 XML android:clipToOutline（API 31）
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        view.setClipToOutline(true);
    }

    private static void applyBottomInset(@NonNull View actions, int extraBottomPx) {
        ViewGroup.LayoutParams raw = actions.getLayoutParams();
        if (!(raw instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) raw;
        // 布局默认 marginBottom=24dp，额外叠加系统栏 inset
        Object tag = actions.getTag(R.id.ll_preview_item_actions);
        int baseBottom;
        if (tag instanceof Integer) {
            baseBottom = (Integer) tag;
        } else {
            baseBottom = lp.bottomMargin;
            actions.setTag(R.id.ll_preview_item_actions, baseBottom);
        }
        int target = baseBottom + Math.max(0, extraBottomPx);
        if (lp.bottomMargin != target) {
            lp.bottomMargin = target;
            actions.setLayoutParams(lp);
        }
    }

    private static void setGone(@Nullable View v) {
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }
}
