package pers.fz.mvvm.utils.common;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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


    public static <T> boolean isStringType(T data) {
        return String.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isObjectType(T data) {
        return Object.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isBooleanType(T data) {
        return Boolean.class.isAssignableFrom(data.getClass()) || boolean.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isIntegerType(T data) {
        return Integer.class.isAssignableFrom(data.getClass()) || int.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isLongType(T data) {
        return Long.class.isAssignableFrom(data.getClass()) || long.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isDoubleType(T data) {
        return Double.class.isAssignableFrom(data.getClass()) || double.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isFloatType(T data) {
        return Float.class.isAssignableFrom(data.getClass()) || float.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isListType(T data) {
        return List.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isMapType(T data) {
        return Map.class.isAssignableFrom(data.getClass());
    }

    public static <T> boolean isArrayType(T data) {
        return data.getClass().isArray();
    }
}

