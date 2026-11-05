package io.coderf.arklab.demo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;

/**
 * PopupWindow 演示页静态模拟数据。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/5
 */
public final class PopupWindowSampleData {

    private PopupWindowSampleData() {
    }

    /** Exposed Dropdown / PopupView 简单城市单选 */
    public static List<PopupWindowBean<?>> cities() {
        return Arrays.asList(
                bean("110000", "BJ", "北京"),
                bean("310000", "SH", "上海"),
                bean("420100", "WH", "武汉"),
                bean("430100", "CS", "长沙"),
                bean("320100", "NJ", "南京"),
                bean("340100", "HF", "合肥"),
                bean("130300", "QHD", "秦皇岛"),
                bean("321000", "YZ", "扬州")
        );
    }

    /** MultiPopupView 网格多选 */
    public static List<PopupWindowBean<?>> materials() {
        return Arrays.asList(
                bean("1", "M1", "316不锈钢"),
                bean("2", "M2", "塑料"),
                bean("3", "M3", "铝合金"),
                bean("4", "M4", "合成金属"),
                bean("5", "M5", "艾德曼合金"),
                bean("6", "M6", "振金")
        );
    }

    /** TreePopupView 左右两栏 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<PopupWindowBean> treeGender() {
        PopupWindowBean unlimited = bean("0", "ALL", "不限");
        unlimited.setChildList(Collections.singletonList(bean("0-0", "ALL", "不限")));

        PopupWindowBean male = bean("1", "M", "男");
        male.setChildList(Arrays.asList(
                bean("1-1", "M1", "性别男爱好女"),
                bean("1-2", "M2", "性别男爱好男"),
                bean("1-3", "M3", "性别男爱好男和女"),
                bean("1-4", "M4", "无所畏惧")
        ));
        setParent(male);

        PopupWindowBean female = bean("2", "F", "女");
        female.setChildList(Arrays.asList(
                bean("2-1", "F1", "性别女爱好女"),
                bean("2-2", "F2", "性别女爱好男"),
                bean("2-3", "F3", "性别女爱好男和女"),
                bean("2-4", "F4", "无所畏惧")
        ));
        setParent(female);

        return Arrays.asList(unlimited, male, female);
    }

    /** 级联：省 → 市 → 区（静态三层） */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<PopupWindowBean> cascadeRegions() {
        List<PopupWindowBean> roots = new ArrayList<>();

        PopupWindowBean hubei = bean("420000", "HB", "湖北省");
        PopupWindowBean wuhan = childOf(hubei, "420100", "WH", "武汉市");
        wuhan.setChildList(Arrays.asList(
                childOf(wuhan, "420102", "JC", "江岸区"),
                childOf(wuhan, "420103", "JA", "江汉区"),
                childOf(wuhan, "420111", "HS", "洪山区")
        ));
        PopupWindowBean yichang = childOf(hubei, "420500", "YC", "宜昌市");
        yichang.setChildList(Arrays.asList(
                childOf(yichang, "420502", "XY", "西陵区"),
                childOf(yichang, "420503", "WT", "伍家岗区")
        ));
        hubei.setChildList(Arrays.asList(wuhan, yichang));
        roots.add(hubei);

        PopupWindowBean anhui = bean("340000", "AH", "安徽省");
        PopupWindowBean hefei = childOf(anhui, "340100", "HF", "合肥市");
        hefei.setChildList(Arrays.asList(
                childOf(hefei, "340102", "YH", "瑶海区"),
                childOf(hefei, "340104", "SS", "蜀山区"),
                childOf(hefei, "340111", "BH", "包河区")
        ));
        PopupWindowBean wuhu = childOf(anhui, "340200", "WU", "芜湖市");
        wuhu.setChildList(Arrays.asList(
                childOf(wuhu, "340202", "JJ", "镜湖区"),
                childOf(wuhu, "340207", "JQ", "鸠江区")
        ));
        anhui.setChildList(Arrays.asList(hefei, wuhu));
        roots.add(anhui);

        PopupWindowBean jiangsu = bean("320000", "JS", "江苏省");
        PopupWindowBean nanjing = childOf(jiangsu, "320100", "NJ", "南京市");
        nanjing.setChildList(Arrays.asList(
                childOf(nanjing, "320102", "XW", "玄武区"),
                childOf(nanjing, "320105", "JL", "建邺区")
        ));
        PopupWindowBean yangzhou = childOf(jiangsu, "321000", "YZ", "扬州市");
        yangzhou.setChildList(Arrays.asList(
                childOf(yangzhou, "321002", "gj", "广陵区"),
                childOf(yangzhou, "321003", "HJ", "邗江区")
        ));
        jiangsu.setChildList(Arrays.asList(nanjing, yangzhou));
        roots.add(jiangsu);

        return roots;
    }

    private static PopupWindowBean<?> bean(String id, String code, String name) {
        return new PopupWindowBean<>(id, code, name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static PopupWindowBean childOf(PopupWindowBean parent, String id, String code, String name) {
        PopupWindowBean child = bean(id, code, name);
        child.setParentPopupId(parent.getPopupId());
        child.setParentPopupCode(parent.getPopupCode());
        return child;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setParent(PopupWindowBean parent) {
        List children = parent.getChildList();
        if (children == null) {
            return;
        }
        for (Object o : children) {
            PopupWindowBean child = (PopupWindowBean) o;
            child.setParentPopupId(parent.getPopupId());
            child.setParentPopupCode(parent.getPopupCode());
        }
    }
}
