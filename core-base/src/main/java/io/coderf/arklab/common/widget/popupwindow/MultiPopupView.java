package io.coderf.arklab.common.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.PopupMultiViewBinding;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowAdapter;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowMultiAdapter;
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * PopupWindow 多选下拉框（非级联），网格布局样式
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/5
 */
public class MultiPopupView<T extends PopupWindowBean> extends BasePopupWindow
        implements PopupWindowAdapter.OnItemClickListener {

    private final SelectedListener<T> selectedListener;
    private final List<T> dataList;
    private PopupWindowMultiAdapter<T> popupWindowAdapter;
    private int columnCount = 2;
    private float columnPadding;
    private @ColorInt int selectTextColor;
    private @ColorInt int unSelectTextColor;
    private Drawable selectBgDrawable;
    private Drawable unSelectBgDrawable;
    private PopupMultiViewBinding binding;

    public MultiPopupView(Context context, List<T> dataList, SelectedListener<T> selectedListener) {
        super(context);
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        selectTextColor = ThemeAttrs.onPrimary(context);
        unSelectTextColor = ThemeAttrs.onSurface(context);
        selectBgDrawable = DrawableUtil.createShapeDrawable(ThemeAttrs.primary(context),
                DensityUtil.dp2px(context, 5f));
        unSelectBgDrawable = DrawableUtil.createShapeDrawable(
                ContextCompat.getColor(context, R.color.default_background),
                DensityUtil.dp2px(context, 5f));
        columnPadding = DensityUtil.dp2px(context, 8f);
        initView();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initView() {
        binding = inflateWithHostAdapt(
                () -> PopupMultiViewBinding.inflate(layoutInflater, null, false));
        setContentView(binding.getRoot());
        applyMatchParentDropDownSize();
        applyDefaultWindowConfig(binding.getRoot());
        setupMask(binding.maskView);

        binding.buttonClear.setOnClickListener(v -> {
            popupWindowAdapter.getSelected().clear();
            popupWindowAdapter.notifyDataSetChanged();
        });
        binding.buttonSubmit.setOnClickListener(v -> {
            dismiss();
            if (selectedListener != null) {
                selectedListener.onSelectedResult(MultiPopupView.this, popupWindowAdapter.getSelected());
            }
        });

        popupWindowAdapter = new PopupWindowMultiAdapter<>();
        popupWindowAdapter.setSelectBgDrawable(selectBgDrawable);
        popupWindowAdapter.setUnSelectBgDrawable(unSelectBgDrawable);
        popupWindowAdapter.setSelectTextColor(selectTextColor);
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
        popupWindowAdapter.setOnItemClickListener(this);
        popupWindowAdapter.setList(dataList);

        binding.multiRecyclerCategory.setLayoutManager(new GridLayoutManager(context, columnCount));
        binding.multiRecyclerCategory.setAdapter(popupWindowAdapter);
        binding.multiRecyclerCategory.addItemDecoration(
                new GridSpacingItemDecoration((int) columnPadding, 0x00000000));
        measureLayout();
    }

    private void measureLayout() {
        int recyclerHeight = calculateGridRecyclerViewHeight(
                binding.multiRecyclerCategory, R.layout.option_text_view);
        ConstraintLayout.LayoutParams recyclerParams =
                (ConstraintLayout.LayoutParams) binding.multiRecyclerCategory.getLayoutParams();
        ConstraintLayout.LayoutParams buttonParams =
                (ConstraintLayout.LayoutParams) binding.buttonSubmit.getLayoutParams();
        ConstraintLayout.LayoutParams lp =
                (ConstraintLayout.LayoutParams) binding.multiClView.getLayoutParams();
        lp.height = recyclerHeight
                + binding.buttonSubmit.getHeight()
                + recyclerParams.topMargin + recyclerParams.bottomMargin
                + buttonParams.height + buttonParams.topMargin + buttonParams.bottomMargin;
        binding.multiClView.setLayoutParams(lp);
    }

    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
    }

    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        popupWindowAdapter.setSelectTextColor(selectTextColor);
    }

    public void setSubmitTextColor(@ColorInt int textColor) {
        binding.buttonSubmit.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(textColor));
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        binding.multiRecyclerCategory.setLayoutManager(new GridLayoutManager(context, columnCount));
        measureLayout();
    }

    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
        popupWindowAdapter.setUnSelectBgDrawable(unSelectBgDrawable);
    }

    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
        popupWindowAdapter.setSelectBgDrawable(selectBgDrawable);
    }

    public PopupMultiViewBinding getBinding() {
        return binding;
    }

    @Override
    public void onItemClick(View view, int position) {
        T item = popupWindowAdapter.getList().get(position);
        if (popupWindowAdapter.getSelected().contains(item)) {
            popupWindowAdapter.getSelected().remove(item);
        } else {
            popupWindowAdapter.getSelected().add(item);
        }
        popupWindowAdapter.notifyItemChanged(position);
    }

    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList);
    }
}
