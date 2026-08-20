package io.coderf.arklab.common.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;

/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/5/26 10:33
 */
public class ViewModelHelper {
    @SuppressWarnings("unchecked")
   public static Class<? extends BaseViewModel> resolveViewModelClass(Class<?> startClass) {
        Class<?> clazz = startClass;
        while (clazz != null && clazz != Object.class) {
            Type type = clazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type vmType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (vmType instanceof Class) {
                    Class<?> vmClass = (Class<?>) vmType;
                    if (BaseViewModel.class.isAssignableFrom(vmClass)
                            && !java.lang.reflect.Modifier.isAbstract(vmClass.getModifiers())) {
                        return (Class<? extends BaseViewModel>) vmClass;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return EmptyViewModel.class;
    }

}

