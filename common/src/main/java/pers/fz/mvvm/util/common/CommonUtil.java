package pers.fz.mvvm.util.common;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

import pers.fz.mvvm.activity.WebViewActivity;
import pers.fz.mvvm.util.encode.MD5Util;

/**
 * Created by fz on 2018/3/16.
 * 一些工具类
 */

public class CommonUtil {
    /**
     * 调用手机浏览器打开连接
     *
     * @param linkUrl         打开地址
     * @param isInsideBrowser 是否调用app内部webView还是自带浏览器
     */
    public static void toBrowser(Context context, String linkUrl, boolean isInsideBrowser) {
        if (linkUrl == null) {
            return;
        }
        if (isInsideBrowser) {
            WebViewActivity.show(context, linkUrl, "详情");
        } else {
            Uri uri = Uri.parse(linkUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    public static File getTempFile(String url, String saveBasePath) {
        String tempFileName = null;
        try {
            tempFileName = "TEMP" + MD5Util.md5Encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(saveBasePath, tempFileName + ".temp.download");
    }

    public synchronized static byte[] drawableToByte(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;
            // 创建一个字节数组输出流,流的大小为size
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            // 设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            // 将字节数组输出流转化为字节数组byte[]
            return baos.toByteArray();
        }
        return null;
    }

    public static synchronized Bitmap byteToDrawable(byte[] img) {
        if (img != null) {
            return BitmapFactory.decodeByteArray(img, 0, img.length);
        }
        return null;

    }

    public static boolean isServiceRunning(Context mContext, Class<?> clx) {
        if (clx == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            ComponentName componentName = serviceInfo.service;
            if (componentName.getClassName().equals(clx.getName()) && componentName.getPackageName().equals(mContext.getPackageName())) {
                // Service已经注册和启动
                return true;
            }
        }
        // 执行相应的操作
        return false;
    }

    public static <T> boolean isStringType(T data) {
        return String.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isObjectType(T data) {
        return Object.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isBooleanType(T data) {
        return Boolean.class.isAssignableFrom(data.getClass()) || boolean.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isIntegerType(T data) {
        return Integer.class.isAssignableFrom(data.getClass()) || int.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isLongType(T data) {
        return Long.class.isAssignableFrom(data.getClass()) || long.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isDoubleType(T data) {
        return Double.class.isAssignableFrom(data.getClass()) || double.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isFloatType(T data) {
        return Float.class.isAssignableFrom(data.getClass()) || float.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isListType(T data) {
        return List.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isMapType(T data) {
        return Map.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isArrayType(T data) {
        return data.getClass().isArray();
    }
}
