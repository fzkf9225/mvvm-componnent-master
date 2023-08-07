package pers.fz.mvvm.base;


import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * Created by fz on 2017/12/28.
 */

public class BaseViewHolder<VDB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private VDB binding;
    private ViewDataBinding headerBinding;
    private boolean isHeaderViewHolder = false;

    public BaseViewHolder(@NotNull VDB binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public BaseViewHolder(@NotNull ViewDataBinding headerBinding, boolean isHeaderViewHolder) {
        super(headerBinding.getRoot());
        this.isHeaderViewHolder = isHeaderViewHolder;
        this.headerBinding = headerBinding;
    }

    public VDB getBinding() {
        return binding;
    }

    public ViewDataBinding getHeaderBinding() {
        return headerBinding;
    }
}
