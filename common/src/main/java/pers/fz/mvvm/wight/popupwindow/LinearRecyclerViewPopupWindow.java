package pers.fz.mvvm.wight.popupwindow;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.listener.OnHeaderViewClickListener;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * PopupWindow 下拉框
 *
 * @author fz
 */
public class LinearRecyclerViewPopupWindow<T extends PopupWindowBean> extends PopupWindow implements PopupWindowAdapter.OnItemClickListener {
    private final SelectCategory<T> selectCategory;
    private final List<T> popupWindowBeanList;

    private RecyclerView lvParentCategory = null;
    private RecyclerView lvChildrenCategory = null;
    private PopupWindowAdapter<T> popupWindowAdapter = null;
    private PopupWindowAdapter<T> childAdapter = null;
    private final PopupWindow popupWindow;

    private Integer parentPosition, childPosition;
    private final Context activity;
    private final boolean hasRight;
    private OnHeadViewClickListener onHeadViewClickListener;

    /**
     * 构造器
     */
    public LinearRecyclerViewPopupWindow(List<T> popupWindowBeanList,
                                         Context activity, boolean hasRight,
                                         SelectCategory<T> selectCategory) {

        this.selectCategory = selectCategory;
        this.popupWindowBeanList = popupWindowBeanList;
        this.activity = activity;
        this.hasRight = hasRight;
        popupWindow = this;
        init();
        initParent();
        if (!hasRight) {
            lvChildrenCategory.setVisibility(View.GONE);
        }
    }

    private void init() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.layout_quyu_choose_view, null);
        DisplayMetrics appDisplayMetrics = activity.getResources().getDisplayMetrics();

        this.setContentView(contentView);
        this.setWidth(appDisplayMetrics.widthPixels);
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        contentView.setFocusableInTouchMode(true);
        setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.pop_bg));
        lvChildrenCategory = contentView.findViewById(R.id.lv_children_category);
        lvParentCategory = contentView.findViewById(R.id.lv_parent_category);
    }

    private void initParent() {
        popupWindowAdapter = new PopupWindowAdapter<>();
        popupWindowAdapter.setOnItemClickListener(this);
        popupWindowAdapter.setList(popupWindowBeanList);
        lvParentCategory.setLayoutManager(new LinearLayoutManager(activity));
        lvParentCategory.setAdapter(popupWindowAdapter);
        lvParentCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, 1,
                        ContextCompat.getColor(activity, R.color.h_line_color)));
        popupWindowAdapter.setHasHeader(true);
        popupWindowAdapter.setOnHeaderViewClickListener(new OnHeaderViewClickListener() {
            @Override
            public void onHeaderViewClick(View view) {
                ((TextView) view.findViewById(R.id.tv_parent_category_name)).setTextColor(ContextCompat.getColor(activity, R.color.themeColor));
                popupWindowAdapter.setSelectedPosition(-1);
                dismiss();
                if (onHeadViewClickListener != null) {
                    onHeadViewClickListener.onHeadViewClick(LinearRecyclerViewPopupWindow.this, true);
                }
            }

            @Override
            public void onHeaderViewLongClick(View view) {

            }
        });
    }

    private void initChild(List<T> childLists) {
        childAdapter = new PopupWindowAdapter<>();
        childAdapter.setOnItemClickListener(childOnItemClickListener);
        childAdapter.setList(childLists);
        lvChildrenCategory.setLayoutManager(new LinearLayoutManager(activity));
        lvChildrenCategory.setAdapter(childAdapter);
        lvChildrenCategory.addItemDecoration(
                new RecycleViewDivider(activity, LinearLayoutManager.HORIZONTAL, 1,
                        ContextCompat.getColor(activity, R.color.h_line_color)));

        childAdapter.setHasHeader(true);
        childAdapter.setOnHeaderViewClickListener(new OnHeaderViewClickListener() {
            @Override
            public void onHeaderViewClick(View view) {
                ((TextView) view.findViewById(R.id.tv_parent_category_name)).setTextColor(ContextCompat.getColor(activity, R.color.themeColor));
                popupWindowAdapter.setSelectedPosition(-1);
                dismiss();
                if (onHeadViewClickListener != null) {
                    onHeadViewClickListener.onHeadViewClick(LinearRecyclerViewPopupWindow.this, false);
                }
            }

            @Override
            public void onHeaderViewLongClick(View view) {

            }
        });
    }
    @Override
    public void onItemClick(View view, int position) {
        if (popupWindowBeanList == null || popupWindowAdapter == null) {
            return;
        }
        if (popupWindowAdapter.getHeaderView() != null) {
            ViewHold viewHold = (ViewHold) popupWindowAdapter.getHeaderView().getTag();
            viewHold.textView.setTextColor(ContextCompat.getColor(activity, R.color.black));
        }

        if ((parentPosition != null && parentPosition == position) || !hasRight) {
            dismiss();
            if (selectCategory != null) {
                selectCategory.selectCategory(popupWindow, popupWindowBeanList, position, childPosition);
            }
        }
        parentPosition = position;
        if (popupWindowAdapter.getHeaderView() != null) {
            ViewHold viewHold = (ViewHold) popupWindowAdapter.getHeaderView().getTag();
            viewHold.textView.setTextColor(ContextCompat.getColor(activity, R.color.black));
        }
        popupWindowAdapter.setSelectedPosition(position);
        if (hasRight) {
            initChild(popupWindowBeanList.get(position).getChildList());
        }
    }

    private final BaseRecyclerViewAdapter.OnItemClickListener childOnItemClickListener = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (popupWindowBeanList == null || popupWindowBeanList.size() <= parentPosition ||
                    popupWindowBeanList.get(parentPosition).getChildList().size() <= position) {
                return;
            }
            childPosition = position;
            ViewHold viewHold = (ViewHold) childAdapter.getHeaderView().getTag();
            viewHold.textView.setTextColor(ContextCompat.getColor(activity, R.color.black));
            childAdapter.setSelectedPosition(position);
            dismiss();
            if (selectCategory != null) {
                selectCategory.selectCategory(popupWindow, popupWindowBeanList, parentPosition, childPosition);
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

    public void setHeadViewClickListener(OnHeadViewClickListener onHeadViewClickListener) {
        this.onHeadViewClickListener = onHeadViewClickListener;
    }

    public interface OnHeadViewClickListener {
        void onHeadViewClick(PopupWindow popupWindow, boolean isParentPosition);
    }

    static class ViewHold {
        TextView textView;
    }
}
