package io.coderf.arklab.common.annotation

import kotlin.reflect.KClass

/**
 * 标注在 Room `@Dao` 类上，由 KSP 自动生成 RawQuery 桥接类，免去手写 `@RawQuery` 方法
 * （含查询 / count / 条件删除返回行数共 8 个 do*）。
 *
 * ## 使用步骤
 * 1. 模块 `build.gradle` 添加：`ksp project(':room-processor')`
 * 2. 在 DAO 上添加本注解并指定实体类型
 * 3. 编译后生成 `{DaoName}RawQueryBridge`（位于 `build/generated/ksp/...`）
 * 4. 将 DAO 父类从 `BaseRoomDao<Entity>` 改为 `{DaoName}RawQueryBridge`
 *
 * ## Kotlin 示例
 * ```kotlin
 * abstract class AttachmentDao : AttachmentDaoRawQueryBridge() {
 *     override fun getTableName() = "AttachmentBean"
 * }
 * ```
 * ## Java 示例
 * ```java
 * public abstract class PersonDao extends PersonDaoRawQueryBridge {
 *     @Override
 *     public String getTableName() { return "Person"; }
 * }
 * ```
 * ## 不使用的场景
 * 老项目可继续 `extends BaseRoomDao<T>()` 并手动 override 底部全部 `do*` RawQuery 方法
 * （含 doCount、doExecute）。
 *
 * @Dao
 * @RoomObservedEntity(AttachmentBean::class)
 * @Dao
 * @RoomObservedEntity(Person.class)
 * @param value 本 DAO 对应的 `@Entity` 类型，用于 `@RawQuery(observedEntities = [...])`
 * @see io.coderf.arklab.common.dao.BaseRoomDao
 * @see io.coderf.arklab.room.processor.RoomObservedEntityProcessor
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RoomObservedEntity(
    val value: KClass<*>
)