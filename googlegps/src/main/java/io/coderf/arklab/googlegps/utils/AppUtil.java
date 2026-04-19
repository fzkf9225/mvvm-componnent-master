package io.coderf.arklab.googlegps.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

/**
 * Created by fz on 2017/6/15.
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 * 可以让所有的activity都继承BaseActivity然后给activity在onCreate中添加到栈中onDetroyed中移除
 * <p>
 * 添加Activity到堆栈
 * AppManager.getAppManager().addActivity(this);
 * 结束Activity&从堆栈中移除
 * AppManager.getAppManager().finishActivity(this);
 */
public class AppUtil {
    public final static String TAG = AppUtil.class.getSimpleName();


    private AppUtil() {
    }

    private static final class InstanceHolder {
        private static final AppUtil INSTANCE = new AppUtil();
    }

    /**
     * 单一实例
     */
    public static AppUtil getAppManager() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 获取app的桌面图标
     * @param context 上下文
     * @return 桌面图标资源id
     */
    public int getAppIcon(Context context){
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.applicationInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取app的应用名称，也就是app模块的那个名字
     * @param context 上下文
     * @return app的桌面名称
     */
    public String getAppName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return context.getPackageManager().getApplicationLabel(pi.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建支持自定义四个角的圆角矩形 Drawable（无尺寸限制）
     * @param color 填充颜色
     * @param topLeftRadius 左上角半径
     * @param topRightRadius 右上角半径
     * @param bottomRightRadius 右下角半径
     * @param bottomLeftRadius 左下角半径
     * @return GradientDrawable
     */
    public static GradientDrawable createRectDrawable(
            int color,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadii(new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        });
        return drawable;
    }
    /**
     * 创建一个圆角矩形Drawable
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     * @param cornerRadius 圆角半径
     * @return 圆角矩形ShapeDrawable
     */
    public static ShapeDrawable createRectDrawable(
            int color,
            int width,
            int height,
            float cornerRadius) {
        float[] radii = new float[]{
                cornerRadius, cornerRadius, // 左上角
                cornerRadius, cornerRadius, // 右上角
                cornerRadius, cornerRadius, // 右下角
                cornerRadius, cornerRadius  // 左下角
        };
        RoundRectShape shape = new RoundRectShape(radii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setAntiAlias(true); // 启用抗锯齿
        shapeDrawable.setIntrinsicWidth(width);
        shapeDrawable.setIntrinsicHeight(height);
        return shapeDrawable;
    }

    /**
     * 创建矩形Drawable（无圆角）
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     */
    public static ShapeDrawable createRectDrawable(
            int color,
            int width,
            int height) {
        return createRectDrawable(color, width, height, 0f);
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
}
