package io.coderf.arklab.common.api;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.common.impl.DefaultExceptionConverter;
import io.coderf.arklab.common.impl.RequestUiAdapters;
import io.coderf.arklab.common.inter.ExceptionConverter;
import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by fz on 2023/11/30 15:52
 * describe : 统一错误处理消费者；内部只依赖 {@link RequestUiCallback}，与数据层约定一致。
 */
public class ErrorConsumer implements Consumer<Throwable> {
    @Nullable
    private final RequestUiCallback requestUi;
    private final ApiRequestOptions apiRequestOptions;
    private final ExceptionConverter exceptionConverter;

    public ErrorConsumer(@Nullable RequestUiCallback requestUi, ApiRequestOptions apiRequestOptions) {
        this(requestUi, apiRequestOptions, new DefaultExceptionConverter());
    }

    public ErrorConsumer(@Nullable RequestUiCallback requestUi, ApiRequestOptions apiRequestOptions, ExceptionConverter converter) {
        this.requestUi = requestUi;
        this.apiRequestOptions = apiRequestOptions != null ? apiRequestOptions : ApiRequestOptions.getDefault();
        this.exceptionConverter = converter != null ? converter : new DefaultExceptionConverter();
    }

    /**
     * 兼容旧调用方（如 commonui）：内部转为 {@link RequestUiCallback}。
     */
    public ErrorConsumer(@Nullable BaseView baseView, ApiRequestOptions apiRequestOptions) {
        this(RequestUiAdapters.fromBaseView(baseView), apiRequestOptions);
    }

    public ErrorConsumer(@Nullable BaseView baseView, ApiRequestOptions apiRequestOptions, ExceptionConverter converter) {
        this(RequestUiAdapters.fromBaseView(baseView), apiRequestOptions, converter);
    }

    @Override
    public void accept(Throwable e) throws Throwable {
        LogUtil.logger(ApiRetrofit.TAG, "ErrorConsumer|系统异常: " + e);

        hideLoadingIfNeeded();
        BaseException be = (e instanceof BaseException) ? (BaseException) e : exceptionConverter.convert(e);

        LogUtil.logger(ApiRetrofit.TAG, "ErrorConsumer|异常消息: " + be.getErrorMsg());

        handleException(be);
    }

    private void handleException(BaseException be) {
        if (requestUi == null) {
            return;
        }

        requestUi.onErrorCode(createErrorResponse(be));

        showToastIfNeeded(be);
    }

    private BaseResponse<?> createErrorResponse(BaseException be) {
        return new BaseResponse<>(
                be.getErrorCode(),
                be.getErrorMsg(),
                apiRequestOptions == null ? null : apiRequestOptions.getRequestParams()
        );
    }

    private void showToastIfNeeded(BaseException be) {
        if (apiRequestOptions != null && apiRequestOptions.isShowToast()) {
            String toastMsg = getToastMessage(be);
            if (!TextUtils.isEmpty(toastMsg)) {
                requestUi.showToast(toastMsg);
            }
        }
    }

    private String getToastMessage(BaseException be) {
        if (!TextUtils.isEmpty(apiRequestOptions.getToastMsg())) {
            return apiRequestOptions.getToastMsg();
        } else {
            return be.getErrorMsg();
        }
    }

    private void hideLoadingIfNeeded() {
        if (requestUi != null && apiRequestOptions != null && apiRequestOptions.isShowDialog()) {
            requestUi.hideLoading();
        }
    }
}
