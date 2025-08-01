package pers.fz.mvvm.api;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import pers.fz.mvvm.base.BaseConverterFactory;
import pers.fz.mvvm.impl.ApiServiceWrapper;
import pers.fz.mvvm.inter.ApiRetrofitService;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.PropertiesUtil;
import pers.fz.mvvm.util.encode.MD5Util;
import pers.fz.mvvm.util.log.LogUtil;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

/**
 * Created by fz on 2019/8/1.
 * describe：Retrofit初始化
 */
public class ApiRetrofit {
    public static final String TAG = ApiRetrofit.class.getSimpleName();
    private static volatile ApiRetrofit apiRetrofit;

    private final Builder builder;

    private ApiRetrofit(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public static void printLog(final Request request, final Response response) {
        LogUtil.show(TAG, "--------------------Request Start--------------------");
        LogUtil.show(TAG, "Request Method：" + request.method());
        LogUtil.show(TAG, "Request Url：" + request.url());
        LogUtil.show(TAG, "Request Headers：" + request.headers());
        try {
            String contentType = request.header("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                LogUtil.show(TAG, "Request Body：" + bodyToString(request.body()));
            }
        } catch (IOException e) {
            LogUtil.show(TAG, "Request parse error");
        }
        try {
            LogUtil.show(TAG, "Response Headers：" + response.headers());
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            LogUtil.show(TAG, "Response Body：" + responseBody.string());
        } catch (Exception e) {
            LogUtil.show(TAG, "Response parse error");
        }
        LogUtil.show(TAG, "--------------------Request End--------------------");
    }

    public static String bodyToString(final RequestBody request) throws IOException {
        final Buffer buffer = new Buffer();
        if (request != null) {
            request.writeTo(buffer);
        } else {
            return "";
        }
        return buffer.readUtf8();
    }

    public Retrofit getRetrofit() {
        return getBuilder().retrofit;
    }

    /**
     * 建造者模式，创建网络请求对象
     */
    public static class Builder {
        /**
         * 基础url
         */
        private String baseUrl;
        /**
         * appId
         */
        private String appId;
        /**
         * appSecret
         */
        private String appSecret;
        /**
         * 系统标识，在非单例的情况下使用，用来区分不同的请求头
         */
        private String system;
        /**
         * 接口协议版本，只是我们自己的版本不是http，为了方便做兼容
         */
        private String protocolVersion;
        /**
         * 请求头
         */
        private final Map<String, String> headerMap = new HashMap<>();
        /**
         * mContext对象
         */
        private final Context mContext;
        /**
         * retrofit对象
         */
        private Retrofit retrofit = null;
        /**
         * okhttp对象
         */
        private OkHttpClient client = null;
        /**
         * 请求和返回转换器
         */
        private Converter.Factory converterFactory = null;
        /**
         * 拦截器
         */
        private final List<Interceptor> interceptorList = new ArrayList<>();
        /**
         * 请求超时时间
         */
        private long timeOut = 15;
        /**
         * 是否使用默认的加密签名的方式
         */
        private boolean useDefaultSign = false;
        /**
         * 请求头、等等错误路由跳转等服务配置
         */
        private ErrorService errorService = null;
        /**
         * 请求接口重试服务
         */
        private RetryService retryService = null;
        /**
         * 请求成功版本号，如果自定义converterFactory情况下不生效
         */
        private String successCode;
        /**
         * 是否单例模式
         */
        private boolean singleInstance = true;

        public Builder setSuccessCode(String successCode) {
            this.successCode = successCode;
            return this;
        }

        public Builder setErrorService(ErrorService errorService) {
            this.errorService = errorService;
            return this;
        }

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setSingleInstance(boolean singleInstance) {
            this.singleInstance = singleInstance;
            return this;
        }

        public Builder setRetryService(RetryService retryService) {
            this.retryService = retryService;
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setSystem(String system) {
            this.system = system;
            return this;
        }

        public Builder setAppSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public Builder setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder setHeader(Map<String, String> headerMap) {
            if (headerMap == null || headerMap.isEmpty()) {
                return this;
            }
            this.headerMap.putAll(headerMap);
            return this;
        }

        public Builder setClient(OkHttpClient okHttpClient) {
            this.client = okHttpClient;
            return this;
        }

        public Builder setTimeOut(long timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public Builder setRetrofit(Retrofit retrofit) {
            this.retrofit = retrofit;
            return this;
        }

        public Builder setConverterFactory(Converter.Factory converterFactory) {
            this.converterFactory = converterFactory;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptorList.add(interceptor);
            return this;
        }

        public Builder addHeader(String key, String value) {
            headerMap.put(key, value);
            return this;
        }

        public Builder setUseDefaultSign(boolean useDefaultSign) {
            this.useDefaultSign = useDefaultSign;
            return this;
        }

        public ApiRetrofit builder() {
            if (TextUtils.isEmpty(baseUrl)) {
                this.baseUrl = PropertiesUtil.getInstance().loadConfig(mContext).getBaseUrl();
            }
            if (TextUtils.isEmpty(appId)) {
                this.appId = PropertiesUtil.getInstance().loadConfig(mContext).getAppId();
                if (!TextUtils.isEmpty(appId)) {
                    headerMap.put("x-appId", appId);
                }
            }

            if (TextUtils.isEmpty(appSecret)) {
                this.appSecret = PropertiesUtil.getInstance().loadConfig(mContext).getAppSecret();
                if (!TextUtils.isEmpty(appSecret)) {
                    headerMap.put("x-appSecret", appSecret);
                }
            }

            if (TextUtils.isEmpty(protocolVersion)) {
                this.protocolVersion = PropertiesUtil.getInstance().loadConfig(mContext).getProtocolVersion();
                if (!TextUtils.isEmpty(protocolVersion)) {
                    headerMap.put("x-protocolVersion", protocolVersion);
                }
            }
            if (client == null) {
                OkHttpClient.Builder build = new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            String timeStamp = DateUtil.getTimestamp();
                            Request.Builder requestBuilder = chain.request().newBuilder();
                            //判断errorService是否为空
                            if (errorService != null) {
                                headerMap.putAll(errorService.defaultRequestHeader(system));
                            }

                            //添加默认请求头
                            requestBuilder.headers(Headers.of(headerMap));

                            if (useDefaultSign) {
                                requestBuilder.addHeader("x-sign", encodeSign(appSecret, bodyToString(chain.request().body()), timeStamp));
                            }

                            Request request = requestBuilder.build();
                            Response response = chain.proceed(request);
                            if (Config.enableDebug.get()) {
                                printLog(request, response);
                            }
                            return response;
                        })
                        .connectTimeout(timeOut, TimeUnit.SECONDS)
                        .readTimeout(timeOut, TimeUnit.SECONDS)
                        .writeTimeout(timeOut, TimeUnit.SECONDS);
                if (!interceptorList.isEmpty()) {
                    for (Interceptor interceptor : interceptorList) {
                        build.addInterceptor(interceptor);
                    }
                }
                client = build.build();
            }
            if (converterFactory == null) {
                converterFactory = BaseConverterFactory.create(successCode);
            }
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(converterFactory)
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .client(this.client).build();
            }
            if (singleInstance) {
                if (apiRetrofit == null) {
                    synchronized (ApiRetrofit.class) {
                        if (apiRetrofit == null) {
                            apiRetrofit = new ApiRetrofit(this);
                        }
                    }
                }
                return apiRetrofit;
            } else {
                return new ApiRetrofit(this);
            }
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getAppId() {
            return appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public String getSystem() {
            return system;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public Map<String, String> getHeaderMap() {
            return headerMap;
        }

        public Context getmContext() {
            return mContext;
        }

        public Retrofit getRetrofit() {
            return retrofit;
        }

        public OkHttpClient getClient() {
            return client;
        }

        public Converter.Factory getConverterFactory() {
            return converterFactory;
        }

        public List<Interceptor> getInterceptorList() {
            return interceptorList;
        }

        public long getTimeOut() {
            return timeOut;
        }

        public boolean isUseDefaultSign() {
            return useDefaultSign;
        }

        public ErrorService getErrorService() {
            return errorService;
        }

        public RetryService getRetryService() {
            return retryService;
        }

        public String getSuccessCode() {
            return successCode;
        }

        public boolean isSingleInstance() {
            return singleInstance;
        }
    }

    public <T> T getApiService(final Class<T> service) {
        T apiService = getBuilder().retrofit.create(service);
        // 如果接口继承自 ApiRetrofitService，则返回装饰后的对象
        if (ApiRetrofitService.class.isAssignableFrom(service)) {
            return ApiServiceWrapper.wrap(apiService, this, service);
        }
        return apiService;
    }

    /**
     * 这里推荐rsa非堆成加密，破解难度大一点
     *
     * @param appSecret appSecret
     * @param postJson  请求体
     * @param timeStamp 时间戳
     * @return 加密后的sign
     */
    private static String encodeSign(String appSecret, String postJson, String timeStamp) {
        try {
            String signOld = ("x-appsecret" + appSecret + postJson + "x-timestamp" + timeStamp).toUpperCase();
            LogUtil.show(TAG, "加密前：" + signOld);

            return MD5Util.md5Encode(signOld).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "签名计算异常：" + e);
        }
        return "";
    }
}
