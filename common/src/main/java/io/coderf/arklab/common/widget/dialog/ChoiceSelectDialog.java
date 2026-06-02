package io.coderf.arklab.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.adapter.CheckBoxAdapter;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.DialogChoiceSelectBinding;
import io.coderf.arklab.common.listener.OnChoiceSelectListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.customview.CornerButton;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;

/**
 * 单选/多选列表弹框，复用 {@link CheckBoxAdapter} 的勾选样式与交互。
 * <p>
 * 支持标题、确认/取消按钮、列表项高度、文字、勾选图标、全选头等全面配置。
 */
public class ChoiceSelectDialog<T extends PopupWindowBean> extends Dialog {

    public static final int MODE_MULTI = CheckBoxAdapter.MODE_MULTI;
    public static final int MODE_SINGLE = CheckBoxAdapter.MODE_SINGLE;
    /** 确认/取消按钮默认圆角 */
    public static final float DEFAULT_BUTTON_RADIUS_DP = 20f;

    private final Context context;
    private DialogChoiceSelectBinding binding;
    private CheckBoxAdapter<T> checkBoxAdapter;

    private List<T> menuData;
    private int selectionMode = MODE_SINGLE;
    private OnChoiceSelectListener<T> choiceSelectListener;

    private boolean outSide = true;
    private int gravity = Gravity.BOTTOM;
    private Drawable bgDrawable;

    private boolean showTitle = true;
    private String titleText;
    private float titleTextSizeSp = 0f;
    private @ColorInt int titleTextColor = -1;

    private boolean showCancelButton = true;
    private boolean showConfirmButton = true;
    private String confirmButtonText;
    private String cancelButtonText;
    private @ColorInt int confirmButtonColor = -1;
    private @ColorInt int cancelButtonColor = -1;
    private @ColorInt int confirmButtonBgColor = -1;
    private @ColorInt int cancelButtonBgColor = -1;
    private float confirmButtonTextSizeSp = 0f;
    private float cancelButtonTextSizeSp = 0f;
    /** 按钮圆角 (px)，小于 0 表示使用 {@link #DEFAULT_BUTTON_RADIUS_DP} */
    private float confirmButtonRadiusPx = -1f;
    private float cancelButtonRadiusPx = -1f;

    private boolean showSelectAllHeader = false;
    private String selectAllHeaderText = "全部";

    private float textSizeSp = -1f;
    private @ColorInt int textColor = -1;
    private @ColorInt int headerTextColor = -1;
    private float headerTextSizeSp = -1f;
    private int itemHeightPx = -1;
    private int line = 1;
    private int textMarginLeft = -1;
    private int textMarginTop = -1;
    private int textMarginRight = -1;
    private int textMarginBottom = -1;
    private int iconMarginLeft = -1;
    private int iconMarginRight = -1;

    private Drawable checkedDrawable;
    private Drawable uncheckedDrawable;
    private boolean showCheckBox = true;

    private boolean showDivider = true;
    private @ColorInt int dividerColor = -1;

    private int maxListHeightPx = -1;
    private int rootPaddingStartPx = -1;
    private int rootPaddingEndPx = -1;
    private int listPaddingStartPx = -1;
    private int listPaddingTopPx = -1;
    private int listPaddingEndPx = -1;
    private int listPaddingBottomPx = -1;
    private int buttonBarMarginTopPx = -1;

    /** 单选模式下点击列表项后是否立即确认并关闭 */
    private boolean dismissOnSingleItemClick = false;

    public ChoiceSelectDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public ChoiceSelectDialog<T> setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public ChoiceSelectDialog<T> setData(List<T> dataList) {
        this.menuData = cloneDataList(dataList);
        return this;
    }

    @SafeVarargs
    public final ChoiceSelectDialog<T> setData(T... data) {
        List<T> list = new ArrayList<>();
        if (data != null) {
            for (T item : data) {
                list.add(item);
            }
        }
        return setData(list);
    }

    public ChoiceSelectDialog<T> setData(String... names) {
        List<T> list = new ArrayList<>();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                list.add((T) new PopupWindowBean(String.valueOf(i + 1), names[i]));
            }
        }
        return setData(list);
    }

    public ChoiceSelectDialog<T> setOnChoiceSelectListener(OnChoiceSelectListener<T> listener) {
        this.choiceSelectListener = listener;
        return this;
    }

    public ChoiceSelectDialog<T> setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public ChoiceSelectDialog<T> setDialogGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public ChoiceSelectDialog<T> setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public ChoiceSelectDialog<T> setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    public ChoiceSelectDialog<T> setTitleText(String titleText) {
        this.titleText = titleText;
        return this;
    }

    public ChoiceSelectDialog<T> setTitleTextSize(float spSize) {
        this.titleTextSizeSp = spSize;
        return this;
    }

    public ChoiceSelectDialog<T> setTitleTextColor(@ColorInt int color) {
        this.titleTextColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setShowCancelButton(boolean show) {
        this.showCancelButton = show;
        return this;
    }

    public ChoiceSelectDialog<T> setShowConfirmButton(boolean show) {
        this.showConfirmButton = show;
        return this;
    }

    public ChoiceSelectDialog<T> setConfirmButtonText(String text) {
        this.confirmButtonText = text;
        return this;
    }

    public ChoiceSelectDialog<T> setCancelButtonText(String text) {
        this.cancelButtonText = text;
        return this;
    }

    public ChoiceSelectDialog<T> setConfirmButtonColor(@ColorInt int color) {
        this.confirmButtonColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setCancelButtonColor(@ColorInt int color) {
        this.cancelButtonColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setConfirmButtonBgColor(@ColorInt int color) {
        this.confirmButtonBgColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setCancelButtonBgColor(@ColorInt int color) {
        this.cancelButtonBgColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setConfirmButtonTextSize(float spSize) {
        this.confirmButtonTextSizeSp = spSize;
        return this;
    }

    public ChoiceSelectDialog<T> setCancelButtonTextSize(float spSize) {
        this.cancelButtonTextSizeSp = spSize;
        return this;
    }

    /**
     * 同时设置确认、取消按钮圆角（dp），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setButtonRadiusDp(float radiusDp) {
        int px = DensityUtil.dp2px(context, radiusDp);
        this.confirmButtonRadiusPx = px;
        this.cancelButtonRadiusPx = px;
        return this;
    }

    /**
     * 设置确认按钮圆角（dp），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setConfirmButtonRadiusDp(float radiusDp) {
        this.confirmButtonRadiusPx = DensityUtil.dp2px(context, radiusDp);
        return this;
    }

    /**
     * 设置取消按钮圆角（dp），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setCancelButtonRadiusDp(float radiusDp) {
        this.cancelButtonRadiusPx = DensityUtil.dp2px(context, radiusDp);
        return this;
    }

    /**
     * 同时设置确认、取消按钮圆角（px），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setButtonRadiusPx(float radiusPx) {
        this.confirmButtonRadiusPx = radiusPx;
        this.cancelButtonRadiusPx = radiusPx;
        return this;
    }

    /**
     * 设置确认按钮圆角（px），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setConfirmButtonRadiusPx(float radiusPx) {
        this.confirmButtonRadiusPx = radiusPx;
        return this;
    }

    /**
     * 设置取消按钮圆角（px），默认 {@value #DEFAULT_BUTTON_RADIUS_DP}dp
     */
    public ChoiceSelectDialog<T> setCancelButtonRadiusPx(float radiusPx) {
        this.cancelButtonRadiusPx = radiusPx;
        return this;
    }

    public ChoiceSelectDialog<T> setButtonBarMarginTopDp(int marginTopDp) {
        this.buttonBarMarginTopPx = DensityUtil.dp2px(context, marginTopDp);
        return this;
    }

    public ChoiceSelectDialog<T> setShowSelectAllHeader(boolean show) {
        this.showSelectAllHeader = show;
        return this;
    }

    public ChoiceSelectDialog<T> setSelectAllHeaderText(String text) {
        this.selectAllHeaderText = text;
        return this;
    }

    public ChoiceSelectDialog<T> setTextSizeSp(float spSize) {
        this.textSizeSp = spSize;
        return this;
    }

    public ChoiceSelectDialog<T> setTextColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setHeaderTextColor(@ColorInt int color) {
        this.headerTextColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setHeaderTextSizeSp(float spSize) {
        this.headerTextSizeSp = spSize;
        return this;
    }

    public ChoiceSelectDialog<T> setItemHeight(int heightPx) {
        this.itemHeightPx = heightPx;
        return this;
    }

    public ChoiceSelectDialog<T> setItemHeightDp(float heightDp) {
        this.itemHeightPx = DensityUtil.dp2px(context, heightDp);
        return this;
    }

    public ChoiceSelectDialog<T> setLine(int line) {
        this.line = line;
        return this;
    }

    public ChoiceSelectDialog<T> setTextMargin(int leftPx, int topPx, int rightPx, int bottomPx) {
        this.textMarginLeft = leftPx;
        this.textMarginTop = topPx;
        this.textMarginRight = rightPx;
        this.textMarginBottom = bottomPx;
        return this;
    }

    public ChoiceSelectDialog<T> setTextMarginDp(float leftDp, float topDp, float rightDp, float bottomDp) {
        return setTextMargin(
                DensityUtil.dp2px(context, leftDp),
                DensityUtil.dp2px(context, topDp),
                DensityUtil.dp2px(context, rightDp),
                DensityUtil.dp2px(context, bottomDp)
        );
    }

    public ChoiceSelectDialog<T> setIconMargin(int leftPx, int rightPx) {
        this.iconMarginLeft = leftPx;
        this.iconMarginRight = rightPx;
        return this;
    }

    public ChoiceSelectDialog<T> setIconMarginDp(float leftDp, float rightDp) {
        return setIconMargin(DensityUtil.dp2px(context, leftDp), DensityUtil.dp2px(context, rightDp));
    }

    public ChoiceSelectDialog<T> setCheckedDrawable(Drawable drawable) {
        this.checkedDrawable = drawable;
        return this;
    }

    public ChoiceSelectDialog<T> setUncheckedDrawable(Drawable drawable) {
        this.uncheckedDrawable = drawable;
        return this;
    }

    public ChoiceSelectDialog<T> setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
        return this;
    }

    public ChoiceSelectDialog<T> setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
        return this;
    }

    public ChoiceSelectDialog<T> setDividerColor(@ColorInt int color) {
        this.dividerColor = color;
        return this;
    }

    public ChoiceSelectDialog<T> setMaxListHeightDp(float maxHeightDp) {
        this.maxListHeightPx = DensityUtil.dp2px(context, maxHeightDp);
        return this;
    }

    public ChoiceSelectDialog<T> setRootHorizontalPaddingDp(int startDp, int endDp) {
        this.rootPaddingStartPx = startDp >= 0 ? DensityUtil.dp2px(context, startDp) : -1;
        this.rootPaddingEndPx = endDp >= 0 ? DensityUtil.dp2px(context, endDp) : -1;
        return this;
    }

    public ChoiceSelectDialog<T> setListPaddingDp(int startDp, int topDp, int endDp, int bottomDp) {
        this.listPaddingStartPx = startDp >= 0 ? DensityUtil.dp2px(context, startDp) : -1;
        this.listPaddingTopPx = topDp >= 0 ? DensityUtil.dp2px(context, topDp) : -1;
        this.listPaddingEndPx = endDp >= 0 ? DensityUtil.dp2px(context, endDp) : -1;
        this.listPaddingBottomPx = bottomDp >= 0 ? DensityUtil.dp2px(context, bottomDp) : -1;
        return this;
    }

    public ChoiceSelectDialog<T> setDismissOnSingleItemClick(boolean dismissOnSingleItemClick) {
        this.dismissOnSingleItemClick = dismissOnSingleItemClick;
        return this;
    }

    public ChoiceSelectDialog<T> builder() {
        initView();
        return this;
    }

    public DialogChoiceSelectBinding getBinding() {
        return binding;
    }

    public CheckBoxAdapter<T> getCheckBoxAdapter() {
        return checkBoxAdapter;
    }

    public List<T> getSelectedItems() {
        return checkBoxAdapter != null ? checkBoxAdapter.getSelectedItems() : new ArrayList<>();
    }

    private void initView() {
        binding = DialogChoiceSelectBinding.inflate(LayoutInflater.from(context), null, false);

        setupTitle();
        setupButtons();
        setupRecyclerView();
        applyLayoutAppearance();

        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());

        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(gravity);
        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                Color.WHITE,
                DensityUtil.dp2px(context, 16f),
                DensityUtil.dp2px(context, 16f),
                0,
                0
        )));
    }

    private void setupTitle() {
        if (showTitle && titleText != null && !titleText.isEmpty()) {
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.vTitleLine.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(titleText);
        } else {
            binding.tvTitle.setVisibility(View.GONE);
            binding.vTitleLine.setVisibility(View.GONE);
        }
        if (titleTextSizeSp > 0f) {
            binding.tvTitle.setTextSize(titleTextSizeSp);
        }
        if (titleTextColor != -1) {
            binding.tvTitle.setTextColor(titleTextColor);
        }
    }

    private void setupButtons() {
        binding.btnCancel.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);
        binding.btnConfirm.setVisibility(showConfirmButton ? View.VISIBLE : View.GONE);

        if (cancelButtonText != null) {
            binding.btnCancel.setText(cancelButtonText);
        }
        if (confirmButtonText != null) {
            binding.btnConfirm.setText(confirmButtonText);
        }
        if (cancelButtonColor != -1) {
            binding.btnCancel.setTextColor(cancelButtonColor);
        }
        if (confirmButtonColor != -1) {
            binding.btnConfirm.setTextColor(confirmButtonColor);
        }
        if (cancelButtonTextSizeSp > 0f) {
            binding.btnCancel.setTextSize(cancelButtonTextSizeSp);
        }
        if (confirmButtonTextSizeSp > 0f) {
            binding.btnConfirm.setTextSize(confirmButtonTextSizeSp);
        }

        applyButtonRadius(binding.btnCancel, cancelButtonRadiusPx, cancelButtonBgColor);
        applyButtonRadius(binding.btnConfirm, confirmButtonRadiusPx, confirmButtonBgColor);

        binding.btnCancel.setOnClickListener(v -> {
            if (choiceSelectListener != null) {
                choiceSelectListener.onCancel(this);
            }
            dismiss();
        });
        binding.btnConfirm.setOnClickListener(v -> dispatchConfirm());
    }

    private void applyButtonRadius(CornerButton button, float radiusPx, @ColorInt int bgColor) {
        float radius = radiusPx >= 0 ? radiusPx : DensityUtil.dp2px(context, DEFAULT_BUTTON_RADIUS_DP);
        if (bgColor != -1) {
            button.setBgColorAndRadius(bgColor, radius);
        } else {
            button.setRadius(radius);
        }
    }

    private void setupRecyclerView() {
        checkBoxAdapter = new CheckBoxAdapter<>(selectionMode);
        checkBoxAdapter.setList(menuData);
        applyAdapterStyles();

        boolean showHeader = showSelectAllHeader && selectionMode == MODE_MULTI;
        checkBoxAdapter.setShowHeader(showHeader);
        if (showHeader) {
            checkBoxAdapter.setHeaderTitle(selectAllHeaderText);
        }

        if (selectionMode == MODE_SINGLE && dismissOnSingleItemClick) {
            checkBoxAdapter.setOnItemClickListener((view, position) -> {
                checkBoxAdapter.selectSingle(position);
                dispatchConfirm();
            });
        }

        binding.rvOptions.setLayoutManager(new LinearLayoutManager(context));
        binding.rvOptions.setAdapter(checkBoxAdapter);
        binding.rvOptions.setNestedScrollingEnabled(true);

        if (showDivider) {
            binding.rvOptions.addItemDecoration(new RecycleViewDivider(
                    context,
                    LinearLayoutManager.VERTICAL,
                    DensityUtil.dp2px(context, 1),
                    dividerColor == -1 ? ContextCompat.getColor(context, R.color.h_line_color) : dividerColor,
                    false
            ));
        }

        if (maxListHeightPx > 0) {
            binding.rvOptions.post(() -> {
                int contentHeight = binding.rvOptions.computeVerticalScrollRange();
                ViewGroup.LayoutParams lp = binding.rvOptions.getLayoutParams();
                lp.height = Math.min(contentHeight, maxListHeightPx);
                binding.rvOptions.setLayoutParams(lp);
            });
        }
    }

    private void applyAdapterStyles() {
        if (textSizeSp > 0f) {
            checkBoxAdapter.setTextSizeSp(textSizeSp);
        }
        if (textColor != -1) {
            checkBoxAdapter.setTextColor(textColor);
        }
        if (headerTextColor != -1) {
            checkBoxAdapter.setHeaderTextColor(headerTextColor);
        }
        if (headerTextSizeSp > 0f) {
            checkBoxAdapter.setHeaderTextSizeSp(headerTextSizeSp);
        }
        if (itemHeightPx > 0) {
            checkBoxAdapter.setItemHeight(itemHeightPx);
        }
        checkBoxAdapter.setLine(line);
        if (textMarginLeft >= 0 || textMarginTop >= 0 || textMarginRight >= 0 || textMarginBottom >= 0) {
            int left = textMarginLeft >= 0 ? textMarginLeft : 0;
            int top = textMarginTop >= 0 ? textMarginTop : 0;
            int right = textMarginRight >= 0 ? textMarginRight : 0;
            int bottom = textMarginBottom >= 0 ? textMarginBottom : 0;
            checkBoxAdapter.setTextMargin(left, top, right, bottom);
        }
        if (iconMarginLeft >= 0 || iconMarginRight >= 0) {
            int left = iconMarginLeft >= 0 ? iconMarginLeft : 0;
            int right = iconMarginRight >= 0 ? iconMarginRight : 0;
            checkBoxAdapter.setIconMargin(left, right);
        }
        if (checkedDrawable != null) {
            checkBoxAdapter.setCheckedDrawable(checkedDrawable);
        }
        if (uncheckedDrawable != null) {
            checkBoxAdapter.setUncheckedDrawable(uncheckedDrawable);
        }
        checkBoxAdapter.setShowCheckBox(showCheckBox);
    }

    private void applyLayoutAppearance() {
        if (rootPaddingStartPx >= 0 || rootPaddingEndPx >= 0) {
            int start = rootPaddingStartPx >= 0 ? rootPaddingStartPx : binding.clRoot.getPaddingStart();
            int end = rootPaddingEndPx >= 0 ? rootPaddingEndPx : binding.clRoot.getPaddingEnd();
            binding.clRoot.setPaddingRelative(start, binding.clRoot.getPaddingTop(), end, binding.clRoot.getPaddingBottom());
        }
        if (listPaddingStartPx >= 0 || listPaddingTopPx >= 0
                || listPaddingEndPx >= 0 || listPaddingBottomPx >= 0) {
            int start = listPaddingStartPx >= 0 ? listPaddingStartPx : binding.rvOptions.getPaddingStart();
            int top = listPaddingTopPx >= 0 ? listPaddingTopPx : binding.rvOptions.getPaddingTop();
            int end = listPaddingEndPx >= 0 ? listPaddingEndPx : binding.rvOptions.getPaddingEnd();
            int bottom = listPaddingBottomPx >= 0 ? listPaddingBottomPx : binding.rvOptions.getPaddingBottom();
            binding.rvOptions.setPaddingRelative(start, top, end, bottom);
        }
        if (buttonBarMarginTopPx >= 0) {
            ViewGroup.MarginLayoutParams lpCancel =
                    (ViewGroup.MarginLayoutParams) binding.btnCancel.getLayoutParams();
            lpCancel.topMargin = buttonBarMarginTopPx;
            binding.btnCancel.setLayoutParams(lpCancel);
            ViewGroup.MarginLayoutParams lpConfirm =
                    (ViewGroup.MarginLayoutParams) binding.btnConfirm.getLayoutParams();
            lpConfirm.topMargin = buttonBarMarginTopPx;
            binding.btnConfirm.setLayoutParams(lpConfirm);
        }
    }

    private void dispatchConfirm() {
        if (choiceSelectListener != null) {
            choiceSelectListener.onConfirm(this, checkBoxAdapter.getSelectedItems());
        }
        dismiss();
    }

    @SuppressWarnings("unchecked")
    private List<T> cloneDataList(List<T> source) {
        List<T> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (T item : source) {
            PopupWindowBean<?> clone = new PopupWindowBean<>(
                    item.getPopupId(),
                    item.getPopupName()
            );
            clone.setPopupCode(item.getPopupCode());
            if (item.getSelected() != null) {
                clone.setSelected(item.getSelected());
            }
            result.add((T) clone);
        }
        return result;
    }

    /**
     * 将选中结果名称拼接为展示文案
     */
    public static String formatSelectedNames(List<? extends PopupWindowBean> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(PopupWindowBean::getPopupName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("、"));
    }
}
