package com.casic.otitan.common.widget.popupwindow;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
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
 * updated by fz on 2025/2/13 14:17
 * describe：PopupWindow 下拉框，竖向下拉的PopupWindow
 */
public class PopupView<T extends PopupWindowBean> extends PopupWindow implements PopupWindowAdapter.OnItemClickListener {
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
     * 列表选中项的文字颜色
     */
    private @ColorInt int selectTextColor;

    /**
     * 列表未选中项的文字颜色
     */
    private @ColorInt int unSelectTextColor;

    /**
     * 列表选中项的背景颜色
     */
    private @ColorInt int selectBgColor;

    /**
     * 列表未选中项的背景颜色
     */
    private @ColorInt int unSelectBgColor;

    /**
     * 列表的高度
     */
    private float itemHeight = 0;

    private PopupViewBinding binding;

    public PopupView(Context context, List<T> dataList, SelectedListener<T> selectedListener) {
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        this.context = context;
        itemHeight = DensityUtil.dp2px(context, 40f);
        selectTextColor = ContextCompat.getColor(context, R.color.themeColor);
        unSelectTextColor = ContextCompat.getColor(context, R.color.autoColor);
        selectBgColor = ContextCompat.getColor(context, R.color.default_background);
        unSelectBgColor = ContextCompat.getColor(context, R.color.white);
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

        popupWindowAdapter = new PopupWindowAdapter<>();
        popupWindowAdapter.setItemHeight(itemHeight);
        popupWindowAdapter.setSelectBgColor(selectBgColor);
        popupWindowAdapter.setUnSelectBgColor(unSelectBgColor);
        popupWindowAdapter.setSelectTextColor(selectTextColor);
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
        popupWindowAdapter.setOnItemClickListener(this);
        popupWindowAdapter.setList(dataList);
        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerCategory.setAdapter(popupWindowAdapter);
        binding.recyclerCategory.addItemDecoration(
                new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(context, 1),
                        ContextCompat.getColor(context, R.color.h_line_color)));
        // 延迟测量 RecyclerView 高度
        int totalHeight = calculateRecyclerViewHeight(binding.recyclerCategory);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.recyclerCategory.getLayoutParams();
        lp.height = totalHeight;
        binding.recyclerCategory.setLayoutParams(lp);

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
     * 设置列表项高度
     *
     * @param itemHeight 高度单位px
     */
    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
        popupWindowAdapter.setItemHeight(itemHeight);
    }

    /**
     * 设置列表未选中时的背景颜色
     *
     * @param unSelectBgColor 颜色
     */
    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        this.unSelectBgColor = unSelectBgColor;
        popupWindowAdapter.setUnSelectBgColor(unSelectBgColor);
    }

    /**
     * 列表选中时的背景颜色
     *
     * @param selectBgColor 颜色
     */
    public void setSelectBgColor(@ColorInt int selectBgColor) {
        this.selectBgColor = selectBgColor;
        popupWindowAdapter.setSelectBgColor(selectBgColor);
    }

    /**
     * 列表未选中时的文字颜色
     * @param unSelectTextColor 颜色
     */
    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
    }

    /**
     * 列表选中时的文字颜色
     * @param selectTextColor 颜色
     */
    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        popupWindowAdapter.setSelectTextColor(selectTextColor);
    }

    public PopupViewBinding getBinding() {
        return binding;
    }

    /**
     * 计算recyclerView高度
     *
     * @param recyclerView 列表控件
     * @return 高度
     */
    private int calculateRecyclerViewHeight(RecyclerView recyclerView) {
        // 获取 RecyclerView 的布局管理器
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || recyclerView.getAdapter() == null) {
            return 0;
        }

        // 获取列数（即数据项的数量）
        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) {
            return 0;
        }

        // 获取第一个子项的 View 高度（假设所有子项高度相同）
        View childView = layoutManager.findViewByPosition(0);
        if (childView == null) {
            // 如果子项未渲染，手动测量高度
            childView = LayoutInflater.from(context).inflate(R.layout.option_text_view, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            return childView.getMeasuredHeight() * itemCount;
        }

        int itemHeight = childView.getHeight();

        // 计算分割线高度（如果有）
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(context, 1); // 假设分割线高度为 1dp
        }
        // 总高度 = 列数 × 每列高度 + (列数 - 1) × 分割线高度
        return (itemHeight * itemCount) + (dividerHeight * (itemCount - 1));
    }


    @Override
    public void onItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null) {
            return;
        }
        dismiss();
        if (selectedListener != null) {
            selectedListener.onSelectedResult(PopupView.this, dataList, position);
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
