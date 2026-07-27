# Keep annotation runtime validation (EntityValidator reflects annotations).
# Packaged into JAR so R8 auto-merges when App minifyEnabled=true.

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
