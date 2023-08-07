package pers.fz.mvvm.wight.multi_image_selector.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

import android.view.WindowManager;

/**
 * 屏幕工具
 * Created by nereo on 15/11/19.
 * Updated by nereo on 2016/1/19.
 */
public class ScreenUtils {

    public static Point getScreenSize(Context context) {
        DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        Point out = new Point();
        out.set(appDisplayMetrics.widthPixels, appDisplayMetrics.heightPixels);
        return out;
    }
}
