package io.coderf.arklab.demo.di;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.base.api.AppPropertiesConfig;

/**
 * 供尚未改为 @HiltViewModel 的类读取 {@link AppPropertiesConfig}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface AppPropertiesEntryPoint {
    AppPropertiesConfig appPropertiesConfig();
}
