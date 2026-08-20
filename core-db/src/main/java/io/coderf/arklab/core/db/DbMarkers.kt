package io.coderf.arklab.core.db

/**
 * core-db 模块占位与扩展点。
 * 现有 Room DAO / Repository 仍在 common，后续按包迁移：
 * - common.dao / database → core-db
 * - RoomRepository* → core-db
 *
 * 新代码可依赖本模块命名空间，避免继续向 common 堆数据库工具。
 */
object CoreDb {
    const val MODULE = "core-db"
}
