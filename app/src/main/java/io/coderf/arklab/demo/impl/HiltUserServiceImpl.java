package io.coderf.arklab.demo.impl;

import io.coderf.arklab.demo.inter.HiltUserService;

import javax.inject.Inject;

/**
 * Created by fz on 2024/5/31 13:50
 * describe :
 */
public class HiltUserServiceImpl implements HiltUserService {
    @Inject
    public HiltUserServiceImpl() {
    }

    @Override
    public void onLogin(String userName, String password) {
        System.out.println("这是HiltUserServiceImpl实现类的onLogin方法，userName："+userName+";password："+password);
    }
}
