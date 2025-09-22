package com.casic.otitan.usercomponent.module;

import com.casic.otitan.userapi.UserService;
import com.casic.otitan.usercomponent.impl.UserServiceImpl;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Created by fz on 2023/5/17 11:20
 * describe :
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public abstract class UserServiceModule {
    @Binds
    abstract UserService bindUserService(UserServiceImpl userServiceImpl);
}
