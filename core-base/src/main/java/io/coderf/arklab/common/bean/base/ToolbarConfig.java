package io.coderf.arklab.common.bean.base;


import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.textview.MaterialTextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FontRes;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import java.util.Objects;

import io.coderf.arklab.common.BR;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeUtils;

/**
 * MaterialToolbar 配置（DataBinding 可观察）。
 * <p>
 * 链式 setter 返回 {@code this}；未显式设置的字段保持历史默认值，兼容已有项目。
 * 状态栏请在链式末尾调用 {@link #applyStatusBar()}。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 15:42
 */
public class ToolbarConfig extends BaseObservable {
    private static final String TAG = "ToolbarConfig";

    /** 默认标题字号（sp），与历史布局 18sp / font_size_xxl 一致。 */
    public static final float DEFAULT_TITLE_TEXT_SIZE_SP = 17f;

    /** 标题水平居中（历史默认）。 */
    public static final int TITLE_GRAVITY_CENTER = Gravity.CENTER;
    /** 标题靠起始方向（LTR 下为左）。 */
    public static final int TITLE_GRAVITY_START = Gravity.START | Gravity.CENTER_VERTICAL;
    /** 标题靠结束方向（LTR 下为右）。 */
    public static final int TITLE_GRAVITY_END = Gravity.END | Gravity.CENTER_VERTICAL;

    private ComponentActivity activity;
    private String title;
    private String titleHint = "请输入...";
    private @DrawableRes int backIconRes = R.drawable.icon_fh;
    /**
     * toolbar 的 menu 主题,默认主题为黑色
     */
    private boolean defaultTheme = true;
    /**
     * 标题字体颜色（默认语义色，暗色模式自动适配）
     */
    private @ColorRes int textColor = R.color.cardOnSurface;
    /**
     * 标题背景色（默认语义色，暗色模式自动适配）
     */
    private @ColorRes int bgColor = R.color.cardSurface;
    /**
     * toolbar 高度（px）；0 表示使用 actionBarSize
     */
    private int height = 0;

    /**
     * 状态栏背景色资源；null 时 applyStatusBar 回退到 bgColor
     */
    private @ColorRes Integer statusBarColor = null;
    /**
     * 状态栏图标模式：false = 深色图标（黑字），true = 浅色图标（白字）
     */
    private boolean isLightMode = false;

    /**
     * 是否启用沉浸式（Edge-to-Edge 下与透明状态栏等价，保留字段兼容业务）
     */
    private boolean enableImmersionBar = false;
    /**
     * 是否显示返回按钮
     */
    private boolean isShowBackButton = true;

    /**
     * 标题字号（单位 sp）；默认 {@link #DEFAULT_TITLE_TEXT_SIZE_SP}
     */
    private float titleTextSizeSp = DEFAULT_TITLE_TEXT_SIZE_SP;

    /**
     * 标题是否加粗（与 {@link #titleFontFamily} / {@link #titleFontRes} 组合时作为 style）
     */
    private boolean titleBold = false;

    /**
     * 标题最大行数；默认 1
     */
    private int titleMaxLines = 1;

    /**
     * 标题在 MaterialToolbar 内的 layout_gravity；默认居中，兼容历史布局。
     */
    private int titleGravity = TITLE_GRAVITY_CENTER;

    /**
     * 系统字族名，如 {@code "sans-serif-medium"}；null 表示不指定。
     */
    @Nullable
    private String titleFontFamily;

    /**
     * 自定义字体资源；0 表示未设置。优先于 {@link #titleFontFamily}。
     */
    @FontRes
    private int titleFontRes = 0;

    /**
     * 字间距（em）；0 为系统默认。
     */
    private float titleLetterSpacing = 0f;

    /**
     * 标题是否全大写。
     */
    private boolean titleAllCaps = false;

    /**
     * MaterialToolbar elevation（dp）；负数表示不修改（兼容历史未设 elevation 的页面）。
     */
    private float elevationDp = -1f;

    public ToolbarConfig(ComponentActivity activity) {
        this.activity = activity;
    }


    public ToolbarConfig(String title, int backIconRes, boolean defaultTheme, int textColor, int bgColor) {
        this.title = title;
        this.backIconRes = backIconRes;
        this.defaultTheme = defaultTheme;
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    public ToolbarConfig(String title, String titleHint, int backIconRes, boolean defaultTheme, int textColor, int bgColor, boolean isShowBackButton, int viewStubLayout) {
        this.title = title;
        this.titleHint = titleHint;
        this.backIconRes = backIconRes;
        this.defaultTheme = defaultTheme;
        this.textColor = textColor;
        this.bgColor = bgColor;
        this.isShowBackButton = isShowBackButton;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public ToolbarConfig setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
        return this;
    }

    @Bindable
    public int getHeight() {
        if (height > 0) {
            return height;
        }
        TypedValue typedValue = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(
                    typedValue.data,
                    activity.getResources().getDisplayMetrics()
            );
        }
        return DensityUtil.dp2px(activity, 48f);
    }

    public ToolbarConfig setHeight(int height) {
        this.height = height;
        notifyPropertyChanged(BR.height);
        return this;
    }

    /** 是否通过 {@link #setHeight(int)} 显式指定了 MaterialToolbar 内容区高度。 */
    public boolean hasCustomHeight() {
        return height > 0;
    }

    @Bindable
    public String getTitleHint() {
        return titleHint;
    }

    public ToolbarConfig setTitleHint(String titleHint) {
        this.titleHint = titleHint;
        notifyPropertyChanged(BR.titleHint);
        return this;
    }

    @Bindable
    public boolean isLightMode() {
        return isLightMode;
    }

    /**
     * 状态栏图标模式。
     *
     * @param lightMode true：浅色图标（白字）；false：深色图标（黑字）
     */
    public ToolbarConfig setLightMode(boolean lightMode) {
        isLightMode = lightMode;
        notifyPropertyChanged(BR.lightMode);
        return this;
    }

    @Bindable
    public boolean isEnableImmersionBar() {
        return enableImmersionBar;
    }

    public ToolbarConfig setEnableImmersionBar(boolean enableImmersionBar) {
        this.enableImmersionBar = enableImmersionBar;
        notifyPropertyChanged(BR.enableImmersionBar);
        return this;
    }

    @Bindable
    public Integer getStatusBarColor() {
        return statusBarColor;
    }

    /**
     * 状态栏参考色资源（Edge-to-Edge 下不写入 Window，仅作业务记录；图标深浅仍由 lightMode 决定）。
     */
    public ToolbarConfig setStatusBarColor(@ColorRes Integer statusBarColor) {
        this.statusBarColor = statusBarColor;
        notifyPropertyChanged(BR.statusBarColor);
        return this;
    }

    @Bindable
    public int getBackIconRes() {
        return backIconRes;
    }

    public ToolbarConfig setBackIconRes(int backIconRes) {
        this.backIconRes = backIconRes;
        notifyPropertyChanged(BR.backIconRes);
        return this;
    }

    @Bindable
    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public ToolbarConfig setDefaultTheme(boolean defaultTheme) {
        this.defaultTheme = defaultTheme;
        notifyPropertyChanged(BR.defaultTheme);
        return this;
    }

    @Bindable
    public int getTextColor() {
        return textColor;
    }

    public ToolbarConfig setTextColor(@ColorRes int textColor) {
        this.textColor = textColor;
        notifyPropertyChanged(BR.textColor);
        return this;
    }

    @Bindable
    public int getBgColor() {
        return bgColor;
    }

    public ToolbarConfig setBgColor(@ColorRes int bgColor) {
        this.bgColor = bgColor;
        notifyPropertyChanged(BR.bgColor);
        return this;
    }

    @Bindable
    public boolean isShowBackButton() {
        return isShowBackButton;
    }

    public ToolbarConfig setShowBackButton(boolean showBackButton) {
        isShowBackButton = showBackButton;
        notifyPropertyChanged(BR.showBackButton);
        return this;
    }

    // -------------------------------------------------------------------------
    // 标题样式
    // -------------------------------------------------------------------------

    @Bindable
    public float getTitleTextSizeSp() {
        return titleTextSizeSp;
    }

    /**
     * 设置标题字号（单位 sp）。
     *
     * @param titleTextSizeSp 例如 16、18、20；≤0 时忽略（保持当前值）
     */
    public ToolbarConfig setTitleTextSizeSp(float titleTextSizeSp) {
        if (titleTextSizeSp > 0f) {
            this.titleTextSizeSp = titleTextSizeSp;
            notifyPropertyChanged(BR.titleTextSizeSp);
        }
        return this;
    }

    /**
     * 从 dimen 资源设置标题字号。
     */
    public ToolbarConfig setTitleTextSizeRes(@DimenRes int dimenRes) {
        if (activity != null) {
            float px = activity.getResources().getDimension(dimenRes);
            float sp = px / activity.getResources().getDisplayMetrics().scaledDensity;
            return setTitleTextSizeSp(sp);
        }
        return this;
    }

    @Bindable
    public boolean isTitleBold() {
        return titleBold;
    }

    /**
     * 标题是否加粗。
     */
    public ToolbarConfig setTitleBold(boolean titleBold) {
        this.titleBold = titleBold;
        notifyPropertyChanged(BR.titleBold);
        return this;
    }

    @Bindable
    public int getTitleMaxLines() {
        return titleMaxLines;
    }

    /**
     * 标题最大行数；至少为 1。
     */
    public ToolbarConfig setTitleMaxLines(int titleMaxLines) {
        this.titleMaxLines = Math.max(1, titleMaxLines);
        notifyPropertyChanged(BR.titleMaxLines);
        return this;
    }

    @Bindable
    public int getTitleGravity() {
        return titleGravity;
    }

    /**
     * 标题对齐：建议使用 {@link #TITLE_GRAVITY_CENTER} / {@link #TITLE_GRAVITY_START} / {@link #TITLE_GRAVITY_END}。
     * 默认 {@link #TITLE_GRAVITY_CENTER}。
     */
    public ToolbarConfig setTitleGravity(int titleGravity) {
        this.titleGravity = titleGravity;
        notifyPropertyChanged(BR.titleGravity);
        return this;
    }

    /** 标题居中（默认行为）。 */
    public ToolbarConfig setTitleGravityCenter() {
        return setTitleGravity(TITLE_GRAVITY_CENTER);
    }

    /** 标题靠起始边。 */
    public ToolbarConfig setTitleGravityStart() {
        return setTitleGravity(TITLE_GRAVITY_START);
    }

    /** 标题靠结束边。 */
    public ToolbarConfig setTitleGravityEnd() {
        return setTitleGravity(TITLE_GRAVITY_END);
    }

    @Bindable
    @Nullable
    public String getTitleFontFamily() {
        return titleFontFamily;
    }

    /**
     * 系统字族，如 {@code "sans-serif-medium"}、{@code "monospace"}。
     * {@code null} 或不调用则保持默认字体。
     */
    public ToolbarConfig setTitleFontFamily(@Nullable String titleFontFamily) {
        this.titleFontFamily = titleFontFamily;
        notifyPropertyChanged(BR.titleFontFamily);
        return this;
    }

    @Bindable
    public int getTitleFontRes() {
        return titleFontRes;
    }

    /**
     * 自定义字体资源（res/font）。优先于 {@link #setTitleFontFamily}。
     * 传 0 清除。
     */
    public ToolbarConfig setTitleFontRes(@FontRes int titleFontRes) {
        this.titleFontRes = titleFontRes;
        notifyPropertyChanged(BR.titleFontRes);
        return this;
    }

    @Bindable
    public float getTitleLetterSpacing() {
        return titleLetterSpacing;
    }

    /**
     * 字间距（em 单位，与 MaterialTextView.setLetterSpacing 一致）。默认 0。
     */
    public ToolbarConfig setTitleLetterSpacing(float titleLetterSpacing) {
        this.titleLetterSpacing = titleLetterSpacing;
        notifyPropertyChanged(BR.titleLetterSpacing);
        return this;
    }

    @Bindable
    public boolean isTitleAllCaps() {
        return titleAllCaps;
    }

    /**
     * 标题是否全大写。默认 false。
     */
    public ToolbarConfig setTitleAllCaps(boolean titleAllCaps) {
        this.titleAllCaps = titleAllCaps;
        notifyPropertyChanged(BR.titleAllCaps);
        return this;
    }

    @Bindable
    public float getElevationDp() {
        return elevationDp;
    }

    /**
     * MaterialToolbar 阴影高度（dp）。默认 -1 表示不修改，避免影响已有页面。
     * 传 0 可去掉阴影。
     */
    public ToolbarConfig setElevationDp(float elevationDp) {
        this.elevationDp = elevationDp;
        notifyPropertyChanged(BR.elevationDp);
        return this;
    }

    /**
     * 应用状态栏：Edge-to-Edge 下状态栏透明，图标深浅跟 {@link #statusBarColor} /
     * {@link #bgColor} 的实际亮度走（暗色表面用浅色图标）。
     * {@link #isLightMode} 仅作历史兼容，不再单独决定图标颜色。
     */
    public ToolbarConfig applyStatusBar() {
        if (activity == null || activity.isFinishing()) {
            return this;
        }
        int color = ContextCompat.getColor(activity, Objects.requireNonNullElseGet(statusBarColor, () -> bgColor));
        ThemeUtils.setupStatusBarAuto(activity, color);
        return this;
    }

    // -------------------------------------------------------------------------
    // DataBinding adapters（供布局 app:bindXxx 使用）
    // -------------------------------------------------------------------------

    @BindingAdapter("bindTitleTextSizeSp")
    public static void bindTitleTextSizeSp(MaterialTextView textView, float sizeSp) {
        if (sizeSp > 0f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        }
    }

    @BindingAdapter(value = {
            "bindTitleBold",
            "bindTitleFontFamily",
            "bindTitleFontRes"
    }, requireAll = false)
    public static void bindTitleTypeface(MaterialTextView textView,
                                         Boolean bold,
                                         @Nullable String fontFamily,
                                         Integer fontRes) {
        boolean isBold = bold != null && bold;
        int style = isBold ? Typeface.BOLD : Typeface.NORMAL;
        Typeface typeface = null;
        int res = fontRes != null ? fontRes : 0;
        if (res != 0) {
            try {
                typeface = ResourcesCompat.getFont(textView.getContext(), res);
            } catch (Exception ignored) {
                // 资源缺失时回退
            }
        }
        if (typeface == null && fontFamily != null && !fontFamily.isEmpty()) {
            typeface = Typeface.create(fontFamily, style);
        }
        if (typeface != null) {
            textView.setTypeface(typeface, style);
        } else {
            Typeface base = textView.getTypeface();
            textView.setTypeface(Typeface.create(base, style));
        }
    }

    @BindingAdapter("bindTitleMaxLines")
    public static void bindTitleMaxLines(MaterialTextView textView, int maxLines) {
        int lines = Math.max(1, maxLines);
        textView.setMaxLines(lines);
        textView.setSingleLine(lines == 1);
    }

    @BindingAdapter("bindTitleGravity")
    public static void bindTitleGravity(MaterialTextView textView, int gravity) {
        ViewGroup.LayoutParams lp = textView.getLayoutParams();
        if (lp instanceof Toolbar.LayoutParams tlp) {
            if (tlp.gravity != gravity) {
                tlp.gravity = gravity;
                textView.setLayoutParams(tlp);
            }
        }
        textView.setGravity(gravity);
        View parent = textView.getParent() instanceof View ? (View) textView.getParent() : null;
        if (parent != null) {
            parent.requestLayout();
        }
    }

    @BindingAdapter("bindTitleLetterSpacing")
    public static void bindTitleLetterSpacing(MaterialTextView textView, float letterSpacing) {
        textView.setLetterSpacing(letterSpacing);
    }

    @BindingAdapter("bindTitleAllCaps")
    public static void bindTitleAllCaps(MaterialTextView textView, boolean allCaps) {
        textView.setAllCaps(allCaps);
    }

    @BindingAdapter("bindToolbarElevationDp")
    public static void bindToolbarElevationDp(MaterialToolbar toolbar, float elevationDp) {
        if (elevationDp < 0f) {
            return;
        }
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                elevationDp,
                toolbar.getResources().getDisplayMetrics()
        );
        toolbar.setElevation(px);
    }

    /**
     * 导航图标：显示开关 + 资源 + 着色色值（int）。
     * 不使用 app:navigationIconTint（DataBinding 对 int 无对应 setter）。
     */
    @BindingAdapter(value = {
            "bindNavIconVisible",
            "bindNavIconRes",
            "bindNavIconTint"
    }, requireAll = false)
    public static void bindNavigationIcon(MaterialToolbar toolbar,
                                          Boolean visible,
                                          Integer iconRes,
                                          Integer tintColor) {
        boolean show = visible == null || visible;
        if (!show || iconRes == null || iconRes == 0) {
            toolbar.setNavigationIcon(null);
            return;
        }
        if (tintColor != null) {
            Drawable d = DrawableUtil.withTint(toolbar.getContext(), iconRes, tintColor);
            toolbar.setNavigationIcon(d);
        } else {
            toolbar.setNavigationIcon(iconRes);
        }
    }
}

