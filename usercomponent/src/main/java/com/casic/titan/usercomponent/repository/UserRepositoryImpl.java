package com.casic.titan.usercomponent.repository;

import androidx.lifecycle.MutableLiveData;

import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.api.UserApiService;
import com.casic.titan.usercomponent.bean.RequestLoginBean;
import com.casic.titan.usercomponent.bean.TokenBean;
import com.casic.titan.usercomponent.bean.UserInfo;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/12/1 10:47
 * describe :
 */
public class UserRepositoryImpl extends RepositoryImpl {
    private final UserApiService userApiService;

    public UserRepositoryImpl(UserApiService userApiService,RetryService retryService, BaseView baseView) {
        super(retryService, baseView);
        this.userApiService = userApiService;
    }

    public void login(RequestLoginBean requestLoginBean, MutableLiveData<UserInfo> liveData) {
        sendRequest(userApiService.getToken(requestLoginBean.getUsername(),
                        requestLoginBean.getPassword(),requestLoginBean.getCode(),
                        "125")
                .flatMap((Function<TokenBean, Observable<UserInfo>>) tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getTokenId());
                    return userApiService.getUserInfo();
                }),
                new RequestConfigEntity
                        .Builder()
                        .setToastMsg("登录失败，请稍后再试！")
                        .build(),
                liveData, null, null);
    }

}
