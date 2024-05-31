package com.casic.titan.demo.impl;


import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Created by fz on 2024/5/31 15:24
 * describe :
 */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface UserServiceEntryPoint {
    HiltUserServiceImpl getHiltUserServiceImpl();
}
