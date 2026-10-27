package io.coderf.arklab.common.base;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * updated by fz on 2024/12/28.
 * RecyclerView的通用ViewHolder，此次修改主要把单击和长按事件放在了ViewHolder中，减少滑动带来的资源消耗
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/27 移除 PagingLive 专用点击分支
 */
public class BaseViewHolder<VDB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private VDB binding;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public BaseViewHolder(@NotNull VDB binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public <T, HVDB extends ViewDataBinding> BaseViewHolder(@NonNull View headerView, boolean isClickable, BaseRecyclerViewAdapter<T, HVDB> adapter) {
        super(headerView);
        if (!isClickable) {
            return;
        }
        itemView.setOnClickListener(v -> {
            if (adapter.onHeaderViewClickListener != null) {
                adapter.onHeaderViewClickListener.onHeaderViewClick(v);
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.onHeaderViewClickListener != null) {
                adapter.onHeaderViewClickListener.onHeaderViewLongClick(v);
                return true;
            }
            return false;
        });
    }

    public <T, HVDB extends ViewDataBinding> BaseViewHolder(@NotNull VDB binding, boolean isClickable, BaseRecyclerViewAdapter<T, HVDB> adapter) {
        this(binding.getRoot(), isClickable, adapter);
        this.binding = binding;
    }

    public <T> BaseViewHolder(@NotNull VDB binding, BaseRecyclerViewAdapter<T, VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        itemView.setOnClickListener(v -> {
            int absPos = getAbsoluteAdapterPosition();
            if (absPos == RecyclerView.NO_POSITION) {
                return;
            }
            int pos = adapter.hasHeaderView() ? (absPos - 1) : absPos;
            if (adapter.mOnItemClickListener != null) {
                adapter.mOnItemClickListener.onItemClick(v, pos);
            }
        });
        itemView.setOnLongClickListener(v -> {
            int absPos = getAbsoluteAdapterPosition();
            if (absPos == RecyclerView.NO_POSITION) {
                return true;
            }
            int pos = adapter.hasHeaderView() ? (absPos - 1) : absPos;
            if (adapter.mOnItemLongClickListener != null) {
                adapter.mOnItemLongClickListener.onItemLongClick(v, pos);
                return true;
            }
            return false;
        });
    }

    public <T> BaseViewHolder(@NonNull VDB binding, BasePagingAdapter<T, VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        // ConcatAdapter 下使用 bindingAdapterPosition，自动忽略 Header/Footer 段偏移
        itemView.setOnClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                int pagingPos = getBindingAdapterPosition();
                if (pagingPos == RecyclerView.NO_POSITION) {
                    return;
                }
                T item = adapter.getAdapterItem(pagingPos);
                if (item != null) {
                    adapter.onPagingAdapterListener.onItemClick(v, item, pagingPos);
                }
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                int pagingPos = getBindingAdapterPosition();
                if (pagingPos == RecyclerView.NO_POSITION) {
                    return true;
                }
                T item = adapter.getAdapterItem(pagingPos);
                if (item != null) {
                    adapter.onPagingAdapterListener.onItemLongClick(v, item, pagingPos);
                }
                return true;
            }
            return false;
        });
    }

    public VDB getBinding() {
        return binding;
    }

}
