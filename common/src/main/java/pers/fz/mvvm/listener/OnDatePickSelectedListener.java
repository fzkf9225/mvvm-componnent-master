package pers.fz.mvvm.listener;

import android.app.Dialog;

/**
 * Created by fz on 2024/12/2 10:02
 * describe：监听事件
 */

public interface OnDatePickSelectedListener {
    void onDatePickSelected(Dialog dialog,int year,int month,int day,int hour,int minute,int second);
}
