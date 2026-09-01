package io.coderf.arklab.core.bean

/**
 * RoomPagingQuery 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
open class RoomPagingQuery(
    open var queryParams: MutableMap<String, Any?> = mutableMapOf(),
    open var keywordsKey: MutableSet<String>? = null,
    open var keywords: String? = null,
    open var orderBy: String? = null
) : PagingQuery()