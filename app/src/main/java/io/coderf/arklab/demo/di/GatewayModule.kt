package io.coderf.arklab.demo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.coderf.arklab.base.gateway.MessageGateway
import io.coderf.arklab.demo.mqtt.MqttMessageGateway
import io.coderf.arklab.mqtt.MqttClient
import javax.inject.Singleton

/**
 * Demo 组装层：业务依赖 base.gateway.*；框架 media/mqtt 不感知 Gateway。
 * MediaGateway 见 [MediaGatewayModule]。
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
