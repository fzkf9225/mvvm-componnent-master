package io.coderf.arklab.demo.config;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.widget.customview.IndicatorTabLayout;
import io.coderf.arklab.common.widget.customview.StyledTabLayout;

/**
 * 将 {@link TabLayoutDemoConfig} 中的配置应用到各自定义 TabLayout。
 */
public final class TabLayoutDemoApplier {

    private TabLayoutDemoApplier() {
    }

    public static void applyStyled(StyledTabLayout tabLayout) {
        TabLayoutDemoConfig config = TabLayoutDemoConfig.get();
        tabLayout.setSelectedTextSize(config.styledSelectedTextSizeSp);
        tabLayout.setUnselectedTextSize(config.styledUnselectedTextSizeSp);
        tabLayout.setSelectedTextBold(config.styledSelectedBold);
        tabLayout.setSelectedBackgroundCornerRadius(config.styledCornerRadiusDp);
        tabLayout.setSelectedTextColor(ContextCompat.getColor(
                tabLayout.getContext(),
                TabLayoutDemoColorPresets.getColorRes(config.styledSelectedColorIndex)));
        tabLayout.setUnselectedTextColor(ContextCompat.getColor(
                tabLayout.getContext(),
                TabLayoutDemoColorPresets.getColorRes(config.styledUnselectedColorIndex)));
    }

    public static void applyIndicator(IndicatorTabLayout tabLayout) {
        TabLayoutDemoConfig config = TabLayoutDemoConfig.get();
        tabLayout.setIndicatorWidthDp(config.indicatorWidthDp);
        tabLayout.setIndicatorHeightDp(config.indicatorHeightDp);
        tabLayout.setIndicatorCornerRadiusDp(config.indicatorCornerRadiusDp);
        tabLayout.setSelectedTextSize(config.indicatorSelectedTextSizeSp);
        tabLayout.setUnselectedTextSize(config.indicatorUnselectedTextSizeSp);
        tabLayout.setSelectedTextBold(config.indicatorSelectedBold);
        tabLayout.setSelectedTextColor(ContextCompat.getColor(
                tabLayout.getContext(),
                TabLayoutDemoColorPresets.getColorRes(config.indicatorSelectedColorIndex)));
        tabLayout.setUnselectedTextColor(ContextCompat.getColor(
                tabLayout.getContext(),
                TabLayoutDemoColorPresets.getColorRes(config.indicatorUnselectedColorIndex)));
        tabLayout.setIndicatorColor(ContextCompat.getColor(
                tabLayout.getContext(),
                TabLayoutDemoColorPresets.getColorRes(config.indicatorBarColorIndex)));
    }
}
