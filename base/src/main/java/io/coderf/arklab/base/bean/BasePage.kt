package io.coderf.arklab.base.bean

/**
 * 通用分页响应结构。
 */
open class BasePage<T> {
    var records: List<T>? = null
    var current: String? = null
    var size: String? = null
    var pages: Int? = null
    var total: Int? = null
}
