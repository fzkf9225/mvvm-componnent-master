package com.casic.otitan.demo.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.casic.otitan.demo.BR;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.Person;
import com.casic.otitan.demo.databinding.RoomPagingItemBinding;

import java.util.Objects;

import com.casic.otitan.common.base.BasePagingAdapter;
import com.casic.otitan.common.base.BaseViewHolder;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingRoomAdapter extends BasePagingAdapter<Person, RoomPagingItemBinding> {
    public PagingRoomAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            public boolean areItemsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
                return Objects.equals(oldItem.getId(), newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
                return Objects.equals(oldItem, newItem);
            }
        });
    }

    @Override
    public void onBindHolder(BaseViewHolder<RoomPagingItemBinding> holder,Person item, int pos) {
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.room_paging_item;
    }
}
