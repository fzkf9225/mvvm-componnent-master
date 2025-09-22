package com.casic.otitan.common.listener;

import androidx.databinding.ViewDataBinding;

import com.casic.otitan.common.base.BasePagingAdapter;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface CustomHomeMenuAdapterCallback {
    <T, VDB extends ViewDataBinding> BasePagingAdapter<T , VDB> getAdapter();
}
