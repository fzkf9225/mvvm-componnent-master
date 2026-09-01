package io.coderf.arklab.common.listener;

import androidx.databinding.ViewDataBinding;

import io.coderf.arklab.common.base.BasePagingAdapter;

/**
 * created fz on 2024/10/22 19:56
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public interface CustomGridMenuAdapterCallback {
    <T, VDB extends ViewDataBinding> BasePagingAdapter<T, VDB> getAdapter();
}
