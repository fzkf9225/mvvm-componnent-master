package pers.fz.mvvm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import pers.fz.mvvm.databinding.PagingFooterBinding;

/**
 * Created by fz on 2023/11/30 10:47
 * describe :
 */
public class PagingFooterAdapter extends LoadStateAdapter<PagingFooterAdapter.ViewHolder> {
    private final Runnable retry;

    public PagingFooterAdapter(Runnable retry) {
        this.retry = retry;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewDataBinding binding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, LoadState loadState) {
        PagingFooterBinding binding = (PagingFooterBinding) holder.binding;
        binding.setMessage("正在加载...");
        binding.progressBar.show();
        if (loadState instanceof LoadState.Error) {
            binding.progressBar.setVisibility(View.GONE);
            binding.progressBar.hide();
            binding.retryButton.setVisibility(View.VISIBLE);
            binding.setMessage("加载失败，点击重试");
            binding.retryButton.setOnClickListener(v -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.retryButton.setVisibility(View.VISIBLE);
                binding.progressBar.show();
                binding.setMessage("正在加载...");
                retry.run();
            });
        } else if (loadState instanceof LoadState.Loading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.retryButton.setVisibility(View.VISIBLE);
            binding.progressBar.show();
            binding.setMessage("正在加载...");
        } else if (loadState instanceof LoadState.NotLoading) {
            binding.progressBar.setVisibility(View.GONE);
            binding.retryButton.setVisibility(View.GONE);
            binding.progressBar.hide();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, LoadState loadState) {
        PagingFooterBinding binding = PagingFooterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }
}