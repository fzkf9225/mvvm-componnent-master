package pers.fz.mvvm.base;

import android.text.TextUtils;

import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;

import io.reactivex.rxjava3.functions.Consumer;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.util.log.LogUtil;
import retrofit2.HttpException;

/**
 * Created by fz on 2023/11/30 15:52
 * describe :
 */
public class ErrorConsumer implements Consumer<Throwable> {
    private final BaseView baseView;
    private RequestConfigEntity requestConfigEntity;

    public ErrorConsumer(BaseView baseView, RequestConfigEntity requestConfigEntity) {
        this.baseView = baseView;
        this.requestConfigEntity = requestConfigEntity;
        if (this.requestConfigEntity == null) {
            this.requestConfigEntity = new RequestConfigEntity.Builder().build();
        }
    }

    @Override
    public void accept(Throwable e) throws Throwable {
        LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|系统异常: " + e);

        if (baseView != null && requestConfigEntity != null && requestConfigEntity.isShowDialog()) {
            baseView.hideLoading();
        }
        BaseException be;

        if (e != null) {
            if (e instanceof BaseException) {
                be = (BaseException) e;
                //回调到view层 处理 或者根据项目情况处理
                if (baseView != null) {
                    if (requestConfigEntity != null && requestConfigEntity.isShowToast()) {
                        if (!TextUtils.isEmpty(requestConfigEntity.getToastMsg())) {
                            baseView.showToast(requestConfigEntity.getToastMsg());
                        } else {
                            baseView.showToast(be.getErrorMsg());
                        }
                    }
                    baseView.onErrorCode(new BaseModelEntity<>(be.getErrorCode(), be.getErrorMsg(), requestConfigEntity == null ? null : requestConfigEntity.getRequestParams()));
                    return;
                }
            } else {
                if (e instanceof HttpException) {
                    //   HTTP错误
                    be = new BaseException(e.getMessage(), e, BaseException.BAD_NETWORK);
                } else if (e instanceof ConnectException
                        || e instanceof UnknownHostException) {
                    //   连接错误
                    be = new BaseException(BaseException.CONNECT_ERROR_MSG, e, BaseException.CONNECT_ERROR);
                } else if (e instanceof InterruptedIOException) {
                    //  连接超时
                    be = new BaseException(BaseException.CONNECT_TIMEOUT_MSG, e, BaseException.CONNECT_TIMEOUT);
                } else if (e instanceof JsonParseException
                        || e instanceof JSONException
                        || e instanceof ParseException) {
                    //  解析错误
                    be = new BaseException(BaseException.PARSE_ERROR_MSG, e, BaseException.PARSE_ERROR);
                } else {
                    be = new BaseException(e.getMessage(), e, BaseException.OTHER);
                }
            }
        } else {
            be = new BaseException(BaseException.OTHER_MSG, BaseException.OTHER);
        }
        LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|异常消息: " + be.getErrorMsg());
        if (baseView != null) {
            baseView.onErrorCode(new BaseModelEntity<>(be.getErrorCode(), be.getErrorMsg(), requestConfigEntity == null ? null : requestConfigEntity.getRequestParams()));
            if (requestConfigEntity.isShowToast()) {
                if (!TextUtils.isEmpty(requestConfigEntity.getToastMsg())) {
                    baseView.showToast(requestConfigEntity.getToastMsg());
                } else {
                    baseView.showToast(be.getErrorMsg());
                }
            }
        }
    }
}
