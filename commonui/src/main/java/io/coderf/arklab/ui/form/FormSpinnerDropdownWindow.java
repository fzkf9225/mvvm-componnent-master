package io.coderf.arklab.ui.form;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowAdapter;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import io.coderf.arklab.ui.R;

/**
 * FormSpinner 锚点下拉列表，复用 {@link PopupWindowBean} 与 {@link PopupWindowAdapter}。
 *
 * @author fz
 */
public final class FormSpinnerDropdownWindow<T extends PopupWindowBean<?>> extends PopupWindow {

    public interface OnItemSelectedListener<T extends PopupWindowBean<?>> {
        void onItemSelected(@NonNull T item, int position);
    }

    private final Context context;
    private PopupWindowAdapter<T> adapter;
    @NonNull
    private FormSpinnerDropdownStyle dropdownStyle;

    public FormSpinnerDropdownWindow(@NonNull Context context) {
        super(context);
        this.context = context;
        this.dropdownStyle = FormSpinnerDropdownStyle.defaultStyle(context);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setElevation(DensityUtil.dp2px(context, 8f));
    }

    public void setDropdownStyle(@NonNull FormSpinnerDropdownStyle style) {
        dropdownStyle.apply(style);
    }

    public void show(@NonNull View anchor, @NonNull List<T> dataList, int maxHeightPx,
                     @Nullable OnItemSelectedListener<T> listener) {
        RecyclerView recyclerView = new RecyclerView(context);
        adapter = new PopupWindowAdapter<>(PopupWindowAdapter.MODE_SINGLE);
        applyAdapterStyle(adapter);
        adapter.setList(dataList);
        adapter.setOnItemClickListener((view, position) -> {
            dismiss();
            if (listener != null && position >= 0 && position < dataList.size()) {
                listener.onItemSelected(dataList.get(position), position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.VERTICAL,
                DensityUtil.dp2px(context, 1),
                dropdownStyle.itemBorderColor));
        int itemHeight = Math.max(1, (int) dropdownStyle.itemHeightPx);
        int divider = Math.max(0, dataList.size() - 1);
        int contentHeight = Math.min(maxHeightPx, itemHeight * dataList.size() + divider);
        setContentView(recyclerView);
        setWidth(anchor.getWidth());
        setHeight(contentHeight);
        setBackgroundDrawable(dropdownStyle.spinnerBackground);
        showAsDropDown(anchor, 0, DensityUtil.dp2px(context, 2f), Gravity.START);
    }

    private void applyAdapterStyle(@NonNull PopupWindowAdapter<T> targetAdapter) {
        targetAdapter.setItemHeight(dropdownStyle.itemHeightPx);
        targetAdapter.setTextColor(dropdownStyle.textColor);
        targetAdapter.setTextSelectedColor(dropdownStyle.textSelectedColor);
        if (dropdownStyle.itemBackground != null) {
            targetAdapter.setBackgroundDrawable(dropdownStyle.itemBackground);
        }
        if (dropdownStyle.itemSelectedBackground != null) {
            targetAdapter.setBackgroundSelectedDrawable(dropdownStyle.itemSelectedBackground);
        }
        targetAdapter.setPaddingLeft(dropdownStyle.paddingLeftPx);
        targetAdapter.setPaddingRight(dropdownStyle.paddingRightPx);
        if (dropdownStyle.textSizePx != null && dropdownStyle.textSizePx > 0f) {
            targetAdapter.setTextSizePx(dropdownStyle.textSizePx);
        }
    }

    public void setSelectedItem(@Nullable T item) {
        if (adapter != null) {
            adapter.setSelectedItem(item);
        }
    }
}
