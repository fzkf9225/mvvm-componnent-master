package io.coderf.arklab.demo.module;

import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import io.coderf.arklab.demo.bean.HiltExpensiveService;
import io.coderf.arklab.demo.bean.HiltSingletonCounter;
import io.coderf.arklab.demo.impl.ConsoleLogPlugin;
import io.coderf.arklab.demo.impl.NetworkLogPlugin;
import io.coderf.arklab.demo.inter.HiltLogPlugin;

/**
 * Hilt 进阶用法演示 Module：@Named、@Singleton、@IntoSet 等。
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class HiltDemoModule {

    @Provides
    @Named("env_prod")
    static String provideProdEnv() {
        return "production";
    }

    @Provides
    @Named("env_dev")
    static String provideDevEnv() {
        return "development";
    }

    @Provides
    @Singleton
    static HiltSingletonCounter provideSingletonCounter() {
        return new HiltSingletonCounter();
    }

    @Provides
    static HiltExpensiveService provideExpensiveService() {
        return new HiltExpensiveService();
    }

    @Binds
    @IntoSet
    abstract HiltLogPlugin bindConsoleLogPlugin(ConsoleLogPlugin impl);

    @Binds
    @IntoSet
    abstract HiltLogPlugin bindNetworkLogPlugin(NetworkLogPlugin impl);

    /** 演示 Set 多绑定的汇总输出，便于在 ViewModel 中一次性调用所有插件。 */
    @Provides
    static String provideLogPluginSummary(Set<HiltLogPlugin> plugins) {
        StringBuilder sb = new StringBuilder();
        for (HiltLogPlugin plugin : plugins) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(plugin.name());
        }
        return sb.toString();
    }
}
