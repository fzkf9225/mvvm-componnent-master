package io.coderf.arklab.demo.config;

/**
 * TabLayout 示例页运行时配置，由 Hub 页修改，各 Sample Fragment 进入时读取并应用。
 */
public final class TabLayoutDemoConfig {

    private static final TabLayoutDemoConfig INSTANCE = new TabLayoutDemoConfig();

    /** StyledTabLayout：选中文字大小（sp） */
    public float styledSelectedTextSizeSp = 12f;
    /** StyledTabLayout：未选中文字大小（sp） */
    public float styledUnselectedTextSizeSp = 12f;
    /** StyledTabLayout：选中是否加粗 */
    public boolean styledSelectedBold = true;
    /** StyledTabLayout：选中背景圆角（dp） */
    public int styledCornerRadiusDp = 23;
    /** StyledTabLayout：选中文字颜色预设索引 */
    public int styledSelectedColorIndex = 4;
    /** StyledTabLayout：未选中文字颜色预设索引 */
    public int styledUnselectedColorIndex = 5;

    /** Fixed TabLayout：指示条固定宽度（dp） */
    public int fixedIndicatorWidthDp = 24;
    /** Fixed TabLayout：指示条高度（dp） */
    public int fixedIndicatorHeightDp = 3;
    /** Fixed TabLayout：选中文字大小（sp） */
    public float fixedSelectedTextSizeSp = 14f;
    /** Fixed TabLayout：未选中文字大小（sp） */
    public float fixedUnselectedTextSizeSp = 12f;
    /** Fixed TabLayout：选中是否加粗 */
    public boolean fixedSelectedBold = true;
    /** Fixed TabLayout：选中文字颜色预设索引 */
    public int fixedSelectedColorIndex = 0;
    /** Fixed TabLayout：未选中文字颜色预设索引 */
    public int fixedUnselectedColorIndex = 5;
    /** Fixed TabLayout：指示条颜色预设索引 */
    public int fixedIndicatorColorIndex = 0;

    /** IndicatorTabLayout：指示条宽度（dp，fixed 模式） */
    public int indicatorWidthDp = 12;
    /** IndicatorTabLayout：指示条高度（dp） */
    public int indicatorHeightDp = 4;
    /** IndicatorTabLayout：指示条圆角（dp） */
    public int indicatorCornerRadiusDp = 8;
    /** IndicatorTabLayout：选中文字大小（sp） */
    public float indicatorSelectedTextSizeSp = 14f;
    /** IndicatorTabLayout：未选中文字大小（sp） */
    public float indicatorUnselectedTextSizeSp = 12f;
    /** IndicatorTabLayout：选中是否加粗 */
    public boolean indicatorSelectedBold = true;
    /** IndicatorTabLayout：选中文字颜色预设索引 */
    public int indicatorSelectedColorIndex = 4;
    /** IndicatorTabLayout：未选中文字颜色预设索引 */
    public int indicatorUnselectedColorIndex = 5;
    /** IndicatorTabLayout：指示条颜色预设索引 */
    public int indicatorBarColorIndex = 0;

    private TabLayoutDemoConfig() {
    }

    public static TabLayoutDemoConfig get() {
        return INSTANCE;
    }
}
