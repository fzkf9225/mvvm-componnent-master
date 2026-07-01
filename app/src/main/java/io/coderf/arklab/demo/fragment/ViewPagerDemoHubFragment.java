package io.coderf.arklab.demo.fragment;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.config.TabLayoutDemoColorPresets;
import io.coderf.arklab.demo.config.TabLayoutDemoConfig;
import io.coderf.arklab.demo.databinding.ViewPagerDemoHubFragmentBinding;

/**
 * ViewPager TabLayout 各基类示例入口，并提供运行时参数配置。
 */
@AndroidEntryPoint
public class ViewPagerDemoHubFragment extends BaseFragment<EmptyViewModel, ViewPagerDemoHubFragmentBinding> {

    private TabLayoutDemoConfig config;

    @Override
    protected int getLayoutId() {
        return R.layout.view_pager_demo_hub_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        config = TabLayoutDemoConfig.get();
        bindNavigation();
        bindStyledConfig();
        bindFixedConfig();
        bindIndicatorConfig();
    }

    private void bindNavigation() {
        binding.btnMaterialTab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_hub_to_material_tab));
        binding.btnStyledTab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_hub_to_styled_tab));
        binding.btnFixedTab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_hub_to_fixed_tab));
        binding.btnIndicatorTab.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_hub_to_indicator_tab));
    }

    private void bindStyledConfig() {
        binding.seekStyledSelectedSize.setProgress(spToProgress(config.styledSelectedTextSizeSp));
        binding.seekStyledUnselectedSize.setProgress(spToProgress(config.styledUnselectedTextSizeSp));
        binding.seekStyledCornerRadius.setProgress(config.styledCornerRadiusDp - 8);
        binding.switchStyledSelectedBold.setChecked(config.styledSelectedBold);
        bindColorChips(
                binding.chipsStyledSelectedColor,
                config.styledSelectedColorIndex,
                index -> {
                    config.styledSelectedColorIndex = index;
                    updateStyledLabels();
                });
        bindColorChips(
                binding.chipsStyledUnselectedColor,
                config.styledUnselectedColorIndex,
                index -> {
                    config.styledUnselectedColorIndex = index;
                    updateStyledLabels();
                });
        updateStyledLabels();

        binding.seekStyledSelectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.styledSelectedTextSizeSp = progressToSp(progress);
                updateStyledLabels();
            }
        });
        binding.seekStyledUnselectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.styledUnselectedTextSizeSp = progressToSp(progress);
                updateStyledLabels();
            }
        });
        binding.seekStyledCornerRadius.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.styledCornerRadiusDp = progress + 8;
                updateStyledLabels();
            }
        });
        binding.switchStyledSelectedBold.setOnCheckedChangeListener((buttonView, isChecked) ->
                config.styledSelectedBold = isChecked);
    }

    private void bindFixedConfig() {
        binding.seekFixedIndicatorWidth.setProgress(config.fixedIndicatorWidthDp - 12);
        binding.seekFixedIndicatorHeight.setProgress(config.fixedIndicatorHeightDp - 2);
        binding.seekFixedSelectedSize.setProgress(spToProgress(config.fixedSelectedTextSizeSp));
        binding.seekFixedUnselectedSize.setProgress(spToProgress(config.fixedUnselectedTextSizeSp));
        binding.switchFixedSelectedBold.setChecked(config.fixedSelectedBold);
        bindColorChips(
                binding.chipsFixedSelectedColor,
                config.fixedSelectedColorIndex,
                index -> {
                    config.fixedSelectedColorIndex = index;
                    updateFixedLabels();
                });
        bindColorChips(
                binding.chipsFixedUnselectedColor,
                config.fixedUnselectedColorIndex,
                index -> {
                    config.fixedUnselectedColorIndex = index;
                    updateFixedLabels();
                });
        bindColorChips(
                binding.chipsFixedIndicatorColor,
                config.fixedIndicatorColorIndex,
                index -> {
                    config.fixedIndicatorColorIndex = index;
                    updateFixedLabels();
                });
        updateFixedLabels();

        binding.seekFixedIndicatorWidth.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.fixedIndicatorWidthDp = progress + 12;
                updateFixedLabels();
            }
        });
        binding.seekFixedIndicatorHeight.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.fixedIndicatorHeightDp = progress + 2;
                updateFixedLabels();
            }
        });
        binding.seekFixedSelectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.fixedSelectedTextSizeSp = progressToSp(progress);
                updateFixedLabels();
            }
        });
        binding.seekFixedUnselectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.fixedUnselectedTextSizeSp = progressToSp(progress);
                updateFixedLabels();
            }
        });
        binding.switchFixedSelectedBold.setOnCheckedChangeListener((buttonView, isChecked) ->
                config.fixedSelectedBold = isChecked);
    }

    private void bindIndicatorConfig() {
        binding.seekIndicatorWidth.setProgress(config.indicatorWidthDp - 8);
        binding.seekIndicatorHeight.setProgress(config.indicatorHeightDp - 2);
        binding.seekIndicatorCornerRadius.setProgress(config.indicatorCornerRadiusDp);
        binding.seekIndicatorSelectedSize.setProgress(spToProgress(config.indicatorSelectedTextSizeSp));
        binding.seekIndicatorUnselectedSize.setProgress(spToProgress(config.indicatorUnselectedTextSizeSp));
        binding.switchIndicatorSelectedBold.setChecked(config.indicatorSelectedBold);
        bindColorChips(
                binding.chipsIndicatorSelectedColor,
                config.indicatorSelectedColorIndex,
                index -> {
                    config.indicatorSelectedColorIndex = index;
                    updateIndicatorLabels();
                });
        bindColorChips(
                binding.chipsIndicatorUnselectedColor,
                config.indicatorUnselectedColorIndex,
                index -> {
                    config.indicatorUnselectedColorIndex = index;
                    updateIndicatorLabels();
                });
        bindColorChips(
                binding.chipsIndicatorBarColor,
                config.indicatorBarColorIndex,
                index -> {
                    config.indicatorBarColorIndex = index;
                    updateIndicatorLabels();
                });
        updateIndicatorLabels();

        binding.seekIndicatorWidth.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.indicatorWidthDp = progress + 8;
                updateIndicatorLabels();
            }
        });
        binding.seekIndicatorHeight.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.indicatorHeightDp = progress + 2;
                updateIndicatorLabels();
            }
        });
        binding.seekIndicatorCornerRadius.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.indicatorCornerRadiusDp = progress;
                updateIndicatorLabels();
            }
        });
        binding.seekIndicatorSelectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.indicatorSelectedTextSizeSp = progressToSp(progress);
                updateIndicatorLabels();
            }
        });
        binding.seekIndicatorUnselectedSize.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                config.indicatorUnselectedTextSizeSp = progressToSp(progress);
                updateIndicatorLabels();
            }
        });
        binding.switchIndicatorSelectedBold.setOnCheckedChangeListener((buttonView, isChecked) ->
                config.indicatorSelectedBold = isChecked);
    }

    private void bindColorChips(LinearLayout container, int selectedIndex, ColorIndexListener listener) {
        container.removeAllViews();
        int margin = DensityUtil.dp2px(requireContext(), 6);
        int chipHeight = DensityUtil.dp2px(requireContext(), 32);
        int chipMinWidth = DensityUtil.dp2px(requireContext(), 40);
        for (int i = 0; i < TabLayoutDemoColorPresets.COLOR_RES.length; i++) {
            final int index = i;
            TextView chip = new TextView(requireContext());
            chip.setText(TabLayoutDemoColorPresets.COLOR_LABELS[i]);
            chip.setTextSize(11f);
            chip.setGravity(Gravity.CENTER);
            chip.setMinWidth(chipMinWidth);
            chip.setPadding(margin, 0, margin, 0);
            chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            chip.setTypeface(null, index == selectedIndex ? Typeface.BOLD : Typeface.NORMAL);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(DensityUtil.dp2px(requireContext(), 16));
            bg.setColor(ContextCompat.getColor(requireContext(), TabLayoutDemoColorPresets.COLOR_RES[i]));
            if (index == selectedIndex) {
                bg.setStroke(DensityUtil.dp2px(requireContext(), 2),
                        ContextCompat.getColor(requireContext(), io.coderf.arklab.common.R.color.autoColor));
            }
            chip.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, chipHeight);
            if (i > 0) {
                lp.setMarginStart(margin);
            }
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                listener.onColorIndexSelected(index);
                bindColorChips(container, index, listener);
            });
            container.addView(chip);
        }
    }

    private void updateStyledLabels() {
        binding.tvStyledSelectedSize.setText("选中字号：" + formatSp(config.styledSelectedTextSizeSp));
        binding.tvStyledUnselectedSize.setText("未选中字号：" + formatSp(config.styledUnselectedTextSizeSp));
        binding.tvStyledCornerRadius.setText("圆角半径：" + config.styledCornerRadiusDp + "dp");
    }

    private void updateFixedLabels() {
        binding.tvFixedIndicatorWidth.setText("指示条宽度：" + config.fixedIndicatorWidthDp + "dp");
        binding.tvFixedIndicatorHeight.setText("指示条高度：" + config.fixedIndicatorHeightDp + "dp");
        binding.tvFixedSelectedSize.setText("选中字号：" + formatSp(config.fixedSelectedTextSizeSp));
        binding.tvFixedUnselectedSize.setText("未选中字号：" + formatSp(config.fixedUnselectedTextSizeSp));
    }

    private void updateIndicatorLabels() {
        binding.tvIndicatorWidth.setText("指示条宽度：" + config.indicatorWidthDp + "dp");
        binding.tvIndicatorHeight.setText("指示条高度：" + config.indicatorHeightDp + "dp");
        binding.tvIndicatorCornerRadius.setText("指示条圆角：" + config.indicatorCornerRadiusDp + "dp");
        binding.tvIndicatorSelectedSize.setText("选中字号：" + formatSp(config.indicatorSelectedTextSizeSp));
        binding.tvIndicatorUnselectedSize.setText("未选中字号：" + formatSp(config.indicatorUnselectedTextSizeSp));
    }

    private static int spToProgress(float sp) {
        return Math.round(sp) - 10;
    }

    private static float progressToSp(int progress) {
        return progress + 10f;
    }

    private static String formatSp(float sp) {
        if (Math.round(sp) == sp) {
            return Math.round(sp) + "sp";
        }
        return sp + "sp";
    }

    @Override
    protected void initData(Bundle bundle) {
    }

    private interface ColorIndexListener {
        void onColorIndexSelected(int index);
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
