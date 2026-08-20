package io.coderf.arklab.demo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.coderf.arklab.mqtt.MqttClient
import io.coderf.arklab.mqtt.gateway.MqttMessageGateway
import io.coderf.arklab.userapi.gateway.MessageGateway
import javax.inject.Singleton

/**
 * 能力网关绑定：业务只依赖 userapi.gateway.*。
 * MediaGateway 由 commonmedia 的 ActivityComponent 模块提供。
 */
@Module
@InstallIn(SingletonComponent::class)
object GatewayModule {

    @Provides
    @Singleton
    fun provideMqttClient(): MqttClient = MqttClient("AppMqtt")

    @Provides
    @Singleton
    fun provideMessageGateway(client: MqttClient): MessageGateway {
        return MqttMessageGateway(client) {
            null
        }
    }
}
