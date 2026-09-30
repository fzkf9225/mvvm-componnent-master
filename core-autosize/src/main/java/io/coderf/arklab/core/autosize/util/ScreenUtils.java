/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.coderf.arklab.core.autosize.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 屏幕尺寸工具：API 30+ 优先 {@link WindowMetrics}，低版本回退 {@link Display}。
 * <p>
 * 传入 {@link android.app.Activity} 时取当前窗口（利于分屏）；Application 等非可视 Context
 * 取默认显示区域。
 */
public final class ScreenUtils {

    private ScreenUtils() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    /**
     * 状态栏高度。优先当前窗口 insets，回退系统 dimen。
     */
    public static int getStatusBarHeight() {
        return getStatusBarHeight(null);
    }

    public static int getStatusBarHeight(@Nullable Context context) {
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = context.getSystemService(WindowManager.class);
                if (wm != null) {
                    WindowMetrics metrics = wm.getCurrentWindowMetrics();
                    int top = metrics.getWindowInsets()
                            .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                            .top;
                    if (top > 0) {
                        return top;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        try {
            int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return Resources.getSystem().getDimensionPixelSize(resourceId);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 当前可用于适配的屏幕尺寸（px）。
     */
    @NonNull
    public static int[] getScreenSize(@NonNull Context context) {
        int[] size = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = context.getSystemService(WindowManager.class);
                if (wm != null) {
                    Rect bounds = wm.getCurrentWindowMetrics().getBounds();
                    size[0] = bounds.width();
                    size[1] = bounds.height();
                    if (size[0] > 0 && size[1] > 0) {
                        return size;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return getScreenSizeLegacy(context);
    }

    /**
     * 设备物理全屏尺寸（含系统栏区域），用于导航栏高度估算等。
     */
    @NonNull
    public static int[] getRawScreenSize(@NonNull Context context) {
        int[] size = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = context.getSystemService(WindowManager.class);
                if (wm != null) {
                    Rect bounds = wm.getMaximumWindowMetrics().getBounds();
                    size[0] = bounds.width();
                    size[1] = bounds.height();
                    if (size[0] > 0 && size[1] > 0) {
                        return size;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return getRawScreenSizeLegacy(context);
    }

    public static int getHeightOfNavigationBar(@NonNull Context context) {
        if (Settings.Global.getInt(context.getContentResolver(), "force_fsg_nav_bar", 0) != 0) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager wm = context.getSystemService(WindowManager.class);
                if (wm != null) {
                    int nav = wm.getCurrentWindowMetrics().getWindowInsets()
                            .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
                            .bottom;
                    if (nav >= 0) {
                        return nav;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        int realHeight = getRawScreenSize(context)[1];
        int displayHeight = getScreenSize(context)[1];
        return Math.max(0, realHeight - displayHeight);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private static int[] getScreenSizeLegacy(@NonNull Context context) {
        int[] size = new int[2];
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        size[0] = metrics.widthPixels;
        size[1] = metrics.heightPixels;
        return size;
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private static int[] getRawScreenSizeLegacy(@NonNull Context context) {
        int[] size = new int[2];
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        try {
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
            widthPixels = realSize.x;
            heightPixels = realSize.y;
        } catch (Exception ignored) {
        }
        size[0] = widthPixels;
        size[1] = heightPixels;
        return size;
    }
}
