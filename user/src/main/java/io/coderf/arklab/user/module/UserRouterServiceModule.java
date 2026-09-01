package io.coderf.arklab.user.module;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.userapi.router.UserRouterService;
import io.coderf.arklab.user.impl.UserRouterServiceImpl;

/**
 * UserRouterServiceModule 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/17 11:20
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public abstract class UserRouterServiceModule {
    @Binds
    abstract UserRouterService bindUserRouterService(UserRouterServiceImpl userRouterServiceImpl);
}
