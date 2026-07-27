# annotation 为 java-library（JAR），Android App 不会自动读取本文件。
# 同步规则已放入 src/main/resources/META-INF/proguard/，R8 会从 JAR 自动合并。

-keep class io.coderf.arklab.annotation.** { *; }
-dontwarn io.coderf.arklab.annotation.**

-keep @interface io.coderf.arklab.annotation.annotation.VerifyEntity
-keep @interface io.coderf.arklab.annotation.annotation.VerifyField
-keep @interface io.coderf.arklab.annotation.annotation.VerifyParams
-keep @interface io.coderf.arklab.annotation.annotation.VerifySort
-keep @interface io.coderf.arklab.annotation.annotation.Valid
-keep @interface io.coderf.arklab.annotation.annotation.VerifyArray
-keep @interface io.coderf.arklab.annotation.annotation.VerifyWhen
-keep @interface io.coderf.arklab.annotation.annotation.VerifyWhenAll
-keep @interface io.coderf.arklab.annotation.annotation.VerifyCrossField
-keep @interface io.coderf.arklab.annotation.annotation.VerifyCrossFields
-keep @interface io.coderf.arklab.annotation.format.FormatDecimal

-keep class io.coderf.arklab.annotation.bean.VerifyResult { *; }
-keep class io.coderf.arklab.annotation.bean.FieldVerifyError { *; }
-keep class io.coderf.arklab.annotation.verify.** { *; }
-keep class io.coderf.arklab.annotation.enums.** { *; }
-keep class io.coderf.arklab.annotation.inter.** { *; }
-keep class io.coderf.arklab.annotation.utils.** { *; }

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable,LocalVariableTable
