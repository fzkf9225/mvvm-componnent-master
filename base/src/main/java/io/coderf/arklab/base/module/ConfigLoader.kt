package io.coderf.arklab.base.module

import android.content.Context
import io.coderf.arklab.base.api.AppPropertiesConfig
import io.coderf.arklab.base.enums.PropertiesKeyEnum
import io.coderf.arklab.common.utils.common.PropertiesUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun loadConfig(configFileName: String): AppPropertiesConfig {
        val properties = PropertiesUtil.getInstance().loadConfig(context, configFileName)
        return AppPropertiesConfig(
            baseUrl = properties.baseUrl.orEmpty(),
            fileBaseUrl = properties.getProperty(PropertiesKeyEnum.FILE_BASE_URL.key).orEmpty(),
            sm2PublicKey = properties.getProperty(PropertiesKeyEnum.SM2_PUBLIC_KEY.key).orEmpty(),
            dataBase = properties.getProperty(PropertiesKeyEnum.DATA_BASE.key).orEmpty(),
            protocolVersion = properties.protocolVersion,
            tokenType = properties.getProperty(PropertiesKeyEnum.TOKEN_TYPE.key).orEmpty(),
            tenantId = properties.getProperty(PropertiesKeyEnum.TENANT_ID.key).orEmpty(),
            businessDataBase = properties.getProperty(PropertiesKeyEnum.BUSINESS_DATA_BASE.key).orEmpty(),
            attachmentDataBase = properties.getProperty(PropertiesKeyEnum.ATTACHMENT_DATA_BASE.key).orEmpty()
        )
    }
}
