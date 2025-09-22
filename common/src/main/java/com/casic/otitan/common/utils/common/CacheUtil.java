package com.casic.otitan.common.utils.common;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by fz on 2016/6/21.
 * 缓存工具类，包含Glide图片缓存和通用应用缓存管理
 */
public class CacheUtil {

    private static final class InstHolder {
        private static final CacheUtil INSTANCE = new CacheUtil();
    }

    public static CacheUtil getInstance() {
        return InstHolder.INSTANCE;
    }

    /**
     * 清除图片磁盘缓存
     */
    public void clearImageDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ThreadExecutor.getInstance().execute(() -> Glide.get(context).clearDiskCache());
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片内存缓存
     */
    public void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片所有缓存
     */
    public void clearImageAllCache(Context context) {
        clearImageDiskCache(context);
        clearImageMemoryCache(context);
        String ImageExternalCatchDir = context.getExternalCacheDir() + ExternalPreferredCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir, true);
    }

    /**
     * 获取Glide造成的缓存大小
     * @return CacheSize
     */
    public String getGlideCacheSize(Context context) {
        try {
            return getFormatSize(getFolderSize(new File(context.getCacheDir() + File.separator + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取应用总缓存大小（包括内部缓存和外部缓存）
     * @param context 上下文
     * @return 格式化后的缓存大小字符串
     */
    public String getTotalCacheSize(Context context) {
        long cacheSize = 0;
        try {
            // 内部缓存
            cacheSize += getFolderSize(context.getCacheDir());

            // 外部缓存（如果可用）
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File externalCacheDir = context.getExternalCacheDir();
                if (externalCacheDir != null) {
                    cacheSize += getFolderSize(externalCacheDir);
                }
            }

            // Glide专用缓存（避免重复计算，但为了完整性可以加上）
            File glideInternalCache = new File(context.getCacheDir() + File.separator + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR);
            if (glideInternalCache.exists()) {
                cacheSize += getFolderSize(glideInternalCache);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFormatSize(cacheSize);
    }

    /**
     * 清理所有应用缓存（包括内部和外部）
     * @param context 上下文
     */
    public void clearAllCache(Context context) {
        // 清理内部缓存
        deleteFolderFile(context.getCacheDir().getAbsolutePath(), false);

        // 清理外部缓存（如果可用）
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                deleteFolderFile(externalCacheDir.getAbsolutePath(), false);
            }
        }

        // 额外清理Glide缓存（确保完全清除）
        clearImageAllCache(context);
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     * @param file 文件夹
     * @return 文件夹大小（字节）
     * @throws Exception
     */
    public long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            if (fileList == null || fileList.length == 0) {
                return 0;
            }
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size += getFolderSize(aFileList);
                } else {
                    size += aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     * @param filePath 文件路径
     * @param deleteThisPath 是否删除传入的路径本身
     */
    public void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File file1 : files) {
                            deleteFolderFile(file1.getAbsolutePath(), true);
                        }
                    }
                }
                if (deleteThisPath && file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 格式化单位
     * @param size 大小（字节）
     * @return 格式化后的字符串
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, RoundingMode.HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, RoundingMode.HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, RoundingMode.HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, RoundingMode.HALF_UP).toPlainString() + "TB";
    }
}