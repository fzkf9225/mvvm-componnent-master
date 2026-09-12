package io.coderf.arklab.common.api;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.common.impl.DefaultExceptionConverter;
import io.coderf.arklab.common.inter.ExceptionConverter;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.RequestUi;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 统一错误处理消费者；内部只依赖 {@link RequestUi}，与数据层约定一致。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2023/11/30 15:52
 * @updated 2026/9/12
 */
public class ErrorConsumer implements Consumer<Throwable> {
    @Nullable
    private final RequestUi requestUi;
    private final ApiRequestOptions apiRequestOptions;
    private final ExceptionConverter exceptionConverter;

    public ErrorConsumer(@Nullable RequestUi requestUi, ApiRequestOptions apiRequestOptions) {
        this(requestUi, apiRequestOptions, new DefaultExceptionConverter());
    }

    public ErrorConsumer(@Nullable RequestUi requestUi, ApiRequestOptions apiRequestOptions, ExceptionConverter converter) {
        this.requestUi = requestUi;
        this.apiRequestOptions = apiRequestOptions != null ? apiRequestOptions : ApiRequestOptions.getDefault();
        this.exceptionConverter = converter != null ? converter : new DefaultExceptionConverter();
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

        String code = be.getErrorCode() != null ? be.getErrorCode() : "";
        // 与旧逻辑一致：始终派发业务码；仅在 isShowToast 时附带可展示文案（Host 对非空 message 会发 Toast）
        String messageForUi = "";
        if (apiRequestOptions != null && apiRequestOptions.isShowToast()) {
            String toastMsg = getToastMessage(be);
            messageForUi = !TextUtils.isEmpty(toastMsg) ? toastMsg : "";
        }
        requestUi.showError(new AppError.Business(code, messageForUi, be));
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
