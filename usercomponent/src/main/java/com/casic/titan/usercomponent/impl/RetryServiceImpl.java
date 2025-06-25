package com.casic.titan.usercomponent.impl;

import com.casic.titan.userapi.bean.UserInfo;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.api.UserApiService;
import com.casic.titan.usercomponent.enumEntity.GrantType;

import org.reactivestreams.Publisher;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.log.LogUtil;
import retrofit2.HttpException;

/**
 * Created by fz on 2020/9/9 14:11
 * describe:请求失败，重试机制，当请求过期时利用Function方法重新请求刷新token方法替换请求token，然后再重新请求
 * 设置3次重试，每次间隔1秒,但仅适用于用户登录过期刷新token和无权限刷新用户菜单时使用
 */
public class RetryServiceImpl implements RetryService {
    private final static String TAG = RetryService.class.getSimpleName();
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
        LogUtil.show(TAG, "-----------------RetryService Observable-------------");
        return observable.flatMap(this::handleThrowable);
    }

    @Override
    public Publisher<?> handleFlowableError(Flowable<Throwable> flowable) {
        LogUtil.show(TAG, "-----------------RetryService Flowable-------------");
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
            LogUtil.show(TAG, "baseException:" + baseException);
            boolean isLoginPastOrNoPermission = true; // 改成实际逻辑
            return ++retryCount <= maxRetries && isLoginPastOrNoPermission;
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            LogUtil.show(TAG, "httpException:" + httpException);
            return 401 == httpException.code();
        }
        return false;
    }

    private Observable<UserInfo> refresh(UserApiService userApiService) {
        UserAccountHelper.saveLoginPast(false);
        return userApiService.refreshToken(UserAccountHelper.getRefreshToken(), "000000",
                        GrantType.REFRESH_TOKEN.getValue(), "all", "account")
                .flatMap(tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfo();
                })
                .doOnNext(userInfo -> UserAccountHelper.saveLoginState(userInfo, true));
    }

    private Flowable<UserInfo> refreshFlow(UserApiService userApiService) {
        UserAccountHelper.saveLoginPast(false);
        return userApiService.refreshTokenFlow(UserAccountHelper.getRefreshToken(), "000000",
                        GrantType.REFRESH_TOKEN.getValue(), "all", "account")
                .flatMap(tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfoFlow();
                })
                .doOnNext(userInfo -> UserAccountHelper.saveLoginState(userInfo, true));
    }
//    @Override
//    public Observable<?> apply(Observable<? extends Throwable> observable) throws Exception {
//        LogUtil.show(TAG, "-----------------RetryService-------------");
//        return observable
//                .flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
//                    if (throwable instanceof BaseException) {
//                        BaseException baseException = (BaseException) throwable;
//                        LogUtil.show(TAG, "baseException:" + baseException.toString());
//                        boolean isLoginPastOrNoPermission = true;//这里的true改成自己的逻辑
//                        if (++retryCount <= maxRetries && isLoginPastOrNoPermission) {
//                            return refresh(userApiService);
//                        }
//                    } else if (throwable instanceof HttpException) {
//                        HttpException httpException = (HttpException) throwable;
//                        LogUtil.show(TAG, "httpException:" + httpException);
//                        if (401 == httpException.code()) {
//                            return refresh(userApiService);
//                        }
//                        return Observable.error(throwable);
//                    }
//                    return Observable.error(throwable);
//                });
//    }
//
//    private Observable<UserInfo> refresh(UserApiService userApiService) {
//        // 如果上面检测到token过期就会进入到这里
//        // 然后下面的方法就是更新token
//        UserAccountHelper.saveLoginPast(false);
//        return userApiService.refreshToken(UserAccountHelper.getRefreshToken(),"000000",
//                        GrantType.REFRESH_TOKEN.getValue(), "all"
//                        , "account")
//                .flatMap((Function<TokenBean, Observable<UserInfo>>) tokenBean -> {
//                    UserAccountHelper.setToken(tokenBean.getAccess_token());
//                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
//                    return userApiService.getUserInfo();
//                })
//                .doOnNext(userInfo -> UserAccountHelper.saveLoginState(userInfo, true));
//    }


    @Override
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetries = maxRetryCount;
    }
}
