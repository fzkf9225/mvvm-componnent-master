package io.coderf.arklab.demo.impl;

import javax.inject.Inject;

import io.coderf.arklab.demo.inter.HiltUserService;

/**
 * NewHiltUserServiceImpl 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/5/31 13:50
 */
public class NewHiltUserServiceImpl implements HiltUserService {
    @Inject
    public NewHiltUserServiceImpl() {
    }

    @Override
    public void onLogin(String userName, String password) {
        System.out.println("这是NewHiltUserServiceImpl实现类的onLogin方法，userName："+userName+";password："+password);
    }
}
