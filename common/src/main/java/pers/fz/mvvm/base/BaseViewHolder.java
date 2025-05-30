package pers.fz.mvvm.base;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * updated by fz on 2024/12/28.
 * describe:RecyclerView的通用ViewHolder，此次修改主要把单击和长按事件放在了ViewHolder中，减少滑动带来的资源消耗
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
        this(binding.getRoot(),isClickable,adapter);
        this.binding = binding;
    }

    public <T> BaseViewHolder(@NotNull VDB binding, BaseRecyclerViewAdapter<T, VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        itemView.setOnClickListener(v -> {
            if (adapter.mOnItemClickListener != null) {
                int realPos = adapter.hasHeaderView() ? (getAbsoluteAdapterPosition() - 1) : getAbsoluteAdapterPosition();
                adapter.mOnItemClickListener.onItemClick(v, realPos);
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.mOnItemLongClickListener != null) {
                int realPos = adapter.hasHeaderView() ? (getAbsoluteAdapterPosition() - 1) : getAbsoluteAdapterPosition();
                adapter.mOnItemLongClickListener.onItemLongClick(v, realPos);
                return true;
            }
            return false;
        });
    }

    public <T> BaseViewHolder(@NonNull VDB binding, BasePagingAdapter<T, VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        itemView.setOnClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                adapter.onPagingAdapterListener.onItemClick(v, adapter.getAdapterItem(getAbsoluteAdapterPosition()), getAbsoluteAdapterPosition());
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                adapter.onPagingAdapterListener.onItemLongClick(v, adapter.getAdapterItem(getAbsoluteAdapterPosition()), getAbsoluteAdapterPosition());
                return true;
            }
            return false;
        });
    }

    public VDB getBinding() {
        return binding;
    }

}
