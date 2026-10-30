package io.coderf.arklab.common.base;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.coderf.arklab.common.bean.Code.ResponseCode;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Create by fz on 2019/8/1
 * 解析接口统一返回数据
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/30
 */
public class BaseResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;
    private final Gson gson;
    private final String successCode;
    private final boolean jumpAutoParse;
    private final Type actualType;

    public BaseResponseBodyConverter(String successCode, boolean jumpAutoParse, Gson gson, TypeAdapter<T> adapter, Type actualType) {
        this.successCode = successCode;
        this.jumpAutoParse = jumpAutoParse;
        this.adapter = adapter;
        this.gson = gson;
        this.actualType = actualType;
    }

    public BaseResponseBodyConverter(String successCode, Gson gson, TypeAdapter<T> adapter, Type actualType) {
        this(successCode, false, gson, adapter, actualType);
    }

    public BaseResponseBodyConverter(Gson gson, TypeAdapter<T> adapter, Type actualType) {
        this(null, false, gson, adapter, actualType);
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String jsonString = value.string();
        // 跳过默认解析：直接按接口声明的类型解析整段 JSON，不拆 code/msg/data
        if (jumpAutoParse) {
            return adapter.fromJson(jsonString);
        }
        // 构建 BaseModelEntity<T> 的完整类型
        Type baseModelType = TypeToken.getParameterized(BaseResponse.class, actualType).getType();
        // 解析 JSON
        BaseResponse<T> baseModel = gson.fromJson(jsonString, baseModelType);
        if (TextUtils.isEmpty(successCode)) {
            if (!ResponseCode.isOK(baseModel.getCode())) {
                throw new BaseException(baseModel.getMessage(), baseModel.getCode());
            }
        } else {
            if (!successCode.equalsIgnoreCase(baseModel.getCode())) {
                throw new BaseException(baseModel.getMessage(), baseModel.getCode());
            }
        }

        if (baseModel.getData() == null) {
            return responseConverterNull();
        }
        // TypeToken 已按 actualType 解析 data，无需再 toJson/fromJson
        return baseModel.getData();
    }

    /**
     * data 为 null 时按接口声明类型返回合理空壳，避免 Observable 直接收到 null。
     * 优先使用构造传入的 {@link #actualType}，不再依赖 TypeAdapter 匿名类泛型反射。
     */
    @SuppressWarnings("unchecked")
    public T responseConverterNull() {
        try {
            Class<?> raw = rawClass(actualType);
            if (raw == null) {
                return null;
            }
            if (String.class.equals(raw)) {
                return (T) "";
            }
            if (Boolean.class.equals(raw) || boolean.class.equals(raw)) {
                return (T) Boolean.FALSE;
            }
            if (Integer.class.equals(raw) || int.class.equals(raw)) {
                return (T) Integer.valueOf(0);
            }
            if (Long.class.equals(raw) || long.class.equals(raw)) {
                return (T) Long.valueOf(0);
            }
            if (Double.class.equals(raw) || double.class.equals(raw)) {
                return (T) Double.valueOf(0);
            }
            if (Float.class.equals(raw) || float.class.equals(raw)) {
                return (T) Float.valueOf(0);
            }
            if (raw.isArray()) {
                return (T) Array.newInstance(raw.getComponentType(), 0);
            }
            if (Map.class.isAssignableFrom(raw)) {
                return (T) new HashMap<>();
            }
            if (Collection.class.isAssignableFrom(raw)) {
                return (T) new ArrayList<>();
            }
            if (Object.class.equals(raw)) {
                return (T) new Object();
            }
            // 业务 Bean：尝试空 JSON 对象
            return gson.fromJson("{}", actualType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> rawClass(Type type) {
        if (type == null) {
            return null;
        }
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            if (raw instanceof Class<?>) {
                return (Class<?>) raw;
            }
        }
        return null;
    }
}
