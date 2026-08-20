package io.coderf.arklab.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.databinding.PagingFooterBinding;

/**
 * Paging3 列表底部加载状态 Footer（加载中 / 失败重试 / 暂无更多）。
 *
 * <p><b>注意：</b>当主列表条目数为 0 时，不会将 Footer 作为 RecyclerView 的 item 展示。
 * 否则在空库场景下 Footer 会成为唯一一行并出现在列表顶部，与 {@link io.coderf.arklab.common.widget.empty.EmptyLayout}
 * 「暂无数据」占位重叠。</p>
 *
 * @see io.coderf.arklab.common.base.BaseSmartPagingFragment
 */
public class PagingFooterAdapter extends LoadStateAdapter<BaseViewHolder<PagingFooterBinding>> {
    private final Runnable retry;
    @Nullable
    private final BasePagingAdapter<?, ?> contentAdapter;

    /**
     * @param retry 加载失败时的重试回调，一般为 {@code adapter::retry}
     */
    public PagingFooterAdapter(Runnable retry) {
        this(retry, null);
    }

    /**
     * @param retry           重试回调
     * @param contentAdapter  主列表 PagingAdapter，用于判断是否有数据；为 null 时不做空列表判断（兼容旧用法）
     */
    public PagingFooterAdapter(Runnable retry, @Nullable BasePagingAdapter<?, ?> contentAdapter) {
        this.retry = retry;
        this.contentAdapter = contentAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<PagingFooterBinding> holder, @NonNull LoadState loadState) {

        if (loadState instanceof LoadState.Error) {
            holder.getBinding().progressBar.setVisibility(View.GONE);
            holder.getBinding().progressBar.hide();
            holder.getBinding().retryButton.setVisibility(View.VISIBLE);
            holder.getBinding().retryButton.setClickable(true);
            holder.getBinding().retryButton.setFocusable(true);
            holder.getBinding().setMessage("加载失败，点击重试");
            holder.getBinding().retryButton.setOnClickListener(v -> {
                holder.getBinding().retryButton.setClickable(false);
                holder.getBinding().retryButton.setFocusable(false);
                holder.getBinding().progressBar.setVisibility(View.VISIBLE);
                holder.getBinding().progressBar.show();
                holder.getBinding().setMessage("正在加载...");
                retry.run();
            });
        } else if (loadState instanceof LoadState.Loading) {
            holder.getBinding().retryButton.setVisibility(View.VISIBLE);
            holder.getBinding().retryButton.setClickable(false);
            holder.getBinding().retryButton.setFocusable(false);
            holder.getBinding().retryButton.setOnClickListener(null);
            holder.getBinding().progressBar.setVisibility(View.VISIBLE);
            holder.getBinding().progressBar.show();
            holder.getBinding().setMessage("正在加载...");
        } else if (loadState instanceof LoadState.NotLoading) {
            if (loadState.getEndOfPaginationReached()) {
                holder.getBinding().progressBar.setVisibility(View.GONE);
                holder.getBinding().retryButton.setVisibility(View.VISIBLE);
                holder.getBinding().retryButton.setClickable(false);
                holder.getBinding().retryButton.setFocusable(false);
                holder.getBinding().retryButton.setOnClickListener(null);
                holder.getBinding().setMessage("暂无更多数据");
                holder.getBinding().progressBar.hide();
            } else {
                holder.getBinding().progressBar.setVisibility(View.GONE);
                holder.getBinding().retryButton.setVisibility(View.GONE);
                holder.getBinding().progressBar.hide();
            }
        } else {
            holder.getBinding().setMessage("暂无更多数据...");
            holder.getBinding().progressBar.setVisibility(View.GONE);
            holder.getBinding().retryButton.setVisibility(View.GONE);
            holder.getBinding().progressBar.hide();
        }
    }

    @NonNull
    @Override
    public BaseViewHolder<PagingFooterBinding> onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LoadState loadState) {
        return new BaseViewHolder<>(PagingFooterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
}
