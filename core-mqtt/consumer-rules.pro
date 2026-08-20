# Eclipse Paho MQTT v5
-dontwarn org.eclipse.paho.**
-keep class org.eclipse.paho.** { *; }

# 对外 API（连接 / 异步门面 / Presence / UI）
-keep public class io.coderf.arklab.mqtt.** { public *; }
