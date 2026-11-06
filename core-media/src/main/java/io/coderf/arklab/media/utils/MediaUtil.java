package io.coderf.arklab.media.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * MediaUtil 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2021/4/12 10:35
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
     * 查询 Uri 对应文件大小（字节）。失败返回 -1。
     */
    public static long queryUriSize(@Nullable Context context, @Nullable Uri uri) {
        if (context == null || uri == null) {
            return -1;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 获取年月日时分秒字符串，用于文件名
     * @return yyyyMMddHHmmssSSS
     */
    public static String getCurrentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
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
            return formatLongitude(readLatLong(exif));
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
            return formatLongitude(readLatLong(exif));
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
            return formatLatitude(readLatLong(exif));
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
            return formatLatitude(readLatLong(exif));
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
            double[] latLong = readLatLong(exif);
            if (latLong == null) {
                return null;
            }
            strings[0] = Double.toString(latLong[1]);
            strings[1] = Double.toString(latLong[0]);
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
            double[] latLong = readLatLong(exif);
            if (latLong == null) {
                return null;
            }
            strings[0] = Double.toString(latLong[1]);
            strings[1] = Double.toString(latLong[0]);
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
            return readAltitude(exif);
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
            return readAltitude(exif);
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
            int width = new ExifInterface(filePath).getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
            if (width > 0) {
                return width;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[] bounds = decodePictureBounds(filePath);
        return bounds[0] > 0 ? bounds[0] : -1;
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
            int height = new ExifInterface(filePath).getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
            if (height > 0) {
                return height;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[] bounds = decodePictureBounds(filePath);
        return bounds[1] > 0 ? bounds[1] : -1;
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
            return readIso(exif);
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
            return readIso(exif);
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
            return readAperture(exif);
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
            return readAperture(exif);
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
            return readFocalLength(exif);
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
            return readFocalLength(exif);
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
            return readExposureTime(exif);
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
            return readExposureTime(exif);
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
            return readLatLong(exif) != null;
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
            return readLatLong(exif) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将 EXIF 格式的 GPS 坐标转换为十进制格式。解析失败时返回 {@link Double#NaN}，避免和合法的 0 度混淆。
     *
     * @param coordinate EXIF 格式的坐标字符串（如 "29/1,56/1,4530/100"）
     * @param ref        参考方向（"N","S","E","W"）
     * @return 十进制坐标，失败时为 NaN
     */
    public static double convertToDecimalCoordinate(String coordinate, String ref) {
        if (TextUtils.isEmpty(coordinate) || TextUtils.isEmpty(ref)) {
            return Double.NaN;
        }

        try {
            String[] parts = coordinate.split(",");
            if (parts.length != 3) {
                return Double.NaN;
            }

            double degrees = parseRational(parts[0]);
            double minutes = parseRational(parts[1]);
            double seconds = parseRational(parts[2]);

            double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);

            if ("S".equalsIgnoreCase(ref) || "W".equalsIgnoreCase(ref)) {
                decimal = -decimal;
            }

            return decimal;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.NaN;
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

    private static double[] readLatLong(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        double[] latLong = exif.getLatLong();
        if (latLong == null || latLong.length < 2) {
            return null;
        }
        return latLong;
    }

    private static String formatLongitude(double[] latLong) {
        if (latLong == null || latLong.length < 2) {
            return null;
        }
        return Double.toString(latLong[1]);
    }

    private static String formatLatitude(double[] latLong) {
        if (latLong == null || latLong.length < 2) {
            return null;
        }
        return Double.toString(latLong[0]);
    }

    private static Double readAltitude(ExifInterface exif) {
        if (exif == null || TextUtils.isEmpty(exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE))) {
            return null;
        }
        double altitude = exif.getAttributeDouble(ExifInterface.TAG_GPS_ALTITUDE, Double.NaN);
        if (Double.isNaN(altitude)) {
            return null;
        }
        if (exif.getAttributeInt(ExifInterface.TAG_GPS_ALTITUDE_REF, 0) == 1) {
            altitude = -altitude;
        }
        return altitude;
    }

    private static String readIso(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        String iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
        if (TextUtils.isEmpty(iso)) {
            iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
        }
        if (TextUtils.isEmpty(iso)) {
            iso = exif.getAttribute(ExifInterface.TAG_RW2_ISO);
        }
        return iso;
    }

    private static String readAperture(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        double fNumber = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, Double.NaN);
        if (Double.isNaN(fNumber) || fNumber <= 0d) {
            return null;
        }
        return "f/" + formatDecimal(fNumber);
    }

    private static String readFocalLength(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, Double.NaN);
        if (Double.isNaN(focalLength) || focalLength <= 0d) {
            return null;
        }
        return formatDecimal(focalLength);
    }

    private static String readExposureTime(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        double seconds = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, Double.NaN);
        if (Double.isNaN(seconds) || seconds <= 0d) {
            return exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        }
        if (seconds >= 1d) {
            return formatDecimal(seconds);
        }
        int denominator = (int) Math.round(1d / seconds);
        if (denominator <= 0) {
            return exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        }
        return "1/" + denominator;
    }

    private static String formatDecimal(double value) {
        String formatted = String.format(Locale.US, "%.2f", value);
        formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
        return formatted;
    }

    private static int[] decodePictureBounds(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    private static void fillExifMap(ExifInterface exif, Map<String, String> exifMap) {
        double[] latLong = readLatLong(exif);
        exifMap.put("GPS Latitude", latLong == null ? null : Double.toString(latLong[0]));
        exifMap.put("GPS Longitude", latLong == null ? null : Double.toString(latLong[1]));
        Double altitude = readAltitude(exif);
        exifMap.put("GPS Altitude", altitude == null ? null : String.valueOf(altitude));
        exifMap.put("DateTime", exif.getAttribute(ExifInterface.TAG_DATETIME));
        exifMap.put("DateTime Original", exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));
        exifMap.put("Make", exif.getAttribute(ExifInterface.TAG_MAKE));
        exifMap.put("Model", exif.getAttribute(ExifInterface.TAG_MODEL));
        exifMap.put("Software", exif.getAttribute(ExifInterface.TAG_SOFTWARE));
        exifMap.put("Width", String.valueOf(exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)));
        exifMap.put("Height", String.valueOf(exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)));
        exifMap.put("ISO", readIso(exif));
        exifMap.put("Aperture", readAperture(exif));
        exifMap.put("Focal Length", readFocalLength(exif));
        exifMap.put("Exposure Time", readExposureTime(exif));
        exifMap.put("Flash", exif.getAttribute(ExifInterface.TAG_FLASH));
        exifMap.put("White Balance", exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
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
            fillExifMap(new ExifInterface(filePath), exifMap);
            int[] bounds = decodePictureBounds(filePath);
            if (bounds[0] > 0) {
                exifMap.put("Width", String.valueOf(bounds[0]));
            }
            if (bounds[1] > 0) {
                exifMap.put("Height", String.valueOf(bounds[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exifMap;
    }

    /**
     * 获取所有EXIF信息并以Map形式返回
     * @param inputStream 图片流。流会被读完，调用方不要再重复读取
     * @return 包含所有EXIF信息的Map
     */
    public static Map<String, String> getAllExifInfo(InputStream inputStream) {
        Map<String, String> exifMap = new HashMap<>();
        if (inputStream == null) {
            return exifMap;
        }
        try {
            fillExifMap(new ExifInterface(inputStream), exifMap);
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
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_ISO_SPEED_RATINGS);
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
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
            copyExifTag(sourceExif, destExif, ExifInterface.TAG_ISO_SPEED_RATINGS);
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
    public static String getFormattedFileSize(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "Unknown";
        }
        File file = new File(filePath);
        if (!file.isFile()) {
            return "Unknown";
        }
        long sizeInBytes = file.length();
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024L * 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.US, "%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText) {
        return createWatermark(originalBitmap, watermarkText, 100);
    }

    /**
     * 添加图片水印
     *
     * @param originalBitmap 图片
     * @param watermarkText  水印文字
     * @param alpha          水印透明度，0-255
     * @return 添加水印后的图片
     */
    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText, int alpha) {
        return createWatermark(originalBitmap, watermarkText, alpha, 32f, Color.rgb(169, 169, 169));
    }

    /**
     * 添加图片水印
     *
     * @param originalBitmap 图片
     * @param watermarkText  水印文字
     * @param alpha          水印透明度，0-255
     * @param textSize       水印文字大小，单位 px
     * @param textColor      水印文字颜色
     * @return 添加水印后的图片
     */
    public static Bitmap createWatermark(Bitmap originalBitmap, String watermarkText, int alpha, float textSize, @ColorInt int textColor) {
        if (originalBitmap == null || TextUtils.isEmpty(watermarkText)) {
            return originalBitmap;
        }
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 255) {
            alpha = 255;
        }
        if (textSize <= 0) {
            textSize = 32f;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return originalBitmap;
        }
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        float textWidth = paint.measureText(watermarkText);
        if (textWidth <= 0f) {
            return resultBitmap;
        }
        canvas.save();
        canvas.rotate(-30, width / 2f, height / 2f);
        float range = (float) Math.hypot(width, height);
        int stepY = Math.max(height / 10 + 80, 1);
        int index = 0;
        for (float positionY = -range; positionY <= range; positionY += stepY) {
            float fromX = -range + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < range; positionX += textWidth * 2) {
                paint.setAlpha(alpha);
                canvas.drawText(watermarkText, positionX, positionY, paint);
            }
        }
        canvas.restore();
        return resultBitmap;
    }

    /**
     * 按 EXIF 方向纠正图片。不限制解码尺寸。
     *
     * @param absolutePath 照片绝对路径
     * @return 纠正方向后的图片；解码失败返回 null
     */
    public static Bitmap orientation(String absolutePath) {
        return orientation(absolutePath, 0);
    }

    /**
     * 按 EXIF 方向纠正图片，并按最长边采样，避免大图整图解码。
     *
     * @param absolutePath 照片绝对路径
     * @param maxSide      最长边像素上限，小于等于 0 时不缩放
     * @return 纠正方向后的图片；解码失败返回 null。方向读取失败时返回未旋转的原图
     */
    public static Bitmap orientation(String absolutePath, int maxSide) {
        if (TextUtils.isEmpty(absolutePath)) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSide);
        Bitmap bitmapOr = BitmapFactory.decodeFile(absolutePath, options);
        if (bitmapOr == null) {
            return null;
        }
        try {
            ExifInterface exif = new ExifInterface(absolutePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.postRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.postRotate(270);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmapOr;
            }
            Bitmap rotated = Bitmap.createBitmap(bitmapOr, 0, 0, bitmapOr.getWidth(), bitmapOr.getHeight(), matrix, true);
            if (rotated != bitmapOr) {
                bitmapOr.recycle();
            }
            return rotated;
        } catch (Exception e) {
            e.printStackTrace();
            return bitmapOr;
        }
    }

    private static int calculateInSampleSize(int width, int height, int maxSide) {
        if (maxSide <= 0 || width <= 0 || height <= 0) {
            return 1;
        }
        int inSampleSize = 1;
        int max = Math.max(width, height);
        while (max / inSampleSize > maxSide) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    /**
     * 保存 bitmap。路径没有父目录时不会因为空指针中断。
     *
     * @param bmp      源图片
     * @param filePath 保存路径，已存在则覆盖
     * @return 是否写入成功
     */
    public static boolean saveBitmap(Bitmap bmp, String filePath) {
        if (bmp == null || TextUtils.isEmpty(filePath)) {
            return false;
        }
        File dest = new File(filePath);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }
        String extension = "";
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = filePath.substring(dotIndex);
        }
        try (FileOutputStream out = new FileOutputStream(dest)) {
            if (!bmp.compress(compressFormatFromExtension(extension), 100, out)) {
                return false;
            }
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据文件扩展名推断 Bitmap 压缩格式（用于压缩/水印等输出）
     *
     * @param extension 扩展名，如 .jpg、.png；不含点号时也可识别
     * @return 对应的压缩格式，默认 JPEG
     */
    public static Bitmap.CompressFormat compressFormatFromExtension(String extension) {
        if (TextUtils.isEmpty(extension)) {
            return Bitmap.CompressFormat.JPEG;
        }
        String normalized = extension.startsWith(".") ? extension.toLowerCase() : ("." + extension).toLowerCase();
        if (".png".equals(normalized)) {
            return Bitmap.CompressFormat.PNG;
        }
        if (".webp".equals(normalized)) {
            return Bitmap.CompressFormat.WEBP;
        }
        return Bitmap.CompressFormat.JPEG;
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
        return index == 0 ? fileName : fileName + "_" + index;
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
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
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
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.US);
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
