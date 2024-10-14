package pers.fz.mvvm.base;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

import pers.fz.mvvm.bean.base.BasePagingBean;

/**
 * created by fz on 2024/10/14 13:31
 * describe:必须继承BasePagingBean类，不然会报错
 */
public class DefaultDiffCallback<T extends BasePagingBean> extends DiffUtil.ItemCallback<T>{
    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return Objects.equals(oldItem, newItem);
    }
}

