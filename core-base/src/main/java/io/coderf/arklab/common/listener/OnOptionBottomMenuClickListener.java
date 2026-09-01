package io.coderf.arklab.common.listener;

import android.app.Dialog;

import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;


/**
 * 监听事件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2019/10/31
 */
public interface OnOptionBottomMenuClickListener<T extends PopupWindowBean> {
    void onOptionBottomMenuClick(Dialog dialog, List<T> list, int pos);
}
