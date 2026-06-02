package io.coderf.arklab.mqttcomponent.mqtt

import android.util.Log

/**
 * MQTT 日志接口，宿主 App 可注入自定义实现（上报、开关控制等）。
 *
 * Java 接入示例：
 * ```
 * MqttConnection connection = new MqttConnection("MyTag", new MqttLogger() {
 *     @Override
 *     public void log(String tag, String message) {
 *         Log.i(tag, message);
 *     }
 * });
 * ```
 */
fun interface MqttLogger {

    fun log(tag: String, message: String)

    companion object {

        /** 默认实现：输出到 Android Log.d */
        @JvmField
        val DEFAULT: MqttLogger = MqttLogger { tag, message -> Log.d(tag, message) }
    }
}
