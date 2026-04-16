package io.coderf.arklab.demo.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import io.coderf.arklab.demo.BR;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.databinding.RoomPagingItemBinding;

import java.util.Objects;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;

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
