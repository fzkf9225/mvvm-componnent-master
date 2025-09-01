package pers.fz.mvvm.listener;

import androidx.databinding.ViewDataBinding;

import pers.fz.mvvm.base.BasePagingAdapter;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface HomeMenuAdapterListener {
    <T, VDB extends ViewDataBinding> BasePagingAdapter<T , VDB> getAdapter();
}
