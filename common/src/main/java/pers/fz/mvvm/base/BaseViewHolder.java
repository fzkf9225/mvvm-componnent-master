package pers.fz.mvvm.base;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * Created by fz on 2017/12/28.
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

    public <T> BaseViewHolder(@NotNull VDB binding,BaseRecyclerViewAdapter<T,VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        itemView.setOnClickListener(v -> {
            if (adapter.mOnItemClickListener != null) {
                adapter.mOnItemClickListener.onItemClick(v, adapter.getRealPosition(this));
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.mOnItemLongClickListener != null) {
                adapter.mOnItemLongClickListener.onItemLongClick(v, adapter.getRealPosition(this));
                return true;
            }
            return false;
        });
    }

    public <T> BaseViewHolder(@NonNull  VDB binding,BasePagingAdapter<T,VDB> adapter) {
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
