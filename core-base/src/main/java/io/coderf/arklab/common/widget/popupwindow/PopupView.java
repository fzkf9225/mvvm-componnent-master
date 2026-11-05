package io.coderf.arklab.common.widget.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.PopupViewBinding;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowAdapter;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * PopupWindow 下拉框，竖向下拉的 PopupWindow（支持单选/多选）
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/11/5
 */
public class PopupView<T extends PopupWindowBean> extends BasePopupWindow {
    private final SelectedListener<T> selectedListener;
    private final List<T> dataList;
    private PopupWindowAdapter<T> popupWindowAdapter;
    private int selectionMode = PopupWindowAdapter.MODE_SINGLE;
    private @ColorInt Integer selectTextColor;
    private @ColorInt Integer unSelectTextColor;
    private Drawable selectBgDrawable;
    private Drawable unSelectBgDrawable;
    private @Dimension(unit = Dimension.PX) Float itemHeight;
    private PopupViewBinding binding;

    public PopupView(Context context, List<T> dataList, SelectedListener<T> selectedListener) {
        super(context);
        this.selectedListener = selectedListener;
        this.dataList = dataList;

        itemHeight = (float) DensityUtil.dp2px(context, 44f);
        selectTextColor = ThemeAttrs.primary(context);
        unSelectTextColor = ThemeAttrs.onSurfaceVariant(context);
        // 整条 item 选中/未选中 + 按下波纹
        selectBgDrawable = createRowBackground(true);
        unSelectBgDrawable = createRowBackground(false);

        initView();
    }

    private void initView() {
        binding = inflateWithHostAdapt(
                () -> PopupViewBinding.inflate(layoutInflater, null, false));
        setContentView(binding.getRoot());
        applyMatchParentDropDownSize();
        applyDefaultWindowConfig(binding.getRoot());
        setupMask(binding.maskView);

        int hPad = DensityUtil.dp2px(context, 14f);
        popupWindowAdapter = new PopupWindowAdapter<>(selectionMode);
        popupWindowAdapter.setItemHeight(itemHeight)
                .setBackgroundDrawable(unSelectBgDrawable)
                .setBackgroundSelectedDrawable(selectBgDrawable)
                .setTextColor(unSelectTextColor)
                .setTextSelectedColor(selectTextColor)
                .setTextGravity(Gravity.START | Gravity.CENTER_VERTICAL)
                .setPadding(hPad, 0, hPad, 0)
                .setOnItemClickListener(this::onItemClick);
        popupWindowAdapter.setList(dataList);

        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerCategory.setAdapter(popupWindowAdapter);
        binding.recyclerCategory.setOverScrollMode(View.OVER_SCROLL_NEVER);

        binding.getRoot().post(() -> {
            int totalHeight = calculateLinearRecyclerViewHeight(
                    binding.recyclerCategory, R.layout.option_text_view);
            ConstraintLayout.LayoutParams lp =
                    (ConstraintLayout.LayoutParams) binding.recyclerCategory.getLayoutParams();
            lp.height = totalHeight;
            binding.recyclerCategory.setLayoutParams(lp);
        });
    }

    /** 整行背景：选中浅主色 / 未选透明，均带按下 ripple */
    private Drawable createRowBackground(boolean selected) {
        int fill = selected
                ? ColorUtils.setAlphaComponent(ThemeAttrs.primary(context), 28)
                : Color.TRANSPARENT;
        int ripple = ColorUtils.setAlphaComponent(ThemeAttrs.onSurface(context), 48);
        return DrawableUtil.createRippleDrawableCompat(new ColorDrawable(fill), ripple);
    }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectionMode(selectionMode);
        }
    }

    public void setItemHeight(@Dimension(unit = Dimension.PX) float itemHeight) {
        this.itemHeight = itemHeight;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setItemHeight(itemHeight);
        }
    }

    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundDrawable(unSelectBgDrawable);
        }
    }

    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        setUnSelectBgDrawable(new ColorDrawable(unSelectBgColor));
    }

    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundSelectedDrawable(selectBgDrawable);
        }
    }

    public void setSelectBgColor(@ColorInt int selectBgColor) {
        setSelectBgDrawable(new ColorDrawable(selectBgColor));
    }

    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextColor(unSelectTextColor);
        }
    }

    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSelectedColor(selectTextColor);
        }
    }

    public void setTextSizeSp(float textSizeSp) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSizeSp(textSizeSp);
        }
    }

    public void setTextGravity(int gravity) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextGravity(gravity);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setPadding(left, top, right, bottom);
        }
    }

    public void setMargin(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setMargin(left, top, right, bottom);
        }
    }

    public PopupViewBinding getBinding() {
        return binding;
    }

    private void onItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null
                || position < 0 || position >= dataList.size()) {
            return;
        }
        // setOnItemClickListener 会盖掉 Adapter 默认选中，这里补上整行选中态
        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            for (int i = 0; i < dataList.size(); i++) {
                T item = dataList.get(i);
                boolean shouldSelect = i == position;
                if (Boolean.TRUE.equals(item.getSelected()) != shouldSelect) {
                    item.setSelected(shouldSelect);
                }
            }
            popupWindowAdapter.notifyDataSetChanged();
            dismiss();
        } else {
            T item = dataList.get(position);
            item.setSelected(!Boolean.TRUE.equals(item.getSelected()));
            popupWindowAdapter.notifyItemChanged(position);
        }
        if (selectedListener != null) {
            selectedListener.onSelectedResult(PopupView.this, dataList, position);
        }
    }

    public T getSelectedItem() {
        return popupWindowAdapter != null ? popupWindowAdapter.getSelectedItem() : null;
    }

    public List<T> getSelectedItems() {
        return popupWindowAdapter != null
                ? popupWindowAdapter.getSelectedItems()
                : new ArrayList<>();
    }

    public void clearSelection() {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.clearSelection();
        }
    }

    public void setSelectedItem(T item) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectedItem(item);
        }
    }

    public void setSelectedItems(List<T> items) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectedItems(items);
        }
    }

    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList, Integer selectedPosition);
    }
}
