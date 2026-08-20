package io.coderf.arklab.common.utils.theme;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.coderf.arklab.common.utils.log.LogUtil;

public class ThemeUtils {
    private final static String TAG = ThemeUtils.class.getSimpleName();

    /**
     * 设置状态栏颜色
     * @param activity activity
     * @param statusColor 状态栏颜色
     */
    public static void setStatusBarColor(Activity activity, int statusColor) {
        ThemeLollipop.setStatusBarColor(activity, statusColor);
    }

    /**
     * 设置状态栏颜色，默认不隐藏状态栏背景
     * @param activity activity
     */
    public static void translucentStatusBar(Activity activity) {
        translucentStatusBar(activity, false);
    }

    /**
     * 设置状态栏颜色，可控制是否完全透明(hideStatusBarBackground=true)或半透明
     * @param activity activity
     */
    public static void translucentStatusBar(Activity activity, boolean hideStatusBarBackground) {
        ThemeLollipop.translucentStatusBar(activity, hideStatusBarBackground);
    }

    /**
     * 为可折叠工具栏(CollapsingToolbarLayout)设置状态栏颜色
     * 处理工具栏展开/折叠时状态栏的显示效果
     * @param activity activity
     * @param appBarLayout appBarLayout
     * @param collapsingToolbarLayout collapsingToolbarLayout
     * @param toolbar 标题栏
     * @param statusColor 状态栏颜色
     */
    public static void setStatusBarColorForCollapsingToolbar(@NonNull Activity activity, AppBarLayout appBarLayout,
                                                             CollapsingToolbarLayout collapsingToolbarLayout, Toolbar toolbar, @ColorInt int statusColor) {
        ThemeLollipop.setStatusBarColorForCollapsingToolbar(activity, appBarLayout, collapsingToolbarLayout, toolbar, statusColor);
    }

    /**
     * 设置沉浸式状态栏（透明状态栏，内容延伸到状态栏）
     * @param activity 当前Activity
     */
    public static void setImmersiveStatusBar(Activity activity) {
        setImmersiveStatusBar(activity, Color.TRANSPARENT, false);
    }

    /**
     * 设置沉浸式状态栏
     * @param activity 当前Activity
     * @param color 状态栏背景颜色（可透明）
     * @param lightStatusBar 是否使用浅色状态栏文字（深色背景时设为false）
     */
    public static void setImmersiveStatusBar(Activity activity, @ColorInt int color, boolean lightStatusBar) {
        try {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();

            // 清除原有flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 设置状态栏颜色
            window.setStatusBarColor(color);

            // 设置内容延伸到状态栏
            int systemUiVisibility = decorView.getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            // 设置状态栏文字颜色
            if (lightStatusBar) {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(systemUiVisibility);

            // 处理MIUI和Flyme的特殊情况
            if (lightStatusBar) {
                if (!MIUISetStatusBarLightMode(activity, true)) {
                    FlymeSetStatusBarLightMode(activity, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loggerE(TAG, "setImmersiveStatusBar异常：" + e);
        }
    }

    /**
     * 隐藏状态栏与导航栏，内容铺满屏幕（全屏视频等场景）。
     * <p>
     * 与 {@link #setImmersiveStatusBar(Activity)} 不同：本方法会完全隐藏系统栏，而非仅透明延伸布局。
     * </p>
     */
    public static void applyHideSystemBarsImmersive(@NonNull Activity activity) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.BLACK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    applyHideSystemBarsImmersive(activity);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loggerE(TAG, "applyHideSystemBarsImmersive异常：" + e);
        }
    }

    /**
     * 退出全屏沉浸式后恢复系统栏（与 {@link #applyHideSystemBarsImmersive} 配对使用）。
     */
    public static void restoreSystemBarsAfterImmersive(@NonNull Activity activity) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                window.setAttributes(lp);
            }
            View decorView = window.getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(null);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loggerE(TAG, "restoreSystemBarsAfterImmersive异常：" + e);
        }
    }

    /**
     * 设置沉浸式状态栏
     * @param activity 当前Activity
     * @param color 状态栏背景颜色（可透明）
     * @param lightStatusBar 是否使用浅色状态栏文字（深色背景时设为false）
     */
    public static void setStatusBarColor(Activity activity, @ColorInt int color, boolean lightStatusBar) {
        try {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();

            // 清除原有flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 设置状态栏颜色
            window.setStatusBarColor(color);

            // 设置内容延伸到状态栏
            int systemUiVisibility = decorView.getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            // 设置状态栏文字颜色
            if (lightStatusBar) {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(systemUiVisibility);

            // 处理MIUI和Flyme的特殊情况
            if (lightStatusBar) {
                if (!MIUISetStatusBarLightMode(activity, true)) {
                    FlymeSetStatusBarLightMode(activity, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loggerE(TAG, "setImmersiveStatusBar异常：" + e);
        }
    }
    /**
     * 设置状态栏颜色和文字颜色
     * @param activity activity
     * @param statusBarColor 状态栏背景色
     * @param lightStatusBar 是否亮色文字
     */
    public static void setupStatusBar(Activity activity, @ColorInt int statusBarColor, boolean lightStatusBar) {
        try {
            Window window = activity.getWindow();
            WindowInsetsControllerCompat insetsController =
                    WindowCompat.getInsetsController(window, window.getDecorView());
            if (insetsController != null) {
                insetsController.setAppearanceLightStatusBars(!lightStatusBar);
            }
            // Edge-to-Edge 下状态栏透明，由 Toolbar 背景色呈现
            window.setStatusBarColor(Color.TRANSPARENT);

            // 处理MIUI和Flyme的特殊情况
            if (lightStatusBar) {
                if (!MIUISetStatusBarLightMode(activity, true)) {
                    FlymeSetStatusBarLightMode(activity, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loggerE(TAG, "setupStatusBar异常：" + e);
        }
    }

    /**
     * 设置状态栏浅色模式(深色文字)
     * 同时设置状态栏背景颜色
     * @param activity activity
     * @param color 通知栏背景色
     */
    public static void setStatusBarLightMode(Activity activity, int color) {
        setupStatusBar(activity, color, true);
    }

    public static void setStatusBarLightForCollapsingToolbar(Activity activity, AppBarLayout appBarLayout,
                                                             CollapsingToolbarLayout collapsingToolbarLayout, Toolbar toolbar, int statusBarColor) {
        ThemeLollipop.setStatusBarWhiteForCollapsingToolbar(activity, appBarLayout, collapsingToolbarLayout, toolbar, statusBarColor);
    }

    /**
     * MIUI的沉浸支持透明白色字体和透明黑色字体
     * https://dev.mi.com/console/doc/detail?pId=1159
     */
    static boolean MIUISetStatusBarLightMode(Activity activity, boolean darkMode) {
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            Class<? extends Window> clazz = activity.getWindow().getClass();
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkMode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格，Flyme4.0以上
     */
    static boolean FlymeSetStatusBarLightMode(Activity activity, boolean darkMode) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkMode) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}