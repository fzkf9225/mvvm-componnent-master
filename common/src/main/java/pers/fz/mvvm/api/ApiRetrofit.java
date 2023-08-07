package pers.fz.mvvm.api;

import android.content.Context;
import android.text.TextUtils;


import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Headers;
import okhttp3.Interceptor;
import pers.fz.mvvm.BuildConfig;
import pers.fz.mvvm.base.BaseConverterFactory;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.apiUtil.DateUtil;
import pers.fz.mvvm.util.apiUtil.PropertiesUtil;
import pers.fz.mvvm.util.jiami.MD5Util;
import pers.fz.mvvm.util.log.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

/**
 * Created by fz on 2019/8/1.
 * describe：Retrofit初始化
 */
public class ApiRetrofit {
    public static final String TAG = ApiRetrofit.class.getSimpleName();
    private static ApiRetrofit apiRetrofit;

    private final Builder builder;

    public ApiRetrofit(Builder builder) {
        this.builder = builder;
    }

    public static void init(Context mContext) {
        new ApiRetrofit.Builder(mContext)
                .builder();
    }

    public Builder getBuilder() {
        return builder;
    }

    public static ApiRetrofit getInstance() {
        return apiRetrofit;
    }

    public void printLog(final Request request, final Response response) {
        LogUtil.show(TAG, "--------------------Request Start--------------------");

        LogUtil.show(TAG, "Method：" + request.method());
        LogUtil.show(TAG, "Url：" + request.url());
        LogUtil.show(TAG, "HttpHeader：" + request.headers().toString());

        try {
            LogUtil.show(TAG, "请求参数：" + bodyToString(request.body()));
        } catch (IOException e) {
            LogUtil.show(TAG, "请求参数解析失败");
        }
        try {
            LogUtil.show(TAG, "返回请求头：" + response.headers().toString());
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            LogUtil.show(TAG, "返回结果：" + responseBody.string());
        } catch (Exception e) {
            LogUtil.show(TAG, "返回结果解析失败");
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
        return apiRetrofit.getBuilder().retrofit;
    }

    /**
     * 建造者模式，创建网络请求对象
     */
    public static class Builder {
        private String baseUrl;
        private String appId;
        private String appSecret;
        private String protocolVersion;
        private final Map<String, String> headerMap = new HashMap<>();
        private final Context mContext;
        private Retrofit retrofit = null;
        private OkHttpClient client = null;
        private Converter.Factory converterFactory = null;
        private final List<Interceptor> interceptorList = new ArrayList<>();
        private long timeOut = 15;
        private boolean useDefaultSign = true;
        private ErrorService errorService = null;

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

        public Builder setAppId(String appId) {
            this.appId = appId;
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

        private Builder setClient(OkHttpClient okHttpClient) {
            this.client = okHttpClient;
            return this;
        }

        public Builder setTimeOut(long timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        private Builder setRetrofit(Retrofit retrofit) {
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

        public Builder addDefaultHeader() {
            String timeStamp = DateUtil.getTimestamp();
//            addHeader("x-timestamp", timeStamp);
//            addHeader("x-appid", appId);
//            addHeader("x-uuid", UUID.randomUUID().toString());
//            addHeader("x-phoneidentity", ApiAccountHelper.getDeviceId(mContext));
//            addHeader("x-protocolversion", protocolVersion);
//            addHeader("x-phoneinfo", ApiAccountHelper.getPhoneInfo(mContext));
            return this;
        }

        public ApiRetrofit builder() {
            if (TextUtils.isEmpty(baseUrl)) {
                this.baseUrl = PropertiesUtil.getInstance().getProperties(mContext).getBaseUrl();
            }
            if (TextUtils.isEmpty(appId)) {
                this.appId = PropertiesUtil.getInstance().getProperties(mContext).getAppId();
            }

            if (TextUtils.isEmpty(appSecret)) {
                this.appSecret = PropertiesUtil.getInstance().getProperties(mContext).getAppSecret();
            }

            if (TextUtils.isEmpty(protocolVersion)) {
                this.protocolVersion = PropertiesUtil.getInstance().getProperties(mContext).getProtocolVersion();
            }
            if (client == null) {
                OkHttpClient.Builder build = new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            String timeStamp = DateUtil.getTimestamp();
                            Request.Builder requestBuilder = chain.request().newBuilder();
                            //判断errorService是否为空
                            if (errorService != null) {
                                headerMap.putAll(errorService.initHeaderMap());
                            }
                            //添加默认请求头
                            requestBuilder.headers(Headers.of(headerMap));
                            if (useDefaultSign) {
                                requestBuilder.addHeader("x-sign", encodeSign(appSecret, bodyToString(chain.request().body()), timeStamp));
                            }

                            Request request = requestBuilder.build();
                            Response response = chain.proceed(request);
                            if (BuildConfig.DEBUG) {
                                ApiRetrofit.getInstance().printLog(request, response);
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
                converterFactory = BaseConverterFactory.create();
            }
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(converterFactory)
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .client(this.client)
                        .build();
            }
            if (apiRetrofit == null) {
                synchronized (ApiRetrofit.class) {
                    if (apiRetrofit == null) {
                        apiRetrofit = new ApiRetrofit(this);
                    }
                }
            }
            return apiRetrofit;
        }
    }

    public <T> T getApiService(final Class<T> service) {
        return getBuilder().retrofit.create(service);
    }

    private static String encodeSign(String appSecret, String postJson, String timeStamp) {
        try {
            String signOld = ("x-appsecret" + appSecret + postJson + "x-timestamp" + timeStamp).toUpperCase();
            LogUtil.show("ApiRetrofit", "加密前：" + signOld);

            return MD5Util.md5Encode(signOld).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show("ApiRetrofit", "签名计算异常：" + e);
        }
        return "";
    }
}
