package io.coderf.arklab.core.bean

/**
 * Room 本地分页查询参数。
 *
 * 由 ViewModel 持有，创建 [io.coderf.arklab.core.db.RoomPagingSource] /
 * [io.coderf.arklab.common.datasource.RxRoomPagingSource] 时快照传入。
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
