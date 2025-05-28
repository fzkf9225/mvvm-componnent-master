package com.casic.titan.demo.bean

import androidx.databinding.BaseObservable

/**
 * created by fz on 2024/10/12 11:00
 * describe:
 */
data class NotificationMessageBean(
    var id: String? = null,
    var createDate: String? = null,
    var createUser: String? = null,
    var content: String? = null,
    /**
     * 是否发布（1是，0否）
     */
    var isPublish: String? = null,
    /**
     * 摘要
     */
    var precis: String? = null,
    /**
     * 发布时间
     */
    var publishDate: String? = null,
    /**
     * 备注
     */
    var remark: String? = null,
    /**
     * 来源
     */
    var source: String? = null,
    /**
     * 标题
     */
    var title: String? = null,
    /**
     * 类型（对应字典表类型编码：NEWS_TYPE）
     */
    var type: String? = null,
    /**
     * 类型名称
     */
    var typeName: String? = null,
    /**
     * 编辑时间
     */
    var updateDate: String? = null,
    /**
     * 编辑用户
     */
    var updateUser: String? = null,
    /**
     * 是否已读（1是，0否）
     */
    var isRead: Int? = null,
    /**
     * 已读时间
     */
    var readDate: String? = null,
): BaseObservable() {
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}