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
    /** 等值 / 精确筛选条件：列名 → 值，拼入 WHERE（key 须为合法列标识符）。 */
    open var queryParams: MutableMap<String, Any?> = mutableMapOf(),
    /** 参与模糊搜索的列名集合；与 [keywords] 一起生成 `col LIKE %kw%`（多列 OR）。null / 空则不加关键字条件。 */
    open var keywordsKey: MutableSet<String>? = null,
    /** 模糊搜索关键字；null / 空时仍可对 [keywordsKey] 列做 `LIKE %%`（通常无实际过滤效果）。 */
    open var keywords: String? = null,
    /** 排序列名；空则由 PagingSource 侧回退默认列（常见为 `id`）。方向由查询 API（ASC/DESC）决定。 */
    open var orderBy: String? = null
) : PagingQuery()
