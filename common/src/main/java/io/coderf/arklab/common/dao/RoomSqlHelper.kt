package io.coderf.arklab.common.dao

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * Room 动态 SQL 拼装工具（仅供 [BaseRoomDao] 内部使用）。
 *
 * ## 职责
 * - 将 **条件值** 用 `?` 占位绑定，降低 SQL 注入风险；
 * - 对 **列名 / 排序字段** 做标识符校验（仅允许字母、数字、下划线）；
 * - 统一生成 [SupportSQLiteQuery]，供 `@RawQuery` 方法执行。
 *
 * ## 注意
 * - 表名 [tableName] 由子类 [BaseRoomDao.getTableName] 提供，须与 `@Entity(tableName)` 一致；
 * - `orderBy`、Map 的 key 等「标识符」不能参数化，只能白名单校验后拼接。
 *
 * @author fz
 * @since 1.0
 */
internal object RoomSqlHelper {

    /** SQL 标识符合法字符：字母、数字、下划线，且不能以数字开头 */
    private val IDENTIFIER_PATTERN = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")

    /**
     * 校验列名或排序字段名是否合法。
     *
     * @param name 待校验名称
     * @param role 用途描述，用于异常信息（如 column、orderBy）
     * @throws IllegalArgumentException 名称不合法时抛出
     */
    fun requireIdentifier(name: String, role: String = "column") {
        require(name.matches(IDENTIFIER_PATTERN)) {
            "Invalid SQL $role name: $name (only letters, digits and underscore allowed)"
        }
    }

    /**
     * 根据等值条件 Map 生成 `WHERE col = ? AND ...` 片段及绑定参数。
     *
     * @param params 列名 -> 值；key 会做标识符校验
     * @return Pair(Where 子句含前导空格，无则空串；绑定参数数组)
     */
    fun buildWhereClause(params: Map<String, Any>): Pair<String, Array<Any>> {
        if (params.isEmpty()) return "" to emptyArray()
        val args = mutableListOf<Any>()
        val conditions = params.entries.joinToString(" AND ") { (key, value) ->
            requireIdentifier(key)
            args.add(value)
            "$key = ?"
        }
        return " WHERE $conditions" to args.toTypedArray()
    }

    /**
     * 生成多字段模糊查询片段：`(col1 LIKE ? OR col2 LIKE ?)`。
     *
     * @param keywordsKey 参与 LIKE 的列名集合
     * @param keywords 关键字，内部会包成 `%keyword%`
     * @return Pair(子句含前导空格与括号；每个列对应一个绑定参数)
     */
    fun buildKeywordClause(
        keywordsKey: Set<String>?,
        keywords: String?
    ): Pair<String, Array<Any>> {
        if (keywordsKey.isNullOrEmpty()) return "" to emptyArray()
        keywordsKey.forEach { requireIdentifier(it) }
        val pattern = "%${keywords.orEmpty()}%"
        val placeholders = keywordsKey.joinToString(" OR ") { "$it LIKE ?" }
        val args: Array<Any> = Array(keywordsKey.size) { pattern }
        return " ($placeholders)" to args
    }

    /**
     * 将关键字条件并入已有 WHERE。
     *
     * @param baseWhere [buildWhereClause] 返回的片段，可为空
     * @param keywordClause [buildKeywordClause] 返回的片段，可为空
     * @return 合并后的 WHERE 子句（含 `WHERE` 关键字）
     */
    fun appendKeywordToWhere(
        baseWhere: String,
        keywordClause: String
    ): String {
        if (keywordClause.isBlank()) return baseWhere
        return if (baseWhere.isBlank()) {
            " WHERE$keywordClause"
        } else {
            "$baseWhere AND$keywordClause"
        }
    }

    /**
     * 生成带 ASC/DESC 的 ORDER BY 子句。
     *
     * @param orderBy 排序列名，空则返回空串
     * @param descending true 为 DESC，false 为 ASC
     */
    fun buildOrderClause(orderBy: String?, descending: Boolean): String {
        if (orderBy.isNullOrBlank()) return ""
        requireIdentifier(orderBy, role = "orderBy")
        return if (descending) " ORDER BY $orderBy DESC" else " ORDER BY $orderBy ASC"
    }

    /**
     * 生成不带 ASC/DESC 的 ORDER BY，兼容原 [BaseRoomDao.doQueryByLimit] 行为。
     *
     * @param orderBy 排序列名，空则返回空串
     */
    fun buildOrderClausePlain(orderBy: String?): String {
        if (orderBy.isNullOrBlank()) return ""
        requireIdentifier(orderBy, role = "orderBy")
        return " ORDER BY $orderBy"
    }

    /**
     * 拼装 SELECT 查询。
     *
     * @param tableName 表名
     * @param where WHERE 片段（可含 `WHERE`）
     * @param bindArgs 与 where 中 `?` 对应的参数
     * @param order ORDER BY 片段
     * @param limit 可选 LIMIT
     * @param offset 可选 OFFSET
     */
    fun query(
        tableName: String,
        where: String = "",
        bindArgs: Array<Any> = emptyArray(),
        order: String = "",
        limit: Int? = null,
        offset: Int? = null
    ): SupportSQLiteQuery {
        val limitSql = if (limit != null) " LIMIT $limit" else ""
        val offsetSql = if (offset != null) " OFFSET $offset" else ""
        val sql = "SELECT * FROM $tableName$where$order$limitSql$offsetSql"
        return if (bindArgs.isEmpty()) SimpleSQLiteQuery(sql) else SimpleSQLiteQuery(sql, bindArgs)
    }

    /**
     * 拼装 DELETE 语句。
     *
     * @param tableName 表名
     * @param where WHERE 片段，空表示删全表
     * @param bindArgs 绑定参数
     */
    fun delete(
        tableName: String,
        where: String = "",
        bindArgs: Array<Any> = emptyArray()
    ): SupportSQLiteQuery {
        val sql = "DELETE FROM $tableName$where"
        return if (bindArgs.isEmpty()) SimpleSQLiteQuery(sql) else SimpleSQLiteQuery(sql, bindArgs)
    }

    /**
     * 按单列等值查询：`SELECT * FROM table WHERE column = ?`。
     *
     * @param tableName 表名
     * @param column 列名（会校验）
     * @param value 列值，使用占位符绑定
     */
    fun selectByColumn(
        tableName: String,
        column: String,
        value: Any
    ): SupportSQLiteQuery {
        requireIdentifier(column)
        return SimpleSQLiteQuery(
            "SELECT * FROM $tableName WHERE $column = ?",
            arrayOf(value)
        )
    }
}
