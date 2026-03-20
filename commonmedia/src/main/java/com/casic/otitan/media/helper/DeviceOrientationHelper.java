package com.casic.otitan.media.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

/**
 * 获取手机俯角仰角等信息
 *
 * @author fz
 * @version 1.0
 * @created 2026/3/20 23:22
 * @since 1.0
 */
public class DeviceOrientationHelper {

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;

    private float yaw;   // 偏航角
    private float pitch; // 俯仰角
    private float roll;  // 翻滚角

    private boolean hasData = false;

    public DeviceOrientationHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    private final SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // 转角度
            yaw = (float) Math.toDegrees(orientation[0]);   // 方位角
            pitch = (float) Math.toDegrees(orientation[1]); // 俯仰角
            roll = (float) Math.toDegrees(orientation[2]);  // 翻滚角

            hasData = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // 开始监听
    public void start() {
        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // 停止监听
    public void stop() {
        sensorManager.unregisterListener(listener);
    }

    // 获取数据（拍照时调用这个）
    public OrientationData getCurrentOrientation() {
        if (!hasData) return null;

        return new OrientationData(yaw, pitch, roll);
    }

    // 数据结构
    public static class OrientationData {
        public float yaw;
        public float pitch;
        public float roll;

        public OrientationData(float yaw, float pitch, float roll) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        @NonNull
        @Override
        public String toString() {
            return "OrientationData{" +
                    "yaw=" + yaw +
                    ", pitch=" + pitch +
                    ", roll=" + roll +
                    '}';
        }
    }
}
