package com.casic.otitan.demo.impl;

import android.app.Application;

import com.casic.otitan.demo.inter.HiltUserService;

import javax.inject.Inject;


/**
 * Created by fz on 2024/5/31 13:50
 * describe :
 */
public class ContextHiltUserServiceImpl implements HiltUserService {
    private Application application;

    @Inject
    public ContextHiltUserServiceImpl( Application application) {
        this.application = application;
    }

    @Override
    public void onLogin(String userName, String password) {
        System.out.println("这是HiltUserServiceImpl实现类的onLogin方法，userName：" + userName + ";password：" + password + ";context是否为空：" + (application == null));
    }
}
