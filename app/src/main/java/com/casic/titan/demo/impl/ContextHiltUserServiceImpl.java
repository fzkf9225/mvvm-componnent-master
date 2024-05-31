package com.casic.titan.demo.impl;

import android.content.Context;

import com.casic.titan.demo.inter.HiltUserService;

import javax.inject.Inject;

import pers.fz.mvvm.helper.ApplicationContext;

/**
 * Created by fz on 2024/5/31 13:50
 * describe :
 */
public class ContextHiltUserServiceImpl implements HiltUserService {
    private Context context;

    @Inject
    public ContextHiltUserServiceImpl(@ApplicationContext Context context) {
        this.context = context;
    }

    @Override
    public void onLogin(String userName, String password) {
        System.out.println("这是HiltUserServiceImpl实现类的onLogin方法，userName：" + userName + ";password：" + password + ";context是否为空：" + (context == null));
    }
}
