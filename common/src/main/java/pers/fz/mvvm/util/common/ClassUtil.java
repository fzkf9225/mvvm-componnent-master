package pers.fz.mvvm.util.common;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * created by fz on 2025/6/26 11:35
 * describe:
 */
public class ClassUtil {
    /**
     * 获取这个类所有的接口对象，包括所有的父类
     * @param clazz class对象
     * @return 所有接口对象
     */
    public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> result = new LinkedHashSet<>();
        getAllInterfaces(clazz, result);
        return result;
    }

    private static void getAllInterfaces(Class<?> clazz, Set<Class<?>> result) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        // 处理当前类的接口
        for (Class<?> iface : clazz.getInterfaces()) {
            if (result.add(iface)) {
                // 递归处理接口继承的父接口
                getAllInterfaces(iface, result);
            }
        }

        // 处理父类
        getAllInterfaces(clazz.getSuperclass(), result);
    }
}

