package io.coderf.arklab.core.bean

/**
 * 分页业务查询参数基类。
 *
 * 业务侧定义具体条件（关键词、筛选、时间范围等），由 [io.coderf.arklab.core.network.NetworkFlowPagingViewModel]
 * 持有，经 [io.coderf.arklab.core.network.NetworkPagingSource] 快照传入
 * [io.coderf.arklab.core.network.NetworkPagingRepository.fetchPage]。
 *
 * 无额外筛选条件时使用 [EmptyPagingQuery]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/22 12:23
 */
open class PagingQuery

/**
 * 无业务筛选条件时的默认查询参数。
 */
class EmptyPagingQuery : PagingQuery()
