package pers.fz.mvvm.util.log;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.wight.dialog.DialogHelper;

/**
 * Created by fz on 2017/5/23.
 * Toast弹框提示
 */

public class ToastUtils {
    public static void showShort(Context context, String str) {
        try {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showLong(Context context, String str) {
        try {
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showMessageDialog(Context context, String message) {
        try {
            DialogHelper.getMessageDialog(context, message).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
