package pers.fz.mvvm.utils.common;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

/**
 * Created by CherishTang on 2018/7/12.
 * px与dp互转
 */
public class DensityUtil {
    private final static String TAG = DensityUtil.class.getSimpleName();

    private static float appScaledDensity;
    private static DisplayMetrics appDisplayMetrics;
    /**
     * 用来参照的的width
     */
    private static float WIDTH;

    public static void setDensity(Activity activity, float width) {
        appDisplayMetrics = activity.getResources().getDisplayMetrics();
        WIDTH = width;
//        registerActivityLifecycleCallbacks(application);

        //初始化的时候赋值
        float appDensity = appDisplayMetrics.density;
        appScaledDensity = appDisplayMetrics.scaledDensity;

        //添加字体变化的监听
        activity.registerComponentCallbacks(new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(@NonNull Configuration newConfig) {
                //字体改变后,将appScaledDensity重新赋值
                if (newConfig.fontScale > 0) {
                    appScaledDensity = activity.getResources().getDisplayMetrics().scaledDensity;
                }
            }

            @Override
            public void onLowMemory() {
            }
        });
        setDefault(activity, appDensity);
    }

    private static void setDefault(Activity activity, float appDensity) {

        final float targetDensity = appDisplayMetrics.widthPixels / WIDTH;

        float targetScaledDensity = targetDensity * (appScaledDensity / appDensity);
        int targetDensityDpi = (int) (160 * targetDensity);

        /*
         * 最后在这里将修改过后的值赋给系统参数
         * 只修改Activity的density值
         */
        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

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
     * 将px转换为sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * convert sp to its equivalent px
     * <p>
     * 将sp转换为px
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
