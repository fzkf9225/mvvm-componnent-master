package io.coderf.arklab.common.utils.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

/**
 * Toolbar / Menu 菜单项常用操作工具类。
 * <p>
 * 封装显示隐藏、启用禁用、图标与标题、文字颜色等常见能力，便于在 Activity / Fragment 中统一处理菜单。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/2 10:20
 */
public class MenuUtil {

    /**
     * 根据菜单项 id 从 {@link Menu} 中查找对应项。
     *
     * @param menu   菜单对象，可为 null
     * @param itemId 菜单项 id（R.id.xxx）
     * @return 找到的菜单项；menu 为 null 或未找到时返回 null
     */
    @Nullable
    public static MenuItem findMenuItem(@Nullable Menu menu, int itemId) {
        if (menu == null) {
            return null;
        }
        return menu.findItem(itemId);
    }

    /**
     * 根据菜单项 id 从 {@link Toolbar} 中查找对应项。
     *
     * @param toolbar Toolbar，可为 null
     * @param itemId  菜单项 id（R.id.xxx）
     * @return 找到的菜单项；toolbar 或其 menu 为 null 或未找到时返回 null
     */
    @Nullable
    public static MenuItem findMenuItem(@Nullable Toolbar toolbar, int itemId) {
        if (toolbar == null) {
            return null;
        }
        return findMenuItem(toolbar.getMenu(), itemId);
    }

    // endregion

    // region 显示 / 隐藏

    /**
     * 设置菜单项是否显示。
     *
     * @param menuItem 菜单项
     * @param visible  true 显示，false 隐藏
     */
    public static void setMenuItemVisible(@Nullable MenuItem menuItem, boolean visible) {
        if (menuItem != null) {
            menuItem.setVisible(visible);
        }
    }

    /**
     * 通过菜单 id 设置菜单项是否显示。
     */
    public static void setMenuItemVisible(@Nullable Menu menu, int itemId, boolean visible) {
        setMenuItemVisible(findMenuItem(menu, itemId), visible);
    }

    /**
     * 通过 Toolbar 与菜单 id 设置菜单项是否显示。
     */
    public static void setMenuItemVisible(@Nullable Toolbar toolbar, int itemId, boolean visible) {
        setMenuItemVisible(findMenuItem(toolbar, itemId), visible);
    }

    /**
     * 显示菜单项。
     */
    public static void showMenuItem(@Nullable MenuItem menuItem) {
        setMenuItemVisible(menuItem, true);
    }

    /**
     * 隐藏菜单项。
     */
    public static void hideMenuItem(@Nullable MenuItem menuItem) {
        setMenuItemVisible(menuItem, false);
    }

    /**
     * 切换菜单项显示状态；当前为显示则隐藏，反之亦然。
     *
     * @return 切换后的显示状态；menuItem 为 null 时返回 false
     */
    public static boolean toggleMenuItemVisible(@Nullable MenuItem menuItem) {
        if (menuItem == null) {
            return false;
        }
        boolean visible = !menuItem.isVisible();
        menuItem.setVisible(visible);
        return visible;
    }

    // endregion

    // region 启用 / 禁用

    /**
     * 设置菜单项是否可点击。
     *
     * @param menuItem 菜单项
     * @param enabled  true 可点击，false 禁用（置灰且不可响应）
     */
    public static void setMenuItemEnabled(@Nullable MenuItem menuItem, boolean enabled) {
        if (menuItem != null) {
            menuItem.setEnabled(enabled);
        }
    }

    /**
     * 通过菜单 id 设置菜单项是否可点击。
     */
    public static void setMenuItemEnabled(@Nullable Menu menu, int itemId, boolean enabled) {
        setMenuItemEnabled(findMenuItem(menu, itemId), enabled);
    }

    /**
     * 通过 Toolbar 与菜单 id 设置菜单项是否可点击。
     */
    public static void setMenuItemEnabled(@Nullable Toolbar toolbar, int itemId, boolean enabled) {
        setMenuItemEnabled(findMenuItem(toolbar, itemId), enabled);
    }

    // endregion

    // region 标题

    /**
     * 设置菜单项标题文本。
     */
    public static void setMenuItemTitle(@Nullable MenuItem menuItem, @Nullable CharSequence title) {
        if (menuItem != null) {
            menuItem.setTitle(title);
        }
    }

    /**
     * 通过字符串资源设置菜单项标题。
     */
    public static void setMenuItemTitle(@Nullable MenuItem menuItem, @StringRes int titleRes) {
        if (menuItem != null) {
            menuItem.setTitle(titleRes);
        }
    }

    // endregion

    // region 文字颜色

    /**
     * 通过颜色资源设置菜单项文字颜色。
     * <p>
     * 内部使用 {@link SpannableString} 包裹标题，适用于不支持直接设置文字颜色的场景。
     */
    public static void setMenuItemTextColor(@NonNull Context context, @Nullable MenuItem menuItem, @ColorRes int color) {
        if (menuItem == null) {
            return;
        }
        setMenuItemTextColor(menuItem, ContextCompat.getColor(context, color));
    }

    /**
     * 通过颜色值设置菜单项文字颜色。
     */
    public static void setMenuItemTextColor(@Nullable MenuItem menuItem, @ColorInt int color) {
        if (menuItem == null) {
            return;
        }
        CharSequence title = menuItem.getTitle();
        if (title == null) {
            return;
        }
        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(
                new ForegroundColorSpan(color),
                0,
                spannableString.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        menuItem.setTitle(spannableString);
    }

    // endregion

    // region 图标

    /**
     * 设置菜单项图标。
     */
    public static void setMenuItemIcon(@Nullable MenuItem menuItem, @DrawableRes int iconRes) {
        if (menuItem != null) {
            menuItem.setIcon(iconRes);
        }
    }

    /**
     * 清除菜单项图标。
     */
    public static void clearMenuItemIcon(@Nullable MenuItem menuItem) {
        if (menuItem != null) {
            menuItem.setIcon(null);
        }
    }

    /**
     * 通过颜色资源设置菜单项图标着色（tint）。
     */
    public static void setMenuItemIconTint(@NonNull Context context, @Nullable MenuItem menuItem, @ColorRes int colorRes) {
        setMenuItemIconTint(menuItem, ContextCompat.getColor(context, colorRes));
    }

    /**
     * 通过颜色值设置菜单项图标着色（tint）。
     */
    public static void setMenuItemIconTint(@Nullable MenuItem menuItem, @ColorInt int color) {
        if (menuItem == null) {
            return;
        }
        MenuItemCompat.setIconTintList(menuItem, ColorStateList.valueOf(color));
    }

    /**
     * 设置菜单项图标着色模式，常用于配合 {@link #setMenuItemIconTint(MenuItem, int)} 使用。
     *
     * @param tintMode 如 {@link PorterDuff.Mode#SRC_IN}、{@link PorterDuff.Mode#MULTIPLY}
     */
    public static void setMenuItemIconTintMode(@Nullable MenuItem menuItem, @NonNull PorterDuff.Mode tintMode) {
        if (menuItem != null) {
            MenuItemCompat.setIconTintMode(menuItem, tintMode);
        }
    }

    /**
     * 清除菜单项图标着色，恢复为图标原始颜色。
     */
    public static void clearMenuItemIconTint(@Nullable MenuItem menuItem) {
        if (menuItem != null) {
            MenuItemCompat.setIconTintList(menuItem, null);
        }
    }

    // endregion

    // region 展示位置

    /**
     * 设置菜单项在 Toolbar 中的展示方式。
     *
     * @param showAsAction 如 {@link MenuItem#SHOW_AS_ACTION_ALWAYS}、
     *                     {@link MenuItem#SHOW_AS_ACTION_IF_ROOM}、
     *                     {@link MenuItem#SHOW_AS_ACTION_NEVER}
     */
    public static void setShowAsAction(@Nullable MenuItem menuItem, int showAsAction) {
        if (menuItem != null) {
            menuItem.setShowAsAction(showAsAction);
        }
    }

    /**
     * 将菜单项固定显示在 Toolbar 上（始终作为 Action 展示）。
     */
    public static void showAsActionAlways(@Nullable MenuItem menuItem) {
        setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    /**
     * 将菜单项放入溢出菜单（三个点更多菜单）。
     */
    public static void showAsActionNever(@Nullable MenuItem menuItem) {
        setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_NEVER);
    }

    // endregion

    // region 可选中

    /**
     * 设置菜单项是否支持选中状态（如勾选样式）。
     */
    public static void setMenuItemCheckable(@Nullable MenuItem menuItem, boolean checkable) {
        if (menuItem != null) {
            menuItem.setCheckable(checkable);
        }
    }

    /**
     * 设置菜单项选中状态，常用于开关类菜单。
     */
    public static void setMenuItemChecked(@Nullable MenuItem menuItem, boolean checked) {
        if (menuItem != null) {
            menuItem.setChecked(checked);
        }
    }

    /**
     * 切换菜单项选中状态。
     *
     * @return 切换后的选中状态；menuItem 为 null 时返回 false
     */
    public static boolean toggleMenuItemChecked(@Nullable MenuItem menuItem) {
        if (menuItem == null) {
            return false;
        }
        boolean checked = !menuItem.isChecked();
        menuItem.setChecked(checked);
        return checked;
    }
}
