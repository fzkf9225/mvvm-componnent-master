package pers.fz.mvvm.listener;

import android.app.Dialog;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;

/**
 * Created by CherishTang on 2019/12/03.
 * 监听事件
 */

public interface OnMenuItemClickListener<T> {
    void onMenuItemClick(Dialog dialog, BaseRecyclerViewAdapter<T,?> baseRecyclerViewAdapter, int position);
}
