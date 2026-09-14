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
        AppError error = toAppError(e);

        LogUtil.logger(ApiRetrofit.TAG, "ErrorConsumer|异常消息: " + error.getMessage());

        if (requestUi == null) {
            return;
        }
        requestUi.showError(applyToastPolicy(error));
    }

    /**
     * 已是 {@link BaseException}（含 {@link io.coderf.arklab.core.request.AppErrorThrowable}）直接映射；
     * 其余仍走 {@link ExceptionConverter}，保留 HttpException errorBody 解析。
     */
    private AppError toAppError(Throwable e) {
        if (e instanceof BaseException) {
            return AppError.from(e);
        }
        return AppError.from(exceptionConverter.convert(e));
    }

    /**
     * 与旧逻辑一致：仅在 isShowToast 时附带可展示文案（Host 对非空 message 会发 Toast）。
     * 业务码仍保留在 {@link AppError.Business} 上，登录过期 / 无权限不受影响。
     */
    private AppError applyToastPolicy(AppError error) {
        if (error instanceof AppError.Cancelled) {
            return error;
        }
        String messageForUi = "";
        if (apiRequestOptions != null && apiRequestOptions.isShowToast()) {
            if (!TextUtils.isEmpty(apiRequestOptions.getToastMsg())) {
                messageForUi = apiRequestOptions.getToastMsg();
            } else if (!TextUtils.isEmpty(error.getMessage())) {
                messageForUi = error.getMessage();
            }
        }
        return error.withMessage(messageForUi);
    }

    private void hideLoadingIfNeeded() {
        if (requestUi != null && apiRequestOptions != null && apiRequestOptions.isShowDialog()) {
            requestUi.hideLoading();
        }
    }
}
