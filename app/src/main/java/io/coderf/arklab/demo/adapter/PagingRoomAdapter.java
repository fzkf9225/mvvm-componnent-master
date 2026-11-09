package io.coderf.arklab.demo.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.BR;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.databinding.RoomPagingItemBinding;

/**
 * PagingRoomAdapter 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/12/1 16:50
 * @updated 2026/11/9
 */
public class PagingRoomAdapter extends BasePagingAdapter<Person, RoomPagingItemBinding> {
    public PagingRoomAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
                return Objects.equals(oldItem.getId(), newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
                // 按列表展示字段比较；勿用引用相等，否则编辑后刷新可能误判未变
                return Objects.equals(oldItem.getName(), newItem.getName())
                        && Objects.equals(oldItem.getSex(), newItem.getSex())
                        && Objects.equals(oldItem.getAge(), newItem.getAge())
                        && Objects.equals(oldItem.getBirthday(), newItem.getBirthday())
                        && Objects.equals(oldItem.getHeight(), newItem.getHeight())
                        && Objects.equals(oldItem.getWeight(), newItem.getWeight())
                        && Objects.equals(oldItem.getEmail(), newItem.getEmail())
                        && Objects.equals(oldItem.getTel(), newItem.getTel())
                        && Objects.equals(oldItem.getMobile(), newItem.getMobile())
                        && Objects.equals(oldItem.getEducationLevel(), newItem.getEducationLevel());
            }
        });
    }

    @Override
    public void onBindHolder(BaseViewHolder<RoomPagingItemBinding> holder, Person item, int pos) {
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.room_paging_item;
    }
}
