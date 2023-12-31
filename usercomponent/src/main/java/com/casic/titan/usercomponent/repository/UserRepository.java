package com.casic.titan.usercomponent.repository;

import androidx.lifecycle.MutableLiveData;

import com.casic.titan.mqttcomponent.CloudDataHelper;
import com.casic.titan.mqttcomponent.MqttBean;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.api.UserApiService;
import com.casic.titan.usercomponent.bean.RequestLoginBean;
import com.casic.titan.usercomponent.bean.TokenBean;
import com.casic.titan.usercomponent.bean.UserInfo;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;
import com.casic.titan.usercomponent.bean.WorkSpaceBean;
import com.casic.titan.usercomponent.enumEntity.GrantType;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/12/1 10:47
 * describe :
 */
public class UserRepository extends RepositoryImpl {
    private final UserApiService userApiService;

    public UserRepository(RetryService retryService) {
        super(retryService);
        userApiService = ApiRetrofit.getInstance().getApiService(UserApiService.class);
    }

    public void login(RequestLoginBean requestLoginBean, MutableLiveData<UserInfo> liveData) {
        sendRequest(userApiService.getToken(requestLoginBean.getUsername(),
                        requestLoginBean.getPassword(), GrantType.LOGIN.getValue(),
                        "all", "000000","account")
                .flatMap((Function<TokenBean, Observable<UserInfo>>) tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfo(tokenBean.getUser_id());
                }),
                new RequestConfigEntity
                        .Builder()
                        .setToastMsg("登录失败，请稍后再试！")
                        .build(),
                liveData, null, null);
    }

}
