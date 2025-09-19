package pers.fz.mvvm.utils.common;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

/**
 * created by fz on 2025/5/30 13:43
 * describe:
 */
public class MenuUtil {

    public static void setMenuItemTextColor(Context context, MenuItem menuItem, @ColorRes int color) {
        if (menuItem == null) {
            return;
        }
        // 设置菜单项文本颜色
        SpannableString spannableString = new SpannableString(menuItem.getTitle());
        spannableString.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(context, color)),
                0,
                spannableString.length(),
                0
        );
        menuItem.setTitle(spannableString);
    }

    public static void setMenuItemTextColor(MenuItem menuItem, @ColorInt int color) {
        if (menuItem == null) {
            return;
        }
        // 设置菜单项文本颜色
        SpannableString spannableString = new SpannableString(menuItem.getTitle());
        spannableString.setSpan(
                new ForegroundColorSpan(color),
                0,
                spannableString.length(),
                0
        );
        menuItem.setTitle(spannableString);
    }
}

