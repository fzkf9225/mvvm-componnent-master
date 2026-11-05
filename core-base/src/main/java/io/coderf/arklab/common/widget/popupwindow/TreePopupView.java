package io.coderf.arklab.common.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.PopupCascadeViewBinding;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.popupwindow.adapter.PopupWindowAdapter;
import io.coderf.arklab.core.utils.CollectionUtil;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * PopupWindow 下拉框，树结构（支持单选/多选）
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/11/5
 */
public class TreePopupView<T extends PopupWindowBean> extends BasePopupWindow {

    private final SelectedListener<T> selectedListener;
    private final List<T> dataList;
    private PopupWindowAdapter<T> popupWindowAdapter;
    private PopupWindowAdapter<T> childAdapter;
    private Integer parentPosition;
    private Integer childPosition;
    private final boolean hasRight;
    private int selectionMode = PopupWindowAdapter.MODE_SINGLE;
    private @ColorInt Integer selectTextColor;
    private @ColorInt Integer unSelectTextColor;
    private Drawable selectBgDrawable;
    private Drawable unSelectBgDrawable;
    private @Dimension(unit = Dimension.PX) Float itemHeight;
    private PopupCascadeViewBinding binding;

    public TreePopupView(Context activity, List<T> dataList, boolean hasRight,
                         SelectedListener<T> selectedListener) {
        super(activity);
        this.selectedListener = selectedListener;
        this.dataList = dataList;
        this.hasRight = hasRight;

        itemHeight = (float) DensityUtil.dp2px(activity, 44f);
        selectTextColor = ThemeAttrs.primary(activity);
        unSelectTextColor = ThemeAttrs.onSurfaceVariant(activity);
        // 整条 item 选中/未选中（无圆角）+ 按下波纹
        selectBgDrawable = createRowBackground(true);
        unSelectBgDrawable = createRowBackground(false);

        init();
        initParent();
        binding.childrenCategory.setVisibility(hasRight ? View.VISIBLE : View.GONE);
    }

    private void init() {
        binding = inflateWithHostAdapt(
                () -> PopupCascadeViewBinding.inflate(layoutInflater, null, false));
        setContentView(binding.getRoot());
        applyMatchParentDropDownSize();
        applyDefaultWindowConfig(binding.getRoot());
        setupMask(binding.maskView);
    }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setSelectionMode(selectionMode);
        }
        if (childAdapter != null) {
            childAdapter.setSelectionMode(selectionMode);
        }
    }

    public void setItemHeight(@Dimension(unit = Dimension.PX) float itemHeight) {
        this.itemHeight = itemHeight;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setItemHeight(itemHeight);
        }
        if (childAdapter != null) {
            childAdapter.setItemHeight(itemHeight);
        }
    }

    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundDrawable(unSelectBgDrawable);
        }
        if (childAdapter != null) {
            childAdapter.setBackgroundDrawable(unSelectBgDrawable);
        }
    }

    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        setUnSelectBgDrawable(new ColorDrawable(unSelectBgColor));
    }

    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setBackgroundSelectedDrawable(selectBgDrawable);
        }
        if (childAdapter != null) {
            childAdapter.setBackgroundSelectedDrawable(selectBgDrawable);
        }
    }

    public void setSelectBgColor(@ColorInt int selectBgColor) {
        setSelectBgDrawable(new ColorDrawable(selectBgColor));
    }

    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextColor(unSelectTextColor);
        }
        if (childAdapter != null) {
            childAdapter.setTextColor(unSelectTextColor);
        }
    }

    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSelectedColor(selectTextColor);
        }
        if (childAdapter != null) {
            childAdapter.setTextSelectedColor(selectTextColor);
        }
    }

    public void setTextSizeSp(float textSizeSp) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextSizeSp(textSizeSp);
        }
        if (childAdapter != null) {
            childAdapter.setTextSizeSp(textSizeSp);
        }
    }

    public void setTextGravity(int gravity) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setTextGravity(gravity);
        }
        if (childAdapter != null) {
            childAdapter.setTextGravity(gravity);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if (popupWindowAdapter != null) {
            popupWindowAdapter.setPadding(left, top, right, bottom);
        }
        if (childAdapter != null) {
            childAdapter.setPadding(left, top, right, bottom);
        }
    }

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

    private void initParent() {
        popupWindowAdapter = new PopupWindowAdapter<>(selectionMode);
        applyItemChrome(popupWindowAdapter);
        popupWindowAdapter.setOnItemClickListener(this::onParentItemClick);
        popupWindowAdapter.setList(dataList);

        binding.parentCategory.setLayoutManager(new LinearLayoutManager(context));
        binding.parentCategory.setAdapter(popupWindowAdapter);
        // 不再加分割线，靠栏底色 + 圆角选中态区分

        binding.getRoot().post(() -> {
            int totalHeight = calculateLinearRecyclerViewHeight(
                    binding.parentCategory, R.layout.option_text_view);
            // 上下 padding 已计入视觉，略加一点避免贴边
            totalHeight += DensityUtil.dp2px(context, 8f);
            ConstraintLayout.LayoutParams lp =
                    (ConstraintLayout.LayoutParams) binding.contentLayout.getLayoutParams();
            lp.height = totalHeight;
            binding.contentLayout.setLayoutParams(lp);

            // 有右栏时默认展开第一项，联动更直观
            if (hasRight && CollectionUtil.isNotEmpty(dataList)) {
                selectParentIndex(0, false);
                initChild(safeChildList(dataList.get(0)));
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initChild(List<T> childLists) {
        List<T> safeList = childLists != null ? childLists : new ArrayList<>();
        if (childAdapter != null) {
            childAdapter.clearSelection();
            childAdapter.setList(safeList);
            childAdapter.notifyDataSetChanged();
            binding.childrenCategory.scrollToPosition(0);
            return;
        }

        childAdapter = new PopupWindowAdapter<>(selectionMode);
        applyItemChrome(childAdapter);
        childAdapter.setOnItemClickListener(this::onChildItemClick);
        childAdapter.setList(safeList);

        binding.childrenCategory.setLayoutManager(new LinearLayoutManager(context));
        binding.childrenCategory.setAdapter(childAdapter);
    }

    /** 左右栏共用的条目视觉：整行选中、左对齐、按下波纹 */
    private void applyItemChrome(PopupWindowAdapter<T> adapter) {
        int hPad = DensityUtil.dp2px(context, 14f);
        adapter.setItemHeight(itemHeight)
                .setBackgroundDrawable(unSelectBgDrawable)
                .setBackgroundSelectedDrawable(selectBgDrawable)
                .setPadding(hPad, 0, hPad, 0)
                .setMargin(0, 0, 0, 0)
                .setTextGravity(Gravity.START | Gravity.CENTER_VERTICAL)
                .setTextColor(unSelectTextColor)
                .setTextSelectedColor(selectTextColor);
    }

    /** 整行背景：选中浅主色 / 未选透明，均带按下 ripple */
    private Drawable createRowBackground(boolean selected) {
        int fill = selected
                ? ColorUtils.setAlphaComponent(ThemeAttrs.primary(context), 28)
                : Color.TRANSPARENT;
        int ripple = ColorUtils.setAlphaComponent(ThemeAttrs.onSurface(context), 48);
        return DrawableUtil.createRippleDrawableCompat(new ColorDrawable(fill), ripple);
    }

    private void onParentItemClick(View view, int position) {
        if (dataList == null || popupWindowAdapter == null
                || position < 0 || position >= dataList.size()) {
            return;
        }

        boolean parentChanged = parentPosition == null || parentPosition != position;
        selectParentIndex(position, false);

        T parent = dataList.get(position);
        boolean noChild = !hasRight || CollectionUtil.isEmpty(parent.getChildList());

        if (selectionMode == PopupWindowAdapter.MODE_SINGLE && noChild) {
            dismiss();
            if (selectedListener != null) {
                selectedListener.onSelectedResult(
                        TreePopupView.this, dataList, parentPosition, childPosition);
            }
            return;
        }

        if (hasRight) {
            // 切换父级时清掉子级选中，避免左右栏「假联动」
            if (parentChanged) {
                childPosition = null;
            }
            initChild(safeChildList(parent));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unchecked")
    private void onChildItemClick(View view, int position) {
        if (dataList == null || parentPosition == null
                || parentPosition < 0 || parentPosition >= dataList.size()) {
            return;
        }
        List<?> children = dataList.get(parentPosition).getChildList();
        if (children == null || position < 0 || position >= children.size()) {
            return;
        }

        childPosition = position;
        // setOnItemClickListener 会盖掉 Adapter 默认选中，这里补上视觉态
        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            for (int i = 0; i < children.size(); i++) {
                T child = (T) children.get(i);
                boolean shouldSelect = i == position;
                if (Boolean.TRUE.equals(child.getSelected()) != shouldSelect) {
                    child.setSelected(shouldSelect);
                }
            }
            if (childAdapter != null) {
                childAdapter.notifyDataSetChanged();
            }
        } else {
            T child = (T) children.get(position);
            child.setSelected(!Boolean.TRUE.equals(child.getSelected()));
            if (childAdapter != null) {
                childAdapter.notifyItemChanged(position);
            }
        }
        // 点子级时同步保持父级高亮
        selectParentIndex(parentPosition, true);

        if (selectionMode == PopupWindowAdapter.MODE_SINGLE) {
            dismiss();
        }
        if (selectedListener != null) {
            selectedListener.onSelectedResult(
                    TreePopupView.this, dataList, parentPosition, childPosition);
        }
    }

    /**
     * 按位置选中父级（不依赖 popupId，避免空 id 误匹配）
     *
     * @param keepChildSelection 是否保留当前 childPosition
     */
    @SuppressLint("NotifyDataSetChanged")
    private void selectParentIndex(int position, boolean keepChildSelection) {
        if (dataList == null || position < 0 || position >= dataList.size()) {
            return;
        }
        parentPosition = position;
        if (!keepChildSelection) {
            childPosition = null;
        }
        for (int i = 0; i < dataList.size(); i++) {
            T item = dataList.get(i);
            boolean shouldSelect = i == position;
            if (Boolean.TRUE.equals(item.getSelected()) != shouldSelect) {
                item.setSelected(shouldSelect);
            }
        }
        if (popupWindowAdapter != null) {
            popupWindowAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> safeChildList(T parent) {
        if (parent == null || CollectionUtil.isEmpty(parent.getChildList())) {
            return new ArrayList<>();
        }
        return parent.getChildList();
    }

    public T getSelectedParentItem() {
        if (parentPosition != null && parentPosition >= 0 && parentPosition < dataList.size()) {
            return dataList.get(parentPosition);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T getSelectedChildItem() {
        if (parentPosition != null && childPosition != null
                && parentPosition >= 0 && parentPosition < dataList.size()
                && dataList.get(parentPosition).getChildList() != null
                && childPosition >= 0
                && childPosition < dataList.get(parentPosition).getChildList().size()) {
            return (T) dataList.get(parentPosition).getChildList().get(childPosition);
        }
        return null;
    }

    public List<T> getSelectedParentItems() {
        return popupWindowAdapter != null
                ? popupWindowAdapter.getSelectedItems()
                : new ArrayList<>();
    }

    public List<T> getSelectedChildItems() {
        return childAdapter != null
                ? childAdapter.getSelectedItems()
                : new ArrayList<>();
    }

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

    public interface SelectedListener<T> {
        void onSelectedResult(PopupWindow popupWindow, List<T> dataList,
                              Integer parentSelectPosition, Integer childrenSelectPosition);
    }
}
