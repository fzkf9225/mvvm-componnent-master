package com.casic.otitan.media.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by fz on 2021/4/12 10:35
 * describe:
 */
public class MediaUtil {
    private final String TAG = this.getClass().getSimpleName();

    public static boolean isImageType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("image/") || mineType.startsWith("IMAGE/");
    }

    public static boolean isVideoType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("video/") || mineType.startsWith("VIDEO/");
    }

    public static boolean isAudioType(String mineType) {
        if (TextUtils.isEmpty(mineType)) {
            return false;
        }
        return mineType.startsWith("audio/") || mineType.startsWith("AUDIO/");
    }

    public static boolean isImage(Uri uri, Context context) {
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("IMAGE/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isVideo(Uri uri, Context context) {
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && (mimeType.startsWith("video/") || mimeType.startsWith("VIDEO/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAudio(Uri uri, Context context) {
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && (mimeType.startsWith("audio/") || mimeType.startsWith("AUDIO/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取年月日时分秒字符串，用于文件名
     * @return yyyyMMddHHmmssSSS
     */
    public static String getCurrentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date());
    }

    /**
     * 获取图片经度
     * @param filePath 图片路径
     * @return 经度
     */
    public static String getPictureLongitude(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片经度
     * @param inputStream 图片流
     * @return 经度
     */
    public static String getPictureLongitude(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片纬度
     * @param filePath 图片路径
     * @return 纬度
     */
    public static String getPictureLatitude(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片纬度
     * @param inputStream 图片流
     * @return 纬度
     */
    public static String getPictureLatitude(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取图片方向
     * @param filePath 图片路径
     * @return 图片方向
     */
    public static Integer getPictureOrientation(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片方向
     * @param inputStream 图片流
     * @return 图片方向
     */
    public static Integer getPictureOrientation(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片经纬度
     * @param filePath 图片路径
     * @return 经纬度
     */
    public static String[] getPictureLocation(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            String[] strings = new String[2];
            ExifInterface exif = new ExifInterface(filePath);
            strings[0] = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            strings[1] = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片经纬度
     * @param inputStream 图片流
     * @return 经纬度
     */
    public static String[] getPictureLocation(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            String[] strings = new String[2];
            ExifInterface exif = new ExifInterface(inputStream);
            strings[0] = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            strings[1] = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片海拔高度
     * @param filePath 图片路径
     * @return 海拔高度（米），如果不存在返回null
     */
    public static Double getPictureAltitude(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttributeDouble(ExifInterface.TAG_GPS_ALTITUDE, 0.0d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片海拔高度
     * @param inputStream 图片流
     * @return 海拔高度（米），如果不存在返回null
     */
    public static Double getPictureAltitude(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeDouble(ExifInterface.TAG_GPS_ALTITUDE, 0.0d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片拍摄时间
     * @param filePath 图片路径
     * @return 拍摄时间字符串，格式为原始EXIF格式
     */
    public static String getPictureDateTime(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片拍摄时间
     * @param inputStream 图片流
     * @return 拍摄时间字符串，格式为原始EXIF格式
     */
    public static String getPictureDateTime(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片原始拍摄时间
     * @param filePath 图片路径
     * @return 原始拍摄时间
     */
    public static String getPictureDateTimeOriginal(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片原始拍摄时间
     * @param inputStream 图片流
     * @return 原始拍摄时间
     */
    public static String getPictureDateTimeOriginal(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备制造商
     * @param filePath 图片路径
     * @return 设备制造商（如"Samsung", "Apple"等）
     */
    public static String getPictureMake(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_MAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备制造商
     * @param inputStream 图片流
     * @return 设备制造商（如"Samsung", "Apple"等）
     */
    public static String getPictureMake(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_MAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备型号
     * @param filePath 图片路径
     * @return 设备型号
     */
    public static String getPictureModel(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备型号
     * @param inputStream 图片流
     * @return 设备型号
     */
    public static String getPictureModel(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片宽度
     * @param filePath 图片路径
     * @return 图片宽度，如果获取失败返回-1
     */
    public static int getPictureWidth(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return -1;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 获取图片宽度
     * @param inputStream 图片流
     * @return 图片宽度，如果获取失败返回-1
     */
    public static int getPictureWidth(InputStream inputStream) {
        if (inputStream == null) {
            return -1;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取图片高度
     * @param filePath 图片路径
     * @return 图片高度，如果获取失败返回-1
     */
    public static int getPictureHeight(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return -1;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取图片高度
     * @param inputStream 图片流
     * @return 图片高度，如果获取失败返回-1
     */
    public static int getPictureHeight(InputStream inputStream) {
        if (inputStream == null) {
            return -1;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取ISO感光度
     * @param filePath 图片路径
     * @return ISO值，如果不存在返回null
     */
    public static String getPictureISO(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_RW2_ISO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取ISO感光度
     * @param inputStream 图片流
     * @return ISO值，如果不存在返回null
     */
    public static String getPictureISO(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_RW2_ISO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取光圈值
     * @param filePath 图片路径
     * @return 光圈值（如"f/2.2"）
     */
    public static String getPictureAperture(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER);
            if (fNumber != null) {
                return "f/" + fNumber;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取光圈值
     * @param  inputStream 图片流
     * @return 光圈值（如"f/2.2"）
     */
    public static String getPictureAperture(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            String fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER);
            if (fNumber != null) {
                return "f/" + fNumber;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取焦距
     * @param filePath 图片路径
     * @return 焦距（如"4.15"表示4.15mm）
     */
    public static String getPictureFocalLength(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取焦距
     * @param inputStream 图片流
     * @return 焦距（如"4.15"表示4.15mm）
     */
    public static String getPictureFocalLength(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取曝光时间
     * @param filePath 图片路径
     * @return 曝光时间（如"1/33"表示1/33秒）
     */
    public static String getPictureExposureTime(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            return exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取曝光时间
     * @param inputStream 图片流
     * @return 曝光时间（如"1/33"表示1/33秒）
     */
    public static String getPictureExposureTime(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查图片是否包含GPS信息
     * @param filePath 图片路径
     * @return true表示包含GPS信息，false表示不包含
     */
    public static boolean hasGpsInfo(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            return lat != null && lon != null && !lat.isEmpty() && !lon.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查图片是否包含GPS信息
     * @param inputStream 图片流
     * @return true表示包含GPS信息，false表示不包含
     */
    public static boolean hasGpsInfo(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }
        try {
            ExifInterface exif = new ExifInterface(inputStream);
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            return lat != null && lon != null && !lat.isEmpty() && !lon.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将EXIF格式的GPS坐标转换为十进制格式
     * @param coordinate EXIF格式的坐标字符串（如"29/1,56/1,4530/100"）
     * @param ref 参考方向（"N","S","E","W"）
     * @return 十进制坐标
     */
    public static double convertToDecimalCoordinate(String coordinate, String ref) {
        if (TextUtils.isEmpty(coordinate) || TextUtils.isEmpty(ref)) {
            return 0.0;
        }

        try {
            String[] parts = coordinate.split(",");
            if (parts.length != 3) {
                return 0.0;
            }

            double degrees = parseRational(parts[0]);
            double minutes = parseRational(parts[1]);
            double seconds = parseRational(parts[2]);

            double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);

            if (ref.equals("S") || ref.equals("W")) {
                decimal = -decimal;
            }

            return decimal;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 解析分数格式的字符串（如"29/1"）
     */
    private static double parseRational(String rational) {
        try {
            String[] parts = rational.split("/");
            if (parts.length == 2) {
                double numerator = Double.parseDouble(parts[0]);
                double denominator = Double.parseDouble(parts[1]);
                return denominator != 0 ? numerator / denominator : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取所有EXIF信息并以Map形式返回
     * @param filePath 图片路径
     * @return 包含所有EXIF信息的Map
     */
    public static Map<String, String> getAllExifInfo(String filePath) {
        Map<String, String> exifMap = new HashMap<>();
        if (TextUtils.isEmpty(filePath)) {
            return exifMap;
        }

        try {
            ExifInterface exif = new ExifInterface(filePath);

            // GPS信息
            exifMap.put("GPS Latitude", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            exifMap.put("GPS Longitude", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            exifMap.put("GPS Altitude", String.valueOf(getPictureAltitude(filePath)));

            // 时间信息
            exifMap.put("DateTime", exif.getAttribute(ExifInterface.TAG_DATETIME));
            exifMap.put("DateTime Original", exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));

            // 设备信息
            exifMap.put("Make", exif.getAttribute(ExifInterface.TAG_MAKE));
            exifMap.put("Model", exif.getAttribute(ExifInterface.TAG_MODEL));
            exifMap.put("Software", exif.getAttribute(ExifInterface.TAG_SOFTWARE));

            // 拍摄参数
            exifMap.put("Width", String.valueOf(getPictureWidth(filePath)));
            exifMap.put("Height", String.valueOf(getPictureHeight(filePath)));
            exifMap.put("ISO", getPictureISO(filePath));
            exifMap.put("Aperture", getPictureAperture(filePath));
            exifMap.put("Focal Length", getPictureFocalLength(filePath));
            exifMap.put("Exposure Time", getPictureExposureTime(filePath));
            exifMap.put("Flash", exif.getAttribute(ExifInterface.TAG_FLASH));
            exifMap.put("White Balance", exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return exifMap;
    }

    /**
     * 获取所有EXIF信息并以Map形式返回
     * @param inputStream 图片流
     * @return 包含所有EXIF信息的Map
     */
    public static Map<String, String> getAllExifInfo(InputStream inputStream) {
        Map<String, String> exifMap = new HashMap<>();
        if (inputStream == null) {
            return exifMap;
        }

        try {
            ExifInterface exif = new ExifInterface(inputStream);

            // GPS信息
            exifMap.put("GPS Latitude", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            exifMap.put("GPS Longitude", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            exifMap.put("GPS Altitude", String.valueOf(getPictureAltitude(inputStream)));

            // 时间信息
            exifMap.put("DateTime", exif.getAttribute(ExifInterface.TAG_DATETIME));
            exifMap.put("DateTime Original", exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));

            // 设备信息
            exifMap.put("Make", exif.getAttribute(ExifInterface.TAG_MAKE));
            exifMap.put("Model", exif.getAttribute(ExifInterface.TAG_MODEL));
            exifMap.put("Software", exif.getAttribute(ExifInterface.TAG_SOFTWARE));

            // 拍摄参数
            exifMap.put("Width", String.valueOf(getPictureWidth(inputStream)));
            exifMap.put("Height", String.valueOf(getPictureHeight(inputStream)));
            exifMap.put("ISO", getPictureISO(inputStream));
            exifMap.put("Aperture", getPictureAperture(inputStream));
            exifMap.put("Focal Length", getPictureFocalLength(inputStream));
            exifMap.put("Exposure Time", getPictureExposureTime(inputStream));
            exifMap.put("Flash", exif.getAttribute(ExifInterface.TAG_FLASH));
            exifMap.put("White Balance", exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return exifMap;
    }

    /**
     * 复制EXIF信息从源文件到目标文件
     * @param sourceFilePath 源文件路径
     * @param destFilePath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyExifInfo(String sourceFilePath, String destFilePath) {
        if (TextUtils.isEmpty(sourceFilePath) || TextUtils.isEmpty(destFilePath)) {
            return false;
        }

        try {
            ExifInterface sourceExif = new ExifInterface(sourceFilePath);
            ExifInterface destExif = new ExifInterface(destFilePath);

            // 复制所有常用标签
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LATITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LATITUDE_REF);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LONGITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LONGITUDE_REF);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_ALTITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_ALTITUDE_REF);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME_ORIGINAL);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME_DIGITIZED);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_MAKE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_MODEL);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_EXPOSURE_TIME);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_F_NUMBER);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_RW2_ISO);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_FOCAL_LENGTH);

            destExif.saveAttributes();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制EXIF信息从源文件到目标文件
     * @param sourceFileInputStream 源文件流
     * @param destFilePath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyExifInfo(InputStream sourceFileInputStream, String destFilePath) {
        if (sourceFileInputStream == null || TextUtils.isEmpty(destFilePath)) {
            return false;
        }

        try {
            ExifInterface sourceExif = new ExifInterface(sourceFileInputStream);
            ExifInterface destExif = new ExifInterface(destFilePath);

            // 复制所有常用标签
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LATITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LATITUDE_REF);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LONGITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_LONGITUDE_REF);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_ALTITUDE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_GPS_ALTITUDE_REF);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME_ORIGINAL);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_DATETIME_DIGITIZED);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_MAKE);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_MODEL);

            copyExifTag(sourceExif, destExif, ExifInterface.TAG_EXPOSURE_TIME);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_F_NUMBER);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_RW2_ISO);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_FOCAL_LENGTH);

            destExif.saveAttributes();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制单个EXIF标签
     */
    private static void copyExifTag(ExifInterface source, ExifInterface dest, String tag) {
        try {
            String value = source.getAttribute(tag);
            if (!TextUtils.isEmpty(value)) {
                dest.setAttribute(tag, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取图片文件大小（单位：KB）
     * @param filePath 文件路径
     * @return 文件大小（KB），如果文件不存在返回-1
     */
    public static long getFileSizeInKB(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return -1;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length() / 1024;
        }
        return -1;
    }

    /**
     * 获取图片文件大小（带单位格式化）
     * @param filePath 文件路径
     * @return 格式化后的文件大小字符串（如"2.5 MB"）
     */
    @SuppressLint("DefaultLocale")
    public static String getFormattedFileSize(String filePath) {
        long sizeInBytes = getFileSizeInKB(filePath) * 1024;
        if (sizeInBytes < 0) {
            return "Unknown";
        }

        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText) {
        return createWatermark(originalBitmap, watermarkText, 100);
    }

    /**
     * 添加图片水印
     *
     * @param originalBitmap 图片
     * @param watermarkText  水印文字，默认添加时间水印，在mark内容的上一行
     * @return 添加水印后的图片
     */
    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText, int alpha) {
        if (watermarkText == null) {
            return originalBitmap;
        }
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 255) {
            alpha = 255;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint p = new Paint();
        // 水印颜色,先不透明
        p.setColor(Color.argb(255, 169, 169, 169));
        // 水印字体大小
        p.setTextSize(32);
        // 抗锯齿
        p.setAntiAlias(true);
        // 绘制图像
        canvas.drawBitmap(originalBitmap, 0, 0, p);

        // 绘制文字
        canvas.save();
        canvas.rotate(-30);
        float textWidth = p.measureText(watermarkText);
        int index = 0;
        for (int positionY = height / 10; positionY <= height; positionY += height / 10 + 80) {
            float fromX = -width + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < width; positionX += textWidth * 2) {
                int spacing = 0;
                // 保存文字透明度// 间距
                p.setAlpha(alpha);
                canvas.drawText(watermarkText, positionX, positionY + spacing, p);
                // 恢复文字透明度
            }
        }
        canvas.restore();
        return resultBitmap;
    }

    /**
     * @param absolutePath 照片的绝对路劲
     * @return 重新调整方向之后的bitmap图片
     * @author yukaida
     */
    public static Bitmap orientation(String absolutePath) {
        Bitmap bitmapOr = BitmapFactory.decodeFile(absolutePath);
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmapOr = Bitmap.createBitmap(bitmapOr, 0, 0, bitmapOr.getWidth(), bitmapOr.getHeight(), matrix, true);
            // rotating bitmap
            return bitmapOr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存bitmap
     *
     * @param bmp      源图片
     * @param filePath 源文件路径，保存的话直接替换源文件
     */
    public static void saveBitmap(Bitmap bmp, String filePath) {
        try { // 获取SDCard指定目录下
            File dirFile = new File(filePath);
            //目录转化成文件夹
            if (!dirFile.getParentFile().exists()) {
                //如果不存在，那就建立这个文件夹
                dirFile.getParentFile().mkdirs();
            }                          //文件夹有啦，就可以保存图片啦
            FileOutputStream out = new FileOutputStream(dirFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getDefaultBasePath(Context mContext) {
        String packageName = mContext.getPackageName();
        String[] packageArr = packageName.split("\\.");
        if (packageArr.length == 0) {
            return "";
        }
        if (packageArr.length == 1) {
            return packageArr[0];
        }
        return packageArr[1];
    }

    public static String getLastPath(String path, String defaultPath) {
        if (TextUtils.isEmpty(path)) {
            return defaultPath;
        }
        String[] pathArr = path.split(File.separator);
        if (pathArr == null) {
            return defaultPath;
        }
        if (pathArr.length == 0) {
            return defaultPath;
        }
        if (pathArr.length == 1) {
            return pathArr[0];
        }
        if (File.separator.equals(pathArr[pathArr.length - 1])) {
            return TextUtils.isEmpty(pathArr[pathArr.length - 2]) ? defaultPath : pathArr[pathArr.length - 2];
        }
        return TextUtils.isEmpty(pathArr[pathArr.length - 1]) ? defaultPath : pathArr[pathArr.length - 1];
    }


    /**
     * 获取basePath下不重复的文件名
     *
     * @param basePath  基础目录
     * @param prefix    默认前缀
     * @param extension 扩展名
     * @return 文件名，不带后缀名的
     */
    public static String getNoRepeatFileName(String basePath, String prefix, String extension) {
        File baseFile = new File(basePath);
        if (!baseFile.exists()) {
            boolean isCreated = baseFile.mkdirs();
        }
        String fileName = prefix + MediaUtil.dateFormat(new Date(), MediaUtil.DATE_TIME_FORMAT) + "_" + new Random().nextInt(1000);
        File file = new File(baseFile, fileName + extension);
        int index = 0;
        //防止重名
        while (file.exists()) {
            index += 1;
            file = new File(baseFile, fileName + "_" + index + extension);
        }
        return fileName;
    }

    /**
     * 获取basePath下不重复的文件名
     *
     * @return 如果存储路径中有重复的则自动+1，如果没有则返回文件名
     */
    public static String autoRenameFileName(String baseSavePath, String oldName) {
        try {
            // 检查文件是否有后缀名
            if (!oldName.contains(".")) {
                oldName += "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
            }

            // 拼接完整的文件路径
            String filePath = baseSavePath + File.separator + oldName;

            // 判断文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return oldName;
            }

            // 文件已存在，查找可用的文件名
            int count = 1;
            while (true) {
                String newFileName = oldName.split("\\.")[0] + count + "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
                String newFilePath = baseSavePath + File.separator + newFileName;
                File newFile = new File(newFilePath);
                if (!newFile.exists()) {
                    return newFileName;
                }
                count++;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return oldName;
    }

    /**
     * 格式化日期显示格式
     *
     * @param date   Date对象
     * @param format 格式化后日期格式
     * @return 格式化后的日期显示
     */
    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return dateSimpleFormat(date, formatter);
    }

    /**
     * 将date转成字符串
     *
     * @param date   Date
     * @param format SimpleDateFormat
     *               <br>
     *               注： SimpleDateFormat为空时，采用默认的yyyy-MM-dd HH:mm:ss格式
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String dateSimpleFormat(Date date, SimpleDateFormat format) {
        if (format == null) {
            synchronized (MediaUtil.class) {
                format = defaultDateTimeFormat.get();
            }
        }
        return (date == null ? "" : format.format(date));
    }


    /**
     * yyyy-MM-dd HH:mm:ss格式
     */
    public static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = new ThreadLocal<>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        }
    };

    /**
     * yyyy-MM-dd HH:mm:ss字符串
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyyMMddHHmmss字符串
     */
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
}
