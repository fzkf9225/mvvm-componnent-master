package pers.fz.mvvm.util.theme;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import pers.fz.mvvm.util.common.ScreenUtil;

class ThemeLollipop {
    static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(statusColor);
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        //让view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            mChildView.setFitsSystemWindows(false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }

    static void translucentStatusBar(Activity activity, boolean hideStatusBarBackground) {
        Window window = activity.getWindow();
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (hideStatusBarBackground) {
            //如果为全透明模式，取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏为透明
            window.setStatusBarColor(Color.TRANSPARENT);
            //设置window的状态栏不可见,SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            //如果为半透明模式，添加设置Window半透明的Flag
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置系统状态栏处于可见状态
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        //view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            mChildView.setFitsSystemWindows(false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }

    static void setStatusBarColorForCollapsingToolbar(final Activity activity, final AppBarLayout appBarLayout, final CollapsingToolbarLayout collapsingToolbarLayout,
                                                      Toolbar toolbar, final int statusColor) {
        final Window window = activity.getWindow();
        //取消设置Window半透明的Flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ////添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏为透明
        window.setStatusBarColor(Color.TRANSPARENT);
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        //通过OnApplyWindowInsetsListener()使Layout在绘制过程中将View向下偏移了,使collapsingToolbarLayout可以占据状态栏
        ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbarLayout, (OnApplyWindowInsetsListener) (view, windowInsets) -> windowInsets);

        ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        //view不根据系统窗口来调整自己的布局
        if (mChildView != null) {
            mChildView.setFitsSystemWindows(false);
            ViewCompat.requestApplyInsets(mChildView);
        }

        ((View) appBarLayout.getParent()).setFitsSystemWindows(false);
        appBarLayout.setFitsSystemWindows(false);
        collapsingToolbarLayout.setFitsSystemWindows(false);
        collapsingToolbarLayout.getChildAt(0).setFitsSystemWindows(false);
        //设置状态栏的颜色
        collapsingToolbarLayout.setStatusBarScrimColor(statusColor);
        toolbar.setFitsSystemWindows(false);
        //为Toolbar添加一个状态栏的高度, 同时为Toolbar添加paddingTop,使Toolbar覆盖状态栏，ToolBar的title可以正常显示.
        if (toolbar.getTag() == null) {
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
            int statusBarHeight = ScreenUtil.getStatusBarHeight(activity);
            lp.height += statusBarHeight;
            toolbar.setLayoutParams(lp);
            toolbar.setPadding(toolbar.getPaddingStart(), toolbar.getPaddingTop() + statusBarHeight, toolbar.getPaddingEnd(), toolbar.getPaddingBottom());
            toolbar.setTag(true);
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private final static int EXPANDED = 0;
            private final static int COLLAPSED = 1;
            private int appBarLayoutState;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //toolbar被折叠时显示状态栏
                if (Math.abs(verticalOffset) > collapsingToolbarLayout.getScrimVisibleHeightTrigger()) {
                    if (appBarLayoutState != COLLAPSED) {
                        appBarLayoutState = COLLAPSED;//修改状态标记为折叠
                        setStatusBarColor(activity, statusColor);
                    }
                } else {
                    //toolbar显示时同时显示状态栏
                    if (appBarLayoutState != EXPANDED) {
                        appBarLayoutState = EXPANDED;//修改状态标记为展开
                        translucentStatusBar(activity, true);
                    }
                }
            }
        });
    }

    static void setStatusBarWhiteForCollapsingToolbar(final Activity activity, final AppBarLayout appBarLayout, final CollapsingToolbarLayout collapsingToolbarLayout, final Toolbar toolbar, final int statusBarColor) {
        final Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbarLayout, (v, insets) -> insets);

        ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            mChildView.setFitsSystemWindows(false);
            ViewCompat.requestApplyInsets(mChildView);
        }

        ((View) appBarLayout.getParent()).setFitsSystemWindows(false);
        appBarLayout.setFitsSystemWindows(false);
        toolbar.setFitsSystemWindows(false);
        if (toolbar.getTag() == null) {
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
            int statusBarHeight =  ScreenUtil.getStatusBarHeight(activity);
            lp.height += statusBarHeight;
            toolbar.setLayoutParams(lp);
            toolbar.setPadding(toolbar.getPaddingStart(), toolbar.getPaddingTop() + statusBarHeight, toolbar.getPaddingEnd(), toolbar.getPaddingBottom());
            toolbar.setTag(true);
        }

        collapsingToolbarLayout.setFitsSystemWindows(false);
        collapsingToolbarLayout.getChildAt(0).setFitsSystemWindows(false);
        collapsingToolbarLayout.setStatusBarScrimColor(statusBarColor);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private final static int EXPANDED = 0;
            private final static int COLLAPSED = 1;
            private int appBarLayoutState;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //toolbar被折叠时显示状态栏
                if (Math.abs(verticalOffset) > collapsingToolbarLayout.getScrimVisibleHeightTrigger()) {
                    if (appBarLayoutState != COLLAPSED) {
                        appBarLayoutState = COLLAPSED;//修改状态标记为折叠

                        //先判断是否为小米设备，设置状态栏不成功判断是否为6.0以上设备，不是6.0以上设备再判断是否为魅族设备，不是魅族设备就只设置状态栏颜色
                        if (ThemeUtils.MIUISetStatusBarLightMode(activity, true)) {
                            return;
                        }
                        activity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        //SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        activity.getWindow().setStatusBarColor(statusBarColor);
                    }
                } else {
                    //toolbar显示时同时显示状态栏
                    if (appBarLayoutState != EXPANDED) {
                        appBarLayoutState = EXPANDED;//修改状态标记为展开

                        if (ThemeUtils.MIUISetStatusBarLightMode(activity, false)) {
                            translucentStatusBar(activity, true);
                            return;
                        }
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        translucentStatusBar(activity, true);
                    }
                }
            }
        });
    }
}
