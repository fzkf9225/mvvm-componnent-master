package com.casic.titan.demo.bean;


import pers.fz.mvvm.bean.base.BasePagingBean;

/**
 * Created by fz on 2023/12/1 15:28
 * describe :
 */
public class NotificationMessageBean extends BasePagingBean {
    private String createDate;
    private String createUser;
    private String content;
    /**
     * 是否发布（1是，0否）
     */
    private String isPublish;
    /**
     * 摘要
     */
    private String precis;
    /**
     * 发布时间
     */
    private String publishDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 来源
     */
    private String source;
    /**
     * 标题
     */
    private String title;
    /**
     * 类型（对应字典表类型编码：NEWS_TYPE）
     */
    private String type;
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 编辑时间
     */
    private String updateDate;
    /**
     * 编辑用户
     */
    private String updateUser;
    /**
     * 是否已读（1是，0否）
     */
    private Integer isRead;
    /**
     * 已读时间
     */
    private String readDate;

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIsPublish() {
        return isPublish;
    }

    public void setIsPublish(String isPublish) {
        this.isPublish = isPublish;
    }

    public String getPrecis() {
        return precis;
    }

    public void setPrecis(String precis) {
        this.precis = precis;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getReadDate() {
        return readDate;
    }

    public void setReadDate(String readDate) {
        this.readDate = readDate;
    }
}
