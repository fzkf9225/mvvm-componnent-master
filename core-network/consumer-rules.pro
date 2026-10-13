# core-network：Retrofit / OkHttp / Rx / 新请求 API
# 第三方库（Retrofit / OkHttp / Okio / Gson / RxJava3）AAR 自带 consumer-rules，
# 宿主 R8 会自动合并，此处不再整包 keep，避免包体积膨胀与优化失效。
# 库自身类被宿主直接引用时由 R8 按引用自动保留，仅反射访问点需要显式 keep。

# 反射/注解必需属性（BaseConverterFactory 的 TypeToken 泛型、Retrofit 参数注解）
-keepattributes Signature,Exceptions,AnnotationDefault,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Retrofit 动态代理接口：方法由注解驱动，保留方法但允许混淆与裁剪（官方同款）
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ApiServiceWrapper 以方法名字符串分发 getRetrofit/setRetrofit，方法名不可混淆
-keep interface io.coderf.arklab.common.inter.ApiRetrofitService {
    <methods>;
}

# RepositoryFactory 反射创建宿主 Repository 子类，构造器不可被 R8 裁剪
-keepclassmembers class * extends io.coderf.arklab.common.base.BaseRepository {
    <init>(...);
}
