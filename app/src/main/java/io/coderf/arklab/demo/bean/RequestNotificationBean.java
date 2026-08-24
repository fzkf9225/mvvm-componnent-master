package io.coderf.arklab.demo.bean;


import io.coderf.arklab.core.bean.PagingQuery;

/**
 * 新闻列表查询条件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/24 13:56
 */
public class RequestNotificationBean extends PagingQuery {
    /**
     * 类型（对应字典表类型编码：NEWS_TYPE）
     */
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
