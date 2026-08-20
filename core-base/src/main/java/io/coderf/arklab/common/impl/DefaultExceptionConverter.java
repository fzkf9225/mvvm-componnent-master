package io.coderf.arklab.common.impl;

import android.net.ParseException;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.inter.ExceptionConverter;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * created by fz on 2025/10/11 10:41
 * describe: 统一异常转换。
 * <p>
 * 部分接口在 HTTP 非 2xx（如 500）时仍返回业务 JSON（code/msg），
 * 需优先解析 errorBody，避免一律提示「服务器或网络异常」。
 */
public class DefaultExceptionConverter implements ExceptionConverter {
    @Override
    public BaseException convert(Throwable e) {
        if (e instanceof HttpException) {
            BaseException business = parseHttpBusinessError((HttpException) e);
            if (business != null) {
                return business;
            }
            return BaseException.asNetworkError(e);
        } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
            return BaseException.asConnectError(e);
        } else if (e instanceof InterruptedIOException) {
            return BaseException.asTimeoutError(e);
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {
            return BaseException.asParseError(e);
        } else if (e != null) {
            return new BaseException(e.getMessage(), e, BaseException.ErrorType.OTHER.getCode());
        } else {
            return new BaseException(BaseException.ErrorType.OTHER);
        }
    }

    /**
     * 从 HttpException 的 errorBody 解析业务错误（msg / message / code）。
     *
     * @return 能解析出业务文案则返回；否则 null，走通用网络异常
     */
    private BaseException parseHttpBusinessError(HttpException httpException) {
        try {
            Response<?> response = httpException.response();
            if (response == null) {
                return null;
            }
            ResponseBody errorBody = response.errorBody();
            if (errorBody == null) {
                return null;
            }
            String text = errorBody.string();
            if (TextUtils.isEmpty(text)) {
                return null;
            }
            JsonElement root = JsonParser.parseString(text);
            if (root == null || !root.isJsonObject()) {
                return null;
            }
            JsonObject obj = root.getAsJsonObject();
            String msg = firstNonEmptyString(obj, "msg", "message", "error_description", "errorMsg");
            if (TextUtils.isEmpty(msg)) {
                return null;
            }
            String code = firstNonEmptyString(obj, "code", "error");
            if (TextUtils.isEmpty(code)) {
                code = String.valueOf(httpException.code());
            }
            return new BaseException(msg, httpException, code);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String firstNonEmptyString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                continue;
            }
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive()) {
                String value = el.getAsString();
                if (!TextUtils.isEmpty(value)) {
                    return value;
                }
            }
        }
        return null;
    }
}
