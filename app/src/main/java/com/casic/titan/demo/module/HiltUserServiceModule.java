package com.casic.titan.demo.module;

import com.casic.titan.demo.impl.ContextHiltUserServiceImpl;
import com.casic.titan.demo.impl.HiltUserServiceImpl;
import com.casic.titan.demo.impl.NewHiltUserServiceImpl;
import com.casic.titan.demo.inter.HiltUserService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Created by fz on 2024/5/31 13:51
 * describe :
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class HiltUserServiceModule {

    @Binds
    @HiltUser
    public abstract HiltUserService bindUserService(HiltUserServiceImpl hiltUserServiceImpl);

    @Binds
    @NewHiltUser
    public abstract HiltUserService bindNewUserService(NewHiltUserServiceImpl newHiltUserService);

    @Binds
    @ContextHiltUser
    public abstract HiltUserService bindContextUserService(ContextHiltUserServiceImpl contextHiltUserService);

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HiltUser {
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NewHiltUser {
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ContextHiltUser {
    }
}

