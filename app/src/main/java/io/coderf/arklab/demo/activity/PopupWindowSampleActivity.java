package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;

import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.popupwindow.CascadeMultiPopupWindow;
import io.coderf.arklab.common.widget.popupwindow.CascadeSinglePopupWindow;
import io.coderf.arklab.common.widget.popupwindow.ExposedDropdownBinder;
import io.coderf.arklab.common.widget.popupwindow.MultiPopupView;
import io.coderf.arklab.common.widget.popupwindow.PopupView;
import io.coderf.arklab.common.widget.popupwindow.TreePopupView;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.data.PopupWindowSampleData;
import io.coderf.arklab.demo.databinding.ActivityPopupWindowSampleBinding;

/**
 * PopupWindow 封装演示：Exposed Dropdown + 自研各类 Popup（静态模拟数据）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/5
 */
@AndroidEntryPoint
public class PopupWindowSampleActivity
        extends BaseActivity<EmptyViewModel, ActivityPopupWindowSampleBinding> {

    private UseCase useCase;
    private ExposedDropdownBinder<PopupWindowBean<?>> exposedBinder;
    private PopupView<PopupWindowBean<?>> cityPopup;
    private MultiPopupView<PopupWindowBean<?>> materialPopup;
    private TreePopupView<PopupWindowBean> treePopup;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_popup_window_sample;
    }

    @Override
    public String setTitleBar() {
        return "PopupWindow 演示";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        setupExposedDropdown();
        setupPopupView();
        setupMultiPopup();
        setupTreePopup();
        setupCascadeSingle();
        setupCascadeMulti();
    }

    @Override
    public void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private void setupExposedDropdown() {
        exposedBinder = ExposedDropdownBinder
                .bind(binding.exposedDropdownText, binding.exposedDropdownLayout)
                .setItems(PopupWindowSampleData.cities())
                .setSelectedByCode("WH")
                .setOnItemSelectedListener((item, position) -> {
                    binding.tvExposedResult.setText(
                            "已选：" + item.getPopupName()
                                    + "（id=" + item.getPopupId()
                                    + ", code=" + item.getPopupCode() + "）");
                    showToast("Exposed：" + item.getPopupName());
                });
        PopupWindowBean<?> selected = exposedBinder.getSelectedItem();
        if (selected != null) {
            binding.tvExposedResult.setText(
                    "已选：" + selected.getPopupName()
                            + "（id=" + selected.getPopupId()
                            + ", code=" + selected.getPopupCode() + "）");
        }
    }

    private void setupPopupView() {
        cityPopup = new PopupView<>(this, PopupWindowSampleData.cities(),
                (popupWindow, dataList, selectedPosition) -> {
                    PopupWindowBean<?> item = dataList.get(selectedPosition);
                    binding.tvPopupView.setText(item.getPopupName());
                    showToast("PopupView：" + item.getPopupName());
                });
        binding.tvPopupView.setOnClickListener(v ->
                cityPopup.showAsDropDown(v, 0, 0, Gravity.TOP));
    }

    private void setupMultiPopup() {
        materialPopup = new MultiPopupView<>(this, PopupWindowSampleData.materials(),
                (popupWindow, dataList) -> {
                    // dataList 已是选中集合，直接拼名称（不依赖 item.selected 标记）
                    String names = joinPopupNames(dataList, "、");
                    binding.tvMultiPopup.setText(
                            dataList.isEmpty() ? "" : ("已选" + dataList.size() + "项：" + names));
                    showToast("MultiPopup：已选 " + dataList.size() + " 项");
                });
        materialPopup.setColumnCount(3);
        materialPopup.setSubmitTextColor(
                ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green));
        materialPopup.setSelectBgDrawable(DrawableUtil.createShapeDrawable(
                ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green),
                DensityUtil.dp2px(this, 5f)));
        binding.tvMultiPopup.setOnClickListener(v ->
                materialPopup.showAsDropDown(v, 0, 0, Gravity.TOP));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupTreePopup() {
        List treeData = PopupWindowSampleData.treeGender();
        treePopup = new TreePopupView<>(this, treeData, true,
                (popupWindow, dataList, parentSelectPosition, childrenSelectPosition) -> {
                    if (parentSelectPosition == null) {
                        return;
                    }
                    PopupWindowBean parent = dataList.get(parentSelectPosition);
                    String text;
                    if (childrenSelectPosition != null
                            && parent.getChildList() != null
                            && childrenSelectPosition < parent.getChildList().size()) {
                        PopupWindowBean child =
                                (PopupWindowBean) parent.getChildList().get(childrenSelectPosition);
                        text = parent.getPopupName() + " / " + child.getPopupName();
                    } else {
                        text = parent.getPopupName();
                    }
                    binding.tvTreePopup.setText(text);
                    showToast("TreePopup：" + text);
                });
        binding.tvTreePopup.setOnClickListener(v ->
                treePopup.showAsDropDown(v, 0, 0, Gravity.TOP));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupCascadeSingle() {
        binding.btnCascadeSingle.setOnClickListener(v -> {
            List data = PopupWindowSampleData.cascadeRegions();
            CascadeSinglePopupWindow popup = new CascadeSinglePopupWindow(
                    this, data,
                    (popupWindow, dataList) -> {
                        String path = joinPopupNames(dataList, " / ");
                        binding.tvCascadeSingleResult.setText("已选：" + path);
                        showToast("CascadeSingle：" + path);
                    });
            popup.setTitle("选择地区");
            popup.setSelectedStyle(
                    ThemeAttrs.primary(this),
                    ThemeAttrs.onPrimary(this),
                    DensityUtil.dp2px(this, 6f));
            popup.setConfirmTextColor(
                    ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green));
            PopupWindowCompat.showAsDropDown(popup, v, 0, 0, Gravity.CENTER);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupCascadeMulti() {
        binding.btnCascadeMulti.setOnClickListener(v -> {
            List data = PopupWindowSampleData.cascadeRegions();
            CascadeMultiPopupWindow popup = new CascadeMultiPopupWindow<>(
                    this, data,
                    (popupWindow, dataList) -> {
                        String names = joinPopupNames(dataList, "、");
                        binding.tvCascadeMultiResult.setText(
                                "已选" + dataList.size() + "项：" + names);
                        showToast("CascadeMulti：已选 " + dataList.size() + " 项");
                    });
            popup.setTitle("多选地区");
            popup.setCheckedDrawable(DrawableUtil.createCheckedDrawable(
                    this, ThemeAttrs.primary(this), DensityUtil.dp2px(this, 16f)));
            popup.setUncheckedDrawable(DrawableUtil.createUncheckedDrawable(
                    DensityUtil.dp2px(this, 1), ThemeAttrs.primary(this),
                    DensityUtil.dp2px(this, 16)));
            popup.setSelectedBgStyle(
                    ContextCompat.getColor(this, io.coderf.arklab.common.R.color.default_background),
                    ThemeAttrs.onSurface(this),
                    DensityUtil.dp2px(this, 6f));
            popup.setConfirmTextColor(
                    ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green));
            PopupWindowCompat.showAsDropDown(popup, v, 0, 0, Gravity.CENTER);
        });
    }

    @SuppressWarnings("rawtypes")
    private static String joinPopupNames(List list, String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (i > 0) {
                sb.append(separator);
            }
            if (item instanceof PopupWindowBean) {
                sb.append(((PopupWindowBean) item).getPopupName());
            }
        }
        return sb.toString();
    }
}
