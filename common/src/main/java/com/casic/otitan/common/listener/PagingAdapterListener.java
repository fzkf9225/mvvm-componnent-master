package com.casic.otitan.common.listener;

import android.view.View;

/**
 * Created by fz on 2023/12/4 9:06
 * describe :PagingAdapter监听器
 */
public interface PagingAdapterListener<T> {

    /**
     * 点击事件
     * @param view 视图
     * @param item 当前列表数据
     * @param position 当前索引
     */
    void onItemClick(View view, T item, int position);
    /**
     * 长按事件
     * @param view 视图
     * @param item 当前列表数据
     * @param position 当前索引
     */
    void onItemLongClick(View view, T item, int position);
}
