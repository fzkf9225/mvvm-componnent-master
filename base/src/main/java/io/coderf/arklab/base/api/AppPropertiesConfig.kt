package io.coderf.arklab.base.api

/**
 * assets 配置文件映射（通用字段；宿主可按项目扩展 data class 或另建 Config）。
 */
data class AppPropertiesConfig(
    val baseUrl: String,
    val fileBaseUrl: String,
    val sm2PublicKey: String,
    val dataBase: String,
    val protocolVersion: String,
    val tokenType: String,
    val tenantId: String,
    val businessDataBase: String,
    val attachmentDataBase: String
)
