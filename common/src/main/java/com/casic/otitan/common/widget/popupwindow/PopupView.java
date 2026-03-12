package com.casic.otitan.common.widget.popupwindow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.casic.otitan.common.R;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.PopupViewBinding;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.DrawableUtil;
import com.casic.otitan.common.widget.popupwindow.adapter.PopupWindowAdapter;
import com.casic.otitan.common.widget.recyclerview.RecycleViewDivider;

/**
 * updated by fz on 2026/3/12
 * describe：PopupWindow 下拉框，竖向下拉的PopupWindow（支持单选/多选）
 */
public class PopupView<T extends PopupWindowBean> extends PopupWindow {
    /**
     * 列表选中结果监听
     */
    private final SelectedListener<T> selectedListener;
    /**
     * 列表数据源
     */
    private final List<T> dataList;

    /**
     * 列表适配器
     */
    private PopupWindowAdapter<T> popupWindowAdapter = null;

    /**
     * 上下文
     */
    private final Context context;

    /**
     * 选择模式（单选/多选）- 默认单选保持兼容
     */
    private int selectionMode = PopupWindowAdapter.MODE_SINGLE;

    /**
     * 列表选中项的文字颜色
     */
    private @ColorInt Integer selectTextColor;

    /**
     * 列表未选中项的文字颜色
     */
    private @ColorInt Integer unSelectTextColor;

    /**
     * 列表选中项的背景Drawable
     */
    private Drawable selectBgDrawable;

    /**
     * 列表未选中项的背景Drawable
     */
    private Drawable unSelectBgDrawable;

    /**
     * 列表项的高度（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float itemHeight;

    private PopupViewBinding binding;

    public PopupView(Context context, List<T> dataList, SelectedListener<T> selectedListener) {
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        this.context = context;

        // 设置默认值
        itemHeight = (float) DensityUtil.dp2px(context, 40f);
        selectTextColor = ContextCompat.getColor(context, R.color.themeColor);
        unSelectTextColor = ContextCompat.getColor(context, R.color.autoColor);
        selectBgDrawable = new android.graphics.drawable.ColorDrawable(
                ContextCompat.getColor(context, R.color.default_background));
        unSelectBgDrawable = new android.graphics.drawable.ColorDrawable(
                ContextCompat.getColor(context, R.color.white));

        initView();
    }

    private void initView() {
        binding = PopupViewBinding.inflate(LayoutInflater.from(context), null, false);
        binding.maskView.setOnClickListener(v -> dismiss());
        this.setContentView(binding.getRoot());
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);
        setBackgroundDrawable(DrawableUtil.createShapeDrawable(0x0000000, 0f));

        popupWindowAdapter = new PopupWindowAdapter<>(selectionMode);

        // 设置样式
        popupWindowAdapter.setItemHeight(itemHeight)
                .setBackgroundDrawable(unSelectBgDrawable)
                .setBackgroundSelectedDrawable(selectBgDrawable)
                .setTextColor(unSelectTextColor)
                .setPaddingLeft(DensityUtil.dp2px(context, 12f))
                .setPaddingRight(DensityUtil.dp2px(context, 12f))
                .setTextSelectedColor(selectTextColor)
                .setOnItemClickListener(this::onItemClick);
        popupWindowAdapter.setList(dataList);

        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerCategory.setAdapter(popupWindowAdapter);
        binding.recyclerCategory.addItemDecoration(
                new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(context, 1),
                        ContextCompat.getColor(context, R.color.h_line_color)));

        // 延迟测量 RecyclerView 高度
        binding.getRoot().post(() -> {
            int totalHeight = calculateRecyclerViewHeight(binding.recyclerCategory);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.recyclerCategory.getLayoutParams();
            lp.height = totalHeight;
            binding.recyclerCategory.setLayoutParams(lp);
        });

        // 延迟设置遮罩层高度
        binding.getRoot().post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
            int maskHeight = metrics.heightPixels;
            if (maskHeight > 0) {
                ViewGroup.LayoutParams params = binding.maskView.getLayoutParams();
                params.height = maskHeight;
                binding.maskView.setLayoutParams(params);
            }
        });
    }

    /**
     * 设置选择模式
     * @param selectionMode PopupWindowAdapter.MODE_SINGLE 或 PopupWindowAdapter.MODE_MULTI
     */
    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectionMode(selectionMode);
        }
    }

    /**
     * 设置列表项高度
     * @param itemHeight 高度单位px
     */
    public void setItemHeight(@Dimension(unit = Dimension.PX) float itemHeight) {
        this.itemHeight = itemHeight;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setItemHeight(itemHeight);
        }
    }

    /**
     * 设置未选中项的背景
     */
    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundDrawable(unSelectBgDrawable);
        }
    }

    /**
     * 设置未选中项的背景颜色（自动转换为ColorDrawable）
     */
    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        setUnSelectBgDrawable(new android.graphics.drawable.ColorDrawable(unSelectBgColor));
    }

    /**
     * 设置选中项的背景
     */
    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundSelectedDrawable(selectBgDrawable);
        }
    }

    /**
     * 设置选中项的背景颜色（自动转换为ColorDrawable）
     */
    public void setSelectBgColor(@ColorInt int selectBgColor) {
        setSelectBgDrawable(new android.graphics.drawable.ColorDrawable(selectBgColor));
    }

    /**
     * 设置未选中项的文字颜色
     */
    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextColor(unSelectTextColor);
        }
    }

    /**
     * 设置选中项的文字颜色
     */
    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSelectedColor(selectTextColor);
        }
    }

    /**
     * 设置文字大小（单位：sp）
     */
    public void setTextSizeSp(float textSizeSp) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSizeSp(textSizeSp);
        }
    }

    /**
     * 设置文字对齐方式
     */
    public void setTextGravity(int gravity) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextGravity(gravity);
        }
    }

    /**
     * 设置内边距
     */
    public void setPadding(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setPadding(left, top, right, bottom);
        }
    }

    /**
     * 设置外边距
     */
    public void setMargin(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setMargin(left, top, right, bottom);
        }
    }

    public PopupViewBinding getBinding() {
        return binding;
    }

    /**
     * 计算recyclerView高度
     * @param recyclerView 列表控件
     * @return 高度
     */
    private int calculateRecyclerViewHeight(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || recyclerView.getAdapter() == null) {
            return 0;
        }

        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) {
            return 0;
        }

        View childView = layoutManager.findViewByPosition(0);
        if (childView == null) {
            childView = LayoutInflater.from(context).inflate(R.layout.option_text_view, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            return childView.getMeasuredHeight() * itemCount;
        }

        int itemHeight = childView.getHeight();
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(context, 1);
        }
        return (itemHeight * itemCount) + (dividerHeight * (itemCount - 1));
    }

    private void onItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null) {
            return;
        }

        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            // 单选模式：点击后关闭
            dismiss();
        }
        // 多选模式：不关闭窗口，只更新选中状态

        if (selectedListener != null) {
            selectedListener.onSelectedResult(PopupView.this, dataList, position);
        }
    }

    /**
     * 获取选中的项（单选模式）
     */
    public T getSelectedItem() {
        if (popupWindowAdapter != null) {
            return popupWindowAdapter.getSelectedItem();
        }
        return null;
    }

    /**
     * 获取所有选中的项（多选模式）
     */
    public List<T> getSelectedItems() {
        if (popupWindowAdapter != null) {
            return popupWindowAdapter.getSelectedItems();
        }
        return new java.util.ArrayList<>();
    }

    /**
     * 清除所有选中
     */
    public void clearSelection() {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.clearSelection();
        }
    }

    /**
     * 设置选中项（单选模式）
     */
    public void setSelectedItem(T item) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectedItem(item);
        }
    }

    /**
     * 设置选中项（多选模式）
     */
    public void setSelectedItems(List<T> items) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectedItems(items);
        }
    }

    /**
     * 选择成功回调
     * 把选中的下标通过方法回调回来
     */
    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList, Integer selectedPosition);
    }
}