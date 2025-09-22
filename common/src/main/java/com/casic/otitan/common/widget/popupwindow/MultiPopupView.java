package com.casic.otitan.common.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.casic.otitan.common.R;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.PopupMultiViewBinding;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.DrawableUtil;
import com.casic.otitan.common.widget.popupwindow.adapter.PopupWindowAdapter;
import com.casic.otitan.common.widget.popupwindow.adapter.PopupWindowMultiAdapter;
import com.casic.otitan.common.widget.recyclerview.GridSpacingItemDecoration;

/**
 * updated by fz on 2025/2/13 14:17
 * describe：PopupWindow 多选下拉框（不是级联）
 */
public class MultiPopupView<T extends PopupWindowBean> extends PopupWindow implements PopupWindowAdapter.OnItemClickListener {
    private final SelectedListener<T> selectedListener;
    private final List<T> dataList;

    private PopupWindowMultiAdapter<T> popupWindowAdapter = null;

    private final Context context;
    /**
     * 网格每行显示的列数
     */
    private int columnCount = 2;
    /**
     * 列表间距
     */
    private float columnPadding = 0f;
    /**
     * 选中文字颜色
     */
    private @ColorInt int selectTextColor;
    /**
     * 未选中文字颜色
     */
    private @ColorInt int unSelectTextColor;

    /**
     * 选择按钮背景样式
     */
    private Drawable selectBgDrawable;
    /**
     * 未选中按钮背景样式
     */
    private Drawable unSelectBgDrawable;

    private PopupMultiViewBinding binding;

    public MultiPopupView(Context context, List<T> dataList, SelectedListener<T> selectedListener) {
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        this.context = context;
        selectTextColor = ContextCompat.getColor(context, R.color.white);
        unSelectTextColor = ContextCompat.getColor(context, R.color.autoColor);
        selectBgDrawable = DrawableUtil.createShapeDrawable(ContextCompat.getColor(context, R.color.themeColor),
                DensityUtil.dp2px(context, 5f));
        unSelectBgDrawable = DrawableUtil.createShapeDrawable(ContextCompat.getColor(context, R.color.default_background),
                DensityUtil.dp2px(context, 5f));
        columnPadding = DensityUtil.dp2px(context, 8f);
        initView();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initView() {
        binding = PopupMultiViewBinding.inflate(LayoutInflater.from(context), null, false);
        binding.maskView.setOnClickListener(v -> dismiss());

        binding.buttonClear.setOnClickListener(v -> {
            popupWindowAdapter.getSelected().clear();
            popupWindowAdapter.notifyDataSetChanged();
        });
        binding.buttonSubmit.setOnClickListener(v -> {
            dismiss();
            if (selectedListener == null) {
                return;
            }
            selectedListener.onSelectedResult(MultiPopupView.this, popupWindowAdapter.getSelected());
        });

        this.setContentView(binding.getRoot());
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);
        setBackgroundDrawable(DrawableUtil.createShapeDrawable(0x0000000, 0f));

        popupWindowAdapter = new PopupWindowMultiAdapter<>();

        popupWindowAdapter.setSelectBgDrawable(selectBgDrawable);
        popupWindowAdapter.setUnSelectBgDrawable(unSelectBgDrawable);
        popupWindowAdapter.setSelectTextColor(selectTextColor);
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
        popupWindowAdapter.setOnItemClickListener(this);
        popupWindowAdapter.setList(dataList);
        binding.multiRecyclerCategory.setLayoutManager(new GridLayoutManager(context, columnCount));
        binding.multiRecyclerCategory.setAdapter(popupWindowAdapter);
        binding.multiRecyclerCategory.addItemDecoration(new GridSpacingItemDecoration(
                (int) columnPadding, 0x00000000
        ));
        measureLayout();
    }

    private void measureLayout() {
        // 延迟测量 RecyclerView 高度
        int recyclerHeight = calculateRecyclerViewHeight(binding.multiRecyclerCategory);
        ConstraintLayout.LayoutParams recyclerParams = (ConstraintLayout.LayoutParams) binding.multiRecyclerCategory.getLayoutParams();
        ConstraintLayout.LayoutParams buttonParams = (ConstraintLayout.LayoutParams) binding.buttonSubmit.getLayoutParams();
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.multiClView.getLayoutParams();
        lp.height = recyclerHeight + binding.buttonSubmit.getHeight() + recyclerParams.topMargin + recyclerParams.bottomMargin + buttonParams.height + buttonParams.topMargin + buttonParams.bottomMargin;
        binding.multiClView.setLayoutParams(lp);

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
     * 设置未选中文字颜色
     * @param unSelectTextColor 未选文字颜色
     */
    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
    }

    /**
     * 设置选中文字颜色
     * @param selectTextColor 选中文字颜色
     */
    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        popupWindowAdapter.setSelectTextColor(selectTextColor);
    }

    /**
     * 设置提交按钮文字颜色
     * @param textColor 颜色
     */
    public void setSubmitTextColor(@ColorInt int textColor) {
        binding.buttonSubmit.setBackColor(textColor);
    }

    /**
     * 设置每行显示列数
     * @param columnCount 每行显示列数
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        binding.multiRecyclerCategory.setLayoutManager(new GridLayoutManager(context, columnCount));
        measureLayout();
    }

    /**
     * 设置未选中背景
     * @param unSelectBgDrawable 未选中背景
     */
    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
        popupWindowAdapter.setUnSelectBgDrawable(unSelectBgDrawable);
    }

    /**
     * 获取选中背景
     * @param selectBgDrawable 选中背景
     */
    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
        popupWindowAdapter.setSelectBgDrawable(selectBgDrawable);
    }

    public PopupMultiViewBinding getBinding() {
        return binding;
    }

    /**
     * 计算recyclerView高度
     *
     * @param recyclerView 列表控件
     * @return 高度
     */
    private int calculateRecyclerViewHeight(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || recyclerView.getAdapter() == null) {
            return 0;
        }

        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        // 获取列数
        int spanCount = layoutManager.getSpanCount();
        // 计算行数
        int rowCount = (int) Math.ceil((double) itemCount / spanCount);

        // 获取第一个子项的View高度（假设所有子项高度相同）
        View childView = layoutManager.findViewByPosition(0);
        if (childView == null) {
            // 如果子项未渲染，手动测量高度
            childView = LayoutInflater.from(context).inflate(R.layout.option_text_view, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth() / spanCount, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            return childView.getMeasuredHeight() * rowCount;
        }

        int itemHeight = childView.getHeight();

        // 计算分割线高度（如果有）
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(context, 1); // 假设分割线高度为 1dp
        }

        // 总高度 = 行数 × 每行高度 + (行数 - 1) × 分割线高度
        return (itemHeight * rowCount) + (dividerHeight * (rowCount - 1));
    }


    @Override
    public void onItemClick(View view, int position) {
        if (popupWindowAdapter.getSelected().contains(popupWindowAdapter.getList().get(position))) {
            popupWindowAdapter.getSelected().remove(popupWindowAdapter.getList().get(position));
        } else {
            popupWindowAdapter.getSelected().add(popupWindowAdapter.getList().get(position));
        }
        popupWindowAdapter.notifyItemChanged(position);
    }


    /**
     * 选择成功回调
     * 把选中的下标通过方法回调回来
     */
    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList);
    }

}
