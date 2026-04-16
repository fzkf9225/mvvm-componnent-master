package io.coderf.arklab.common.widget.popupwindow;

import android.text.TextUtils;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.CollectionUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * PopupWindow工具类，主要用于数据判断
 * 提供对 PopupWindowBean 列表的各种操作方法
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/12 14:24
 */
public class PopupWindowUtil {

    /**
     * 方法一：获取列表中所有选中的项（包括子级中的选中项）
     *
     * @param list 数据列表
     * @return 所有选中的项（扁平化列表）
     */
    public static <T extends PopupWindowBean<?>> List<T> getAllSelectedItems(List<T> list) {
        List<T> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }

        for (T item : list) {
            // 检查当前项是否选中
            if (item.getSelected() != null && item.getSelected()) {
                result.add(item);
            }

            // 递归检查子级
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                result.addAll(getAllSelectedItems((List<T>) item.getChildList()));
            }
        }

        return result;
    }

    /**
     * 方法二：根据 parentPopupId 获取指定层级下所有选中的项
     *
     * @param list         数据列表
     * @param parentPopupId 父级ID，为null时表示顶层
     * @return 指定层级下所有选中的项
     */
    public static <T extends PopupWindowBean<?>> List<T> getSelectedItemsByParentId(List<T> list, String parentPopupId) {
        List<T> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }

        // 如果parentPopupId为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupId)) {
            for (T item : list) {
                if (item.getSelected() != null && item.getSelected()) {
                    result.add(item);
                }
            }
            return result;
        }

        // 查找指定parentPopupId的层级
        for (T item : list) {
            if (parentPopupId.equals(item.getPopupId())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() != null) {
                    for (Object child : item.getChildList()) {
                        T childItem = (T) child;
                        if (childItem.getSelected() != null && childItem.getSelected()) {
                            result.add(childItem);
                        }
                    }
                }
                break;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                result.addAll(getSelectedItemsByParentId((List<T>) item.getChildList(), parentPopupId));
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * 方法三：根据 parentPopupCode 获取指定层级下所有选中的项
     *
     * @param list          数据列表
     * @param parentPopupCode 父级编码，为null时表示顶层
     * @return 指定层级下所有选中的项
     */
    public static <T extends PopupWindowBean<?>> List<T> getSelectedItemsByParentCode(List<T> list, String parentPopupCode) {
        List<T> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }

        // 如果parentPopupCode为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupCode)) {
            for (T item : list) {
                if (item.getSelected() != null && item.getSelected()) {
                    result.add(item);
                }
            }
            return result;
        }

        // 查找指定parentPopupCode的层级
        for (T item : list) {
            if (parentPopupCode.equals(item.getPopupCode())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() != null) {
                    for (Object child : item.getChildList()) {
                        T childItem = (T) child;
                        if (childItem.getSelected() != null && childItem.getSelected()) {
                            result.add(childItem);
                        }
                    }
                }
                break;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                result.addAll(getSelectedItemsByParentCode((List<T>) item.getChildList(), parentPopupCode));
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * 方法四：判断列表中是否存在任意选中的项（包括子级）
     *
     * @param list 数据列表
     * @return 是否存在选中的项
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelected(List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        for (T item : list) {
            if (item.getSelected() != null && item.getSelected()) {
                return true;
            }

            // 递归检查子级
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                if (hasAnySelected((List<T>) item.getChildList())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 方法五：判断列表中是否所有项都被选中（包括子级）
     *
     * @param list 数据列表
     * @return 是否所有项都被选中
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelected(List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        for (T item : list) {
            // 检查当前项
            if (item.getSelected() == null || !item.getSelected()) {
                return false;
            }

            // 递归检查子级
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                if (!isAllSelected((List<T>) item.getChildList())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 方法六（ID版）：根据 parentPopupId 判断指定层级下是否存在任意选中的项
     *
     * @param list         数据列表
     * @param parentPopupId 父级ID，为null时表示顶层
     * @return 指定层级下是否存在选中的项
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelectedByParentId(List<T> list, String parentPopupId) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        // 如果parentPopupId为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupId)) {
            for (T item : list) {
                if (item.getSelected() != null && item.getSelected()) {
                    return true;
                }
            }
            return false;
        }

        // 查找指定parentPopupId的层级
        for (T item : list) {
            if (parentPopupId.equals(item.getPopupId())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() != null) {
                    for (Object child : item.getChildList()) {
                        T childItem = (T) child;
                        if (childItem.getSelected() != null && childItem.getSelected()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                if (hasAnySelectedByParentId((List<T>) item.getChildList(), parentPopupId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 方法六（Code版）：根据 parentPopupCode 判断指定层级下是否存在任意选中的项
     *
     * @param list          数据列表
     * @param parentPopupCode 父级编码，为null时表示顶层
     * @return 指定层级下是否存在选中的项
     */
    public static <T extends PopupWindowBean<?>> boolean hasAnySelectedByParentCode(List<T> list, String parentPopupCode) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        // 如果parentPopupCode为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupCode)) {
            for (T item : list) {
                if (item.getSelected() != null && item.getSelected()) {
                    return true;
                }
            }
            return false;
        }

        // 查找指定parentPopupCode的层级
        for (T item : list) {
            if (parentPopupCode.equals(item.getPopupCode())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() != null) {
                    for (Object child : item.getChildList()) {
                        T childItem = (T) child;
                        if (childItem.getSelected() != null && childItem.getSelected()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                if (hasAnySelectedByParentCode((List<T>) item.getChildList(), parentPopupCode)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 方法七（ID版）：根据 parentPopupId 判断指定层级下是否所有项都被选中
     *
     * @param list         数据列表
     * @param parentPopupId 父级ID，为null时表示顶层
     * @return 指定层级下是否所有项都被选中
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelectedByParentId(List<T> list, String parentPopupId) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        // 如果parentPopupId为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupId)) {
            for (T item : list) {
                if (item.getSelected() == null || !item.getSelected()) {
                    return false;
                }
            }
            return true;
        }

        // 查找指定parentPopupId的层级
        for (T item : list) {
            if (parentPopupId.equals(item.getPopupId())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() == null || item.getChildList().isEmpty()) {
                    return false;
                }

                for (Object child : item.getChildList()) {
                    T childItem = (T) child;
                    if (childItem.getSelected() == null || !childItem.getSelected()) {
                        return false;
                    }
                }
                return true;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                boolean result = isAllSelectedByParentId((List<T>) item.getChildList(), parentPopupId);
                if (result) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 方法七（Code版）：根据 parentPopupCode 判断指定层级下是否所有项都被选中
     *
     * @param list          数据列表
     * @param parentPopupCode 父级编码，为null时表示顶层
     * @return 指定层级下是否所有项都被选中
     */
    public static <T extends PopupWindowBean<?>> boolean isAllSelectedByParentCode(List<T> list, String parentPopupCode) {
        if (CollectionUtil.isEmpty(list)) {
            return false;
        }

        // 如果parentPopupCode为null，表示查找顶层
        if (TextUtils.isEmpty(parentPopupCode)) {
            for (T item : list) {
                if (item.getSelected() == null || !item.getSelected()) {
                    return false;
                }
            }
            return true;
        }

        // 查找指定parentPopupCode的层级
        for (T item : list) {
            if (parentPopupCode.equals(item.getPopupCode())) {
                // 找到目标层级，检查其子级
                if (item.getChildList() == null || item.getChildList().isEmpty()) {
                    return false;
                }

                for (Object child : item.getChildList()) {
                    T childItem = (T) child;
                    if (childItem.getSelected() == null || !childItem.getSelected()) {
                        return false;
                    }
                }
                return true;
            }

            // 递归在子级中查找
            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                boolean result = isAllSelectedByParentCode((List<T>) item.getChildList(), parentPopupCode);
                if (result) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 辅助方法：统计选中的数量
     *
     * @param list 数据列表
     * @return 选中的数量
     */
    public static <T extends PopupWindowBean<?>> int countSelected(List<T> list) {
        return getAllSelectedItems(list).size();
    }

    /**
     * 辅助方法：清除所有选中状态
     *
     * @param list 数据列表
     */
    public static <T extends PopupWindowBean<?>> void clearAllSelected(List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        for (T item : list) {
            if (item.getSelected() != null && item.getSelected()) {
                item.setSelected(false);
            }

            if (item.getChildList() != null && !item.getChildList().isEmpty()) {
                clearAllSelected((List<T>) item.getChildList());
            }
        }
    }
}

