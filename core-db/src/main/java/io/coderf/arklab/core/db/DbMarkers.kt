package io.coderf.arklab.core.db

/**
 * core-db 模块说明。
 *
 * ## 两套并行栈（互不桥接）
 * - **RxJava3**：`common.repository.RoomRepositoryImpl` + Dao 的 Completable / Flowable / Single
 * - **协程**：本包 [RoomDataRepository] / [DefaultRoomRepository] + Dao 的 Room 原生 `*Suspend`
 *
 * 新 Kotlin 代码优先协程栈；存量 Java / Rx 继续用 RoomRepositoryImpl。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
object CoreDb {
    const val MODULE = "core-db"
}
