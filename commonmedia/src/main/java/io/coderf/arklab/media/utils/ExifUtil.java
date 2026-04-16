package io.coderf.arklab.media.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import io.coderf.arklab.media.MediaHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * 获取多媒体文件的经纬度等信息
 *
 * @author fz
 * @version 1.0
 * @created 2026/3/20 22:35
 * @since 1.0
 */
public class ExifUtil {

    private static final String TAG = "ExifUtil";

    public static class ExifData {
        public float pitch = 0f;      // 俯仰角
        public float yaw = 0f;        // 偏航角
        public float roll = 0f;       // 翻滚角
        public double latitude = 0.0; // 纬度
        public double longitude = 0.0;// 经度
        public double altitude = 0.0; // 海拔

        @NonNull
        @Override
        public String toString() {
            return "ExifData{" +
                    "pitch=" + pitch +
                    ", yaw=" + yaw +
                    ", roll=" + roll +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", altitude=" + altitude +
                    '}';
        }
    }

    /**
     * 通过Uri获取Exif信息
     *
     * @param context 上下文
     * @param uri     图片Uri
     * @return ExifData对象
     */
    public static ExifData getExifData(Context context, Uri uri) {
        ExifData exifData = new ExifData();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.e(TAG, "InputStream is null for uri: " + uri);
                return exifData;
            }

            ExifInterface exifInterface = new ExifInterface(inputStream);

            // 获取旋转角信息，部分厂商会写在不同的tag中
            // Android ExifInterface 没有标准的 pitch/yaw/roll tag，通常存在于 MakerNote 中，需要特定解析
            // 这里示例尝试读取一般的 "Orientation"
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Field[] fields = ExifInterface.class.getFields();

            for (Field field : fields) {
                if (field.getName().startsWith("TAG_")) {
                    try {
                        String tag = (String) field.get(null);
                        String value = exifInterface.getAttribute(tag);
                        if (value != null) {
                           LogUtil.show(MediaHelper.TAG, field.getName() + " = " + value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{
                            MediaStore.Images.Media.LATITUDE,
                            MediaStore.Images.Media.LONGITUDE
                    },
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                double lat = cursor.getDouble(0);
                double lon = cursor.getDouble(1);
                cursor.close();
            }
            // 可以根据 orientation 转换成 roll/yaw/pitch 简单示例
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    exifData.roll = 0f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    exifData.roll = 90f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    exifData.roll = 180f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    exifData.roll = 270f;
                    break;
                default:
                    exifData.roll = 0f;
                    break;
            }

            // 获取经纬度
            double[] latLong = exifInterface.getLatLong();
            if (latLong != null && latLong.length == 2) {
                exifData.latitude = latLong[0];
                exifData.longitude = latLong[1];
            }

            // 获取海拔
            exifData.altitude = exifInterface.getAltitude(0.0);

            // 获取 MakerNote 中的 pitch/yaw，如果是厂商相机信息，可以在这里扩展
            // Android标准ExifInterface没有直接tag，部分厂商使用 "GPano:PosePitchDegrees" 等
            String pitchStr = exifInterface.getAttribute("GPano:PosePitchDegrees");
            String yawStr = exifInterface.getAttribute("GPano:PoseYawDegrees");
            String rollStr = exifInterface.getAttribute("GPano:PoseRollDegrees");

            if (pitchStr != null) {
                try {
                    exifData.pitch = Float.parseFloat(pitchStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid pitch: " + pitchStr, e);
                }
            }

            if (yawStr != null) {
                try {
                    exifData.yaw = Float.parseFloat(yawStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid yaw: " + yawStr, e);
                }
            }

            if (rollStr != null) {
                try {
                    exifData.roll = Float.parseFloat(rollStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid roll: " + rollStr, e);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Failed to read Exif from uri: " + uri, e);
        }

        return exifData;
    }
    /**
     * 通过文件路径写入EXIF信息
     */
    public static void writeExifByFile(
            String filePath,
            double latitude,
            double longitude,
            double altitude,
            float pitch,
            float yaw,
            float roll
    ) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "文件不存在: " + filePath);
                return;
            }

            ExifInterface exif = new ExifInterface(filePath);

            // 1️⃣ GPS 信息
            Location location = new Location("custom");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAltitude(altitude);
            exif.setGpsInfo(location);

            // 设置GPS参考方向
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");

            // 2️⃣ 姿态信息（保存到USER_COMMENT）
            String attitude = "{\"pitch\":" + pitch +
                    ",\"yaw\":" + yaw +
                    ",\"roll\":" + roll + "}";
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, attitude);

            // 可选：保存到MAKER_NOTE（部分相机支持）
            exif.setAttribute(ExifInterface.TAG_MAKER_NOTE, attitude);

            // 3️⃣ 保存
            exif.saveAttributes();

            Log.d(TAG, "EXIF写入成功: lat=" + latitude + ", lon=" + longitude);

        } catch (Exception e) {
            Log.e(TAG, "写入EXIF失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通过Uri写入EXIF信息（适用于Android 10+）
     */
    public static void writeExifByUri(
            Context context,
            Uri uri,
            double latitude,
            double longitude,
            double altitude,
            float pitch,
            float yaw,
            float roll
    ) {
        ParcelFileDescriptor pfd = null;
        OutputStream outputStream = null;
        try {
            // Android 10+ 需要使用openFileDescriptor
            pfd = context.getContentResolver().openFileDescriptor(uri, "rw");
            if (pfd != null) {
                ExifInterface exif = new ExifInterface(pfd.getFileDescriptor());

                // 1️⃣ GPS 信息
                Location location = new Location("custom");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                exif.setGpsInfo(location);

                // 设置GPS参考方向
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");

                // 2️⃣ 姿态信息
                String attitude = "{\"pitch\":" + pitch +
                        ",\"yaw\":" + yaw +
                        ",\"roll\":" + roll + "}";
                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, attitude);

                // 3️⃣ 保存
                exif.saveAttributes();

                Log.d(TAG, "EXIF写入成功(通过Uri): lat=" + latitude + ", lon=" + longitude);
            }
        } catch (Exception e) {
            Log.e(TAG, "写入EXIF失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pfd != null) {
                    pfd.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void writeExifByFile(
            Context context,
            Uri uri,
            double latitude,
            double longitude,
            double altitude,
            float pitch,
            float yaw,
            float roll
    ) {
        InputStream inputStream = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            inputStream = contentResolver.openInputStream(uri);
            if(inputStream!=null){
                ExifInterface exif = new ExifInterface(inputStream);

                // 1️⃣ GPS 信息
                Location location = new Location("custom");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                exif.setGpsInfo(location);

                // 2️⃣ 姿态信息（建议 JSON）
                String attitude = "{\"pitch\":" + pitch +
                        ",\"yaw\":" + yaw +
                        ",\"roll\":" + roll + "}";

                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, attitude);
                exif.setAltitude(180.0);
                exif.setLatLong(131.54878,31.15454);
                // 3️⃣ 保存
                exif.saveAttributes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
