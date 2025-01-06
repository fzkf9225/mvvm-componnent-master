package pers.fz.mvvm.base;

import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2021/2/7 14:13
 * describe:自定义toolbar样式baseActivity
 */
public abstract class BaseToolbarActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseActivity<VM, VDB> {

    /**
     * 给toolbar添加菜单
     *
     * @param menuStr  按钮文字
     * @param listener 按钮点击事件
     */
    protected void addMenu(String menuStr, Toolbar.OnMenuItemClickListener listener) {
        if (toolbarBind == null) {
            return;
        }
        toolbarBind.mainBar.inflateMenu(R.menu.menu_more);
        MenuItem menuItem = getToolbar().getMenu().findItem(R.id.toolbar_more);
        menuItem.setTitle(menuStr);
        toolbarBind.mainBar.setOnMenuItemClickListener(listener);
    }

    /**
     * 给toolbar添加菜单
     *
     * @param menuRes  按钮资源
     * @param listener 按钮点击事件
     */
    protected void addMenu(@MenuRes int menuRes, Toolbar.OnMenuItemClickListener listener) {
        if (toolbarBind == null) {
            return;
        }
        toolbarBind.mainBar.inflateMenu(menuRes);
        toolbarBind.mainBar.setOnMenuItemClickListener(listener);
    }

    protected void setMenuText(String menuText) {
        setMenuText(R.id.toolbar_more, menuText);
    }

    protected void setMenuText(@StringRes int strRes) {
        setMenuText(R.id.toolbar_more, strRes);
    }

    protected void setMenuText(@IdRes int idRes, String menuText) {
        MenuItem menuItem = getToolbar().getMenu().findItem(idRes);
        if (menuItem == null) {
            return;
        }
        menuItem.setTitle(menuText);
    }

    protected void setMenuText(@IdRes int idRes, @StringRes int strRes) {
        MenuItem menuItem = getToolbar().getMenu().findItem(idRes);
        if (menuItem == null) {
            return;
        }
        menuItem.setTitle(strRes);
    }
}
