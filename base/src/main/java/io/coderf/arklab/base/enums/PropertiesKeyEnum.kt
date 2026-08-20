package io.coderf.arklab.base.enums

/**
 * Properties 文件配置 key（与 [io.coderf.arklab.base.api.AppPropertiesConfig] 对应）。
 */
enum class PropertiesKeyEnum(
    val key: String,
    val desc: String
) {
    BASE_URL("BASE_URL", "系统模块 baseUrl"),
    FILE_BASE_URL("FILE_BASE_URL", "文件 baseUrl"),
    SM2_PUBLIC_KEY("SM2_PUBLIC_KEY", "SM2 publicKey"),
    DATA_BASE("DATA_BASE", "数据库名称"),
    BUSINESS_DATA_BASE("BUSINESS_DATA_BASE", "业务模块数据库名称"),
    ATTACHMENT_DATA_BASE("ATTACHMENT_DATA_BASE", "附件数据库名称"),
    PROTOCOL_VERSION("PROTOCOL_VERSION", "接口协议版本"),
    TOKEN_TYPE("TOKEN_TYPE", "token 类型"),
    TENANT_ID("TENANT_ID", "租户 ID"),
}
