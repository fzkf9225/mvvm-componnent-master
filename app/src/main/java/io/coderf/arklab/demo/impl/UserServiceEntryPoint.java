package io.coderf.arklab.demo.impl;


import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * UserServiceEntryPoint 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/5/31 15:24
 */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface UserServiceEntryPoint {
    HiltUserServiceImpl getHiltUserServiceImpl();
}
