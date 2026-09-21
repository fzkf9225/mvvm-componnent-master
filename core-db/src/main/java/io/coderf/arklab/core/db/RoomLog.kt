package io.coderf.arklab.core.db

import android.os.SystemClock
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteProgram
import androidx.sqlite.db.SupportSQLiteQuery
import io.coderf.arklab.common.api.Config
import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.common.utils.log.LogUtil
import java.util.concurrent.Executor

/**
 * Room 调试日志
 *
 * 仅在 [Config.enableDebug] 为 true 时输出（与网络拦截器同一开关）。
 * 打开 [Config.setResponseBodyLogConverterJson] 时，结果体会额外走 [LogUtil.json]。
 *
 * ## 宿主接入
 * 1. `Config.getInstance().enableDebug(true)`（与网络日志相同）
 * 2. 构建数据库时调用 [attachTo]，可打印 Room 引擎执行的全部 SQL（含 `@Insert` / `@Update` 生成语句）
 *
 * ```java
 * PersonDatabase db = RoomLog.attachTo(
 *         Room.databaseBuilder(context, PersonDatabase.class, "xxx")
 *                 .allowMainThreadQueries()
 * ).build();
 * ```
 *
 * 未调用 [attachTo] 时，[io.coderf.arklab.common.dao.BaseRoomDao] 的动态 SQL 仍会打印。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/21
 */
object RoomLog {
    const val TAG = "RoomDb"

    private const val MAX_CHARS = 4096
    private val INTERNAL_TABLES = arrayOf(
        "room_table_modification_log",
        "room_master_table",
        "android_metadata"
    )
    private val DIRECT_EXECUTOR = Executor { command -> command.run() }

    /** 与 [Config.enableDebug] / 网络拦截器同一开关 */
    @JvmStatic
    fun isEnabled(): Boolean = Config.enableDebug.get()

    /**
     * 挂到 [RoomDatabase.Builder]，由 Room 引擎回调每一条实际执行的 SQL。
     *
     * @param builder [androidx.room.Room.databaseBuilder] 返回的 Builder
     * @return 原 Builder，便于继续链式调用
     */
    @JvmStatic
    fun <T : RoomDatabase> attachTo(
        builder: RoomDatabase.Builder<T>
    ): RoomDatabase.Builder<T> {
        return builder.setQueryCallback(
            { sql, bindArgs -> printQuery(sql, bindArgs) },
            DIRECT_EXECUTOR
        )
    }

    /**
     * 打印 Room 引擎或 DAO 执行的 SQL（对齐网络日志的 Url / Body）。
     */
    @JvmStatic
    @JvmOverloads
    fun printQuery(
        sql: String,
        bindArgs: List<Any?>? = null,
        tableName: String? = null
    ) {
        if (!isEnabled()) return
        if (INTERNAL_TABLES.any { sql.contains(it, ignoreCase = true) }) return
        val traceId = Integer.toHexString(System.identityHashCode(sql) xor sql.hashCode())
        val log = StringBuilder(256)
        log.append("--------------------Room SQL Start--------------------\n")
        log.append("Trace Id：").append(traceId).append('\n')
        if (!tableName.isNullOrBlank()) {
            log.append("Table：").append(tableName).append('\n')
        }
        log.append("SQL：").append(sql).append('\n')
        log.append("Bind Args：").append(formatBindArgs(bindArgs)).append('\n')
        log.append("--------------------Room SQL End--------------------")
        LogUtil.logger(TAG, log.toString())
    }

    /**
     * 打印 [SupportSQLiteQuery]（动态 RawQuery）。
     */
    @JvmStatic
    fun printSql(tableName: String, query: SupportSQLiteQuery) {
        if (!isEnabled()) return
        printQuery(query.sql, collectBindArgs(query), tableName)
    }

    /**
     * 开始一次仓库操作会话；在 subscribe 时调用，完成后 [Session.success] / [Session.error]。
     */
    @JvmStatic
    fun begin(
        repositoryName: String,
        operation: String,
        tableName: String,
        options: RoomRequestOptions?,
        payload: Any?
    ): Session {
        val traceId = Integer.toHexString(System.identityHashCode(Any()))
        return Session(traceId, repositoryName, operation, tableName, options, payload)
    }

    class Session internal constructor(
        private val traceId: String,
        private val repositoryName: String,
        private val operation: String,
        private val tableName: String,
        private val options: RoomRequestOptions?,
        private val payload: Any?
    ) {
        private val startMs = SystemClock.elapsedRealtime()

        fun success(result: Any?) {
            print(result, null)
        }

        fun error(error: Throwable) {
            print(null, error)
        }

        private fun print(result: Any?, error: Throwable?) {
            if (!isEnabled()) return
            val duration = SystemClock.elapsedRealtime() - startMs
            val log = StringBuilder(384)
            log.append("--------------------Room Start--------------------\n")
            log.append("Trace Id：").append(traceId).append('\n')
            log.append("Operation：").append(operation).append('\n')
            log.append("Table：").append(tableName).append('\n')
            log.append("Repository：").append(repositoryName).append('\n')
            if (options != null) {
                log.append("Show Dialog：").append(options.isShowDialog).append('\n')
                log.append("Dialog Message：").append(options.dialogMessage).append('\n')
                log.append("Timeout Seconds：").append(options.timeoutSeconds).append('\n')
                log.append("Throw On Empty：").append(options.isThrowOnEmptyList).append('\n')
            }
            log.append("Payload：").append(formatValue(payload)).append('\n')
            log.append("Duration：").append(duration).append("ms\n")
            if (error != null) {
                log.append("Error：")
                    .append(error.javaClass.simpleName)
                    .append(": ")
                    .append(error.message)
                    .append('\n')
            } else {
                log.append("Result：").append(formatValue(result)).append('\n')
            }
            log.append("--------------------Room End--------------------")
            LogUtil.logger(TAG, log.toString())
            if (error != null) {
                LogUtil.loggerE(TAG, "$operation failed", error)
            } else if (Config.getInstance().isResponseBodyLogConverterJson()) {
                jsonResult(result)
            }
        }
    }

    private fun jsonResult(result: Any?) {
        if (result == null || result is Unit || result == "complete") return
        val text = formatValue(result)
        if (text.startsWith("{") || text.startsWith("[")) {
            LogUtil.json(TAG, text)
        }
    }

    private fun collectBindArgs(query: SupportSQLiteQuery): List<Any?> {
        val count = query.argCount
        if (count <= 0) return emptyList()
        val collector = ArgsCollector(count)
        return try {
            query.bindTo(collector)
            collector.args.toList()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /** Bind Args 完整展开，避免只打 first。 */
    private fun formatBindArgs(bindArgs: List<Any?>?): String {
        if (bindArgs == null) return "null"
        if (bindArgs.isEmpty()) return "[]"
        return truncate(
            bindArgs.mapIndexed { index, arg ->
                "[$index]=${formatScalar(arg)}"
            }.joinToString(prefix = "[", postfix = "]", separator = ", ")
        )
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is ByteArray -> "[BLOB ${value.size} bytes]"
            is Collection<*> -> {
                if (value.isEmpty()) return "[]"
                truncate(
                    value.mapIndexed { index, item ->
                        "[$index]=${formatScalar(item)}"
                    }.joinToString(prefix = "[", postfix = "]", separator = ", ")
                )
            }
            is Array<*> -> formatValue(value.asList())
            is IntArray -> formatValue(value.toList())
            is LongArray -> formatValue(value.toList())
            is Map<*, *> -> {
                if (value.isEmpty()) return "{}"
                truncate(
                    value.entries.joinToString(prefix = "{", postfix = "}", separator = ", ") {
                        "${it.key}=${formatScalar(it.value)}"
                    }
                )
            }
            else -> truncate(value.toString())
        }
    }

    /** 单值展开：集合只做浅层摘要，避免 Bind Args / 嵌套结构递归爆炸。 */
    private fun formatScalar(value: Any?): String {
        return when (value) {
            null -> "null"
            is ByteArray -> "[BLOB ${value.size} bytes]"
            is String -> "\"$value\""
            is Collection<*> -> "Collection(size=${value.size})"
            is Array<*> -> "Array(size=${value.size})"
            is Map<*, *> -> "Map(size=${value.size})"
            else -> value.toString()
        }
    }

    private fun truncate(text: String): String {
        return if (text.length > MAX_CHARS) {
            text.take(MAX_CHARS) + "...(truncated, ${text.length} chars)"
        } else {
            text
        }
    }

    private class ArgsCollector(size: Int) : SupportSQLiteProgram {
        val args = arrayOfNulls<Any>(size)

        override fun bindNull(index: Int) {
            setArg(index, null)
        }

        override fun bindLong(index: Int, value: Long) {
            setArg(index, value)
        }

        override fun bindDouble(index: Int, value: Double) {
            setArg(index, value)
        }

        override fun bindString(index: Int, value: String) {
            setArg(index, value)
        }

        override fun bindBlob(index: Int, value: ByteArray) {
            setArg(index, "[BLOB ${value.size} bytes]")
        }

        override fun clearBindings() {
            args.fill(null)
        }

        override fun close() {
            // no-op
        }

        private fun setArg(index: Int, value: Any?) {
            val i = index - 1
            if (i in args.indices) {
                args[i] = value
            }
        }
    }
}
