package io.coderf.arklab.demo.bean

import io.coderf.arklab.common.bean.AttachmentBean

/**
 * 人员编辑回显数据包：实体 + 多类型附件列表。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/9
 */
data class PersonEditModel(
    val person: Person,
    val images: List<AttachmentBean> = emptyList(),
    val videos: List<AttachmentBean> = emptyList(),
    val media: List<AttachmentBean> = emptyList(),
    val files: List<AttachmentBean> = emptyList()
)
