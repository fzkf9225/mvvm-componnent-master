package io.coderf.arklab.demo.bean;

import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * Family 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/8/12 17:01
 */
@VerifyEntity(sort = true)
public class Family {
    @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请填写妻子姓名！")
    private String wife;

    @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请填写丈夫姓名！")
    private String husband;
    private String son;
    private String daughter;

    public Family(String wife, String husband) {
        this.wife = wife;
        this.husband = husband;
    }

    public Family() {
    }

    public String getWife() {
        return wife;
    }

    public void setWife(String wife) {
        this.wife = wife;
    }

    public String getHusband() {
        return husband;
    }

    public void setHusband(String husband) {
        this.husband = husband;
    }

    public String getSon() {
        return son;
    }

    public void setSon(String son) {
        this.son = son;
    }

    public String getDaughter() {
        return daughter;
    }

    public void setDaughter(String daughter) {
        this.daughter = daughter;
    }

    @Override
    public String toString() {
        return "Family{" +
                "wife='" + wife + '\'' +
                ", husband='" + husband + '\'' +
                ", son='" + son + '\'' +
                ", daughter='" + daughter + '\'' +
                '}';
    }
}

