package com.casic.titan.commonui.enums;

/**
 * Created by fz on 2024/2/28 10:46
 * describe :附件字段枚举，TB_ATTACHMENT_INFO对应字段fieldName
 */
public enum AttachmentFieldEnum {
    /**
     * 命名规则，实体类（或表名）_字段名
     */
    DEFAULT("ID", "主表id"),
    ;
    public String field;
    public String describe;

    AttachmentFieldEnum(String field, String describe) {
        this.field = field;
        this.describe = describe;
    }

    public String getField() {
        return field;
    }

    public String getDescribe() {
        return describe;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
