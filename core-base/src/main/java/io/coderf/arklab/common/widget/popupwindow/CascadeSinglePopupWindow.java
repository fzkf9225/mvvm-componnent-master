package io.coderf.arklab.common.widget.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.PopupMultiCascadeBinding;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowCheckBoxAdapter;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowSelectedAdapter;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import io.coderf.arklab.core.utils.CollectionUtil;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * 单选级联 PopupWindow
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/5
 */
public class CascadeSinglePopupWindow<T extends PopupWindowBean<T>> extends BasePopupWindow
        implements PopupWindowSelectedAdapter.OnItemSelectedClearListener {

    public static final String TAG = "PopupSingleCascadeView";

    private PopupWindowCheckBoxAdapter<T> popupWindowAdapter;
    private PopupWindowSelectedAdapter<T> popupWindowSelectedAdapter;
    private PopupMultiCascadeBinding binding;
    private final List<T> dataList;
    private final SelectedListener<T> selectedListener;
    private @ColorInt int selectTextColor;
    private @ColorInt int unSelectTextColor;
    private float radius;
    private @ColorInt int selectBgColor;
    private @ColorInt int unSelectBgColor;
    private @ColorInt int selectionTextColor;
    private float paddingStart, paddingTop, paddingEnd, paddingBottom;
    private float itemHeight;

    public CascadeSinglePopupWindow(Activity context, List<T> dataList,
                                    SelectedListener<T> selectedListener) {
        super(context);
        this.dataList = dataList;
        this.selectedListener = selectedListener;

        itemHeight = DensityUtil.dp2px(context, 40f);
        selectionTextColor = ThemeAttrs.onSurface(context);
        selectTextColor = ThemeAttrs.onPrimary(context);
        unSelectTextColor = ThemeAttrs.onSurface(context);
        radius = DensityUtil.dp2px(context, 6f);
        selectBgColor = ThemeAttrs.primary(context);
        unSelectBgColor = ContextCompat.getColor(context, R.color.default_background);
        paddingStart = DensityUtil.dp2px(context, 12f);
        paddingTop = DensityUtil.dp2px(context, 6f);
        paddingEnd = DensityUtil.dp2px(context, 12f);
        paddingBottom = DensityUtil.dp2px(context, 6f);

        setLandscapeCenterEnabled(true);
        enableHostDim(DEFAULT_HOST_DIM_ALPHA);
        initViews();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViews() {
        binding = inflateWithHostAdapt(
                () -> PopupMultiCascadeBinding.inflate(layoutInflater, null, false));

        popupWindowSelectedAdapter = new PopupWindowSelectedAdapter<>();
        popupWindowSelectedAdapter.setOnItemSelectedClearListener(this);
        popupWindowAdapter = new PopupWindowCheckBoxAdapter<>(popupWindowSelectedAdapter);
        popupWindowAdapter.setItemHeight(itemHeight);
        popupWindowAdapter.setTextColor(selectionTextColor);
        popupWindowAdapter.setShowCheckBox(false);

        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
        popupWindowSelectedAdapter.setRadius(radius);
        popupWindowSelectedAdapter.setUnSelectBgColor(unSelectBgColor);
        popupWindowSelectedAdapter.setSelectBgColor(selectBgColor);
        popupWindowSelectedAdapter.setSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setUnSelectTextColor(unSelectTextColor);

        popupWindowAdapter.setOnItemClickListener((view, position) -> {
            if (!binding.mRecyclerviewSelected.isShown()) {
                binding.mRecyclerviewSelected.setVisibility(View.VISIBLE);
            }

            T selectedItem = popupWindowAdapter.getList().get(position);
            boolean hasChild = CollectionUtil.isNotEmpty(selectedItem.getChildList());
            if (hasChild) {
                popupWindowAdapter.setList(selectedItem.getChildList());
                popupWindowAdapter.notifyDataSetChanged();
            }

            if (CollectionUtil.isEmpty(popupWindowSelectedAdapter.getList())) {
                popupWindowSelectedAdapter.getList().add(selectedItem);
            } else {
                T lastItem = popupWindowSelectedAdapter.getList()
                        .get(popupWindowSelectedAdapter.getList().size() - 1);
                boolean sameParent = lastItem != null
                        && !TextUtils.isEmpty(lastItem.getParentPopupId())
                        && lastItem.getParentPopupId().equals(selectedItem.getParentPopupId());
                if (sameParent) {
                    popupWindowSelectedAdapter.getList()
                            .set(popupWindowSelectedAdapter.getList().size() - 1, selectedItem);
                } else {
                    popupWindowSelectedAdapter.getList().add(selectedItem);
                }
            }
            popupWindowSelectedAdapter.notifyDataSetChanged();
        });

        popupWindowAdapter.setList(dataList);
        binding.mRecyclerviewOptions.addItemDecoration(
                new RecycleViewDivider(context, LinearLayoutManager.VERTICAL,
                        DensityUtil.dp2px(context, 1f), ThemeAttrs.outlineVariant(context)));
        binding.mRecyclerviewOptions.setLayoutManager(
                new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        binding.mRecyclerviewOptions.setAdapter(popupWindowAdapter);

        binding.mRecyclerviewSelected.setLayoutManager(
                new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        binding.mRecyclerviewSelected.setAdapter(popupWindowSelectedAdapter);

        binding.tvClearSelected.setOnClickListener(v -> {
            popupWindowSelectedAdapter.setList(new ArrayList<>());
            popupWindowSelectedAdapter.notifyDataSetChanged();
            popupWindowAdapter.setList(dataList);
            popupWindowAdapter.notifyDataSetChanged();
        });

        binding.tvConfirm.setOnClickListener(v -> {
            if (CollectionUtil.isEmpty(popupWindowSelectedAdapter.getList())) {
                Toast.makeText(context, "请至少选择一项", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedListener.onSelectedResult(this, popupWindowSelectedAdapter.getList());
            dismiss();
        });

        setContentView(binding.getRoot());
        applyDefaultWindowConfig(binding.getRoot());
        setAnimationStyle(android.R.style.Animation_Dialog);
        applyCascadePanelSize();
    }

    public void setTitle(String title) {
        binding.tvTitle.setText(title);
    }

    public void setConfirmText(String confirmText) {
        binding.tvConfirm.setText(confirmText);
    }

    public void setClearText(String clearText) {
        binding.tvClearSelected.setText(clearText);
    }

    public void setConfirmTextColor(@ColorInt int confirmTextColor) {
        binding.tvConfirm.setTextColor(confirmTextColor);
    }

    public void setClearTextColor(@ColorInt int clearTextColor) {
        binding.tvClearSelected.setTextColor(clearTextColor);
    }

    public void setConfirmTextSize(float confirmTextSize) {
        binding.tvConfirm.setTextSize(confirmTextSize);
    }

    public void setClearTextSize(float clearTextSize) {
        binding.tvClearSelected.setTextSize(clearTextSize);
    }

    public void setTitleTextColor(@ColorInt int titleTextColor) {
        binding.tvTitle.setTextColor(titleTextColor);
    }

    public void setTitleBold(boolean titleBold) {
        binding.tvTitle.setTypeface(titleBold ? Typeface.defaultFromStyle(Typeface.BOLD) : null);
    }

    public void setPadding(float paddingStart, float paddingTop, float paddingEnd, float paddingBottom) {
        this.paddingStart = paddingStart;
        this.paddingTop = paddingTop;
        this.paddingEnd = paddingEnd;
        this.paddingBottom = paddingBottom;
        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
    }

    public void setUnSelectedStyle(@ColorInt int bgColorRes, @ColorInt int textColorRes, int radius) {
        this.radius = radius;
        this.unSelectTextColor = textColorRes;
        this.unSelectBgColor = bgColorRes;
        popupWindowSelectedAdapter.setRadius(this.radius);
        popupWindowSelectedAdapter.setUnSelectTextColor(unSelectTextColor);
        popupWindowSelectedAdapter.setUnSelectBgColor(this.unSelectBgColor);
    }

    public void setSelectedStyle(@ColorInt int bgColorRes, @ColorInt int textColorRes, int radius) {
        this.radius = radius;
        this.selectTextColor = textColorRes;
        this.selectBgColor = bgColorRes;
        popupWindowSelectedAdapter.setRadius(this.radius);
        popupWindowSelectedAdapter.setSelectTextColor(this.selectTextColor);
        popupWindowSelectedAdapter.setSelectBgColor(this.selectBgColor);
    }

    public void setSelectionTextColor(@ColorInt int selectionTextColor) {
        this.selectionTextColor = selectionTextColor;
        popupWindowAdapter.setTextColor(selectionTextColor);
    }

    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
        popupWindowAdapter.setItemHeight(itemHeight);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSelectedClear(ShapeableImageView ivCheckView, int position) {
        List<T> newList;
        if (position == 0) {
            newList = dataList;
        } else {
            T parent = PopupWindowUtil.findById(
                    dataList, popupWindowSelectedAdapter.getList().get(position - 1).getPopupId());
            newList = parent != null ? parent.getChildList() : null;
        }
        popupWindowAdapter.setList(newList);
        popupWindowAdapter.notifyDataSetChanged();

        List<T> filteredList = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            filteredList.add(popupWindowSelectedAdapter.getList().get(i));
        }
        popupWindowSelectedAdapter.setList(filteredList);
        popupWindowSelectedAdapter.notifyDataSetChanged();
    }

    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList);
    }
}
