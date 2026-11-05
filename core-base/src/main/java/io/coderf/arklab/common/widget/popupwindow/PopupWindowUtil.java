package io.coderf.arklab.common.widget.popupwindow;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.core.utils.CollectionUtil;

/**
 * PopupWindow 树形数据工具：选中查询/写入、节点查找、路径与扁平化等。
 * <p>
 * 只处理 {@link PopupWindowBean} 数据，不负责展示。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/3/12 14:24
 * @updated 2026/11/5
 */
public final class PopupWindowUtil {

    private PopupWindowUtil() {
    }

    // region 选中查询

    /**
     * 获取列表中所有选中的项（含子孙）
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> getAllSelectedItems(@Nullable List<T> list) {
        List<T> result = new ArrayList<>();
        walk(list, item -> {
            if (isSelected(item)) {
                result.add(item);
            }
            return false;
        });
        return result;
    }

    /**
     * 根据 parentPopupId 获取指定层级下所有选中的项；parentPopupId 为空表示顶层
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> getSelectedItemsByParentId(
            @Nullable List<T> list, @Nullable String parentPopupId) {
        return getSelectedItemsAtLevel(list, parentPopupId, true);
    }

    /**
     * 根据 parentPopupCode 获取指定层级下所有选中的项；parentPopupCode 为空表示顶层
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> getSelectedItemsByParentCode(
            @Nullable List<T> list, @Nullable String parentPopupCode) {
        return getSelectedItemsAtLevel(list, parentPopupCode, false);
    }

    /**
     * 是否存在任意选中项（含子孙）
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelected(@Nullable List<T> list) {
        return walk(list, PopupWindowUtil::isSelected);
    }

    /**
     * 是否所有项都被选中（含子孙；空列表返回 false）
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelected(@Nullable List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        final boolean[] all = {true};
        walk(list, item -> {
            if (!isSelected(item)) {
                all[0] = false;
                return true;
            }
            return false;
        });
        return all[0];
    }

    /**
     * 指定 parentPopupId 层级下是否存在选中项
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelectedByParentId(
            @Nullable List<T> list, @Nullable String parentPopupId) {
        return !getSelectedItemsByParentId(list, parentPopupId).isEmpty();
    }

    /**
     * 指定 parentPopupCode 层级下是否存在选中项
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelectedByParentCode(
            @Nullable List<T> list, @Nullable String parentPopupCode) {
        return !getSelectedItemsByParentCode(list, parentPopupCode).isEmpty();
    }

    /**
     * 指定 parentPopupId 层级下是否全部选中
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelectedByParentId(
            @Nullable List<T> list, @Nullable String parentPopupId) {
        return isAllSelectedAtLevel(list, parentPopupId, true);
    }

    /**
     * 指定 parentPopupCode 层级下是否全部选中
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelectedByParentCode(
            @Nullable List<T> list, @Nullable String parentPopupCode) {
        return isAllSelectedAtLevel(list, parentPopupCode, false);
    }

    /**
     * 统计选中数量（含子孙）
     */
    public static <T extends PopupWindowBean<?>> int countSelected(@Nullable List<T> list) {
        return getAllSelectedItems(list).size();
    }

    // endregion

    // region 选中写入

    /**
     * 清除所有选中状态（含子孙）
     */
    public static <T extends PopupWindowBean<?>> void clearAllSelected(@Nullable List<T> list) {
        walk(list, item -> {
            if (isSelected(item)) {
                item.setSelected(false);
            }
            return false;
        });
    }

    /**
     * 全选（含子孙）
     */
    public static <T extends PopupWindowBean<?>> void selectAll(@Nullable List<T> list) {
        walk(list, item -> {
            item.setSelected(true);
            return false;
        });
    }

    /**
     * 切换单节点选中状态（不递归子孙）
     */
    public static <T extends PopupWindowBean<?>> void toggleSelected(@Nullable T item) {
        if (item == null) {
            return;
        }
        item.setSelected(!isSelected(item));
    }

    /**
     * 按 id 集合设置选中；{@code clearOthers=true} 时先清空再选中（适合单选回填）
     */
    public static <T extends PopupWindowBean<?>> void setSelectedByIds(
            @Nullable List<T> list,
            @Nullable Collection<String> ids,
            boolean clearOthers) {
        setSelectedByKeys(list, ids, true, clearOthers);
    }

    /**
     * 按 code 集合设置选中
     */
    public static <T extends PopupWindowBean<?>> void setSelectedByCodes(
            @Nullable List<T> list,
            @Nullable Collection<String> codes,
            boolean clearOthers) {
        setSelectedByKeys(list, codes, false, clearOthers);
    }

    /**
     * 父节点选中时同步子孙选中状态
     */
    @SuppressWarnings("unchecked")
    public static <T extends PopupWindowBean<?>> void selectChildrenWhenParent(
            @Nullable T parent, boolean selected) {
        if (parent == null || CollectionUtil.isEmpty(parent.getChildList())) {
            return;
        }
        walk((List<T>) parent.getChildList(), item -> {
            item.setSelected(selected);
            return false;
        });
    }

    /**
     * 根据子孙选中情况回写父节点：全部选中则父选中，否则取消
     */
    @SuppressWarnings("unchecked")
    public static <T extends PopupWindowBean<?>> void updateParentByChildren(@Nullable List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (T item : list) {
            List<T> children = (List<T>) item.getChildList();
            if (CollectionUtil.isEmpty(children)) {
                continue;
            }
            updateParentByChildren(children);
            item.setSelected(isAllSelected(children));
        }
    }

    // endregion

    // region 查找 / 路径 / 扁平化

    /**
     * 按 popupId 递归查找节点
     */
    @Nullable
    public static <T extends PopupWindowBean<?>> T findById(
            @Nullable List<T> list, @Nullable String popupId) {
        if (TextUtils.isEmpty(popupId)) {
            return null;
        }
        return findFirst(list, item -> popupId.equals(item.getPopupId()));
    }

    /**
     * 按 popupCode 递归查找节点
     */
    @Nullable
    public static <T extends PopupWindowBean<?>> T findByCode(
            @Nullable List<T> list, @Nullable String popupCode) {
        if (TextUtils.isEmpty(popupCode)) {
            return null;
        }
        return findFirst(list, item -> popupCode.equals(item.getPopupCode()));
    }

    /**
     * 从根到目标节点的路径（含目标）；找不到返回空列表
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> getSelectedPathById(
            @Nullable List<T> list, @Nullable String popupId) {
        if (TextUtils.isEmpty(popupId)) {
            return Collections.emptyList();
        }
        List<T> path = new ArrayList<>();
        return findPath(list, item -> popupId.equals(item.getPopupId()), path) ? path : Collections.emptyList();
    }

    /**
     * 从根到目标节点的路径（按 code）
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> getSelectedPathByCode(
            @Nullable List<T> list, @Nullable String popupCode) {
        if (TextUtils.isEmpty(popupCode)) {
            return Collections.emptyList();
        }
        List<T> path = new ArrayList<>();
        return findPath(list, item -> popupCode.equals(item.getPopupCode()), path)
                ? path : Collections.emptyList();
    }

    /**
     * 树转平铺（深度优先）
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> flatten(@Nullable List<T> list) {
        List<T> result = new ArrayList<>();
        walk(list, item -> {
            result.add(item);
            return false;
        });
        return result;
    }

    /**
     * 按名称模糊过滤（忽略大小写），返回匹配节点的平铺列表（不含未匹配祖先）
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> List<T> filterByName(
            @Nullable List<T> list, @Nullable String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return flatten(list);
        }
        String key = keyword.trim().toLowerCase();
        List<T> result = new ArrayList<>();
        walk(list, item -> {
            String name = item.getPopupName();
            if (!TextUtils.isEmpty(name) && name.toLowerCase().contains(key)) {
                result.add(item);
            }
            return false;
        });
        return result;
    }

    /**
     * 树最大深度；空树返回 0
     */
    @SuppressWarnings("unchecked")
    public static <T extends PopupWindowBean<?>> int getMaxDepth(@Nullable List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return 0;
        }
        int max = 1;
        for (T item : list) {
            max = Math.max(max, 1 + getMaxDepth((List<T>) item.getChildList()));
        }
        return max;
    }

    public static <T extends PopupWindowBean<?>> boolean hasChildren(@Nullable T item) {
        return item != null && CollectionUtil.isNotEmpty(item.getChildList());
    }

    // endregion

    // region 聚合输出

    @NonNull
    public static <T extends PopupWindowBean<?>> List<String> getSelectedIds(@Nullable List<T> list) {
        List<String> result = new ArrayList<>();
        for (T item : getAllSelectedItems(list)) {
            if (!TextUtils.isEmpty(item.getPopupId())) {
                result.add(item.getPopupId());
            }
        }
        return result;
    }

    @NonNull
    public static <T extends PopupWindowBean<?>> List<String> getSelectedCodes(@Nullable List<T> list) {
        List<String> result = new ArrayList<>();
        for (T item : getAllSelectedItems(list)) {
            if (!TextUtils.isEmpty(item.getPopupCode())) {
                result.add(item.getPopupCode());
            }
        }
        return result;
    }

    @NonNull
    public static <T extends PopupWindowBean<?>> List<String> getSelectedNames(@Nullable List<T> list) {
        List<String> result = new ArrayList<>();
        for (T item : getAllSelectedItems(list)) {
            if (!TextUtils.isEmpty(item.getPopupName())) {
                result.add(item.getPopupName());
            }
        }
        return result;
    }

    /**
     * 选中名称拼接，便于回填到 {@code TextPopupView}
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> String joinSelectedNames(
            @Nullable List<T> list, @Nullable String separator) {
        String sep = separator == null ? "," : separator;
        List<String> names = getSelectedNames(list);
        if (names.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(names.get(i));
        }
        return sb.toString();
    }

    /**
     * 深拷贝树（新 List + 新节点实例，优先无参构造；失败则退回 {@link PopupWindowBean}）。
     * 用于弹层内编辑时避免直接改动业务原列表。
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends PopupWindowBean<?>> List<T> deepCopy(@Nullable List<T> list) {
        List<T> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }
        for (T item : list) {
            if (item == null) {
                continue;
            }
            T copy = (T) copyNode(item);
            if (CollectionUtil.isNotEmpty(item.getChildList())) {
                copy.setChildList(deepCopy((List) item.getChildList()));
            }
            result.add(copy);
        }
        return result;
    }

    // endregion

    // region 内部实现

    private static boolean isSelected(@Nullable PopupWindowBean<?> item) {
        return item != null && Boolean.TRUE.equals(item.getSelected());
    }

    /**
     * 深度优先遍历；predicate 返回 true 时提前结束。
     *
     * @return 是否因 predicate 提前结束
     */
    @SuppressWarnings("unchecked")
    private static <T extends PopupWindowBean<?>> boolean walk(
            @Nullable List<T> list, @NonNull Predicate<T> stopIfTrue) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        for (T item : list) {
            if (item == null) {
                continue;
            }
            if (stopIfTrue.test(item)) {
                return true;
            }
            if (walk((List<T>) item.getChildList(), stopIfTrue)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static <T extends PopupWindowBean<?>> T findFirst(
            @Nullable List<T> list, @NonNull Predicate<T> matcher) {
        final Object[] holder = new Object[1];
        walk(list, item -> {
            if (matcher.test(item)) {
                holder[0] = item;
                return true;
            }
            return false;
        });
        return (T) holder[0];
    }

    @SuppressWarnings("unchecked")
    private static <T extends PopupWindowBean<?>> boolean findPath(
            @Nullable List<T> list,
            @NonNull Predicate<T> matcher,
            @NonNull List<T> path) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        for (T item : list) {
            if (item == null) {
                continue;
            }
            path.add(item);
            if (matcher.test(item)) {
                return true;
            }
            if (findPath((List<T>) item.getChildList(), matcher, path)) {
                return true;
            }
            path.remove(path.size() - 1);
        }
        return false;
    }

    @NonNull
    private static <T extends PopupWindowBean<?>> List<T> getSelectedItemsAtLevel(
            @Nullable List<T> list, @Nullable String parentKey, boolean byId) {
        List<T> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }
        if (TextUtils.isEmpty(parentKey)) {
            for (T item : list) {
                if (isSelected(item)) {
                    result.add(item);
                }
            }
            return result;
        }
        T parent = byId ? findById(list, parentKey) : findByCode(list, parentKey);
        if (parent == null || CollectionUtil.isEmpty(parent.getChildList())) {
            return result;
        }
        for (Object child : parent.getChildList()) {
            T childItem = (T) child;
            if (isSelected(childItem)) {
                result.add(childItem);
            }
        }
        return result;
    }

    private static <T extends PopupWindowBean<?>> boolean isAllSelectedAtLevel(
            @Nullable List<T> list, @Nullable String parentKey, boolean byId) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }
        if (TextUtils.isEmpty(parentKey)) {
            for (T item : list) {
                if (!isSelected(item)) {
                    return false;
                }
            }
            return true;
        }
        T parent = byId ? findById(list, parentKey) : findByCode(list, parentKey);
        if (parent == null || CollectionUtil.isEmpty(parent.getChildList())) {
            return false;
        }
        for (Object child : parent.getChildList()) {
            if (!isSelected((T) child)) {
                return false;
            }
        }
        return true;
    }

    private static <T extends PopupWindowBean<?>> void setSelectedByKeys(
            @Nullable List<T> list,
            @Nullable Collection<String> keys,
            boolean byId,
            boolean clearOthers) {
        if (clearOthers) {
            clearAllSelected(list);
        }
        if (CollectionUtil.isEmpty(list) || keys == null || keys.isEmpty()) {
            return;
        }
        Set<String> keySet = new HashSet<>();
        for (String key : keys) {
            if (!TextUtils.isEmpty(key)) {
                keySet.add(key);
            }
        }
        if (keySet.isEmpty()) {
            return;
        }
        walk(list, item -> {
            String key = byId ? item.getPopupId() : item.getPopupCode();
            if (!TextUtils.isEmpty(key) && keySet.contains(key)) {
                item.setSelected(true);
            }
            return false;
        });
    }

    @NonNull
    private static PopupWindowBean<?> copyNode(@NonNull PopupWindowBean<?> item) {
        PopupWindowBean<?> copy;
        try {
            copy = (PopupWindowBean<?>) item.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            copy = new PopupWindowBean<>();
        }
        copy.setPopupId(item.getPopupId());
        copy.setPopupCode(item.getPopupCode());
        copy.setPopupName(item.getPopupName());
        copy.setParentPopupId(item.getParentPopupId());
        copy.setParentPopupCode(item.getParentPopupCode());
        copy.setSelected(item.getSelected());
        copy.setSingleSelected(item.getSingleSelected());
        return copy;
    }

    // endregion
}
