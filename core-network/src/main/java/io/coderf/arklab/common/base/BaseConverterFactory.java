package io.coderf.arklab.common.base;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.coderf.arklab.common.bean.Code.ResponseCode;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 默认解析工厂
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/28 15:42
 */
public final class BaseConverterFactory extends Converter.Factory {
    private String successCode = ResponseCode.OK;
    private boolean jumpAutoParse = false;

    public static BaseConverterFactory create() {
        return create(null, false, new GsonBuilder().disableHtmlEscaping().create());
    }

    public static BaseConverterFactory create(String successCode) {
        return create(successCode, false, new GsonBuilder().disableHtmlEscaping().create());
    }

    public static BaseConverterFactory create(Gson gson) {
        return create(null, false, gson);
    }

    public static BaseConverterFactory create(String successCode, boolean jumpAutoParse) {
        return create(successCode, jumpAutoParse, new GsonBuilder().disableHtmlEscaping().create());
    }

    public static BaseConverterFactory create(String successCode, Gson gson) {
        return create(successCode, false, gson);
    }

    public static BaseConverterFactory create(String successCode, boolean jumpAutoParse, Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson must be not null");
        }
        return new BaseConverterFactory(successCode, jumpAutoParse, gson);
    }

    private final Gson gson;

    private BaseConverterFactory(String successCode, boolean jumpAutoParse, Gson gson) {
        this.successCode = successCode;
        this.jumpAutoParse = jumpAutoParse;
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NotNull Type type, @NonNull @NotNull Annotation[] annotations,
                                                            @NotNull Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new BaseResponseBodyConverter<>(successCode, jumpAutoParse, gson, adapter, type);
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

    public boolean isJumpAutoParse() {
        return jumpAutoParse;
    }

    public void setJumpAutoParse(boolean jumpAutoParse) {
        this.jumpAutoParse = jumpAutoParse;
    }
}
