package io.coderf.arklab.common.utils.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * px与dp互转
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/7/12
 */
public class DensityUtil {

    /**
     * 是否是平板
     *
     * @param context 上下文
     * @return 是平板则返回true，反之返回false
     */
    public static boolean isPad(Context context) {
        try {
            boolean isPad = (context.getResources().getConfiguration().screenLayout
                    & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
            DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();

            double x = Math.pow(appDisplayMetrics.widthPixels / appDisplayMetrics.xdpi, 2);
            double y = Math.pow(appDisplayMetrics.heightPixels / appDisplayMetrics.ydpi, 2);
            // 屏幕尺寸
            double screenInches = Math.sqrt(x + y);
            return isPad || screenInches >= 7.0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultWidth(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_width_in_dp", 360);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 360;
    }

    /**
     * 屏幕适配尺寸，很多人把基准写在AndroidManifest中，但是我选择直接写BaseActivity中，是为了更好的支持各个Activity自愈更改
     *
     * @return 默认360dp
     */
    private float getDefaultHeight(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return info.metaData.getInt("design_height_in_dp", 640);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 640;
    }
    /**
     * convert px to its equivalent dp
     * <p>
     * 将px转换为与之相等的dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * convert dp to its equivalent px
     * <p>
     * 将dp转换为与之相等的px
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    /**
     * convert px to its equivalent sp
     * <p>
     * 将px转换为sp。不使用已废弃的 {@link DisplayMetrics#scaledDensity}：
     * API 34+ 走 {@link TypedValue#deriveDimension}，更低版本用 density × fontScale。
     */
    public static int px2sp(Context context, float pxValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return Math.round(TypedValue.deriveDimension(
                    TypedValue.COMPLEX_UNIT_SP, pxValue, metrics));
        }
        float scaled = metrics.density * context.getResources().getConfiguration().fontScale;
        if (scaled == 0f) {
            return 0;
        }
        return Math.round(pxValue / scaled);
    }


    /**
     * convert sp to its equivalent px
     * <p>
     * 将sp转换为px（{@link TypedValue#applyDimension}，兼容系统字体缩放）。
     */
    public static int sp2px(Context context, float spValue) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spValue,
                context.getResources().getDisplayMetrics()));
    }
}
