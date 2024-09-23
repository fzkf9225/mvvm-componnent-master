package pers.fz.mvvm.util.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * created by fz on 2024/9/23 10:21
 * describe:
 */

public class IterUtil {

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        return null == iterable || isEmpty(iterable.iterator());
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     */
    public static boolean isEmpty(Iterator<?> Iterator) {
        return null == Iterator || false == Iterator.hasNext();
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(Iterable<?> iterable) {
        return null != iterable && isNotEmpty(iterable.iterator());
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(Iterator<?> Iterator) {
        return null != Iterator && Iterator.hasNext();
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iter 被检查的{@link Iterable}对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     */
    public static boolean hasNull(Iterable<?> iter) {
        return hasNull(null == iter ? null : iter.iterator());
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iter 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     */
    public static boolean hasNull(Iterator<?> iter) {
        if (null == iter) {
            return true;
        }
        while (iter.hasNext()) {
            if (null == iter.next()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iter 被检查的{@link Iterable}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     * @since 3.3.0
     */
    public static boolean isAllNull(Iterable<?> iter) {
        return isAllNull(null == iter ? null : iter.iterator());
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iter 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     * @since 3.3.0
     */
    public static boolean isAllNull(Iterator<?> iter) {
        if (null == iter) {
            return true;
        }

        while (iter.hasNext()) {
            if (null != iter.next()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map}<br>
     * 所谓元素计数就是假如这个集合中某个元素出现了n次，那将这个元素做为key，n做为value<br>
     * 例如：[a,b,c,c,c] 得到：<br>
     * a: 1<br>
     * b: 1<br>
     * c: 3<br>
     *
     * @param <T>  集合元素类型
     * @param iter {@link Iterable}，如果为null返回一个空的Map
     * @return {@link Map}
     * @deprecated 如果对象同时实现Iterable和Iterator接口会产生歧义，请使用CollUtil.countMap
     */
    @Deprecated
    public static <T> Map<T, Integer> countMap(Iterable<T> iter) {
        return countMap(null == iter ? null : iter.iterator());
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map}<br>
     * 所谓元素计数就是假如这个集合中某个元素出现了n次，那将这个元素做为key，n做为value<br>
     * 例如：[a,b,c,c,c] 得到：<br>
     * a: 1<br>
     * b: 1<br>
     * c: 3<br>
     *
     * @param <T>  集合元素类型
     * @param iter {@link Iterator}，如果为null返回一个空的Map
     * @return {@link Map}
     */
    public static <T> Map<T, Integer> countMap(Iterator<T> iter) {
        final HashMap<T, Integer> countMap = new HashMap<>();
        if (null != iter) {
            Integer count;
            T t;
            while (iter.hasNext()) {
                t = iter.next();
                count = countMap.get(t);
                if (null == count) {
                    countMap.put(t, 1);
                } else {
                    countMap.put(t, count + 1);
                }
            }
        }
        return countMap;
    }

    /**
     * 将Entry集合转换为HashMap
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param entryIter entry集合
     * @return Map
     */
    public static <K, V> HashMap<K, V> toMap(Iterable<Entry<K, V>> entryIter) {
        final HashMap<K, V> map = new HashMap<>();
        if (isNotEmpty(entryIter)) {
            for (Entry<K, V> entry : entryIter) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * 将键列表和值列表转换为Map<br>
     * 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。<br>
     * 如果值多于键，忽略多余的值。
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return 标题内容Map
     * @since 3.1.0
     */
    public static <K, V> Map<K, V> toMap(Iterable<K> keys, Iterable<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * 将键列表和值列表转换为Map<br>
     * 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。<br>
     * 如果值多于键，忽略多余的值。
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder 是否有序
     * @return 标题内容Map
     * @since 4.1.12
     */
    public static <K, V> Map<K, V> toMap(Iterable<K> keys, Iterable<V> values, boolean isOrder) {
        return toMap(null == keys ? null : keys.iterator(), null == values ? null : values.iterator(), isOrder);
    }

    /**
     * 将键列表和值列表转换为Map<br>
     * 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。<br>
     * 如果值多于键，忽略多余的值。
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return 标题内容Map
     * @since 3.1.0
     */
    public static <K, V> Map<K, V> toMap(Iterator<K> keys, Iterator<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * 将键列表和值列表转换为Map<br>
     * 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。<br>
     * 如果值多于键，忽略多余的值。
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder 是否有序
     * @return 标题内容Map
     * @since 4.1.12
     */
    public static <K, V> Map<K, V> toMap(Iterator<K> keys, Iterator<V> values, boolean isOrder) {
        final Map<K, V> resultMap = MapUtil.newHashMap(isOrder);
        if (isNotEmpty(keys)) {
            while (keys.hasNext()) {
                resultMap.put(keys.next(), (null != values && values.hasNext()) ? values.next() : null);
            }
        }
        return resultMap;
    }

    /**
     * Iterator转List<br>
     * 不判断，直接生成新的List
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return List
     * @since 4.0.6
     */
    public static <E> List<E> toList(Iterable<E> iter) {
        if (null == iter) {
            return null;
        }
        return toList(iter.iterator());
    }

    /**
     * Iterator转List<br>
     * 不判断，直接生成新的List
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return List
     * @since 4.0.6
     */
    public static <E> List<E> toList(Iterator<E> iter) {
        final List<E> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    /**
     * {@link Iterator} 转为 {@link Iterable}
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return {@link Iterable}
     */
    public static <E> Iterable<E> asIterable(final Iterator<E> iter) {
        return () -> iter;
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return 第一个元素
     */
    public static <T> T getFirst(Iterable<T> iterable) {
        if (null == iterable) {
            return null;
        }
        return getFirst(iterable.iterator());
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return 第一个元素
     */
    public static <T> T getFirst(Iterator<T> iterator) {
        if (null != iterator && iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * 返回一个空Iterator
     *
     * @param <T> 元素类型
     * @return 空Iterator
     * @see Collections#emptyIterator()
     * @since 5.3.1
     */
    public static <T> Iterator<T> empty() {
        return Collections.emptyIterator();
    }
}
