package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.coderf.arklab.common.adapter.CheckBoxAdapter;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.DialogChoiceSelectBinding;
import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.listener.OnChoiceSelectListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import com.google.android.material.button.MaterialButton;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 单选 / 多选列表弹框，列表交互与勾选样式复用 {@link CheckBoxAdapter}。
 * <p>
 * 可配置：标题、确认/取消、全选头、列表项高度与字色、勾选图标、分割线、
 * 列表最大高度、根/列表 padding、弹窗位置（默认底部）。
 * <p>
 * <b>单选示例：</b>
 * <pre>
 * new ChoiceSelectDialog&lt;PopupWindowBean&gt;(context)
 *     .setSelectionMode(ChoiceSelectDialog.MODE_SINGLE)
 *     .setTitleText("请选择")
 *     .setData("选项A", "选项B", "选项C")
 *     .setDismissOnSingleItemClick(true)  // 点一项即回传并关闭
 *     .setOnChoiceSelectListener((dialog, selected) -&gt; {
 *         // selected 为勾选项列表
 *     })
 *     .builder()
 *     .show();
 * </pre>
 * <b>多选 + 全选头：</b>
 * <pre>
 * new ChoiceSelectDialog&lt;PopupWindowBean&gt;(context)
 *     .setSelectionMode(ChoiceSelectDialog.MODE_MULTI)
 *     .setShowSelectAllHeader(true)
 *     .setSelectAllHeaderText("全选")
 *     .setData(beanList)
 *     .setOnChoiceSelectListener((dialog, selected) -&gt; { })
 *     .builder()
 *     .show();
 * </pre>
 * 数据需实现 {@link PopupWindowBean}（至少含展示名）；也可用 {@link #setData(String...)} 快速构造。
 *
 * @param &lt;T&gt; 列表项类型，需继承 {@link PopupWindowBean}
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/21
 */
public class ChoiceSelectDialog<T extends PopupWindowBean> extends BaseDialog {

    /** 多选（可勾多项，点确认回传） */
    public static final int MODE_MULTI = CheckBoxAdapter.MODE_MULTI;
    /** 单选（同时仅一项勾选） */
    public static final int MODE_SINGLE = CheckBoxAdapter.MODE_SINGLE;
    /** 确认/取消按钮默认圆角（dp） */
    public static final float DEFAULT_BUTTON_RADIUS_DP = 20f;

    /** 上下文，用于 inflate、dp 换算与主题色 */
    private final Context context;
    /** 布局 Binding，{@link #builder()} 后才非 null */
    private DialogChoiceSelectBinding binding;
    /** 列表勾选 Adapter，{@link #builder()} 后才非 null */
    private CheckBoxAdapter<T> checkBoxAdapter;

    /** 选项数据（内部拷贝，含勾选状态） */
    private List<T> menuData;
    /** 选择模式：{@link #MODE_SINGLE} / {@link #MODE_MULTI}，默认单选 */
    private int selectionMode = MODE_SINGLE;
    /** 确认选择回调 */
    private OnChoiceSelectListener<T> choiceSelectListener;

    /** 弹窗 gravity，默认 {@link Gravity#BOTTOM} */
    private int gravity = Gravity.BOTTOM;

    /** 是否显示顶部标题，默认 true */
    private boolean showTitle = true;
    /** 标题文案 */
    private String titleText;
    /** 标题字号（sp），{@code <= 0} 沿用布局 */
    private float titleTextSizeSp = 0f;
    /** 标题颜色，{@code -1} 沿用布局 */
    private @ColorInt int titleTextColor = -1;

    /** 是否显示「取消」按钮，默认 true */
    private boolean showCancelButton = true;
    /** 是否显示「确定」按钮，默认 true */
    private boolean showConfirmButton = true;
    /** 确定按钮文案；null/空用布局默认「确定」 */
    private String confirmButtonText;
    /** 取消按钮文案；null/空用布局默认「取消」 */
    private String cancelButtonText;
    /** 确定按钮文字颜色，{@code -1} 沿用布局 */
    private @ColorInt int confirmButtonColor = -1;
    /** 取消按钮文字颜色，{@code -1} 沿用布局 */
    private @ColorInt int cancelButtonColor = -1;
    /** 确定按钮背景色，{@code -1} 沿用布局/主题 */
    private @ColorInt int confirmButtonBgColor = -1;
    /** 取消按钮背景色，{@code -1} 沿用布局/主题 */
    private @ColorInt int cancelButtonBgColor = -1;
    /** 确定按钮字号（sp），{@code <= 0} 不改 */
    private float confirmButtonTextSizeSp = 0f;
    /** 取消按钮字号（sp），{@code <= 0} 不改 */
    private float cancelButtonTextSizeSp = 0f;
    /** 确定按钮圆角（px），{@code < 0} 使用 {@link #DEFAULT_BUTTON_RADIUS_DP} */
    private float confirmButtonRadiusPx = -1f;
    /** 取消按钮圆角（px），{@code < 0} 使用 {@link #DEFAULT_BUTTON_RADIUS_DP} */
    private float cancelButtonRadiusPx = -1f;

    /** 是否显示「全选」表头（多选），默认 false */
    private boolean showSelectAllHeader = false;
    /** 全选表头文案，默认「全部」 */
    private String selectAllHeaderText = "全部";

    /** 列表项正文字号（sp），{@code < 0} 用 Adapter 默认 */
    private float textSizeSp = -1f;
    /** 列表项正文颜色，{@code -1} 不改 */
    private @ColorInt int textColor = -1;
    /** 全选表头文字颜色，{@code -1} 不改 */
    private @ColorInt int headerTextColor = -1;
    /** 全选表头字号（sp），{@code < 0} 不改 */
    private float headerTextSizeSp = -1f;
    /** 列表项高度（px），{@code < 0} 为 wrap_content */
    private int itemHeightPx = -1;
    /** 列表项文字最大行数，默认 1 */
    private int line = 1;
    /** 列表项文字左 margin（px），{@code -1} 不改 */
    private int textMarginLeft = -1;
    /** 列表项文字上 margin（px），{@code -1} 不改 */
    private int textMarginTop = -1;
    /** 列表项文字右 margin（px），{@code -1} 不改 */
    private int textMarginRight = -1;
    /** 列表项文字下 margin（px），{@code -1} 不改 */
    private int textMarginBottom = -1;
    /** 勾选图标左 margin（px），{@code -1} 不改 */
    private int iconMarginLeft = -1;
    /** 勾选图标右 margin（px），{@code -1} 不改 */
    private int iconMarginRight = -1;

    /** 已勾选图标，null 用 Adapter 默认 */
    private Drawable checkedDrawable;
    /** 未勾选图标，null 用 Adapter 默认 */
    private Drawable uncheckedDrawable;
    /** 是否显示勾选图标，默认 true */
    private boolean showCheckBox = true;

    /** 是否显示列表分割线，默认 true */
    private boolean showDivider = true;
    /** 分割线颜色，{@code -1} 用主题 outlineVariant */
    private @ColorInt int dividerColor = -1;
    /** 分割线左侧缩进（px），{@code -1} 表示 0（全宽） */
    private int borderMarginLeft = -1;
    /** 分割线右侧缩进（px），{@code -1} 表示 0（全宽） */
    private int borderMarginRight = -1;
    /**
     * 分割线左右缩进区域的填充色，避免透出列表背景。
     * {@code -1} 表示不填充（透明）；有缩进时建议设置，如 {@link ThemeAttrs#surfaceContainerHigh}。
     */
    private @ColorInt int borderInsetColor = -1;

    /** 列表最大高度（px），{@code < 0} 不限制 */
    private int maxListHeightPx = -1;
    /** 根布局 start padding（px），{@code -1} 不改 */
    private int rootPaddingStartPx = -1;
    /** 根布局 end padding（px），{@code -1} 不改 */
    private int rootPaddingEndPx = -1;
    /** 列表 start padding（px），{@code -1} 不改 */
    private int listPaddingStartPx = -1;
    /** 列表 top padding（px），{@code -1} 不改 */
    private int listPaddingTopPx = -1;
    /** 列表 end padding（px），{@code -1} 不改 */
    private int listPaddingEndPx = -1;
    /** 列表 bottom padding（px），{@code -1} 不改 */
    private int listPaddingBottomPx = -1;
    /** 按钮区相对列表顶间距（px），{@code -1} 不改 */
    private int buttonBarMarginTopPx = -1;

    /**
     * 仅单选有效：点列表项后立即回调并关闭。
     * 多选忽略。默认 false。
     */
    private boolean dismissOnSingleItemClick = false;

    /**
     * @param context 用于 inflate 与主题色解析，通常传 Activity
     */
    public ChoiceSelectDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    /**
     * @param context    上下文
     * @param themeResId Dialog 主题，如 {@code R.style.ActionSheetDialogStyle}
     */
    public ChoiceSelectDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    /**
     * 选择模式，默认 {@link #MODE_SINGLE}。
     *
     * @param selectionMode {@link #MODE_SINGLE} 单选，或 {@link #MODE_MULTI} 多选
     * @return this，便于链式调用
     */
    public ChoiceSelectDialog<T> setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    /**
     * 设置列表数据（内部浅拷贝，避免外部 list 被勾选状态污染）。
     *
     * @param dataList 选项列表，元素需为 {@link PopupWindowBean} 子类；可为 null（视为空）
     * @return this
     */
    public ChoiceSelectDialog<T> setData(List<T> dataList) {
        this.menuData = cloneDataList(dataList);
        return this;
    }

    /**
     * 可变参数设置数据。
     *
     * @param data 若干选项实体
     * @return this
     */
    @SafeVarargs
    public final ChoiceSelectDialog<T> setData(T... data) {
        List<T> list = new ArrayList<>();
        if (data != null) {
            Collections.addAll(list, data);
        }
        return setData(list);
    }

    /**
     * 用展示名快速构造选项（id 为 {@code "1"}..{@code "n"} 的 {@link PopupWindowBean}）。
     *
     * @param names 展示在列表上的文案
     * @return this
     */
    @SuppressWarnings("unchecked")
    public ChoiceSelectDialog<T> setData(String... names) {
        List<T> list = new ArrayList<>();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                list.add((T) new PopupWindowBean(String.valueOf(i + 1), names[i]));
            }
        }
        return setData(list);
    }

    /**
     * 确认选择回调：点「确定」，或单选且 {@link #setDismissOnSingleItemClick(boolean)} 为 true 时触发。
     *
     * @param listener 回调；第二个参数为当前勾选的数据列表（可空列表，不为 null）
     * @return this
     */
    public ChoiceSelectDialog<T> setOnChoiceSelectListener(OnChoiceSelectListener<T> listener) {
        this.choiceSelectListener = listener;
        return this;
    }

    /**
     * 是否允许点击外部或返回键关闭。
     *
     * @param outSide true 可取消，false 不可点外部关闭
     * @return this
     */
    @Override
    public ChoiceSelectDialog<T> setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    /**
     * 弹窗在屏幕上的位置，默认 {@link Gravity#BOTTOM}。
     *
     * @param gravity 如 {@link Gravity#BOTTOM}、{@link Gravity#CENTER}
     * @return this
     */
    public ChoiceSelectDialog<T> setDialogGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * 自定义整窗背景；不设则使用 {@link BaseDialog} 默认 surface + 圆角。
     *
     * @param bgDrawable 背景 drawable，可为 null 表示默认
     * @return this
     */
    @Override
    public ChoiceSelectDialog<T> setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        return this;
    }

    /**
     * 是否显示顶部标题区域，默认 true。
     *
     * @param showTitle false 时隐藏标题与标题下分割线
     * @return this
     */
    public ChoiceSelectDialog<T> setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    /**
     * @param titleText 标题文案；空则显示默认「提示」类文案（视布局而定）
     * @return this
     */
    public ChoiceSelectDialog<T> setTitleText(String titleText) {
        this.titleText = titleText;
        return this;
    }

    /**
     * @param spSize 标题字号，单位 sp；{@code <= 0} 表示沿用布局默认
     * @return this
     */
    public ChoiceSelectDialog<T> setTitleTextSize(float spSize) {
        this.titleTextSizeSp = spSize;
        return this;
    }

    /**
     * @param color 标题文字颜色（{@link ColorInt}）
     * @return this
     */
    public ChoiceSelectDialog<T> setTitleTextColor(@ColorInt int color) {
        this.titleTextColor = color;
        return this;
    }

    /**
     * @param show 是否显示「取消」按钮，默认 true
     * @return this
     */
    public ChoiceSelectDialog<T> setShowCancelButton(boolean show) {
        this.showCancelButton = show;
        return this;
    }

    /**
     * @param show 是否显示「确定」按钮，默认 true；单选即时关闭时可关
     * @return this
     */
    public ChoiceSelectDialog<T> setShowConfirmButton(boolean show) {
        this.showConfirmButton = show;
        return this;
    }

    /**
     * @param text 确定按钮文案，null/空用默认「确定」
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonText(String text) {
        this.confirmButtonText = text;
        return this;
    }

    /**
     * @param text 取消按钮文案，null/空用默认「取消」
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonText(String text) {
        this.cancelButtonText = text;
        return this;
    }

    /**
     * @param color 确定按钮文字颜色
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonColor(@ColorInt int color) {
        this.confirmButtonColor = color;
        return this;
    }

    /**
     * @param color 取消按钮文字颜色
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonColor(@ColorInt int color) {
        this.cancelButtonColor = color;
        return this;
    }

    /**
     * @param color 确定按钮背景填充色（配合圆角绘制）
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonBgColor(@ColorInt int color) {
        this.confirmButtonBgColor = color;
        return this;
    }

    /**
     * @param color 取消按钮背景填充色
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonBgColor(@ColorInt int color) {
        this.cancelButtonBgColor = color;
        return this;
    }

    /**
     * @param spSize 确定按钮字号（sp），{@code <= 0} 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonTextSize(float spSize) {
        this.confirmButtonTextSizeSp = spSize;
        return this;
    }

    /**
     * @param spSize 取消按钮字号（sp），{@code <= 0} 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonTextSize(float spSize) {
        this.cancelButtonTextSizeSp = spSize;
        return this;
    }

    /**
     * 同时设置确定、取消按钮圆角。
     *
     * @param radiusDp 圆角半径，单位 dp，默认见 {@link #DEFAULT_BUTTON_RADIUS_DP}
     * @return this
     */
    public ChoiceSelectDialog<T> setButtonRadiusDp(float radiusDp) {
        int px = DensityUtil.dp2px(context, radiusDp);
        this.confirmButtonRadiusPx = px;
        this.cancelButtonRadiusPx = px;
        return this;
    }

    /**
     * @param radiusDp 确定按钮圆角（dp）
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonRadiusDp(float radiusDp) {
        this.confirmButtonRadiusPx = DensityUtil.dp2px(context, radiusDp);
        return this;
    }

    /**
     * @param radiusDp 取消按钮圆角（dp）
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonRadiusDp(float radiusDp) {
        this.cancelButtonRadiusPx = DensityUtil.dp2px(context, radiusDp);
        return this;
    }

    /**
     * @param radiusPx 确定与取消按钮共用圆角（px）
     * @return this
     */
    public ChoiceSelectDialog<T> setButtonRadiusPx(float radiusPx) {
        this.confirmButtonRadiusPx = radiusPx;
        this.cancelButtonRadiusPx = radiusPx;
        return this;
    }

    /**
     * @param radiusPx 确定按钮圆角（px）
     * @return this
     */
    public ChoiceSelectDialog<T> setConfirmButtonRadiusPx(float radiusPx) {
        this.confirmButtonRadiusPx = radiusPx;
        return this;
    }

    /**
     * @param radiusPx 取消按钮圆角（px）
     * @return this
     */
    public ChoiceSelectDialog<T> setCancelButtonRadiusPx(float radiusPx) {
        this.cancelButtonRadiusPx = radiusPx;
        return this;
    }

    /**
     * 按钮区域相对列表顶部的间距。
     *
     * @param marginTopDp 顶间距，单位 dp
     * @return this
     */
    public ChoiceSelectDialog<T> setButtonBarMarginTopDp(int marginTopDp) {
        this.buttonBarMarginTopPx = DensityUtil.dp2px(context, marginTopDp);
        return this;
    }

    /**
     * 是否显示「全选」表头行（多选时常用），默认 false。
     *
     * @param show true 显示全选头
     * @return this
     */
    public ChoiceSelectDialog<T> setShowSelectAllHeader(boolean show) {
        this.showSelectAllHeader = show;
        return this;
    }

    /**
     * @param text 全选表头文案，默认「全部」
     * @return this
     */
    public ChoiceSelectDialog<T> setSelectAllHeaderText(String text) {
        this.selectAllHeaderText = text;
        return this;
    }

    /**
     * 列表项正文的字号。
     *
     * @param spSize 单位 sp；{@code < 0} 表示沿用 Adapter 默认
     * @return this
     */
    public ChoiceSelectDialog<T> setTextSizeSp(float spSize) {
        this.textSizeSp = spSize;
        return this;
    }

    /**
     * @param color 列表项正文颜色
     * @return this
     */
    public ChoiceSelectDialog<T> setTextColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }

    /**
     * @param color 全选表头文字颜色
     * @return this
     */
    public ChoiceSelectDialog<T> setHeaderTextColor(@ColorInt int color) {
        this.headerTextColor = color;
        return this;
    }

    /**
     * @param spSize 全选表头字号（sp），{@code < 0} 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setHeaderTextSizeSp(float spSize) {
        this.headerTextSizeSp = spSize;
        return this;
    }

    /**
     * @param heightPx 列表项高度（px）；{@code < 0} 为 wrap_content
     * @return this
     */
    public ChoiceSelectDialog<T> setItemHeight(int heightPx) {
        this.itemHeightPx = heightPx;
        return this;
    }

    /**
     * @param heightDp 列表项高度（dp）
     * @return this
     */
    public ChoiceSelectDialog<T> setItemHeightDp(float heightDp) {
        this.itemHeightPx = DensityUtil.dp2px(context, heightDp);
        return this;
    }

    /**
     * 列表项文字最大行数，默认 1（单行省略）。
     *
     * @param line 最大行数，至少为 1
     * @return this
     */
    public ChoiceSelectDialog<T> setLine(int line) {
        this.line = line;
        return this;
    }

    /**
     * 列表项文字相对勾选区的 margin（px）。某侧传 {@code -1} 表示不修改该侧。
     *
     * @param leftPx   左边距 px，-1 不改
     * @param topPx    上边距 px，-1 不改
     * @param rightPx  右边距 px，-1 不改
     * @param bottomPx 下边距 px，-1 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setTextMargin(int leftPx, int topPx, int rightPx, int bottomPx) {
        this.textMarginLeft = leftPx;
        this.textMarginTop = topPx;
        this.textMarginRight = rightPx;
        this.textMarginBottom = bottomPx;
        return this;
    }

    /**
     * 同 {@link #setTextMargin(int, int, int, int)}，参数单位为 dp。
     *
     * @param leftDp   左
     * @param topDp    上
     * @param rightDp  右
     * @param bottomDp 下
     * @return this
     */
    public ChoiceSelectDialog<T> setTextMarginDp(float leftDp, float topDp, float rightDp, float bottomDp) {
        return setTextMargin(
                DensityUtil.dp2px(context, leftDp),
                DensityUtil.dp2px(context, topDp),
                DensityUtil.dp2px(context, rightDp),
                DensityUtil.dp2px(context, bottomDp)
        );
    }

    /**
     * 勾选图标左右间距。
     *
     * @param leftPx  图标左侧 margin（px）
     * @param rightPx 图标右侧 margin（px）
     * @return this
     */
    public ChoiceSelectDialog<T> setIconMargin(int leftPx, int rightPx) {
        this.iconMarginLeft = leftPx;
        this.iconMarginRight = rightPx;
        return this;
    }

    /**
     * @param leftDp  图标左 margin（dp）
     * @param rightDp 图标右 margin（dp）
     * @return this
     */
    public ChoiceSelectDialog<T> setIconMarginDp(float leftDp, float rightDp) {
        return setIconMargin(DensityUtil.dp2px(context, leftDp), DensityUtil.dp2px(context, rightDp));
    }

    /**
     * @param drawable 已勾选状态图标，null 用 Adapter 默认
     * @return this
     */
    public ChoiceSelectDialog<T> setCheckedDrawable(Drawable drawable) {
        this.checkedDrawable = drawable;
        return this;
    }

    /**
     * @param drawable 未勾选状态图标，null 用 Adapter 默认
     * @return this
     */
    public ChoiceSelectDialog<T> setUncheckedDrawable(Drawable drawable) {
        this.uncheckedDrawable = drawable;
        return this;
    }

    /**
     * 是否绘制勾选图标；为 false 时仍可点选，只是不显示 checkbox。
     *
     * @param showCheckBox 默认 true
     * @return this
     */
    public ChoiceSelectDialog<T> setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
        return this;
    }

    /**
     * @param showDivider 是否显示列表项之间的分割线，默认 true
     * @return this
     */
    public ChoiceSelectDialog<T> setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
        return this;
    }

    /**
     * @param color 分割线中间色条颜色；未设时用主题 outlineVariant
     * @return this
     */
    public ChoiceSelectDialog<T> setDividerColor(@ColorInt int color) {
        this.dividerColor = color;
        return this;
    }

    /**
     * 分割线左右缩进（px）。默认全宽；设置后中间画 {@link #setDividerColor}，两侧用 {@link #setDividerInsetColor}。
     * <p>
     * 对应 {@link RecycleViewDivider#setHorizontalInset(int, int)}。
     *
     * @param leftPx  左侧缩进 px，{@code < 0} 按 0 处理
     * @param rightPx 右侧缩进 px，{@code < 0} 按 0 处理
     * @return this
     */
    public ChoiceSelectDialog<T> setDividerHorizontalMargin(int leftPx, int rightPx) {
        this.borderMarginLeft = leftPx;
        this.borderMarginRight = rightPx;
        return this;
    }

    /**
     * 同 {@link #setDividerHorizontalMargin(int, int)}，参数单位 dp。
     *
     * @param leftDp  左侧缩进 dp
     * @param rightDp 右侧缩进 dp
     * @return this
     */
    public ChoiceSelectDialog<T> setDividerHorizontalMarginDp(float leftDp, float rightDp) {
        return setDividerHorizontalMargin(
                DensityUtil.dp2px(context, leftDp),
                DensityUtil.dp2px(context, rightDp));
    }

    /**
     * 分割线左右缩进区域的填充色（与 item/弹窗底一致时视觉才连续）。
     * <p>
     * 对应 {@link RecycleViewDivider#setSideColor(int)}。未设且有缩进时两侧透明，会透出列表背景。
     *
     * @param color 两侧填充色，如 {@code ThemeAttrs.surfaceContainerHigh(context)}
     * @return this
     */
    public ChoiceSelectDialog<T> setDividerInsetColor(@ColorInt int color) {
        this.borderInsetColor = color;
        return this;
    }

    /**
     * 一次设置分割线中间色、左右缩进与两侧填充色。
     *
     * @param dividerColor 中间色条颜色
     * @param leftPx       左缩进 px
     * @param rightPx      右缩进 px
     * @param insetColor   左右缩进区填充色
     * @return this
     */
    public ChoiceSelectDialog<T> setDividerStyle(@ColorInt int dividerColor,
                                                 int leftPx, int rightPx,
                                                 @ColorInt int insetColor) {
        this.dividerColor = dividerColor;
        this.borderMarginLeft = leftPx;
        this.borderMarginRight = rightPx;
        this.borderInsetColor = insetColor;
        return this;
    }

    /**
     * 限制列表可视最大高度，超出可滚动，避免选项过多撑满屏。
     *
     * @param maxHeightDp 最大高度（dp）
     * @return this
     */
    public ChoiceSelectDialog<T> setMaxListHeightDp(float maxHeightDp) {
        this.maxListHeightPx = DensityUtil.dp2px(context, maxHeightDp);
        return this;
    }

    /**
     * 根布局左右 padding。
     *
     * @param startDp 起始侧 padding（dp），{@code < 0} 不改
     * @param endDp   结束侧 padding（dp），{@code < 0} 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setRootHorizontalPaddingDp(int startDp, int endDp) {
        this.rootPaddingStartPx = startDp >= 0 ? DensityUtil.dp2px(context, startDp) : -1;
        this.rootPaddingEndPx = endDp >= 0 ? DensityUtil.dp2px(context, endDp) : -1;
        return this;
    }

    /**
     * 列表 RecyclerView 四边 padding。
     *
     * @param startDp  左/start（dp），{@code < 0} 不改
     * @param topDp    上（dp），{@code < 0} 不改
     * @param endDp    右/end（dp），{@code < 0} 不改
     * @param bottomDp 下（dp），{@code < 0} 不改
     * @return this
     */
    public ChoiceSelectDialog<T> setListPaddingDp(int startDp, int topDp, int endDp, int bottomDp) {
        this.listPaddingStartPx = startDp >= 0 ? DensityUtil.dp2px(context, startDp) : -1;
        this.listPaddingTopPx = topDp >= 0 ? DensityUtil.dp2px(context, topDp) : -1;
        this.listPaddingEndPx = endDp >= 0 ? DensityUtil.dp2px(context, endDp) : -1;
        this.listPaddingBottomPx = bottomDp >= 0 ? DensityUtil.dp2px(context, bottomDp) : -1;
        return this;
    }

    /**
     * 仅 {@link #MODE_SINGLE} 有效：点击某一项后立即回调并关闭，无需再点确定。
     * 多选模式下忽略。
     *
     * @param dismissOnSingleItemClick true 开启即时确认
     * @return this
     */
    public ChoiceSelectDialog<T> setDismissOnSingleItemClick(boolean dismissOnSingleItemClick) {
        this.dismissOnSingleItemClick = dismissOnSingleItemClick;
        return this;
    }

    /**
     * 根据当前配置 inflate 布局并绑定事件；须在 {@link #show()} 之前调用。
     *
     * @return this
     */
    public ChoiceSelectDialog<T> builder() {
        initView();
        return this;
    }

    /**
     * 展示弹窗，并按 {@link #setDialogGravity(int)} 设置 Window 位置。
     * 横屏只收窄宽度，不改变垂直弹出位置；水平居中需补偿 DisplayCutout。
     */
    @Override
    public void show() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            if (isLandscape()) {
                lp.width = resolveLandscapeSheetWidthPx();
                lp.gravity = resolveLandscapeSheetGravity(gravity);
            } else {
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.gravity = gravity;
            }
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            applyHorizontalCutoutOffset(lp);
            window.setAttributes(lp);
        }
        super.show();
    }

    /**
     * @return 布局 Binding；须在 {@link #builder()} 之后使用，否则可能为 null
     */
    public DialogChoiceSelectBinding getBinding() {
        return binding;
    }

    /**
     * @return 内部 {@link CheckBoxAdapter}，可在 builder 后做额外配置；未 builder 时为 null
     */
    public CheckBoxAdapter<T> getCheckBoxAdapter() {
        return checkBoxAdapter;
    }

    /**
     * 当前已勾选的数据快照。
     *
     * @return 勾选项列表；未 builder 时返回空列表（非 null）
     */
    public List<T> getSelectedItems() {
        return checkBoxAdapter != null ? checkBoxAdapter.getSelectedItems() : new ArrayList<>();
    }

    private void initView() {
        binding = inflateWithHostAdapt(
                () -> DialogChoiceSelectBinding.inflate(layoutInflater, null, false));

        setupTitle();
        setupButtons();
        setupRecyclerView();
        applyLayoutAppearance();
        applySurfaceBackground();

        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyBottomSheetWindow(gravity);
    }

    private void applySurfaceBackground() {
        if (bgDrawable != null) {
            binding.getRoot().setBackground(bgDrawable);
            return;
        }
        binding.getRoot().setBackground(DrawableUtil.createRectDrawable(
                ThemeAttrs.surfaceContainerHigh(context),
                DensityUtil.dp2px(context, 16f)));
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

    private void applyButtonRadius(MaterialButton button, float radiusPx, @ColorInt int bgColor) {
        float radius = radiusPx >= 0 ? radiusPx : DensityUtil.dp2px(context, DEFAULT_BUTTON_RADIUS_DP);
        if (bgColor != -1) {
            CornerShapeHelper.apply(button, radius, bgColor);
        } else {
            CornerShapeHelper.apply(button, radius);
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
            int lineColor = dividerColor == -1
                    ? ThemeAttrs.outlineVariant(context)
                    : dividerColor;
            RecycleViewDivider decoration = new RecycleViewDivider(
                    context,
                    LinearLayoutManager.VERTICAL,
                    DensityUtil.dp2px(context, 1),
                    lineColor,
                    false
            );
            int insetL = Math.max(0, borderMarginLeft);
            int insetR = Math.max(0, borderMarginRight);
            if (insetL > 0 || insetR > 0) {
                decoration.setHorizontalInset(insetL, insetR);
                if (borderInsetColor != -1) {
                    decoration.setSideColor(borderInsetColor);
                }
            }
            binding.rvOptions.addItemDecoration(decoration);
        }

        applyListHeight();
    }

    /**
     * 在 show 之前同步确定列表高度，避免 post 异步改高度导致弹窗先居中再跳到底部。
     */
    private void applyListHeight() {
        int listHeight = computeListHeightPx();
        if (listHeight <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = binding.rvOptions.getLayoutParams();
        lp.height = listHeight;
        binding.rvOptions.setLayoutParams(lp);
    }

    private int getListItemCount() {
        int count = menuData != null ? menuData.size() : 0;
        if (showSelectAllHeader && selectionMode == MODE_MULTI) {
            count += 1;
        }
        return count;
    }

    private int computeListHeightPx() {
        int itemCount = getListItemCount();
        if (itemCount == 0) {
            return 0;
        }
        int singleItemHeight = itemHeightPx > 0
                ? itemHeightPx
                : DensityUtil.dp2px(context, 35f);
        int dividerHeight = showDivider ? DensityUtil.dp2px(context, 1) * (itemCount - 1) : 0;
        int totalHeight = singleItemHeight * itemCount + dividerHeight;
        if (maxListHeightPx > 0) {
            totalHeight = Math.min(totalHeight, maxListHeightPx);
        }
        // 横屏按可用高度再裁切，保证底部取消/确定按钮可见
        int landscapeCap = resolveLandscapeMaxContentHeightPx(LANDSCAPE_CONTENT_RESERVED_DP);
        if (landscapeCap > 0) {
            totalHeight = Math.min(totalHeight, landscapeCap);
        }
        return totalHeight;
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
     * 将勾选项的展示名用顿号「、」拼接，便于显示在 TextView 等处。
     *
     * @param items {@link #getSelectedItems()} 或回调中的选中列表，可为 null
     * @return 如 {@code "选项A、选项B"}；空或 null 返回 {@code ""}
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
