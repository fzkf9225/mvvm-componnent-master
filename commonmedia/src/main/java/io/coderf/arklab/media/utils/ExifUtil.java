package io.coderf.arklab.media.utils;


import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 拍照元数据读写：GPS 使用标准 EXIF 标签，姿态角写入 USER_COMMENT（JSON）。
 * 写入失败时不抛异常，不影响原有拍照流程。
 */
public class ExifUtil {

    private static final String TAG = "ExifUtil";

    /** USER_COMMENT 前缀，便于与系统/其他应用注释区分 */
    public static final String META_PREFIX = "ARKLAB_META:";

    public static class CaptureMetadata {
        public float pitch;
        public float yaw;
        public float roll;
        public double latitude;
        public double longitude;
        public double height;
        public boolean hasOrientation;
        public boolean hasLocation;

        public boolean hasWritableData() {
            return hasOrientation || hasLocation;
        }

        @Override
        public String toString() {
            return "CaptureMetadata{" +
                    "pitch=" + pitch +
                    ", yaw=" + yaw +
                    ", roll=" + roll +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", height=" + height +
                    ", hasOrientation=" + hasOrientation +
                    ", hasLocation=" + hasLocation +
                    '}';
        }
    }

    public static class ExifData {
        public float pitch;
        public float yaw;
        public float roll;
        public double latitude;
        public double longitude;
        /** 海拔高程（米），与 height 同义 */
        public double altitude;
        /** 拍照海拔，读取时与 altitude 一致 */
        public double height;
        /** 是否解析到本库/厂商写入的姿态元数据（不含仅 TAG_ORIENTATION 推导的 roll） */
        public boolean hasParsedCaptureAttitude;
        /** 是否解析到 EXIF GPS 经纬度 */
        public boolean hasParsedGps;

        @Override
        public String toString() {
            return "ExifData{" +
                    "pitch=" + pitch +
                    ", yaw=" + yaw +
                    ", roll=" + roll +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", height=" + height +
                    '}';
        }

        public String toDisplayString() {
            return "俯仰角(pitch): " + pitch + "°\n"
                    + "偏航角(yaw): " + yaw + "°\n"
                    + "翻滚角(roll): " + roll + "°\n"
                    + "经度(longitude): " + longitude + "\n"
                    + "纬度(latitude): " + latitude + "\n"
                    + "海拔(height): " + height + " m";
        }
    }

    /**
     * 通过 Uri 读取拍照元数据（优先解析本库写入的 USER_COMMENT，其次标准 GPS / GPano 标签）。
     */
    public static ExifData getExifData(Context context, Uri uri) {
        ExifData exifData = new ExifData();
        if (context == null || uri == null) {
            return exifData;
        }
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.w(TAG, "openInputStream null: " + uri);
                return exifData;
            }
            fillFromExifInterface(new ExifInterface(inputStream), exifData);
        } catch (IOException e) {
            Log.w(TAG, "read exif failed: " + uri, e);
        }
        exifData.height = exifData.altitude;
        return exifData;
    }

    private static void fillFromExifInterface(ExifInterface exifInterface, ExifData exifData) {
        parseAttitudeFromUserComment(exifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT), exifData);
        if (!hasAttitude(exifData)) {
            parseAttitudeFromUserComment(exifInterface.getAttribute(ExifInterface.TAG_MAKER_NOTE), exifData);
        }
        if (!hasAttitude(exifData)) {
            parseGpanoAttitude(exifInterface, exifData);
        }
        if (!hasAttitude(exifData)) {
            mapOrientationToRoll(exifInterface, exifData);
        }

        double[] latLong = exifInterface.getLatLong();
        if (latLong != null && latLong.length == 2) {
            exifData.latitude = latLong[0];
            exifData.longitude = latLong[1];
            exifData.hasParsedGps = true;
        }
        if (exifData.hasParsedGps) {
            exifData.altitude = exifInterface.getAltitude(0.0);
            exifData.height = exifData.altitude;
        }
    }

    /**
     * 是否包含可同步的拍照元数据（姿态或 GPS），用于压缩后拷贝等场景。
     */
    public static boolean hasReadableCaptureMetadata(@Nullable ExifData exifData) {
        return exifData != null && (exifData.hasParsedCaptureAttitude || exifData.hasParsedGps);
    }

    /**
     * 将源图拍照元数据写入目标图；源图无元数据或源/目标相同则不写入，兼容旧逻辑。
     *
     * @return 是否执行了写入尝试且成功
     */
    public static boolean copyCaptureMetadataIfPresent(Context context, Uri sourceUri, Uri targetUri) {
        if (context == null || sourceUri == null || targetUri == null || sourceUri.equals(targetUri)) {
            return false;
        }
        ExifData exifData = getExifData(context, sourceUri);
        if (!hasReadableCaptureMetadata(exifData)) {
            return false;
        }
        return tryWriteCaptureMetadata(context, targetUri, toCaptureMetadata(exifData));
    }

    public static CaptureMetadata toCaptureMetadata(ExifData exifData) {
        CaptureMetadata metadata = new CaptureMetadata();
        if (exifData.hasParsedCaptureAttitude) {
            metadata.pitch = exifData.pitch;
            metadata.yaw = exifData.yaw;
            metadata.roll = exifData.roll;
            metadata.hasOrientation = true;
        }
        if (exifData.hasParsedGps) {
            metadata.latitude = exifData.latitude;
            metadata.longitude = exifData.longitude;
            metadata.height = exifData.height != 0d ? exifData.height : exifData.altitude;
            metadata.hasLocation = true;
        }
        return metadata;
    }

    private static boolean hasAttitude(ExifData exifData) {
        return exifData.pitch != 0f || exifData.yaw != 0f || exifData.roll != 0f;
    }

    private static void parseGpanoAttitude(ExifInterface exifInterface, ExifData exifData) {
        boolean[] parsed = {false};
        parseFloatTag(exifInterface.getAttribute("GPano:PosePitchDegrees"), value -> {
            exifData.pitch = value;
            parsed[0] = true;
        });
        parseFloatTag(exifInterface.getAttribute("GPano:PoseYawDegrees"), value -> {
            exifData.yaw = value;
            parsed[0] = true;
        });
        parseFloatTag(exifInterface.getAttribute("GPano:PoseRollDegrees"), value -> {
            exifData.roll = value;
            parsed[0] = true;
        });
        if (parsed[0]) {
            exifData.hasParsedCaptureAttitude = true;
        }
    }

    private static void mapOrientationToRoll(ExifInterface exifInterface, ExifData exifData) {
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
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
    }

    private static void parseFloatTag(@Nullable String value, FloatConsumer consumer) {
        if (value == null) {
            return;
        }
        try {
            consumer.accept(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid float tag: " + value);
        }
    }

    private interface FloatConsumer {
        void accept(float value);
    }

    private static void parseAttitudeFromUserComment(@Nullable String userComment, ExifData exifData) {
        if (userComment == null || userComment.isEmpty()) {
            return;
        }
        String json = userComment;
        int prefixIndex = userComment.indexOf(META_PREFIX);
        if (prefixIndex >= 0) {
            json = userComment.substring(prefixIndex + META_PREFIX.length()).trim();
        } else if (!userComment.trim().startsWith("{")) {
            return;
        }
        try {
            JSONObject object = new JSONObject(json);
            boolean parsed = false;
            if (object.has("pitch")) {
                exifData.pitch = (float) object.getDouble("pitch");
                parsed = true;
            }
            if (object.has("yaw")) {
                exifData.yaw = (float) object.getDouble("yaw");
                parsed = true;
            }
            if (object.has("roll")) {
                exifData.roll = (float) object.getDouble("roll");
                parsed = true;
            }
            if (parsed) {
                exifData.hasParsedCaptureAttitude = true;
            }
        } catch (Exception e) {
            Log.w(TAG, "parse attitude json failed: " + json, e);
        }
    }

    /**
     * 拍照完成后尽力写入元数据；失败返回 false，不抛异常。
     */
    public static boolean tryWriteCaptureMetadata(Context context, Uri uri, CaptureMetadata metadata) {
        if (context == null || uri == null || metadata == null || !metadata.hasWritableData()) {
            return false;
        }
        if (tryWriteByUri(context, uri, metadata)) {
            return true;
        }
        String filePath = resolveFilePath(context, uri);
        if (filePath != null) {
            return tryWriteByFile(filePath, metadata);
        }
        return false;
    }

    private static boolean tryWriteByUri(Context context, Uri uri, CaptureMetadata metadata) {
        ParcelFileDescriptor pfd = null;
        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "rw");
            if (pfd == null) {
                return false;
            }
            ExifInterface exif = new ExifInterface(pfd.getFileDescriptor());
            applyMetadata(exif, metadata);
            exif.saveAttributes();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "write exif by uri failed: " + uri, e);
            return false;
        } finally {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean tryWriteByFile(String filePath, CaptureMetadata metadata) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
            ExifInterface exif = new ExifInterface(filePath);
            applyMetadata(exif, metadata);
            exif.saveAttributes();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "write exif by file failed: " + filePath, e);
            return false;
        }
    }

    private static void applyMetadata(ExifInterface exif, CaptureMetadata metadata) throws IOException {
        if (metadata.hasLocation) {
            Location location = new Location("capture");
            location.setLatitude(metadata.latitude);
            location.setLongitude(metadata.longitude);
            if (metadata.height != 0d) {
                location.setAltitude(metadata.height);
            }
            exif.setGpsInfo(location);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, metadata.latitude >= 0 ? "N" : "S");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, metadata.longitude >= 0 ? "E" : "W");
        }
        if (metadata.hasOrientation) {
            String attitudeJson = "{\"pitch\":" + metadata.pitch
                    + ",\"yaw\":" + metadata.yaw
                    + ",\"roll\":" + metadata.roll + "}";
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, META_PREFIX + attitudeJson);
        }
    }

    @Nullable
    private static String resolveFilePath(Context context, Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        try (android.database.Cursor cursor = context.getContentResolver().query(
                uri, new String[]{android.provider.MediaStore.MediaColumns.DATA}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * @deprecated 请使用 {@link #tryWriteCaptureMetadata(Context, Uri, CaptureMetadata)}
     */
    @Deprecated
    public static void writeExifByFile(
            String filePath,
            double latitude,
            double longitude,
            double altitude,
            float pitch,
            float yaw,
            float roll
    ) {
        CaptureMetadata metadata = new CaptureMetadata();
        metadata.latitude = latitude;
        metadata.longitude = longitude;
        metadata.height = altitude;
        metadata.hasLocation = true;
        metadata.pitch = pitch;
        metadata.yaw = yaw;
        metadata.roll = roll;
        metadata.hasOrientation = true;
        tryWriteByFile(filePath, metadata);
    }

    /**
     * @deprecated 请使用 {@link #tryWriteCaptureMetadata(Context, Uri, CaptureMetadata)}
     */
    @Deprecated
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
        CaptureMetadata metadata = new CaptureMetadata();
        metadata.latitude = latitude;
        metadata.longitude = longitude;
        metadata.height = altitude;
        metadata.hasLocation = true;
        metadata.pitch = pitch;
        metadata.yaw = yaw;
        metadata.roll = roll;
        metadata.hasOrientation = true;
        tryWriteCaptureMetadata(context, uri, metadata);
    }
}
