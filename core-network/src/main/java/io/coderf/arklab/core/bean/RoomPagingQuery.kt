package io.coderf.arklab.core.bean

open class RoomPagingQuery(
    open var queryParams: MutableMap<String, Any?> = mutableMapOf(),
    open var keywordsKey: MutableSet<String>? = null,
    open var keywords: String? = null,
    open var orderBy: String? = null
) : PagingQuery()