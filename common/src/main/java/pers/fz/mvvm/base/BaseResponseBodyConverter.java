package pers.fz.mvvm.base;

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
import java.util.Objects;

import okhttp3.ResponseBody;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.bean.Code.ResponseCode;
import pers.fz.mvvm.util.log.LogUtil;
import retrofit2.Converter;

/**
 * Create by CherishTang on 2019/8/1
 * describe:解析接口统一返回数据
 */
public class BaseResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;
    private final Gson gson;
    private final String successCode;
    private final Type actualType;

    public BaseResponseBodyConverter(String successCode, Gson gson, TypeAdapter<T> adapter,Type actualType) {
        this.successCode = successCode;
        this.adapter = adapter;
        this.gson = gson;
        this.actualType = actualType;
    }

    public BaseResponseBodyConverter(Gson gson, TypeAdapter<T> adapter,Type actualType) {
        this(null,gson,adapter,actualType);
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String jsonString = value.string();
        // 构建 BaseModelEntity<T> 的完整类型
        Type baseModelType = TypeToken.getParameterized(BaseModelEntity.class, actualType).getType();
        // 解析 JSON
        BaseModelEntity<T> baseModel = gson.fromJson(jsonString, baseModelType);
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
            return adapter.fromJson(gson.toJson(responseConverterNull()));
        }
        return adapter.fromJson(gson.toJson(baseModel.getData()));
    }

    public T responseConverterNull() {
        // Observable 不能直接返回 null，所以根据泛型创建一个空对象
        try {
            Type rawType = ((ParameterizedType) adapter.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            // 判断类型是否为 String
            if (String.class.equals(rawType)) {
                return (T) "";
            }
            // 判断类型是否为 Boolean
            else if (Boolean.class.equals(rawType) || boolean.class.equals(rawType)) {
                return (T) Boolean.FALSE;
            }
            // 判断类型是否为 Integer
            else if (Integer.class.equals(rawType) || int.class.equals(rawType)) {
                return (T) Integer.valueOf(0);
            }
            // 判断类型是否为 Long
            else if (Long.class.equals(rawType) || long.class.equals(rawType)) {
                return (T) Long.valueOf(0);
            }
            // 判断类型是否为 Double
            else if (Double.class.equals(rawType) || double.class.equals(rawType)) {
                return (T) Double.valueOf(0);
            }
            // 判断类型是否为 Float
            else if (Float.class.equals(rawType) || float.class.equals(rawType)) {
                return (T) Float.valueOf(0);
            } else if (Array.class.equals(rawType)) {
                return (T) new Object[0];
            }
            // 判断是否为 Map 类型
            else if (Map.class.equals(rawType)) {
                return (T) new HashMap<>();
            }
            // 判断是否为 Collection 类型
            else if (Collection.class.equals(rawType)) {
                return (T) new ArrayList<>();
            } else if (Object.class.equals(rawType)) {
                return (T) new Object();
            } else {
                // 处理特殊的情况，若没有匹配到任何条件
                return (T) new Object();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
