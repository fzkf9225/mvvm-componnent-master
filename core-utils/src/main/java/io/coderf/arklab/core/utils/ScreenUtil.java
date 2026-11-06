package io.coderf.arklab.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 屏幕相关辅助类。
 * <p>
 * API 30+ 优先 {@link WindowMetrics} / {@link WindowInsetsCompat}（分屏、挖孔、Edge-to-Edge 更准）；
 * 低版本回退 {@link DisplayMetrics} / 系统 dimen。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/1/11
 */
public class ScreenUtil {
    private ScreenUtil() {
    }

    // -------------------------------------------------------------------------
    // 状态栏 / 导航栏 / Insets
    // -------------------------------------------------------------------------

    /**
     * 获取状态栏高度（px）。
     * <p>
     * 优先 {@link WindowInsetsCompat}（Edge-to-Edge / 挖孔准确）；Insets 未就绪时回退
     * 系统 {@code status_bar_height}（框架内部 dimen，公开 R 无对应常量）。
     * </p>
     */
    public static int getStatusBarHeight(@Nullable Activity activity) {
        if (activity == null) {
            return 0;
        }
        try {
            Insets status = resolveInsets(activity, WindowInsetsCompat.Type.statusBars(), true);
            if (status.top > 0) {
                return status.top;
            }
            return statusBarHeightFromSystem(activity);
        } catch (Exception e) {
            e.printStackTrace();
            return statusBarHeightFromSystem(activity);
        }
    }

    /**
     * 获取状态栏高度（px）。{@link Activity} 走 Insets；普通 {@link Context} 走系统 dimen。
     */
    public static int getStatusBarHeight(@NonNull Context context) {
        if (context instanceof Activity) {
            return getStatusBarHeight((Activity) context);
        }
        return statusBarHeightFromSystem(context);
    }

    /**
     * @deprecated 请改用 {@link #getStatusBarHeight(Context)}。保留以兼容旧调用。
     */
    @Deprecated
    public static int getStatusHeight(Context context) {
        return getStatusBarHeight(context);
    }

    /**
     * 导航栏高度（px）。手势导航时多为底部细条 inset，传统三键导航为完整高度。
     * <p>
     * 优先当前窗口 {@link WindowInsetsCompat.Type#navigationBars()}；拿不到时回退系统 dimen。
     * </p>
     */
    public static int getNavigationBarHeight(@Nullable Activity activity) {
        if (activity == null) {
            return 0;
        }
        try {
            Insets nav = resolveInsets(activity, WindowInsetsCompat.Type.navigationBars(), true);
            int h = Math.max(nav.bottom, Math.max(nav.left, nav.right));
            if (h > 0) {
                return h;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return navigationBarHeightFromSystem(activity);
    }

    public static int getNavigationBarHeight(@NonNull Context context) {
        if (context instanceof Activity) {
            return getNavigationBarHeight((Activity) context);
        }
        return navigationBarHeightFromSystem(context);
    }

    /**
     * 系统栏 Insets（状态栏 + 导航栏），忽略当前可见性（沉浸隐藏时仍返回占用高度）。
     */
    @NonNull
    public static Insets getSystemBarsInsets(@Nullable Activity activity) {
        return resolveInsets(activity, WindowInsetsCompat.Type.systemBars(), true);
    }

    /**
     * DisplayCutout（挖孔 / 刘海）Insets。无挖孔时为 {@link Insets#NONE}。
     */
    @NonNull
    public static Insets getDisplayCutoutInsets(@Nullable Activity activity) {
        return resolveInsets(activity, WindowInsetsCompat.Type.displayCutout(), true);
    }

    /**
     * 安全区 Insets = 系统栏 ∪ 挖孔。Edge-to-Edge 布局避让时优先用这个。
     */
    @NonNull
    public static Insets getSafeInsets(@Nullable Activity activity) {
        int type = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
        return resolveInsets(activity, type, true);
    }

    // -------------------------------------------------------------------------
    // 屏幕 / 窗口尺寸（WindowMetrics）
    // -------------------------------------------------------------------------

    /**
     * 当前窗口宽度（px）。
     * <p>
     * Activity 上等价于 {@link WindowMetrics#getBounds()} 宽（含系统栏区域的窗口边界）；
     * 非 Activity Context 回退 {@link DisplayMetrics#widthPixels}。
     * 分屏 / 多窗时返回的是当前窗口宽，不是物理全屏宽。
     * </p>
     */
    public static int getScreenWidth(Context context) {
        int[] size = getScreenSize(context);
        return size[0];
    }

    /**
     * 当前窗口高度（px）。语义同 {@link #getScreenWidth(Context)}。
     */
    public static int getScreenHeight(Context context) {
        int[] size = getScreenSize(context);
        return size[1];
    }

    /**
     * 当前窗口尺寸 {@code [width, height]}（px）。
     * <p>
     * API 30+：{@link WindowManager#getCurrentWindowMetrics()}；低版本：{@link Display#getMetrics}。
     * </p>
     */
    @NonNull
    public static int[] getScreenSize(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = windowManagerOf(context);
                if (wm != null) {
                    Rect bounds = wm.getCurrentWindowMetrics().getBounds();
                    if (bounds.width() > 0 && bounds.height() > 0) {
                        return new int[]{bounds.width(), bounds.height()};
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return screenSizeLegacy(context, false);
    }

    /**
     * 设备最大可用显示区域 {@code [width, height]}（px），约等于物理全屏（含系统栏区域）。
     * <p>
     * API 30+：{@link WindowManager#getMaximumWindowMetrics()}；低版本：{@link Display#getRealSize}。
     * 分屏场景下仍返回整屏，而不是当前窗口。
     * </p>
     */
    @NonNull
    public static int[] getRawScreenSize(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = windowManagerOf(context);
                if (wm != null) {
                    Rect bounds = wm.getMaximumWindowMetrics().getBounds();
                    if (bounds.width() > 0 && bounds.height() > 0) {
                        return new int[]{bounds.width(), bounds.height()};
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return screenSizeLegacy(context, true);
    }

    /**
     * 应用可用内容区 {@code [width, height]}（px）=
     * 当前窗口 bounds 扣除 systemBars ∪ displayCutout。
     * <p>
     * 接近旧 {@link Display#getSize} 语义，适合算弹窗宽高、列表满屏高度等。
     * </p>
     */
    @NonNull
    public static int[] getAppUsableSize(@NonNull Context context) {
        int[] window = getScreenSize(context);
        Insets safe = Insets.NONE;
        if (context instanceof Activity) {
            safe = getSafeInsets((Activity) context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = windowManagerOf(context);
                if (wm != null) {
                    WindowInsetsCompat compat = WindowInsetsCompat.toWindowInsetsCompat(
                            wm.getCurrentWindowMetrics().getWindowInsets());
                    int type = WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout();
                    safe = compat.getInsetsIgnoringVisibility(type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int w = Math.max(0, window[0] - safe.left - safe.right);
        int h = Math.max(0, window[1] - safe.top - safe.bottom);
        return new int[]{w, h};
    }

    public static int getAppUsableWidth(@NonNull Context context) {
        return getAppUsableSize(context)[0];
    }

    public static int getAppUsableHeight(@NonNull Context context) {
        return getAppUsableSize(context)[1];
    }

    /**
     * 屏幕密度（dpi 对应的 density 值）。优先当前窗口 density（API 34+），否则 DisplayMetrics。
     */
    public static float getDensity(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && context instanceof Activity) {
            try {
                WindowManager wm = ((Activity) context).getWindowManager();
                if (wm != null) {
                    return wm.getCurrentWindowMetrics().getDensity();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return context.getResources().getDisplayMetrics().density;
    }

    // -------------------------------------------------------------------------
    // 截图
    // -------------------------------------------------------------------------

    /**
     * 获取当前屏幕截图，包含状态栏。
     * <p>
     * 使用 {@link View#draw(Canvas)}，替代已废弃的 DrawingCache API。
     * </p>
     *
     * @return 截图像素；activity / Window 不可用时返回 null
     */
    @Nullable
    public static Bitmap snapShotWithStatusBar(@Nullable Activity activity) {
        return captureDecorView(activity);
    }

    /**
     * 获取当前屏幕截图，不包含状态栏。
     *
     * @return 截图像素；activity / Window 不可用时返回 null
     */
    @Nullable
    public static Bitmap snapShotWithoutStatusBar(@Nullable Activity activity) {
        Bitmap full = captureDecorView(activity);
        if (full == null) {
            return null;
        }
        int statusBarHeight = getStatusBarHeight(activity);
        int width = full.getWidth();
        int height = full.getHeight() - statusBarHeight;
        if (statusBarHeight <= 0 || height <= 0) {
            return full;
        }
        Bitmap cropped = Bitmap.createBitmap(full, 0, statusBarHeight, width, height);
        full.recycle();
        return cropped;
    }

    // -------------------------------------------------------------------------
    // 内部
    // -------------------------------------------------------------------------

    @NonNull
    private static Insets resolveInsets(
            @Nullable Activity activity, int typeMask, boolean ignoringVisibility) {
        if (activity == null) {
            return Insets.NONE;
        }
        try {
            Window window = activity.getWindow();
            if (window != null) {
                View decor = window.getDecorView();
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(decor);
                if (insets != null) {
                    Insets visible = insets.getInsets(typeMask);
                    if (!isEmpty(visible)) {
                        return visible;
                    }
                    if (ignoringVisibility) {
                        Insets ignored = insets.getInsetsIgnoringVisibility(typeMask);
                        if (!isEmpty(ignored)) {
                            return ignored;
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager wm = activity.getWindowManager();
                if (wm != null) {
                    // 用 Compat 包装平台 WindowInsets，避免直接依赖 API 30 的 Type.*
                    WindowInsetsCompat compat = WindowInsetsCompat.toWindowInsetsCompat(
                            wm.getCurrentWindowMetrics().getWindowInsets());
                    return ignoringVisibility
                            ? compat.getInsetsIgnoringVisibility(typeMask)
                            : compat.getInsets(typeMask);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Insets.NONE;
    }

    private static boolean isEmpty(@NonNull Insets insets) {
        return insets.left == 0 && insets.top == 0 && insets.right == 0 && insets.bottom == 0;
    }

    @Nullable
    private static WindowManager windowManagerOf(@NonNull Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).getWindowManager();
        }
        return context.getSystemService(WindowManager.class);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private static int[] screenSizeLegacy(@NonNull Context context, boolean real) {
        try {
            WindowManager wm = windowManagerOf(context);
            if (wm != null) {
                Display display = wm.getDefaultDisplay();
                if (real) {
                    Point out = new Point();
                    display.getRealSize(out);
                    if (out.x > 0 && out.y > 0) {
                        return new int[]{out.x, out.y};
                    }
                }
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                if (metrics.widthPixels > 0 && metrics.heightPixels > 0) {
                    return new int[]{metrics.widthPixels, metrics.heightPixels};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }

    /**
     * 读取框架内部 {@code status_bar_height}。公开 SDK 无对应 R 常量，只能按名查找；
     * {@code @Discouraged} 针对的是应用自身资源，此处是官方常见回退写法。
     */
    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    private static int statusBarHeightFromSystem(@NonNull Context context) {
        int resId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return context.getResources().getDimensionPixelSize(resId);
        }
        return 0;
    }

    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    private static int navigationBarHeightFromSystem(@NonNull Context context) {
        int resId = context.getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) {
            return context.getResources().getDimensionPixelSize(resId);
        }
        return 0;
    }

    /**
     * 将 DecorView 绘制到 Bitmap（同步），替代 DrawingCache。
     */
    @Nullable
    private static Bitmap captureDecorView(@Nullable Activity activity) {
        if (activity == null) {
            return null;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return null;
        }
        View view = window.getDecorView();
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            width = getScreenWidth(activity);
            height = getScreenHeight(activity);
        }
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
