package io.coderf.arklab.room.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP 入口：注册 [RoomObservedEntityProcessor]。
 *
 * 在消费模块的 `build.gradle` 中添加：
 * `ksp project(':room-processor')`
 *
 * @see RoomObservedEntityProcessor
 * @see io.coderf.arklab.common.annotation.RoomObservedEntity
 */
class RoomObservedEntityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RoomObservedEntityProcessor(environment.codeGenerator, environment.logger)
    }
}
