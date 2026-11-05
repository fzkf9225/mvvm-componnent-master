package io.coderf.arklab.common.widget.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowCheckBoxAdapter;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowSelectedAdapter;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import io.coderf.arklab.core.utils.CollectionUtil;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * 多选级联 PopupWindow
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/5
 */
public class CascadeMultiPopupWindow<T extends PopupWindowBean> extends BasePopupWindow
        implements PopupWindowCheckBoxAdapter.OnItemSelectedChangedListener,
        PopupWindowSelectedAdapter.OnItemSelectedClearListener {

    public static final String TAG = "PopupMultiCascadeView";

    private PopupWindowCheckBoxAdapter<T> popupWindowAdapter;
    private PopupWindowSelectedAdapter<T> popupWindowSelectedAdapter;
    private PopupMultiCascadeBinding binding;
    private final List<T> dataList;
    private final SelectedListener<T> selectedListener;
    private @ColorInt int selectTextColor;
    private float radius;
    private @ColorInt int selectBgColor;
    private @ColorInt int selectionTextColor;
    private Drawable checkedDrawable;
    private Drawable uncheckedDrawable;
    private float paddingStart, paddingTop, paddingEnd, paddingBottom;
    private float itemHeight;

    public CascadeMultiPopupWindow(Activity context, List<T> dataList,
                                   SelectedListener<T> selectedListener) {
        super(context);
        this.dataList = dataList;
        this.selectedListener = selectedListener;

        itemHeight = DensityUtil.dp2px(context, 40f);
        selectionTextColor = ThemeAttrs.onSurface(context);
        selectTextColor = ThemeAttrs.onSurface(context);
        radius = DensityUtil.dp2px(context, 6f);
        selectBgColor = ContextCompat.getColor(context, R.color.default_background);
        paddingStart = DensityUtil.dp2px(context, 12f);
        paddingTop = DensityUtil.dp2px(context, 6f);
        paddingEnd = DensityUtil.dp2px(context, 12f);
        paddingBottom = DensityUtil.dp2px(context, 6f);
        checkedDrawable = DrawableUtil.createCheckedDrawable(
                context, ThemeAttrs.primary(context), DensityUtil.dp2px(context, 16f));
        uncheckedDrawable = DrawableUtil.createUncheckedDrawable(
                DensityUtil.dp2px(context, 1), ThemeAttrs.primary(context),
                DensityUtil.dp2px(context, 16));

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
        popupWindowAdapter.setOnItemSelectedChangedListener(this);
        popupWindowAdapter.setItemHeight(itemHeight);
        popupWindowAdapter.setCheckedDrawable(checkedDrawable);
        popupWindowAdapter.setUncheckedDrawable(uncheckedDrawable);
        popupWindowAdapter.setTextColor(selectionTextColor);
        popupWindowAdapter.setShowCheckBox(true);

        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
        popupWindowSelectedAdapter.setSelectBgColor(selectBgColor);
        popupWindowSelectedAdapter.setRadius(radius);
        popupWindowSelectedAdapter.setSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setUnSelectTextColor(selectTextColor);

        popupWindowAdapter.setOnItemClickListener((view, position) -> {
            if (!binding.mRecyclerviewSelected.isShown()) {
                binding.mRecyclerviewSelected.setVisibility(View.VISIBLE);
            }
            T item = popupWindowAdapter.getList().get(position);
            if (CollectionUtil.isNotEmpty(item.getChildList())) {
                popupWindowAdapter.setList(item.getChildList());
                popupWindowAdapter.notifyDataSetChanged();
            }
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

    public void setSelectedBgStyle(@ColorInt int bgColorRes, @ColorInt int textColorRes, int radius) {
        this.radius = radius;
        this.selectBgColor = bgColorRes;
        this.selectTextColor = textColorRes;
        popupWindowSelectedAdapter.setSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setUnSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setSelectBgColor(this.selectBgColor);
        popupWindowSelectedAdapter.setUnSelectBgColor(this.selectBgColor);
        popupWindowSelectedAdapter.setRadius(this.radius);
    }

    public void setPadding(float paddingStart, float paddingTop, float paddingEnd, float paddingBottom) {
        this.paddingStart = paddingStart;
        this.paddingTop = paddingTop;
        this.paddingEnd = paddingEnd;
        this.paddingBottom = paddingBottom;
        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
    }

    public void setSelectionTextColor(int selectionTextColor) {
        this.selectionTextColor = selectionTextColor;
        popupWindowAdapter.setTextColor(selectionTextColor);
    }

    public void setCheckedDrawable(Drawable checkedDrawable) {
        this.checkedDrawable = checkedDrawable;
        popupWindowAdapter.setCheckedDrawable(checkedDrawable);
    }

    public void setUncheckedDrawable(Drawable uncheckedDrawable) {
        this.uncheckedDrawable = uncheckedDrawable;
        popupWindowAdapter.setUncheckedDrawable(uncheckedDrawable);
    }

    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
        popupWindowAdapter.setItemHeight(itemHeight);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSelectedChanged(ShapeableImageView ivCheckView, int position) {
        if (!binding.mRecyclerviewSelected.isShown()) {
            binding.mRecyclerviewSelected.setVisibility(View.VISIBLE);
        }
        T selectedItem = popupWindowAdapter.getList().get(position);
        if (PopupWindowBean.containsById(popupWindowSelectedAdapter.getList(), selectedItem.getPopupId())) {
            popupWindowSelectedAdapter.getList().remove(selectedItem);
        } else {
            popupWindowSelectedAdapter.getList().add(selectedItem);
        }
        popupWindowSelectedAdapter.notifyDataSetChanged();
        popupWindowAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSelectedClear(ShapeableImageView ivCheckView, int position) {
        popupWindowSelectedAdapter.getList().remove(position);
        popupWindowSelectedAdapter.notifyDataSetChanged();
        popupWindowAdapter.notifyDataSetChanged();
    }

    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList);
    }
}
