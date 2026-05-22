package io.coderf.arklab.room.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import java.io.OutputStreamWriter

private const val ANNOTATION_NAME = "io.coderf.arklab.common.annotation.RoomObservedEntity"
private const val BASE_DAO_NAME = "io.coderf.arklab.common.dao.BaseRoomDao"

/**
 * 处理 [@RoomObservedEntity][io.coderf.arklab.common.annotation.RoomObservedEntity] 注解，
 * 为每个 DAO 生成 `{DaoName}RawQueryBridge` 抽象类。
 *
 * ## 生成内容
 * - 继承 `BaseRoomDao<Entity>`
 * - 实现 6 个 `@RawQuery(observedEntities = [Entity::class])` 的 `do*` 方法
 *
 * ## 开发者操作
 * 将手写 DAO 的父类从 `BaseRoomDao` 改为生成的 `XxxDaoRawQueryBridge` 即可。
 *
 * @see RoomObservedEntityProcessorProvider
 */
class RoomObservedEntityProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /**
     * 扫描所有带 [RoomObservedEntity] 的类并生成 Bridge 文件。
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME)
        symbols.filterIsInstance<KSClassDeclaration>().forEach { daoClass ->
            processDao(daoClass)
        }
        return emptyList()
    }

    /** 为单个 DAO 生成 RawQueryBridge 源文件 */
    private fun processDao(daoClass: KSClassDeclaration) {
        val daoName = daoClass.simpleName.asString()
        val packageName = daoClass.packageName.asString()
        val entityType = resolveEntityType(daoClass) ?: return
        val entityName = entityType.declaration.simpleName.asString()
        val bridgeName = "${daoName}RawQueryBridge"

        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = false, daoClass.containingFile!!),
            packageName,
            bridgeName
        )
        OutputStreamWriter(file).use { out ->
            out.write(
                """
                package $packageName

                import androidx.lifecycle.LiveData
                import androidx.room.RawQuery
                import androidx.sqlite.db.SupportSQLiteQuery
                import io.coderf.arklab.common.dao.BaseRoomDao
                import io.reactivex.rxjava3.core.Flowable
                import io.reactivex.rxjava3.core.Single
                import ${entityType.declaration.packageName.asString()}.$entityName

                /**
                 * KSP 自动生成：为 [$daoName] 绑定 @RawQuery observedEntities = [$entityName::class]
                 * 请勿手动修改。请将 [$daoName] 的父类声明为 [$bridgeName]。
                 */
                abstract class $bridgeName : BaseRoomDao<$entityName>() {

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doFindLiveData(query: SupportSQLiteQuery): LiveData<$entityName>

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doFindListLiveData(query: SupportSQLiteQuery): LiveData<List<$entityName>>

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doFind(query: SupportSQLiteQuery): Single<$entityName>

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doFindList(query: SupportSQLiteQuery): Single<List<$entityName>>

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doQueryFlowable(query: SupportSQLiteQuery): Flowable<List<$entityName>>

                    @RawQuery(observedEntities = [$entityName::class])
                    protected abstract override fun doQueryList(query: SupportSQLiteQuery): List<$entityName>
                }

                """.trimIndent()
            )
        }
        logger.info("Generated $packageName.$bridgeName for $daoName")
    }

    /** 从注解参数或父类泛型解析实体类型 */
    private fun resolveEntityType(daoClass: KSClassDeclaration): KSType? {
        val annotation = daoClass.annotations.firstOrNull {
            it.shortName.asString() == "RoomObservedEntity"
        } ?: return null
        when (val value = annotation.arguments.firstOrNull()?.value) {
            is KSType -> return value
            is KSClassDeclaration -> return value.asType(emptyList())
        }
        return findEntityTypeFromSupertypes(daoClass)
    }

    /** 从 BaseRoomDao&lt;T&gt; 或已有 RawQueryBridge 的泛型参数解析 T */
    private fun findEntityTypeFromSupertypes(daoClass: KSClassDeclaration): KSType? {
        for (superRef in daoClass.superTypes) {
            val type = superRef.resolve()
            val qn = type.declaration.qualifiedName?.asString().orEmpty()
            val simple = type.declaration.simpleName.asString()
            if (qn == BASE_DAO_NAME || simple == "BaseRoomDao" || simple.endsWith("RawQueryBridge")) {
                type.arguments.firstOrNull()?.type?.resolve()?.let { return it }
            }
        }
        logger.error("Cannot resolve entity type for ${daoClass.simpleName.asString()}", daoClass)
        return null
    }
}
