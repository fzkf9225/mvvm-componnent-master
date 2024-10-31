package com.casic.titan.usercomponent.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.lifecycle.HiltViewModel;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.api.UserApiService;
import com.casic.titan.usercomponent.bean.UserInfo;

import dagger.hilt.components.SingletonComponent;
import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.jiami.MD5Util;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.ToastUtils;

import com.casic.titan.usercomponent.bean.RequestLoginBean;
import com.casic.titan.usercomponent.repository.UserRepositoryImpl;
import com.casic.titan.usercomponent.view.UserView;

import javax.inject.Inject;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:loginViewModel
 */
@HiltViewModel
public class UserViewModel extends BaseViewModel<UserRepositoryImpl, UserView> {
    private final MutableLiveData<UserInfo> liveData = new MutableLiveData<>();

    @Inject
    UserApiService userApiService;

    @Inject
    public RetryService retryService;

    @Inject
    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected UserRepositoryImpl createRepository() {
        return new UserRepositoryImpl(userApiService, retryService, baseView);
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
                baseView.showToast( "请填写用户名");
                return;
            }
            if (StringUtil.isEmpty(password)) {
                baseView.showToast(  "请填写密码");
                return;
            }
            try {
                requestLoginBean.setPassword(MD5Util.md5Encode(password));
            } catch (Exception e) {
                e.printStackTrace();
                baseView.showToast("密码加密异常");
            }
//            iRepository.login(requestLoginBean, liveData);
            //模拟登录情况
            baseView.showLoading("正在登录，请稍后...");
            UserAccountHelper.setToken("this is token !!!");
            UserAccountHelper.setRefreshToken("this is refresh_token !!!");
            UserInfo userInfo = new UserInfo() {{
                setId("1");
                setAvatar("https://img2.baidu.com/it/u=2948556484,2204941832&fm=253&fmt=auto&app=120&f=JPEG?w=655&h=436");
                setEmail("fzkf3318@163.com");
                setName("张三");
                setPhone("15210230000");
                setRealName("张韶涵");
                setRoleName("演员");
                setSex(1);
            }};
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                baseView.hideLoading();
                liveData.setValue(userInfo);
            }, 3000);
        }
    }

    public void loginCallback(UserInfo userInfo, String userName) {
        //存储登录信息和登录状态
        UserAccountHelper.saveLoginState(userInfo, true);
        //这里只是判断本地账号和上次账号是否为同一个，如果不是同一个则不能继续之前操作，则需要返回App首页刷新，并且同事判断下当前app是不是只有当前登录页一个页面
        if (TextUtils.isEmpty(userName) || !userName.equals(UserAccountHelper.getAccount()) ||
                AppManager.getAppManager().getActivityStack().size() == 1) {
            UserAccountHelper.saveAccount(userName);
            //打开MainActivity
            baseView.toMain();
            return;
        }
        //存储本地登录的账号
        UserAccountHelper.saveAccount(userName);
        if (baseView.hasTarget()) {
            baseView.toTarget();
            return;
        }
        baseView.toLast();
    }
}
