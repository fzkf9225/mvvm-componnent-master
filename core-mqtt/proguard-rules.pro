# Add project specific ProGuard rules here.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Eclipse Paho MQTT v5
-dontwarn org.eclipse.paho.**
-keep class org.eclipse.paho.** { *; }
