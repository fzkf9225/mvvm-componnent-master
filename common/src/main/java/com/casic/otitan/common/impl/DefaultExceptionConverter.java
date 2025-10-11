package com.casic.otitan.common.impl;

import android.net.ParseException;

import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.inter.ExceptionConverter;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * created by fz on 2025/10/11 10:41
 * describe:
 */
public class DefaultExceptionConverter implements ExceptionConverter {
    @Override
    public BaseException convert(Throwable e) {
        if (e instanceof HttpException) {
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
}

