package pers.fz.annotation.enums;


/**
 * Created by fz on 2025/8/13.
 * describe：验证分组
 */
public enum VerifyGroupEnum {
    /*
     * default
     */
    DEFAULT("default"),
    /*
     * 新增
     */
    CREATE("create"),
    /*
     * 编辑
     */
    EDITOR("editor"),
    ;

    private final String group;

    VerifyGroupEnum(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

}
