package com.casic.otitan.common.base;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import com.casic.otitan.common.bean.Code.ResponseCode;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 作者： fz
 * 时间： 2019/7/30
 * 描述：
 */

public final class BaseConverterFactory extends Converter.Factory {
    private String successCode = ResponseCode.OK;

    public static BaseConverterFactory create() {
        return create(null, new GsonBuilder().disableHtmlEscaping().create());
    }

    public static BaseConverterFactory create(String successCode) {
        return create(successCode, new GsonBuilder().disableHtmlEscaping().create());
    }

    public static BaseConverterFactory create(Gson gson) {
        return create(null, gson);
    }

    public static BaseConverterFactory create(String successCode, Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson == null");
        }
        return new BaseConverterFactory(successCode, gson);
    }

    private final Gson gson;

    private BaseConverterFactory(String successCode, Gson gson) {
        this.successCode = successCode;
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NotNull Type type, @NonNull @NotNull Annotation[] annotations,
                                                            @NotNull Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new BaseResponseBodyConverter<>(successCode, gson, adapter,type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(@NotNull Type type,
                                                          @NonNull @NotNull Annotation[] parameterAnnotations, @NonNull @NotNull Annotation[] methodAnnotations, @NonNull Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new BaseRequestBodyConverter<>(gson, adapter);
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }
}
