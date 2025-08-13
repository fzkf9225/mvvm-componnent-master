package pers.fz.annotation.utils;

import java.util.Arrays;

public class ValidatorUtil {
    /**
     * 判断分组是否包含当前验证的这个组别
     *
     * @param groups       总的分组
     * @param currentGroup 当前需要验证的分组
     * @return true包含
     */
    public static boolean containsGroup(Class<?>[] groups, Class<?> currentGroup) {
        if (groups == null || groups.length == 0) {
            return false;
        }
        if (currentGroup == null) {
            return false;
        }
        return Arrays.asList(groups).contains(currentGroup);
    }
}
