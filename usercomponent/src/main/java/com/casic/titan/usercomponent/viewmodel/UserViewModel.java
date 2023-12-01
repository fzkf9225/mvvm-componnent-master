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
import com.casic.titan.usercomponent.repository.UserRepository;
import com.casic.titan.usercomponent.view.UserView;

import java.util.List;

import javax.inject.Inject;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:loginViewModel
 */
@HiltViewModel
public class UserViewModel extends BaseViewModel<UserRepository, UserView> {
    private final MutableLiveData<UserInfo> liveData = new MutableLiveData<>();

    @Inject
    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected UserRepository createRepository() {
        return new UserRepository(retryService);
    }

    public void loginClick(View v, RequestLoginBean requestLoginBean) {
        loginClick(v, requestLoginBean, null);
    }

    public MutableLiveData<UserInfo> getLiveData() {
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
                e.printStackTrace();
                baseView.showToast("密码加密异常");
            }
            iRepository.login(requestLoginBean, liveData);
        }
    }

    public void loginSuccess(UserInfo userInfo, String userName) {
        UserAccountHelper.saveLoginState(userInfo,true);
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
