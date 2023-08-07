package pers.fz.mvvm.util.apiUtil;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by fz on 2023/5/4 17:05
 * describe :内存大小工具类
 */
public class StorageUtil {
    /**
     * RAM内存大小, 返回1GB/2GB/3GB/4GB/8G/16G
     *
     * @return
     */
    public static String getTotalRam() {
        String path = "/proc/meminfo";
        String ramMemorySize = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 4096);
            ramMemorySize = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ramMemorySize != null) {
            totalRam = (int) Math.ceil((Float.valueOf(Float.parseFloat(ramMemorySize) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";
    }

    /**
     * ROM内存大小，返回 64G/128G/256G/512G
     *
     * @return
     */
    private static String getTotalRom() {
        File dataDir = Environment.getDataDirectory();
        StatFs stat = new StatFs(dataDir.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long size = totalBlocks * blockSize;
        long GB = 1024 * 1024 * 1024;
        final long[] deviceRomMemoryMap = {2 * GB, 4 * GB, 8 * GB, 16 * GB, 32 * GB, 64 * GB, 128 * GB, 256 * GB, 512 * GB, 1024 * GB, 2048 * GB};
        String[] displayRomSize = {"2GB", "4GB", "8GB", "16GB", "32GB", "64GB", "128GB", "256GB", "512GB", "1024GB", "2048GB"};
        int i;
        for (i = 0; i < deviceRomMemoryMap.length; i++) {
            if (size <= deviceRomMemoryMap[i]) {
                break;
            }
            if (i == deviceRomMemoryMap.length) {
                i--;
            }
        }
        return displayRomSize[i];
    }

    /**
     * * 获取android总运行内存大小
     * * @param context
     * *
     */
    public static long getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }
            // 获得系统总内存，单位是KB
            int i = Integer.valueOf(arrayOfString[1]).intValue();
            //int值乘以1024转换为long类型
            initial_memory = new Long((long) i * 1024);
            localBufferedReader.close();
        } catch (IOException e) {
        }
        long memory = initial_memory / 1024 / 1024;//获取MB
        return memory;
    }


    /**
     * * 获取android当前可用运行内存大小
     * * @param context
     * *
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
// mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * * 获取android总运行内存大小
     * * @param context
     * *
     */
    public static String getTotalMemoryString(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }
            // 获得系统总内存，单位是KB
            int i = Integer.valueOf(arrayOfString[1]).intValue();
            //int值乘以1024转换为long类型
            initial_memory = new Long((long) i * 1024);
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 方法名：getMemoryInfo()
     * 功    能：获取手机总存储,可用存储
     * 参    数：无
     * 返回值：long
     */
    public static long getMemoryInfo() {
        //获取到手机数据文件
        File path = Environment.getDataDirectory();
        //获取一个磁盘的状态对象
        StatFs stat = new StatFs(path.getPath());
        //获得一个扇区的大小
        long blockSizes = stat.getBlockSize();
        //获取扇区总数
        long totalBlocks = stat.getBlockCount();
        //获得可用扇区数量
        long availableBlocks = stat.getAvailableBlocks();
        //获得总存储空间,MB
        long memory = blockSizes * totalBlocks / 1024 / 1024;
        //获得总存储空间
        //String totalMemory = Formatter.formatFileSize(this, blockSizes * totalBlocks);
        //获得可用存储空间
        //String availableMemory = Formatter.formatFileSize(this, availableBlocks * blockSizes);
        return memory;
    }

    /**
     * 如果小米手机获取Build.MODEL的结果是M2004J19C这样的格式
     * 那么使用反射获取小米手机型号 例如：Redmi note 9
     *
     * @return
     */
    public static String getXiaoMiModel() {
        String deviceName = "";
        try {
            @SuppressLint("PrivateApi") Class c = Class.forName("android.os.SystemProperties");
            @SuppressLint("PrivateApi") Method m = c.getDeclaredMethod("get", String.class);
            m.setAccessible(true);
            deviceName = (String) m.invoke(null, "ro.product.vendor.marketname");
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = (String) m.invoke(null, "ro.product.marketname");
            }
            Log.d("WorkPhoneCheck", "deviceName: " + deviceName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(deviceName) ? Build.MODEL : deviceName;
    }

    /**
     * 获取总内存
     * @param context
     * @return
     */
    public static double getTotalMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return (double) memoryInfo.totalMem / (1024 * 1024 * 1024);
    }

    /**
     * 获取剩余内存
     * @param context
     * @return
     */
    public static double getLastMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return (double) memoryInfo.availMem / (1024 * 1024 * 1024);
    }

    /**
     * 获取已使用内存
     * @param context
     * @return
     */
    public static double getUsedMemoryInfo(Context context) {
        return getTotalMemoryInfo(context) - getLastMemoryInfo(context);
    }

}
