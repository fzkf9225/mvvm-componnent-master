package io.coderf.arklab.common.adapter;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.databinding.PagingFooterBinding;
import io.coderf.arklab.common.widget.empty.EmptyLayoutConfig;

/**
 * Paging3 列表底部加载状态 Footer（加载中 / 失败重试 / 暂无更多）。
 *
 * <p>样式由 {@link EmptyLayoutConfig} 统一控制（转圈尺寸、描边、颜色、文字字号与颜色）。
 * 未注入 config 时使用 {@link EmptyLayoutConfig#defaults()} 的推荐默认值。</p>
 *
 * <p><b>注意：</b>当主列表条目数为 0 时，不会将 Footer 作为 RecyclerView 的 item 展示。
 * 否则在空库场景下 Footer 会成为唯一一行并出现在列表顶部，与
 * {@link io.coderf.arklab.common.widget.empty.EmptyLayout}「暂无数据」占位重叠。</p>
 *
 * @see io.coderf.arklab.common.base.BaseSmartPagingFragment
 * @see EmptyLayoutConfig
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/20 接入 EmptyLayoutConfig Footer 样式配置
 */
public class PagingFooterAdapter extends LoadStateAdapter<BaseViewHolder<PagingFooterBinding>> {

    private final Runnable retry;
    @Nullable
    private final BasePagingAdapter<?, ?> contentAdapter;
    /** 进程级或页面级配置；为 null 时使用 defaults() */
    @Nullable
    private EmptyLayoutConfig config;

    /**
     * @param retry 加载失败时的重试回调，一般为 {@code adapter::retry}
     */
    public PagingFooterAdapter(Runnable retry) {
        this(retry, null, null);
    }

    /**
     * @param retry          重试回调
     * @param contentAdapter 主列表 PagingAdapter，用于判断是否有数据；为 null 时不做空列表判断（兼容旧用法）
     */
    public PagingFooterAdapter(Runnable retry, @Nullable BasePagingAdapter<?, ?> contentAdapter) {
        this(retry, contentAdapter, null);
    }

    /**
     * @param retry          重试回调
     * @param contentAdapter 主列表 PagingAdapter，用于判断是否有数据；为 null 时不做空列表判断
     * @param config         Footer / 空态外观配置；为 null 时使用 {@link EmptyLayoutConfig#defaults()}
     */
    public PagingFooterAdapter(
            Runnable retry,
            @Nullable BasePagingAdapter<?, ?> contentAdapter,
            @Nullable EmptyLayoutConfig config) {
        this.retry = retry;
        this.contentAdapter = contentAdapter;
        this.config = config;
    }

    /**
     * 运行时更新配置（例如宿主在 Config.init 之后才拿到全局配置）。
     * 已创建的 ViewHolder 会在下次 bind 时生效。
     */
    public void setConfig(@Nullable EmptyLayoutConfig config) {
        this.config = config;
    }

    @Nullable
    public EmptyLayoutConfig getConfig() {
        return config;
    }

    @NonNull
    private EmptyLayoutConfig resolveConfig() {
        return config != null ? config : EmptyLayoutConfig.defaults();
    }

    // -------------------------------------------------------------------------
    // LoadStateAdapter
    // -------------------------------------------------------------------------

    @Override
    public void onBindViewHolder(
            @NonNull BaseViewHolder<PagingFooterBinding> holder,
            @NonNull LoadState loadState) {

        PagingFooterBinding binding = holder.getBinding();
        // 每次 bind 都应用一次样式，保证运行时 setConfig 也能生效
        applyFooterStyle(binding);

        if (loadState instanceof LoadState.Error) {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressBar.hide();
            binding.retryButton.setVisibility(View.VISIBLE);
            binding.retryButton.setClickable(true);
            binding.retryButton.setFocusable(true);
            binding.setMessage("加载失败，点击重试");
            binding.retryButton.setOnClickListener(v -> {
                binding.retryButton.setClickable(false);
                binding.retryButton.setFocusable(false);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.show();
                binding.setMessage("正在加载...");
                retry.run();
            });
        } else if (loadState instanceof LoadState.Loading) {
            binding.retryButton.setVisibility(View.VISIBLE);
            binding.retryButton.setClickable(false);
            binding.retryButton.setFocusable(false);
            binding.retryButton.setOnClickListener(null);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.show();
            binding.setMessage("正在加载...");
        } else if (loadState instanceof LoadState.NotLoading) {
            if (loadState.getEndOfPaginationReached()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.retryButton.setVisibility(View.VISIBLE);
                binding.retryButton.setClickable(false);
                binding.retryButton.setFocusable(false);
                binding.retryButton.setOnClickListener(null);
                binding.setMessage("暂无更多数据");
                binding.progressBar.hide();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.retryButton.setVisibility(View.GONE);
                binding.progressBar.hide();
            }
        } else {
            binding.setMessage("暂无更多数据");
            binding.progressBar.setVisibility(View.GONE);
            binding.retryButton.setVisibility(View.GONE);
            binding.progressBar.hide();
        }
    }

    @NonNull
    @Override
    public BaseViewHolder<PagingFooterBinding> onCreateViewHolder(
            @NonNull ViewGroup parent,
            @NonNull LoadState loadState) {
        PagingFooterBinding binding = PagingFooterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        // 创建时先应用一次，避免首帧闪烁
        applyFooterStyle(binding);
        return new ViewHolder(binding);
    }

    /**
     * 仅在有列表数据时展示 Footer，保证「暂无更多」出现在列表末尾（需上拉才可见）。
     */
    @Override
    public boolean displayLoadStateAsItem(@NonNull LoadState loadState) {
        if (!hasContentItems()) {
            return false;
        }
        return loadState instanceof LoadState.Loading
                || loadState instanceof LoadState.Error
                || (loadState instanceof LoadState.NotLoading && loadState.getEndOfPaginationReached());
    }

    /** 主列表是否至少有一条数据（不含 Header / Footer） */
    private boolean hasContentItems() {
        if (contentAdapter == null) {
            return true;
        }
        return contentAdapter.getItemCount() > 0;
    }

    // -------------------------------------------------------------------------
    // 样式应用
    // -------------------------------------------------------------------------

    /**
     * 根据 {@link EmptyLayoutConfig} 设置转圈尺寸、描边、颜色以及文字字号与颜色。
     * 颜色为 0 时回退到 Material 主题属性，保证与系统暗色/品牌色一致。
     */
    private void applyFooterStyle(@NonNull PagingFooterBinding binding) {
        EmptyLayoutConfig cfg = resolveConfig();
        Resources res = binding.getRoot().getResources();
        float density = res.getDisplayMetrics().density;

        // --- 转圈 ---
        CircularProgressIndicator progress = binding.progressBar;
        int sizePx = Math.round(cfg.getFooterIndicatorSizeDp() * density);
        int thicknessPx = Math.round(cfg.getFooterIndicatorThicknessDp() * density);

        progress.setIndicatorSize(sizePx);
        progress.setTrackThickness(thicknessPx);

        // layout 宽高同步，避免 measure 时仍用 XML 的 24dp
        ViewGroup.LayoutParams lp = progress.getLayoutParams();
        if (lp != null) {
            lp.width = sizePx;
            lp.height = sizePx;
            progress.setLayoutParams(lp);
        }

        if (cfg.getFooterIndicatorColor() != 0) {
            progress.setIndicatorColor(cfg.getFooterIndicatorColor());
        } else {
            // 主题 colorPrimary
            int primary = MaterialColors.getColor(
                    progress, androidx.appcompat.R.attr.colorPrimary);
            progress.setIndicatorColor(primary);
        }

        if (cfg.getFooterTrackColor() != 0) {
            progress.setTrackColor(cfg.getFooterTrackColor());
        } else {
            // 主题 colorOutlineVariant
            int track = MaterialColors.getColor(
                    progress, com.google.android.material.R.attr.colorOutlineVariant);
            progress.setTrackColor(track);
        }

        // --- 文字 ---
        binding.retryButton.setTextSize(
                TypedValue.COMPLEX_UNIT_SP, cfg.getFooterTextSizeSp());

        if (cfg.getFooterTextColor() != 0) {
            binding.retryButton.setTextColor(cfg.getFooterTextColor());
        } else {
            // 主题 colorOnSurfaceVariant —— 辅助态，对比度更低
            int onVariant = MaterialColors.getColor(
                    binding.retryButton,
                    com.google.android.material.R.attr.colorOnSurfaceVariant);
            binding.retryButton.setTextColor(onVariant);
        }
    }

    // -------------------------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------------------------

    public static class ViewHolder extends BaseViewHolder<PagingFooterBinding> {

        public ViewHolder(@NonNull PagingFooterBinding binding) {
            super(binding);
        }
    }
}