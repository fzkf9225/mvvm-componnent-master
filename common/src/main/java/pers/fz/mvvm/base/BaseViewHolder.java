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


    public VDB getBinding() {
        return binding;
    }

}
