package io.coderf.arklab.annotation.verify;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.utils.CompareUtil;
import io.coderf.arklab.annotation.utils.ValidatorUtil;

/**
 * {@link EntityValidator} 反射元数据缓存，可通过 {@link #setCacheEnabled(boolean)} 开关。
 *
 * @author fz
 */
public final class EntityValidatorCache {

    private static volatile boolean cacheEnabled = true;

    private static final ConcurrentHashMap<Class<?>, EntityMeta> CACHE = new ConcurrentHashMap<>();

    private EntityValidatorCache() {
    }

    /**
     * 是否启用反射缓存，默认 {@code true}。
     * 关闭时会清空已有缓存。
     */
    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
        if (!enabled) {
            CACHE.clear();
        }
    }

    public static boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * 清空全部缓存（例如热修复 / 动态加载场景）。
     */
    public static void clearCache() {
        CACHE.clear();
    }

    static EntityMeta resolve(Class<?> clazz) {
        if (!cacheEnabled) {
            return loadMeta(clazz);
        }
        return CACHE.computeIfAbsent(clazz, EntityValidatorCache::loadMeta);
    }

    private static EntityMeta loadMeta(Class<?> clazz) {
        VerifyEntity verifyEntity = clazz.getAnnotation(VerifyEntity.class);
        List<Field> fields = ValidatorUtil.collectDeclaredFields(clazz);
        Map<Field, Boolean> validationFlags = new HashMap<>();
        for (Field field : fields) {
            validationFlags.put(field, EntityValidator.hasValidationAnnotations(field));
        }
        Map<String, Field> fieldMap = CompareUtil.buildFieldMap(fields);
        return new EntityMeta(verifyEntity, fields, validationFlags, fieldMap);
    }

    static final class EntityMeta {
        final VerifyEntity verifyEntity;
        final List<Field> fields;
        final Map<Field, Boolean> validationFlags;
        final Map<String, Field> fieldMap;

        EntityMeta(VerifyEntity verifyEntity, List<Field> fields, Map<Field, Boolean> validationFlags,
                   Map<String, Field> fieldMap) {
            this.verifyEntity = verifyEntity;
            this.fields = fields;
            this.validationFlags = validationFlags;
            this.fieldMap = fieldMap;
        }
    }
}
