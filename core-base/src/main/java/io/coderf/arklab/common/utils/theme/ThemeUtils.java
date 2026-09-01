package io.coderf.arklab.common.utils.theme;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * 系统栏（状态栏 / 导航栏）统一工具。
 * <p>
 * 约定：
 * <ul>
 *   <li>{@code darkIcons == true}：深色图标/文字（浅色背景）→
 *       {@link WindowInsetsControllerCompat#setAppearanceLightStatusBars(true)}</li>
 *   <li>{@code darkIcons == false}：浅色图标/文字（深色背景）</li>
 *   <li>默认配合 Edge-to-Edge：状态栏背景透明，由 MaterialToolbar / 页面背景呈现颜色</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/27 14:15
 */
public final class ThemeUtils {

    private static final String TAG = "ThemeUtils";

    /** 相对亮度阈值：高于此值视为浅色背景，使用深色图标。 */
    private static final float LIGHT_LUMINANCE_THRESHOLD = 0.5f;

    private ThemeUtils() {
    }

    // -------------------------------------------------------------------------
    // 状态栏
    // -------------------------------------------------------------------------

    /**
     * 设置状态栏颜色与图标深浅。
     * <p>非 Edge-to-Edge 场景会真正写入 statusBarColor；Edge-to-Edge 下仍写入颜色，
     * 若业务已启用 EdgeToEdge，建议改用 {@link #setupStatusBar} 保持透明。</p>
     *
     * @param darkIcons {@code true} 深色图标（浅色背景），{@code false} 浅色图标（深色背景）
     */
    public static void setStatusBar(@NonNull Activity activity, @ColorInt int color, boolean darkIcons) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            applyStatusBarAppearance(window, darkIcons);
        } catch (Exception e) {
            LogUtil.loggerE(TAG, "setStatusBar: " + e);
        }
    }

    /**
     * 状态栏透明 + 指定图标深浅（内容延伸到状态栏区域）。
     */
    public static void setStatusBarTransparent(@NonNull Activity activity, boolean darkIcons) {
        setStatusBar(activity, Color.TRANSPARENT, darkIcons);
    }

    /**
     * Edge-to-Edge 场景下的状态栏配置：状态栏始终透明，仅设置图标 Appearance。
     * <p>
     * {@code darkIcons} 语义与 {@link #setStatusBar} 一致。
     * MaterialToolbar 背景色由业务自行设置，状态栏区域视觉效果由 MaterialToolbar 顶到状态栏呈现。
     * </p>
     *
     * @param ignoredColorForEdgeToEdge 保留参数便于调用方传入 MaterialToolbar 色；当前实现不写入状态栏
     * @param darkIcons                 {@code true} 深色图标
     */
    public static void setupStatusBar(@NonNull Activity activity,
                                      @ColorInt int ignoredColorForEdgeToEdge,
                                      boolean darkIcons) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            applyStatusBarAppearance(window, darkIcons);
        } catch (Exception e) {
            LogUtil.loggerE(TAG, "setupStatusBar: " + e);
        }
    }

    /**
     * 根据背景色亮度自动选择图标深浅并设置状态栏颜色。
     */
    public static void setStatusBarAuto(@NonNull Activity activity, @ColorInt int backgroundColor) {
        setStatusBar(activity, backgroundColor, isColorLight(backgroundColor));
    }

    /**
     * 仅根据背景色自动设置 Appearance（状态栏保持透明，适合 Edge-to-Edge）。
     */
    public static void setupStatusBarAuto(@NonNull Activity activity, @ColorInt int backgroundColor) {
        setupStatusBar(activity, backgroundColor, isColorLight(backgroundColor));
    }

    // -------------------------------------------------------------------------
    // 导航栏
    // -------------------------------------------------------------------------

    /**
     * 设置导航栏颜色与图标深浅。
     *
     * @param darkIcons {@code true} 深色导航图标，{@code false} 浅色
     */
    public static void setNavigationBar(@NonNull Activity activity, @ColorInt int color, boolean darkIcons) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(color);
            applyNavigationBarAppearance(window, darkIcons);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(false);
            }
        } catch (Exception e) {
            LogUtil.loggerE(TAG, "setNavigationBar: " + e);
        }
    }

    /**
     * 导航栏透明 + 指定图标深浅。
     */
    public static void setNavigationBarTransparent(@NonNull Activity activity, boolean darkIcons) {
        setNavigationBar(activity, Color.TRANSPARENT, darkIcons);
    }

    // -------------------------------------------------------------------------
    // 系统栏一次性设置
    // -------------------------------------------------------------------------

    /**
     * 同时配置状态栏与导航栏颜色及图标。
     */
    public static void setSystemBars(@NonNull Activity activity,
                                     @ColorInt int statusColor,
                                     @ColorInt int navigationColor,
                                     boolean darkStatusIcons,
                                     boolean darkNavigationIcons) {
        setStatusBar(activity, statusColor, darkStatusIcons);
        setNavigationBar(activity, navigationColor, darkNavigationIcons);
    }

    // -------------------------------------------------------------------------
    // 查询
    // -------------------------------------------------------------------------

    public static boolean isAppearanceLightStatusBars(@NonNull Activity activity) {
        Window window = activity.getWindow();
        if (window == null) {
            return false;
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        return controller != null && controller.isAppearanceLightStatusBars();
    }

    public static boolean isAppearanceLightNavigationBars(@NonNull Activity activity) {
        Window window = activity.getWindow();
        if (window == null) {
            return false;
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        return controller != null && controller.isAppearanceLightNavigationBars();
    }

    /**
     * 判断颜色是否为浅色（相对亮度 ≥ {@link #LIGHT_LUMINANCE_THRESHOLD}）。
     */
    public static boolean isColorLight(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= LIGHT_LUMINANCE_THRESHOLD;
    }

    // -------------------------------------------------------------------------
    // 对比度强制（API 29+）
    // -------------------------------------------------------------------------

    public static void setStatusBarContrastEnforced(@NonNull Activity activity, boolean enforced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Window window = activity.getWindow();
            if (window != null) {
                window.setStatusBarContrastEnforced(enforced);
            }
        }
    }

    public static void setNavigationBarContrastEnforced(@NonNull Activity activity, boolean enforced) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Window window = activity.getWindow();
            if (window != null) {
                window.setNavigationBarContrastEnforced(enforced);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 全屏隐藏系统栏（视频等）
    // -------------------------------------------------------------------------

    /**
     * 隐藏状态栏与导航栏，内容铺满屏幕（全屏视频等）。
     * 使用 {@link WindowInsetsControllerCompat}，不再依赖已废弃的 systemUiVisibility 监听递归。
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
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars()
                        | WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } catch (Exception e) {
            LogUtil.loggerE(TAG, "applyHideSystemBarsImmersive: " + e);
        }
    }

    /**
     * 退出全屏沉浸，显示系统栏（状态栏透明 + 深色图标；需业务再按需配置导航栏）。
     */
    public static void restoreSystemBarsAfterImmersive(@NonNull Activity activity) {
        restoreSystemBarsAfterImmersive(activity, Color.TRANSPARENT, true);
    }

    /**
     * 退出全屏沉浸并恢复状态栏颜色与 Appearance。
     *
     * @param statusColor     恢复后的状态栏颜色（Edge-to-Edge 下建议 {@link Color#TRANSPARENT}）
     * @param darkStatusIcons 状态栏是否深色图标
     */
    public static void restoreSystemBarsAfterImmersive(@NonNull Activity activity,
                                                       @ColorInt int statusColor,
                                                       boolean darkStatusIcons) {
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
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.statusBars()
                        | WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
            }
            window.setStatusBarColor(statusColor);
            applyStatusBarAppearance(window, darkStatusIcons);
        } catch (Exception e) {
            LogUtil.loggerE(TAG, "restoreSystemBarsAfterImmersive: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // internal
    // -------------------------------------------------------------------------

    private static void applyStatusBarAppearance(@NonNull Window window, boolean darkIcons) {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(darkIcons);
        }
    }

    private static void applyNavigationBarAppearance(@NonNull Window window, boolean darkIcons) {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(darkIcons);
        }
    }
}

