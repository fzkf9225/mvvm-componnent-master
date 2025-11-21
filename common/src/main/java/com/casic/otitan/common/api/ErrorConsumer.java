package com.casic.otitan.common.api;

import android.text.TextUtils;

import com.casic.otitan.common.impl.DefaultExceptionConverter;
import com.casic.otitan.common.inter.ExceptionConverter;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;

import io.reactivex.rxjava3.functions.Consumer;
import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.base.BaseResponse;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.bean.ApiRequestOptions;
import com.casic.otitan.common.utils.log.LogUtil;
import retrofit2.HttpException;

/**
 * Created by fz on 2023/11/30 15:52
 * describe : 统一错误处理消费者
 */
public class ErrorConsumer implements Consumer<Throwable> {
    private final BaseView baseView;
    private final ApiRequestOptions apiRequestOptions;
    private final ExceptionConverter exceptionConverter;

    public ErrorConsumer(BaseView baseView, ApiRequestOptions apiRequestOptions) {
        this(baseView, apiRequestOptions, new DefaultExceptionConverter());
    }

    public ErrorConsumer(BaseView baseView, ApiRequestOptions apiRequestOptions, ExceptionConverter converter) {
        this.baseView = baseView;
        this.apiRequestOptions = apiRequestOptions != null ? apiRequestOptions : ApiRequestOptions.getDefault();
        this.exceptionConverter = converter != null ? converter : new DefaultExceptionConverter();
    }
    @Override
    public void accept(Throwable e) throws Throwable {
        LogUtil.show(ApiRetrofit.TAG, "ErrorConsumer|系统异常: " + e);

        // 隐藏加载框
        hideLoadingIfNeeded();
        BaseException be = (e instanceof BaseException) ? (BaseException) e : exceptionConverter.convert(e);

        LogUtil.show(ApiRetrofit.TAG, "ErrorConsumer|异常消息: " + be.getErrorMsg());

        // 处理异常回调
        handleException(be);
    }

    /**
     * 处理 BaseException
     */
    private void handleException(BaseException be) {
        if (baseView == null) {
            return;
        }

        // 回调错误码
        baseView.onErrorCode(createErrorResponse(be));

        // 显示Toast提示
        showToastIfNeeded(be);
    }

    /**
     * 创建错误响应
     */
    private BaseResponse<?> createErrorResponse(BaseException be) {
        return new BaseResponse<>(
                be.getErrorCode(),
                be.getErrorMsg(),
                apiRequestOptions == null ? null : apiRequestOptions.getRequestParams()
        );
    }

    /**
     * 显示Toast提示
     */
    private void showToastIfNeeded(BaseException be) {
        if (apiRequestOptions != null && apiRequestOptions.isShowToast()) {
            String toastMsg = getToastMessage(be);
            if (!TextUtils.isEmpty(toastMsg)) {
                baseView.showToast(toastMsg);
            }
        }
    }

    /**
     * 获取Toast显示消息
     */
    private String getToastMessage(BaseException be) {
        if (!TextUtils.isEmpty(apiRequestOptions.getToastMsg())) {
            return apiRequestOptions.getToastMsg();
        } else {
            return be.getErrorMsg();
        }
    }

    /**
     * 隐藏加载框
     */
    private void hideLoadingIfNeeded() {
        if (baseView != null && apiRequestOptions != null && apiRequestOptions.isShowDialog()) {
            baseView.hideLoading();
        }
    }
}