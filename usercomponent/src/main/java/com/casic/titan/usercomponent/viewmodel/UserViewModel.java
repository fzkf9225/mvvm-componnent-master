package com.casic.titan.usercomponent.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.api.ApiRetrofit;

import com.casic.titan.mqttcomponent.CloudDataHelper;
import com.casic.titan.mqttcomponent.MqttBean;
import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.api.UserApiService;
import com.casic.titan.usercomponent.bean.UserInfo;

import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.jiami.MD5Util;
import pers.fz.mvvm.util.log.ToastUtils;

import com.casic.titan.usercomponent.bean.RequestLoginBean;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;
import com.casic.titan.usercomponent.bean.WorkSpaceBean;
import com.casic.titan.usercomponent.enumEntity.GrantType;
import com.casic.titan.usercomponent.view.UserView;

import java.util.List;

import javax.inject.Inject;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:loginViewModel
 */
@HiltViewModel
public class UserViewModel extends BaseViewModel<UserView> {
    private UserApiService userApiService;
    private final MutableLiveData<List<WebSocketSubscribeBean>> liveData = new MutableLiveData<>();

    @Inject
    public UserViewModel(@NonNull Application application) {
        super(application);
        userApiService = ApiRetrofit.getInstance().getApiService(UserApiService.class);
    }

    public void loginClick(View v, RequestLoginBean requestLoginBean) {
        loginClick(v, requestLoginBean, null);
    }

    public MutableLiveData<List<WebSocketSubscribeBean>> getLiveData() {
        return liveData;
    }

    public void loginClick(View v, RequestLoginBean requestLoginBean, String password) {
        int id = v.getId();
        if (id == R.id.login_submit) {
            if (StringUtil.isEmpty(requestLoginBean.getUsername())) {
                ToastUtils.showShort(v.getContext(), "请填写用户名");
                return;
            }
            if (StringUtil.isEmpty(password)) {
                ToastUtils.showShort(v.getContext(), "请填写密码");
                return;
            }
            try {
                requestLoginBean.setPassword(MD5Util.md5Encode(password));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            observe(userApiService.getToken(requestLoginBean.getUsername(),
                            requestLoginBean.getPassword(), GrantType.LOGIN.getValue(),
                            "all", "000000")
                    .flatMap((Function<UserInfo, Observable<MqttBean>>) userInfo -> {
                        UserAccountHelper.setToken(userInfo.getAccess_token());
                        UserAccountHelper.setRefreshToken(userInfo.getRefresh_token());
                        UserAccountHelper.saveLoginState(userInfo, false);
                        return userApiService.getCloudConfig();
                    }).flatMap((Function<MqttBean, Observable<WorkSpaceBean>>) mqttBean -> {
                        CloudDataHelper.saveMqttData(mqttBean);
                        return userApiService.getWorkSpace();
                    }).flatMap((Function<WorkSpaceBean, Observable<List<WebSocketSubscribeBean>>>) workSpaceBean -> {
                        UserAccountHelper.setWorkSpace(workSpaceBean);
                        return userApiService.getWebSocketSubscribeInfo(workSpaceBean.getWorkspaceId());
                    }), liveData, "用户名或密码错误！");
        }
    }

    public void loginSuccess(List<WebSocketSubscribeBean> subscribeBeanList, String userName) {
        UserAccountHelper.setWebSocketSubscribe(subscribeBeanList);
        UserAccountHelper.saveLoginPast(true);
        if (TextUtils.isEmpty(userName) || !userName.equals(UserAccountHelper.getAccount()) ||
                AppManager.getAppManager().getActivityStack().size() == 1) {
            UserAccountHelper.saveAccount(userName);
            //打开MainActivity
            baseView.showMainActivity();
            return;
        }
        UserAccountHelper.saveAccount(userName);
        baseView.loginSuccess();
    }
}
