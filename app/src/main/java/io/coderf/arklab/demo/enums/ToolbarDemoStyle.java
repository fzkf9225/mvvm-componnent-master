package io.coderf.arklab.demo.enums;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.activity.ToolbarPreviewActivity;

/**
 * Toolbar 配置演示的每一种预设。
 * <p>
 * {@link #apply} 只改 {@link ToolbarConfig}；状态栏由调用方在末尾 {@code applyStatusBar()}。
 */
public enum ToolbarDemoStyle {
    DEFAULT(
            "默认配置",
            "默认配置",
            "表面色、居中 17sp 标题、默认返回箭头",
            "对齐 BaseActivity.createdToolbarConfig：setBgColor(cardSurface)、setTextColor(cardOnSurface)、"
                    + "setBackIconRes(icon_fh)、setShowBackButton(true)、setTitleGravityCenter、"
                    + "setTitleTextSizeSp(17)、setTitleMaxLines(1)。"
                    + "setLightMode 已废弃，调用后不改变状态栏图标；图标深浅由 applyStatusBar 按背景亮度计算。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setLightMode(false)
                    .setTextColor(C.cardOnSurface)
                    .setBackIconRes(D.icon_fh)
                    .setBgColor(C.cardSurface)
                    .setShowBackButton(true)
                    .setTitleGravityCenter()
                    .setTitleTextSizeSp(ToolbarConfig.DEFAULT_TITLE_TEXT_SIZE_SP)
                    .setTitleMaxLines(1)
                    .setTitleBold(false)
                    .setTitleAllCaps(false)
                    .setTitleLetterSpacing(0f);
        }
    },
    BRAND(
            "品牌色 + 浅色图标",
            "品牌色",
            "themeColor 底、onPrimary 字，强制浅色状态栏图标",
            "setBgColor(themeColor)、setTextColor(onPrimary)、setBackIconColor(onPrimary)、"
                    + "setStatusBarIconsLight(true)、setElevationDp(4)。"
                    + "返回图标色与标题色拆开设置；这里两者相同，用来和「自定义返回图标」那一页对比。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setBgColor(C.themeColor)
                    .setTextColor(C.onPrimary)
                    .setBackIconColor(C.onPrimary)
                    .setStatusBarIconsLight(true)
                    .setElevationDp(4f);
        }
    },
    FORCE_DARK_ICONS(
            "强制深色状态栏图标",
            "深色图标",
            "深色底上故意用深色图标，覆盖自动计算结果",
            "背景仍是 themeColor。不设置 override 时 applyStatusBar 会选出浅色图标；"
                    + "setStatusBarIconsLight(false) 强制深色图标。传 null 可恢复自动。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setBgColor(C.themeColor)
                    .setTextColor(C.onPrimary)
                    .setBackIconColor(C.onPrimary)
                    .setStatusBarIconsLight(false)
                    .setElevationDp(0f);
        }
    },
    STATUS_BAR_COLOR(
            "状态栏参考色与底色分离",
            "参考色分离",
            "标题栏是黄色，图标深浅按另一套深色计算",
            "setBgColor(theme_yellow) 只画 Toolbar。setStatusBarColor(themeColor) 不涂状态栏"
                    + "（Edge-to-Edge 下状态栏透明），只作为 applyStatusBar 判断图标深浅的颜色。"
                    + "所以黄底上会得到浅色状态栏图标。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setBgColor(C.theme_yellow)
                    .setTextColor(C.cardOnSurface)
                    .setBackIconColor(C.cardOnSurface)
                    .setStatusBarColor(C.themeColor)
                    .setElevationDp(0f);
        }
    },
    HIDE_BACK(
            "隐藏返回按钮",
            "无返回",
            "setShowBackButton(false)，导航图标不显示",
            "仍可通过系统返回离开本页。需要自己控制返回时，给 MaterialToolbar 换 navigation 点击，"
                    + "或改用页内 TitleBar.setOnBackClickListener。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setShowBackButton(false)
                    .setTitleGravityCenter();
        }
    },
    CUSTOM_BACK(
            "自定义返回图标",
            "自定义返回",
            "关闭图标，并且只给返回图标着红色",
            "setBackIconRes(ic_toolbar_close)、setBackIconColor(theme_red)。"
                    + "标题仍用 cardOnSurface。传 setBackIconColor(0) 会重新跟随 setTextColor。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setBackIconRes(R.drawable.ic_toolbar_close)
                    .setBackIconColor(C.theme_red)
                    .setTextColor(C.cardOnSurface)
                    .setBgColor(C.cardSurface);
        }
    },
    GRAVITY_START(
            "标题靠左",
            "靠左标题",
            "setTitleGravityStart，紧挨返回按钮",
            "等价于 setTitleGravity(TITLE_GRAVITY_START)。"
                    + "居中走 MaterialToolbar.setTitleCentered；靠左/靠右改标题 TextView 的 layout_gravity。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleGravityStart()
                    .setTitleTextSizeSp(17f);
        }
    },
    GRAVITY_END(
            "标题靠右",
            "靠右",
            "setTitleGravityEnd，标题贴结束边",
            "等价于 setTitleGravity(TITLE_GRAVITY_END)。有右侧菜单时不要再用靠右，会和 action 重叠；"
                    + "菜单那一页改成了靠左。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleGravityEnd();
        }
    },
    TITLE_STYLE(
            "字号加粗字距全大写",
            "Toolbar Config",
            "22sp、加粗、字距、全大写、sans-serif-medium",
            "setTitleTextSizeSp(22)、setTitleBold(true)、setTitleLetterSpacing(0.15)、"
                    + "setTitleAllCaps(true)、setTitleFontFamily(\"sans-serif-medium\")。"
                    + "英文标题才能看出全大写。字号也可以走 setTitleTextSizeRes(dimen)，见等宽字体那一页。"
                    + "自定义字体文件用 setTitleFontRes，优先级高于 fontFamily；本仓库没有 res/font。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleTextSizeSp(22f)
                    .setTitleBold(true)
                    .setTitleLetterSpacing(0.15f)
                    .setTitleAllCaps(true)
                    .setTitleFontFamily("sans-serif-medium")
                    .setTitleGravityCenter();
        }
    },
    MULTILINE(
            "多行标题",
            "这是一段比较长的标题，用来演示最多两行，超出后省略",
            "最多两行，并加高内容区",
            "setTitleMaxLines(2)、setTitleTextSizeSp(16)、setTitleGravityStart、"
                    + "setHeight(72dp)。自定义高度要在配置里设好，BaseActivity 会调用 applyToolbarHeight；"
                    + "若在 initView 里才 setHeight，需要再手动调一次 applyToolbarHeight。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleMaxLines(2)
                    .setTitleTextSizeSp(16f)
                    .setTitleGravityStart()
                    .setTitleBold(false)
                    .setHeight(DensityUtil.dp2px(host, 72f));
        }
    },
    CUSTOM_HEIGHT(
            "自定义高度和阴影",
            "加高加阴影",
            "内容区 72dp，阴影 8dp",
            "setHeight(72dp) 指的是标题内容区，Edge-to-Edge 会再叠状态栏高度。"
                    + "setElevationDp(8) 写到 MaterialToolbar。默认 -1 表示不改阴影；传 0 可去掉。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setHeight(DensityUtil.dp2px(host, 72f))
                    .setElevationDp(8f)
                    .setTitleTextSizeSp(20f)
                    .setTitleBold(true);
        }
    },
    FLAT(
            "去掉阴影",
            "无阴影",
            "setElevationDp(0)，标题栏与页面齐平",
            "外壳 AppBarLayout 本身 elevation 是 0。这里显式把 MaterialToolbar 阴影设为 0，"
                    + "避免主题或后续 setElevationDp 留下投影。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setElevationDp(0f)
                    .setBgColor(C.cardSurface);
        }
    },
    MONOSPACE(
            "等宽字体",
            "Mono 0123",
            "系统 monospace，字号来自 dimen",
            "setTitleFontFamily(\"monospace\")、setTitleTextSizeRes(font_size_xxl)。"
                    + "setTitleFontRes 非 0 时会盖过 fontFamily。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleFontFamily("monospace")
                    .setTitleTextSizeRes(D.font_size_xxl)
                    .setTitleBold(true)
                    .setTitleLetterSpacing(0.05f);
        }
    },
    MENU(
            "菜单",
            "菜单",
            "文字按钮、图标按钮和溢出菜单",
            "不走 onCreateOptionsMenu。initView 里 inflateToolbarMenu(menu_toolbar_demo)："
                    + "「完成」常驻文字、「搜索」常驻图标、溢出里是「分享 / 设置」。"
                    + "有右侧 action 时 setTitleGravityStart，避免标题和菜单重叠。",
            false,
            true,
            false,
            false
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleGravityStart()
                    .setElevationDp(2f);
        }
    },
    UNDER_CONTENT(
            "正文伸到标题栏下",
            "透明标题栏",
            "透明 Toolbar 浮在内容上，并打开沉浸标记",
            "setContentUnderToolbar(true) 去掉 ScrollingViewBehavior，AppBar 浮在正文上。"
                    + "setBgColor(transparent)、setElevationDp(0)、setTextColor / setBackIconColor(onPrimary)、"
                    + "setStatusBarIconsLight(true)。setEnableImmersionBar(true) 是历史沉浸字段，"
                    + "Edge-to-Edge 下状态栏本来就是透明的，图标仍由 applyStatusBar 决定。"
                    + "色块从屏幕顶部铺起，上滑会钻进标题栏。",
            true,
            false,
            false,
            true
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setContentUnderToolbar(true)
                    .setEnableImmersionBar(true)
                    .setBgColor(C.transparent)
                    .setTextColor(C.onPrimary)
                    .setBackIconColor(C.onPrimary)
                    .setElevationDp(0f)
                    .setStatusBarIconsLight(true);
        }
    },
    IMMERSIVE_STATUS(
            "沉浸式状态栏",
            "沉浸式",
            "状态栏透明，图片从屏幕顶部铺开",
            "enableImmersionBar() 配合透明标题栏和 setContentUnderToolbar(true)。"
                    + "状态栏、标题栏都还在，轮播和下面几张图从顶部开始，上滑可以继续看。",
            true,
            false,
            false,
            true,
            false,
            false,
            false,
            true,
            null
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setContentUnderToolbar(true)
                    .setEnableImmersionBar(true)
                    .setBgColor(C.transparent)
                    .setTextColor(C.onPrimary)
                    .setBackIconColor(C.onPrimary)
                    .setElevationDp(0f)
                    .setStatusBarIconsLight(true);
        }
    },
    HIDE_TITLE_BAR(
            "隐藏标题栏",
            "",
            "去掉 Activity 标题栏，状态栏保持显示",
            "只重写 hasToolBar() 为 false。状态栏和导航栏仍在，内容会避开它们。",
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            false,
            null
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setBgColor(C.cardSurface);
        }
    },
    HIDE_SYSTEM_BARS(
            "隐藏状态栏",
            "状态栏已盖住",
            "普通模式：去掉状态栏内容，标题栏背景向上盖住",
            "setKeepStatusBarHeight(true)。状态栏图标隐藏后仍保留原高度，"
                    + "这块区域用标题栏背景色铺上，标题不会顶上去。导航栏不动。",
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            null
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setKeepStatusBarHeight(true)
                    .setElevationDp(0f);
        }
    },
    IMMERSIVE_HIDE_STATUS(
            "沉浸式隐藏状态栏",
            "沉浸式",
            "状态栏内容收起，高度仍在，由正文图片盖住",
            "setKeepStatusBarHeight(true)。状态栏图标隐藏后，标题栏仍留出原高度，"
                    + "透明区域里看到的是下面的图片，标题不会顶上去。",
            true,
            false,
            false,
            true,
            false,
            true,
            false,
            true,
            null
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setContentUnderToolbar(true)
                    .setEnableImmersionBar(true)
                    .setKeepStatusBarHeight(true)
                    .setBgColor(C.transparent)
                    .setTextColor(C.onPrimary)
                    .setBackIconColor(C.onPrimary)
                    .setElevationDp(0f)
                    .setStatusBarIconsLight(true);
        }
    },
    TRANSPARENT_ACTIONS(
            "透明栏仅返回和菜单",
            "",
            "状态栏与标题栏都透明，只留返回图标和右侧菜单",
            "setTitle 置空、setBgColor(transparent)、setContentUnderToolbar(true)、setElevationDp(0)、"
                    + "setShowBackButton(true)、setBackIconColor(onPrimary)、setStatusBarIconsLight(true)、"
                    + "setEnableImmersionBar(true)。右侧菜单走 inflateToolbarMenu，溢出图标着成 onPrimary。",
            true,
            true,
            false,
            true,
            false,
            false,
            true,
            false,
            "标题栏没有文字，背景透明。左边是返回，右边是菜单，状态栏同样透明。"
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitle("")
                    .setContentUnderToolbar(true)
                    .setEnableImmersionBar(true)
                    .setBgColor(C.transparent)
                    .setShowBackButton(true)
                    .setBackIconRes(D.icon_fh)
                    .setBackIconColor(C.onPrimary)
                    .setElevationDp(0f)
                    .setStatusBarIconsLight(true);
        }
    },
    RUNTIME(
            "运行时修改",
            "运行时修改",
            "点按钮改 ToolbarConfig，标题栏跟着变",
            "setter 会 notify DataBinding。改高度后调用 applyToolbarHeight()；"
                    + "改背景或 setStatusBarIconsLight 后调用 applyStatusBar()。"
                    + "setContentUnderToolbar 只在 initToolbar 时生效，所以不放在运行时切换里。",
            false,
            false,
            true,
            false
    ) {
        @Override
        public void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config) {
            config.setTitleGravityCenter()
                    .setShowBackButton(true)
                    .setElevationDp(2f);
        }
    };

    private static final class C {
        static final int cardSurface = io.coderf.arklab.common.R.color.cardSurface;
        static final int cardOnSurface = io.coderf.arklab.common.R.color.cardOnSurface;
        static final int themeColor = io.coderf.arklab.common.R.color.themeColor;
        static final int onPrimary = io.coderf.arklab.common.R.color.onPrimary;
        static final int theme_yellow = io.coderf.arklab.common.R.color.theme_yellow;
        static final int theme_red = io.coderf.arklab.common.R.color.theme_red;
        static final int transparent = io.coderf.arklab.common.R.color.transparent;

        private C() {
        }
    }

    private static final class D {
        static final int icon_fh = io.coderf.arklab.common.R.drawable.icon_fh;
        static final int font_size_xxl = io.coderf.arklab.common.R.dimen.font_size_xxl;

        private D() {
        }
    }

    public final String listName;
    public final String toolbarTitle;
    public final String summary;
    public final String detail;
    public final boolean contentUnder;
    public final boolean menu;
    public final boolean runtime;
    public final boolean immersion;
    /** 不使用 Activity 外壳标题栏。 */
    public final boolean noToolbar;
    /** 隐藏状态栏和导航栏。 */
    public final boolean hideSystemBars;
    /** 右侧只放溢出菜单，并把图标着成浅色。 */
    public final boolean lightMenu;
    /** 用轮播和大图把页面撑长。 */
    public final boolean gallery;
    @androidx.annotation.Nullable
    public final String bannerCaption;

    ToolbarDemoStyle(String listName, String toolbarTitle, String summary, String detail) {
        this(listName, toolbarTitle, summary, detail, false, false, false, false);
    }

    ToolbarDemoStyle(String listName, String toolbarTitle, String summary, String detail,
                     boolean contentUnder, boolean menu, boolean runtime, boolean immersion) {
        this(listName, toolbarTitle, summary, detail, contentUnder, menu, runtime, immersion,
                false, false, false, false, null);
    }

    ToolbarDemoStyle(String listName, String toolbarTitle, String summary, String detail,
                     boolean contentUnder, boolean menu, boolean runtime, boolean immersion,
                     boolean noToolbar, boolean hideSystemBars, boolean lightMenu, boolean gallery,
                     @androidx.annotation.Nullable String bannerCaption) {
        this.listName = listName;
        this.toolbarTitle = toolbarTitle;
        this.summary = summary;
        this.detail = detail;
        this.contentUnder = contentUnder;
        this.menu = menu;
        this.runtime = runtime;
        this.immersion = immersion;
        this.noToolbar = noToolbar;
        this.hideSystemBars = hideSystemBars;
        this.lightMenu = lightMenu;
        this.gallery = gallery;
        this.bannerCaption = bannerCaption;
    }

    public abstract void apply(@NonNull ToolbarPreviewActivity host, @NonNull ToolbarConfig config);

    @NonNull
    public static ToolbarDemoStyle from(@androidx.annotation.Nullable String name) {
        if (name == null) {
            return DEFAULT;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return DEFAULT;
        }
    }
}
