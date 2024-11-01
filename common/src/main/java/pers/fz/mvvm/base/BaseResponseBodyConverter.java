package pers.fz.mvvm.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;

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

    BaseResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.adapter = adapter;
        this.gson = gson;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String jsonString = value.string();
        JSONObject jsonObject;
        try (value) {
            jsonObject = new JSONObject(jsonString);
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.show(ApiRetrofit.TAG, ex.getMessage());
            //数据解析异常
            throw new RuntimeException(ex);
        }
        if (jsonObject.has("code") || jsonObject.has("error")) {
            BaseModelEntity<T> baseModel = gson.fromJson(jsonString,
                    new TypeToken<BaseModelEntity<T>>() {
                    }.getType());
            if (!ResponseCode.OK.equals(baseModel.getCode())) {
                throw new BaseException(baseModel.getMessage(), baseModel.getCode());
            }
            return adapter.fromJson(gson.toJson(baseModel.getData()));
        }
        return adapter.fromJson(jsonString);

    }
}
