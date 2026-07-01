package io.coderf.arklab.demo.config;

import io.coderf.arklab.common.R;

/**
 * Hub 页颜色预设，供示例运行时切换 Tab 文字/指示条颜色。
 */
public final class TabLayoutDemoColorPresets {

    public static final int[] COLOR_RES = {
            R.color.themeColor,
            R.color.theme_red,
            R.color.theme_green,
            R.color.theme_orange,
            R.color.autoColor,
            R.color.dark_light,
            R.color.gray,
            R.color.hint_text_color,
    };

    public static final String[] COLOR_LABELS = {
            "蓝", "红", "绿", "橙", "深灰", "浅灰", "灰", "浅"
    };

    private TabLayoutDemoColorPresets() {
    }

    public static int getColorRes(int index) {
        if (index < 0 || index >= COLOR_RES.length) {
            return COLOR_RES[0];
        }
        return COLOR_RES[index];
    }

    public static String getLabel(int index) {
        if (index < 0 || index >= COLOR_LABELS.length) {
            return COLOR_LABELS[0];
        }
        return COLOR_LABELS[index];
    }
}
