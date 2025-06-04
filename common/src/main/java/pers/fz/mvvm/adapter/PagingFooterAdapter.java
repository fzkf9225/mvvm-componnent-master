package pers.fz.mvvm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;

import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.PagingFooterBinding;

/**
 * Created by fz on 2023/11/30 10:47
 * describe :
 */
public class PagingFooterAdapter extends LoadStateAdapter<BaseViewHolder<PagingFooterBinding>> {
    private final Runnable retry;

    public PagingFooterAdapter(Runnable retry) {
        this.retry = retry;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<PagingFooterBinding> holder, @NonNull LoadState loadState) {

        if (loadState instanceof LoadState.Error) {
            holder.getBinding().progressBar.setVisibility(View.GONE);
            holder.getBinding().progressBar.hide();
            holder.getBinding().retryButton.setVisibility(View.VISIBLE);
            holder.getBinding().setMessage("加载失败，点击重试");
            holder.getBinding().retryButton.setOnClickListener(v -> {
                holder.getBinding().progressBar.setVisibility(View.VISIBLE);
                holder.getBinding().retryButton.setVisibility(View.VISIBLE);
                holder.getBinding().progressBar.show();
                holder.getBinding().setMessage("正在加载...");
                retry.run();
            });
        } else if (loadState instanceof LoadState.Loading) {
            holder.getBinding().progressBar.setVisibility(View.VISIBLE);
            holder.getBinding().retryButton.setVisibility(View.VISIBLE);
            holder.getBinding().progressBar.show();
            holder.getBinding().setMessage("正在加载...");
        } else if (loadState instanceof LoadState.NotLoading) {
            if (loadState.getEndOfPaginationReached()) {
                holder.getBinding().progressBar.setVisibility(View.GONE);
                holder.getBinding().retryButton.setVisibility(View.VISIBLE);
                holder.getBinding().setMessage("暂无更多数据");
            } else {
                holder.getBinding().progressBar.setVisibility(View.GONE);
                holder.getBinding().retryButton.setVisibility(View.GONE);
                holder.getBinding().progressBar.hide();
            }
        } else {
            holder.getBinding().setMessage("暂无更多数据...");
            holder.getBinding().progressBar.setVisibility(View.GONE);
            holder.getBinding().retryButton.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public BaseViewHolder<PagingFooterBinding> onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LoadState loadState) {
        return new BaseViewHolder<>(PagingFooterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public boolean displayLoadStateAsItem(@NonNull LoadState loadState) {
        return loadState instanceof LoadState.Loading ||
                loadState instanceof LoadState.Error ||
                (loadState instanceof LoadState.NotLoading && loadState.getEndOfPaginationReached());
    }

}