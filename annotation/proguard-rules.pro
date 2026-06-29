# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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
-keep class io.coderf.arklab.annotation.verify.EntityValidator { *; }
-keep class io.coderf.arklab.annotation.enums.** { *; }
-keep class io.coderf.arklab.annotation.inter.** { *; }

-keepattributes *Annotation*
-keepattributes *Comments*
