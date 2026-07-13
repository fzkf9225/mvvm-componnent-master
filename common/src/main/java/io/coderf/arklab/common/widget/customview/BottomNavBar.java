package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;

/**
 * 底部导航栏封装：基于 Material {@link BottomNavigationView}，支持代码配置菜单项与选中监听。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:20
 */
public class BottomNavBar extends BottomNavigationView {

    private OnNavItemSelectedListener navItemSelectedListener;
    private final List<NavItem> navItems = new ArrayList<>();

    public BottomNavBar(@NonNull Context context) {
        super(context);
        initDefaults();
    }

    public BottomNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initDefaults();
    }

    public BottomNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults();
    }

    private void initDefaults() {
        setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        setOnItemSelectedListener(item -> {
            if (navItemSelectedListener != null) {
                return navItemSelectedListener.onNavItemSelected(item.getItemId());
            }
            return true;
        });
    }

    /**
     * 设置导航项并刷新 Menu；会清空原有菜单。
     */
    public BottomNavBar setNavItems(@NonNull List<NavItem> items) {
        navItems.clear();
        navItems.addAll(items);
        getMenu().clear();
        for (NavItem navItem : items) {
            MenuItem menuItem = getMenu().add(Menu.NONE, navItem.itemId, Menu.NONE, navItem.title);
            menuItem.setIcon(navItem.iconRes);
        }
        return this;
    }

    public BottomNavBar setSelectedItem(@IdRes int itemId) {
        setSelectedItemId(itemId);
        return this;
    }

    public BottomNavBar setNavItemColors(@ColorInt int selectedColor, @ColorInt int unselectedColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{selectedColor, unselectedColor};
        setItemIconTintList(new ColorStateList(states, colors));
        setItemTextColor(new ColorStateList(states, colors));
        return this;
    }

    public BottomNavBar setOnNavItemSelectedListener(@Nullable OnNavItemSelectedListener listener) {
        this.navItemSelectedListener = listener;
        return this;
    }

    @NonNull
    public List<NavItem> getNavItems() {
        return new ArrayList<>(navItems);
    }

    /**
     * 底部导航菜单项配置。
     */
    public static class NavItem {
        public final @IdRes int itemId;
        public final @DrawableRes int iconRes;
        @NonNull
        public final String title;

        public NavItem(@IdRes int itemId, @DrawableRes int iconRes, @NonNull String title) {
            this.itemId = itemId;
            this.iconRes = iconRes;
            this.title = title;
        }
    }

    public interface OnNavItemSelectedListener {
        /**
         * @return true 表示选中生效
         */
        boolean onNavItemSelected(@IdRes int itemId);
    }
}
