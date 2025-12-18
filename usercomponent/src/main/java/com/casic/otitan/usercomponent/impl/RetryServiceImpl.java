package com.casic.otitan.usercomponent.impl;

import com.casic.otitan.common.api.ApiRetrofit;
import com.casic.otitan.userapi.bean.UserInfo;
import com.casic.otitan.usercomponent.api.UserAccountHelper;
import com.casic.otitan.usercomponent.api.UserApiService;

import org.reactivestreams.Publisher;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;

import com.casic.otitan.common.api.ConstantsHelper;
import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.inter.RetryService;
import com.casic.otitan.common.utils.log.LogUtil;

import retrofit2.HttpException;

/**
 * Created by fz on 2020/9/9 14:11
 * describe:请求失败，重试机制，当请求过期时利用Function方法重新请求刷新token方法替换请求token，然后再重新请求
 * 设置3次重试，每次间隔1秒,但仅适用于用户登录过期刷新token和无权限刷新用户菜单时使用
 */
public class RetryServiceImpl implements RetryService {
    /**
     * 最大出错重试次数
     */
    private int maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT;
    /**
     * 当前出错重试次数
     */
    private int retryCount = 0;

    @Inject
    UserApiService userApiService;

    @Inject
    public RetryServiceImpl() {
    }

    /**
     * @param maxRetries 最大重试次数
     */
    public RetryServiceImpl(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Observable<?> handleObservableError(Observable<? extends Throwable> observable) {
        LogUtil.show(ApiRetrofit.TAG, "-----------------RetryService Observable-------------");
        return observable.flatMap(this::handleThrowable);
    }

    @Override
    public Publisher<?> handleFlowableError(Flowable<Throwable> flowable) {
        LogUtil.show(ApiRetrofit.TAG, "-----------------RetryService Flowable-------------");
        return flowable.flatMap(this::handleFlowableThrowable);
    }

    private ObservableSource<?> handleThrowable(Throwable throwable) throws Exception {
        if (shouldRetry(throwable)) {
            return refresh(userApiService);
        }
        return Observable.error(throwable);
    }

    private Publisher<?> handleFlowableThrowable(Throwable throwable) throws Exception {
        if (shouldRetry(throwable)) {
            return refreshFlow(userApiService);
        }
        return Flowable.error(throwable);
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof BaseException) {
            BaseException baseException = (BaseException) throwable;
            LogUtil.show(ApiRetrofit.TAG, "第 " + retryCount + " 次重试，" + "baseException：" + baseException);
            boolean isLoginPastOrNoPermission = true; // 改成实际逻辑
            if (++retryCount <= maxRetries && isLoginPastOrNoPermission) {
                return true;
            }
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            LogUtil.show(ApiRetrofit.TAG, "第 " + retryCount + " 次重试，" + "httpException：" + httpException);
            if (++retryCount <= maxRetries && 401 == httpException.code()) {
                return true;
            }
        }
        retryCount = 0;
        LogUtil.show(ApiRetrofit.TAG, "不满足重试条件！");
        return false;
    }

    private Observable<UserInfo> refresh(UserApiService userApiService) {
        UserAccountHelper.saveLoginPast(false);
        return userApiService.refreshToken(UserAccountHelper.getRefreshToken())
                .flatMap(tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfo();
                })
                .doOnNext(userInfo -> {
                    UserAccountHelper.saveLoginState(userInfo, true);
                    retryCount = 0; // 重置计数器
                });
    }

    private Flowable<UserInfo> refreshFlow(UserApiService userApiService) {
        UserAccountHelper.saveLoginPast(false);
        return userApiService.refreshTokenFlow(UserAccountHelper.getRefreshToken())
                .flatMap(tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfoFlow();
                })
                .doOnNext(userInfo -> {
                    UserAccountHelper.saveLoginState(userInfo, true);
                    retryCount = 0; // 重置计数器
                });
    }

    @Override
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetries = maxRetryCount;
    }
}
