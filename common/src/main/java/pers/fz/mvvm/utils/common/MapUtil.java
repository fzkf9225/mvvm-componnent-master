package pers.fz.mvvm.utils.common;

import com.google.android.datatransport.runtime.dagger.internal.MapBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * created by fz on 2024/9/23 10:11
 * describe:
 */
public class MapUtil {

    /**
     * 默认初始大小
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * 默认增长因子，当Map的size达到 容量*增长因子时，开始扩充Map
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return null != map && !map.isEmpty();
    }

    /**
     * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合<br>
     * 空集合使用{@link Collections#emptyMap()}
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param set 提供的集合，可能为null
     * @return 原集合，若为null返回空集合
     * @since 4.6.3
     */
    public static <K, V> Map<K, V> emptyIfNull(Map<K, V> set) {
        return (null == set) ? Collections.emptyMap() : set;
    }

    /**
     * 如果给定Map为空，返回默认Map
     *
     * @param <T>        集合类型
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        Map
     * @param defaultMap 默认Map
     * @return 非空（empty）的原Map或默认Map
     * @since 4.6.9
     */
    public static <T extends Map<K, V>, K, V> T defaultIfEmpty(T map, T defaultMap) {
        return isEmpty(map) ? defaultMap : map;
    }

    // ----------------------------------------------------------------------------------------------- new HashMap

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param size    初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75 + 1
     * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象
     * @since 3.0.4
     */
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
        int initialCapacity = (int) (size / DEFAULT_LOAD_FACTOR) + 1;
        return isOrder ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75 + 1
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return newHashMap(size, false);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(boolean isOrder) {
        return newHashMap(DEFAULT_INITIAL_CAPACITY, isOrder);
    }

    /**
     * 新建TreeMap，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param comparator Key比较器
     * @return TreeMap
     * @since 3.2.3
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * 新建TreeMap，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param map        Map
     * @param comparator Key比较器
     * @return TreeMap
     * @since 3.2.3
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Map<K, V> map, Comparator<? super K> comparator) {
        final TreeMap<K, V> treeMap = new TreeMap<>(comparator);
        if (!isEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    /**
     * 创建键不重复Map
     *
     * @param <K>  key的类型
     * @param <V>  value的类型
     * @param size 初始容量
     * @return {@link IdentityHashMap}
     * @since 4.5.7
     */
    public static <K, V> Map<K, V> newIdentityMap(int size) {
        return new IdentityHashMap<>(size);
    }

    /**
     * 新建一个初始容量为{@link MapUtil#DEFAULT_INITIAL_CAPACITY} 的ConcurrentHashMap
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @return ConcurrentHashMap
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 新建一个ConcurrentHashMap
     *
     * @param size 初始容量，当传入的容量小于等于0时，容量为{@link MapUtil#DEFAULT_INITIAL_CAPACITY}
     * @param <K>  key的类型
     * @param <V>  value的类型
     * @return ConcurrentHashMap
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int size) {
        final int initCapacity = size <= 0 ? DEFAULT_INITIAL_CAPACITY : size;
        return new ConcurrentHashMap<>(initCapacity);
    }

    /**
     * 传入一个Map将其转化为ConcurrentHashMap类型
     *
     * @param map map
     * @param <K> key的类型
     * @param <V> value的类型
     * @return ConcurrentHashMap
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(Map<K, V> map) {
        if (isEmpty(map)) {
            return new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
        }
        return new ConcurrentHashMap<>(map);
    }

    // ----------------------------------------------------------------------------------------------- value of

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param key   键
     * @param value 值
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(K key, V value) {
        return of(key, value, false);
    }

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param key     键
     * @param value   值
     * @param isOrder 是否有序
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(K key, V value, boolean isOrder) {
        final HashMap<K, V> map = newHashMap(isOrder);
        map.put(key, value);
        return map;
    }


    /**
     * 将键值对转换为二维数组，第一维是key，第二纬是value
     *
     * @param map map
     * @return 数组
     * @since 4.1.9
     */
    public static Object[][] toObjectArray(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        final Object[][] result = new Object[map.size()][2];
        if (map.isEmpty()) {
            return result;
        }
        int index = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result[index][0] = entry.getKey();
            result[index][1] = entry.getValue();
            index++;
        }
        return result;
    }

    /**
     * 排序已有Map，Key有序的Map，使用默认Key排序方式（字母顺序）
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map Map
     * @return TreeMap
     * @see #newTreeMap(Map, Comparator)
     * @since 4.0.1
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map) {
        return sort(map, null);
    }

    /**
     * 排序已有Map，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param map        Map，为null返回null
     * @param comparator Key比较器
     * @return TreeMap，map为null返回null
     * @see #newTreeMap(Map, Comparator)
     * @since 4.0.1
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
        if (null == map) {
            return null;
        }

        TreeMap<K, V> result;
        if (map instanceof TreeMap) {
            // 已经是可排序Map，此时只有比较器一致才返回原map
            result = (TreeMap<K, V>) map;
            if (null == comparator || comparator.equals(result.comparator())) {
                return result;
            }
        } else {
            result = newTreeMap(map, comparator);
        }

        return result;
    }

    /**
     * 将对应Map转换为不可修改的Map
     *
     * @param map Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不修改Map
     * @since 5.2.6
     */
    public static <K, V> Map<K, V> unmodifiable(Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    // ----------------------------------------------------------------------------------------------- builder

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder() {
        return builder(new HashMap<>());
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map 实际使用的map
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(Map<K, V> map) {
        MapBuilder<K, V> mapBuilder = MapBuilder.newMapBuilder(map.size());
        mapBuilder.putAll(map);
        return mapBuilder;
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param k   key
     * @param v   value
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(K k, V v) {
        return (builder(new HashMap<K, V>())).put(k, v);
    }

    /**
     * 去掉Map中指定key的键值对，修改原Map
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  Map
     * @param keys 键列表
     * @return 修改后的key
     * @since 5.0.5
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> removeAny(Map<K, V> map, final K... keys) {
        for (K key : keys) {
            map.remove(key);
        }
        return map;
    }

    /**
     * 去除Map中值为{@code null}的键值对<br>
     * 注意：此方法在传入的Map上直接修改。
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map Map
     * @return map
     * @since 4.6.5
     */
    public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
        if (isEmpty(map)) {
            return map;
        }

        final Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        Map.Entry<K, V> entry;
        while (iter.hasNext()) {
            entry = iter.next();
            if (null == entry.getValue()) {
                iter.remove();
            }
        }

        return map;
    }

    /**
     * 返回一个空Map
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 空Map
     * @see Collections#emptyMap()
     * @since 5.3.1
     */
    public static <K, V> Map<K, V> empty() {
        return Collections.emptyMap();
    }


}


