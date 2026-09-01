package io.coderf.arklab.common.listener;

import android.app.Dialog;

/**
 * 监听事件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/12/2 10:02
 */

public interface OnDatePickSelectedListener {
    void onDatePickSelected(Dialog dialog,int year,int month,int day,int hour,int minute,int second);
}
