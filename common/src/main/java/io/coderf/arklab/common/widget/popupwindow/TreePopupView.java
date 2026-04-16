package io.coderf.arklab.common.widget.popupwindow;

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

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.PopupCascadeViewBinding;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowAdapter;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;

/**
 * updated by fz on 2026/3/12
 * describe：PopupWindow 下拉框，树结构的PopupWindow（支持单选/多选）
 */
public class TreePopupView<T extends PopupWindowBean> extends PopupWindow {
    /**
     * 选择监听
     */
    private final SelectedListener<T> selectedListener;
    /**
     * 数据源
     */
    private final List<T> dataList;

    /**
     * 父级适配器
     */
    private PopupWindowAdapter<T> popupWindowAdapter = null;
    /**
     * 子级适配器
     */
    private PopupWindowAdapter<T> childAdapter = null;

    /**
     * 当前选中的父级位置、子集位置，可能为null
     */
    private Integer parentPosition, childPosition;
    /**
     * 上下文
     */
    private final Context activity;
    /**
     * 是否有右侧按钮，也就是是否有子集
     */
    private final boolean hasRight;

    /**
     * 选择模式（单选/多选）- 默认单选保持兼容
     */
    private int selectionMode = PopupWindowAdapter.MODE_SINGLE;

    /**
     * 选中项的文字颜色
     */
    private @ColorInt Integer selectTextColor;

    /**
     * 未选中项的文字颜色
     */
    private @ColorInt Integer unSelectTextColor;

    /**
     * 选中项的背景Drawable
     */
    private Drawable selectBgDrawable;

    /**
     * 未选中项的背景Drawable
     */
    private Drawable unSelectBgDrawable;

    /**
     * 列表项的高度（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float itemHeight;

    private PopupCascadeViewBinding binding;

    public TreePopupView(Context activity, List<T> dataList, boolean hasRight, SelectedListener<T> selectedListener) {
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        this.activity = activity;
        this.hasRight = hasRight;

        // 设置默认值
        itemHeight = (float) DensityUtil.dp2px(activity, 40f);
        selectTextColor = ContextCompat.getColor(activity, R.color.themeColor);
        unSelectTextColor = ContextCompat.getColor(activity, R.color.autoColor);
        selectBgDrawable = new android.graphics.drawable.ColorDrawable(
                ContextCompat.getColor(activity, R.color.default_background));
        unSelectBgDrawable = new android.graphics.drawable.ColorDrawable(
                ContextCompat.getColor(activity, R.color.white));

        init();
        initParent();
        binding.childrenCategory.setVisibility(hasRight ? View.VISIBLE : View.GONE);
    }

    private void init() {
        binding = PopupCascadeViewBinding.inflate(LayoutInflater.from(activity), null, false);
        binding.maskView.setOnClickListener(v -> dismiss());
        this.setContentView(binding.getRoot());
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);
        setBackgroundDrawable(DrawableUtil.createShapeDrawable(0x0000000, 0f));
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
        if (childAdapter != null) {
            childAdapter.setSelectionMode(selectionMode);
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
        if (childAdapter != null) {
            childAdapter.setItemHeight(itemHeight);
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
        if (childAdapter != null) {
            childAdapter.setBackgroundDrawable(unSelectBgDrawable);
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
        if (childAdapter != null) {
            childAdapter.setBackgroundSelectedDrawable(selectBgDrawable);
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
        if (childAdapter != null) {
            childAdapter.setTextColor(unSelectTextColor);
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
        if (childAdapter != null) {
            childAdapter.setTextSelectedColor(selectTextColor);
        }
    }

    /**
     * 设置文字大小（单位：sp）
     */
    public void setTextSizeSp(float textSizeSp) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSizeSp(textSizeSp);
        }
        if (childAdapter != null) {
            childAdapter.setTextSizeSp(textSizeSp);
        }
    }

    /**
     * 设置文字对齐方式
     */
    public void setTextGravity(int gravity) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextGravity(gravity);
        }
        if (childAdapter != null) {
            childAdapter.setTextGravity(gravity);
        }
    }

    /**
     * 设置内边距
     */
    public void setPadding(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setPadding(left, top, right, bottom);
        }
        if (childAdapter != null) {
            childAdapter.setPadding(left, top, right, bottom);
        }
    }

    /**
     * 设置外边距
     */
    public void setMargin(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setMargin(left, top, right, bottom);
        }
        if (childAdapter != null) {
            childAdapter.setMargin(left, top, right, bottom);
        }
    }

    public PopupCascadeViewBinding getBinding() {
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
            childView = LayoutInflater.from(activity).inflate(R.layout.option_text_view, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            return childView.getMeasuredHeight() * itemCount;
        }

        int itemHeight = childView.getHeight();
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(activity, 1);
        }
        return (itemHeight * itemCount) + (dividerHeight * (itemCount - 1));
    }

    private void initParent() {
        popupWindowAdapter = new PopupWindowAdapter<>(selectionMode);

        // 设置样式
        popupWindowAdapter.setItemHeight(itemHeight)
                .setBackgroundDrawable(unSelectBgDrawable)
                .setBackgroundSelectedDrawable(selectBgDrawable)
                .setPaddingLeft(DensityUtil.dp2px(activity, 12f))
                .setPaddingRight(DensityUtil.dp2px(activity, 12f))
                .setTextColor(unSelectTextColor)
                .setTextSelectedColor(selectTextColor)
                .setOnItemClickListener(this::onParentItemClick);
        popupWindowAdapter.setList(dataList);

        binding.parentCategory.setLayoutManager(new LinearLayoutManager(activity));
        binding.parentCategory.setAdapter(popupWindowAdapter);
        binding.parentCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(activity, 1),
                        ContextCompat.getColor(activity, R.color.h_line_color)));

        // 延迟测量 RecyclerView 高度
        binding.getRoot().post(() -> {
            int totalHeight = calculateRecyclerViewHeight(binding.parentCategory);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.contentLayout.getLayoutParams();
            lp.height = totalHeight;
            binding.contentLayout.setLayoutParams(lp);
        });

        // 延迟设置遮罩层高度
        binding.getRoot().post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
            int maskHeight = metrics.heightPixels;
            if (maskHeight > 0) {
                ViewGroup.LayoutParams params = binding.maskView.getLayoutParams();
                params.height = maskHeight;
                binding.maskView.setLayoutParams(params);
            }
        });
    }

    private void initChild(List<T> childLists) {
        if (childAdapter != null) {
            childAdapter.setList(childLists);
            childAdapter.notifyDataSetChanged();
            return;
        }

        childAdapter = new PopupWindowAdapter<>(selectionMode);
        childAdapter.setItemHeight(itemHeight)
                .setBackgroundDrawable(unSelectBgDrawable)
                .setBackgroundSelectedDrawable(selectBgDrawable)
                .setPaddingLeft(DensityUtil.dp2px(activity, 12f))
                .setPaddingRight(DensityUtil.dp2px(activity, 12f))
                .setTextColor(unSelectTextColor)
                .setTextSelectedColor(selectTextColor)
                .setOnItemClickListener(this::onChildItemClick);
        childAdapter.setList(childLists);

        binding.childrenCategory.setLayoutManager(new LinearLayoutManager(activity));
        binding.childrenCategory.setAdapter(childAdapter);
        binding.childrenCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(activity, 1),
                        ContextCompat.getColor(activity, R.color.h_line_color)));
    }

    private void onParentItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null) {
            return;
        }

        parentPosition = position;

        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            // 单选模式：如果不需要子级或者点击的是同一项，则关闭
            if (!hasRight || (hasRight && dataList.get(position).getChildList() == null)) {
                dismiss();
                if (selectedListener != null) {
                    selectedListener.onSelectedResult(TreePopupView.this, dataList, parentPosition, childPosition);
                }
            }
        } else {
            // 多选模式：点击父级不关闭窗口，只更新子级
            if (hasRight) {
                initChild(dataList.get(position).getChildList());
            }
        }

        if (hasRight) {
            initChild(dataList.get(position).getChildList());
        }
    }

    private void onChildItemClick(View view, int position) {
        if (dataList == null || dataList.size() <= parentPosition ||
                dataList.get(parentPosition).getChildList().size() <= position) {
            return;
        }

        childPosition = position;

        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            // 单选模式：点击子项后关闭
            dismiss();
            if (selectedListener != null) {
                selectedListener.onSelectedResult(TreePopupView.this, dataList, parentPosition, childPosition);
            }
        } else {
            // 多选模式：只更新选中状态，不关闭窗口
            if (selectedListener != null) {
                selectedListener.onSelectedResult(TreePopupView.this, dataList, parentPosition, childPosition);
            }
        }
    }

    /**
     * 获取选中的父级项（单选模式）
     */
    public T getSelectedParentItem() {
        if (parentPosition != null && parentPosition >= 0 && parentPosition < dataList.size()) {
            return dataList.get(parentPosition);
        }
        return null;
    }

    /**
     * 获取选中的子级项（单选模式）
     */
    public T getSelectedChildItem() {
        if (parentPosition != null && childPosition != null &&
                parentPosition >= 0 && parentPosition < dataList.size() &&
                dataList.get(parentPosition).getChildList() != null &&
                childPosition >= 0 && childPosition < dataList.get(parentPosition).getChildList().size()) {
            return (T) dataList.get(parentPosition).getChildList().get(childPosition);
        }
        return null;
    }

    /**
     * 获取所有选中的父级项（多选模式）
     */
    public List<T> getSelectedParentItems() {
        if (popupWindowAdapter != null) {
            return popupWindowAdapter.getSelectedItems();
        }
        return new java.util.ArrayList<>();
    }

    /**
     * 获取所有选中的子级项（多选模式）
     */
    public List<T> getSelectedChildItems() {
        if (childAdapter != null) {
            return childAdapter.getSelectedItems();
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
        if (childAdapter != null) {
            childAdapter.clearSelection();
        }
        parentPosition = null;
        childPosition = null;
    }

    /**
     * 选择成功回调
     * 把选中的下标通过方法回调回来
     */
    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList, Integer parentSelectPosition, Integer childrenSelectPosition);
    }
}