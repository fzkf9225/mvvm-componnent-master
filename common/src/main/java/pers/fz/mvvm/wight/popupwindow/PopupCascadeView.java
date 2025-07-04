package pers.fz.mvvm.wight.popupwindow;

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

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.PopupCascadeViewBinding;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * updated by fz on 2025/2/13 14:17
 * describe：PopupWindow 下拉框
 */
public class PopupCascadeView<T extends PopupWindowBean> extends PopupWindow implements PopupWindowAdapter.OnItemClickListener {
    private final SelectCategory<T> selectCategory;
    private final List<T> dataList;

    private PopupWindowAdapter<T> popupWindowAdapter = null;
    private PopupWindowAdapter<T> childAdapter = null;

    private Integer parentPosition, childPosition;
    private final Context activity;
    private final boolean hasRight;


    private @ColorInt int selectTextColor;

    private @ColorInt int unSelectTextColor;

    private @ColorInt int selectBgColor;

    private @ColorInt int unSelectBgColor;

    /**
     * 列表的高度
     */
    private float itemHeight = 0;

    private PopupCascadeViewBinding binding;

    public PopupCascadeView(Context activity,List<T> dataList, boolean hasRight, SelectCategory<T> selectCategory) {
        this.selectCategory = selectCategory;
        this.dataList = dataList;
        this.activity = activity;
        this.hasRight = hasRight;
        itemHeight = DensityUtil.dp2px(activity, 40f);
        selectTextColor = ContextCompat.getColor(activity, R.color.themeColor);
        unSelectTextColor = ContextCompat.getColor(activity, R.color.autoColor);
        selectBgColor = ContextCompat.getColor(activity, R.color.default_background);
        unSelectBgColor = ContextCompat.getColor(activity, R.color.white);
        init();
        initParent();
        binding.childrenCategory.setVisibility(hasRight ? View.VISIBLE : View.GONE);
    }

    private void init() {
        binding = PopupCascadeViewBinding.inflate(LayoutInflater.from(activity), null, false);
        binding.maskView.setOnClickListener(v-> dismiss());
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
     * 设置列表项高度
     * @param itemHeight 高度单位px
     */
    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
        popupWindowAdapter.setItemHeight(itemHeight);
    }

    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        this.unSelectBgColor = unSelectBgColor;
        popupWindowAdapter.setUnSelectBgColor(unSelectBgColor);
    }

    public void setSelectBgColor(@ColorInt int selectBgColor) {
        this.selectBgColor = selectBgColor;
        popupWindowAdapter.setSelectBgColor(selectBgColor);
    }

    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
    }

    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        popupWindowAdapter.setSelectTextColor(selectTextColor);
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
            childView = LayoutInflater.from(activity).inflate(R.layout.option_text_view, recyclerView, false);
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
            dividerHeight = DensityUtil.dp2px(activity, 1); // 假设分割线高度为 1dp
        }
        // 总高度 = 列数 × 每列高度 + (列数 - 1) × 分割线高度
        return (itemHeight * itemCount) + (dividerHeight * (itemCount - 1));
    }

    private void initParent() {
        popupWindowAdapter = new PopupWindowAdapter<>();
        popupWindowAdapter.setItemHeight(itemHeight);
        popupWindowAdapter.setSelectBgColor(selectBgColor);
        popupWindowAdapter.setUnSelectBgColor(unSelectBgColor);
        popupWindowAdapter.setSelectTextColor(selectTextColor);
        popupWindowAdapter.setUnSelectTextColor(unSelectTextColor);
        popupWindowAdapter.setOnItemClickListener(this);
        popupWindowAdapter.setList(dataList);
        binding.parentCategory.setLayoutManager(new LinearLayoutManager(activity));
        binding.parentCategory.setAdapter(popupWindowAdapter);
        binding.parentCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(activity, 1),
                        ContextCompat.getColor(activity, R.color.h_line_color)));
        // 延迟测量 RecyclerView 高度
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.contentLayout.getLayoutParams();
        lp.height = calculateRecyclerViewHeight(binding.parentCategory);
        binding.contentLayout.setLayoutParams(lp);
        // 延迟设置遮罩层高度
        binding.getRoot().post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE))
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
        childAdapter = new PopupWindowAdapter<>();
        childAdapter.setItemHeight(itemHeight);
        childAdapter.setSelectBgColor(selectBgColor);
        childAdapter.setUnSelectBgColor(unSelectBgColor);
        childAdapter.setSelectTextColor(selectTextColor);
        childAdapter.setUnSelectTextColor(unSelectTextColor);
        childAdapter.setOnItemClickListener(childOnItemClickListener);
        childAdapter.setList(childLists);
        binding.childrenCategory.setLayoutManager(new LinearLayoutManager(activity));
        binding.childrenCategory.setAdapter(childAdapter);
        binding.childrenCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(activity, 1),
                        ContextCompat.getColor(activity, R.color.h_line_color)));
    }

    @Override
    public void onItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null) {
            return;
        }
        if ((parentPosition != null && parentPosition == position) || !hasRight) {
            dismiss();
            if (selectCategory != null) {
                selectCategory.selectCategory(PopupCascadeView.this, dataList, position, childPosition);
            }
        }
        parentPosition = position;
        popupWindowAdapter.setSelectedPosition(position);
        if (hasRight) {
            initChild(dataList.get(position).getChildList());
        }
    }

    private final BaseRecyclerViewAdapter.OnItemClickListener childOnItemClickListener = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (dataList == null || dataList.size() <= parentPosition ||
                    dataList.get(parentPosition).getChildList().size() <= position) {
                return;
            }
            childPosition = position;
            childAdapter.setSelectedPosition(position);
            dismiss();
            if (selectCategory != null) {
                selectCategory.selectCategory(PopupCascadeView.this, dataList, parentPosition, childPosition);
            }
        }
    };

    /**
     * 选择成功回调
     * 把选中的下标通过方法回调回来
     */
    public interface SelectCategory<T> {
        void selectCategory(PopupWindow popupWindow, List<T> dataList, Integer parentSelectPosition, Integer childrenSelectPosition);
    }

}
