# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#自定义注解混淆
-keep @interface com.casic.otitan.annotation.annotation.VerifyEntity
-keep @interface com.casic.otitan.annotation.annotation.VerifyField
-keep @interface com.casic.otitan.annotation.annotation.VerifyParams
-keep @interface com.casic.otitan.annotation.annotation.VerifyFieldSort
-keep @interface com.casic.otitan.annotation.annotation.Valid
-keep @interface com.casic.otitan.annotation.format.FormatDecimal
-keep class com.casic.otitan.annotation.annotation.bean.VerifyResult

#保留注释
-keepattributes *Annotation*
-keepattributes *Comments*