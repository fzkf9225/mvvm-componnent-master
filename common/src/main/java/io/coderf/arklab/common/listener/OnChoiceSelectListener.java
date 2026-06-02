package io.coderf.arklab.common.listener;

import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.widget.dialog.ChoiceSelectDialog;

/**
 * 单选/多选列表弹框确认回调
 */
public interface OnChoiceSelectListener<T extends PopupWindowBean> {

    /**
     * 点击确认：单选返回 0~1 项，多选返回全部选中项
     */
    void onConfirm(ChoiceSelectDialog<T> dialog, List<T> selectedItems);

    default void onCancel(ChoiceSelectDialog<T> dialog) {
    }
}
