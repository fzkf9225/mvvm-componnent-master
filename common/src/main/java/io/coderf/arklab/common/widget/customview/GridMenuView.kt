package io.coderf.arklab.common.widget.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import io.coderf.arklab.common.R
import io.coderf.arklab.common.adapter.GridMenuViewPager2Adapter
import io.coderf.arklab.common.bean.GridMenuBean
import io.coderf.arklab.common.listener.CustomGridMenuAdapterCallback
import io.coderf.arklab.common.listener.OnMenuClickListener
import io.coderf.arklab.common.listener.PagingAdapterListener
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.common.DrawableUtil

/**
 * 分页网格菜单。默认数据模型为 {@link io.coderf.arklab.common.bean.GridMenuBean}；
 * 轻量场景可实现 {@link io.coderf.arklab.common.widget.customview.inter.IGridMenuItem} 后映射为本类。
 * 圆角/描边等外观属性继承自 {@link CornerConstraintLayout}（bgColor、radius、stroke* 等）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/4
 */
open class GridMenuView : CornerConstraintLayout {
    private var lifecycleOwner: LifecycleOwner? = null
    private var fragmentManager: FragmentManager? = null

    /**
     * 指示器高度
     */
    protected var dotHeight: Int? = null

    /**
     * 指示器与底部距离
     */
    protected var dotBottomMargin = 0

    /**
     * 指示器与左侧边距
     */
    protected var dotLeftMargin: Int = 0

    /**
     * 指示器与右侧边距
     */
    protected var dotRightMargin: Int = 0

    /**
     * 指示器内部 margin，点与点之间的间距
     */
    protected var dotPadding: Int = 0

    /**
     * 每行显示几个
     */
    private var columnCount = 4

    /**
     * 一共几行（用于分页）
     */
    private var rowCount = 2

    /**
     * ViewPager 的 topMargin
     */
    protected var pagerTopMargin = 0

    /**
     * label 显示行数
     */
    var labelLines = Int.MAX_VALUE

    /**
     * 是否自适应高度
     */
    protected var isWrap = true

    /**
     * 网格内容左右内边距
     */
    var contentPaddingStart = 0
    var contentPaddingEnd = 0

    /**
     * 指示器与 ViewPager 的间距
     */
    protected var indicatorSpacing = 0

    /**
     * 列间距
     */
    var columnMargin = 0

    /**
     * 行间距；未单独配置时与 columnMargin 相同
     */
    var rowMargin: Int? = null

    /**
     * 是否显示指示器
     */
    var showIndicator = true

    /**
     * View 级默认图标尺寸
     */
    var defaultIconWidth: Int? = null
    var defaultIconHeight: Int? = null

    /**
     * View 级默认 label 样式
     */
    var defaultLabelTextColor: Int? = null
    var defaultLabelTextSize: Float? = null
    var defaultLabelIconMargin: Int? = null

    /**
     * 选中时圆点样式
     */
    protected val defaultDrawableResCurrent: Drawable by lazy {
        DrawableUtil.createCircleDrawable(
            ContextCompat.getColor(context, R.color.white),
            DensityUtil.dp2px(context, 5f)
        )
    }

    /**
     * 未选中时圆点样式
     */
    protected val defaultDrawableResNormal: Drawable by lazy {
        DrawableUtil.createCircleDrawable(
            ContextCompat.getColor(context, R.color.gray),
            DensityUtil.dp2px(context, 5f)
        )
    }

    /**
     * 选中圆点样式
     */
    var drawableResCurrent: Drawable? = null

    /**
     * 未选中圆点样式
     */
    var drawableResNormal: Drawable? = null

    /**
     * 菜单 item 点击事件
     */
    var onMenuClickListener: OnMenuClickListener? = null

    /**
     * 自定义 item 回调
     */
    var customGridMenuAdapterCallback: CustomGridMenuAdapterCallback? = null

    private var pageCallbackRegistered = false

    protected val menuViewPager by lazy {
        ViewPager2(context).apply {
            id = generateViewId()
        }
    }

    protected val viewPagerLayoutParams by lazy {
        LayoutParams(
            0,
            if (isWrap) {
                LayoutParams.WRAP_CONTENT
            } else 0
        ).apply {
            topToTop = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
            bottomToTop = dotsLayout.id
            if (!isWrap) {
                verticalWeight = 1f
            }
            topMargin = this@GridMenuView.pagerTopMargin
        }
    }

    protected var adapter: GridMenuViewPager2Adapter<GridMenuBean>? = null

    protected val dotsLayout by lazy {
        LinearLayout(context).apply {
            id = generateViewId()
            setVerticalGravity(Gravity.CENTER)
            setHorizontalGravity(Gravity.CENTER)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    protected val dotLayoutParams by lazy {
        LayoutParams(
            LayoutParams.WRAP_CONTENT,
            dotHeight ?: LayoutParams.WRAP_CONTENT,
        ).apply {
            leftMargin = dotLeftMargin
            rightMargin = dotRightMargin
            bottomMargin = dotBottomMargin
            topToBottom = menuViewPager.id
            topMargin = this@GridMenuView.indicatorSpacing
            endToEnd = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
        }
    }

    /**
     * 上一次索引位置
     */
    protected var lastPos = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            setDefaultValues()
        } else {
            parseAttributes(context, attrs)
        }
        if (background == null) {
            setBgColorAndRadius(
                ContextCompat.getColor(context, R.color.white),
                DensityUtil.dp2px(context, 16f).toFloat()
            )
        }
        removeAllViews()
        addView(menuViewPager, viewPagerLayoutParams)
        addView(dotsLayout, dotLayoutParams)
    }

    private fun setDefaultValues() {
        dotLeftMargin = DensityUtil.dp2px(context, 12f)
        dotRightMargin = DensityUtil.dp2px(context, 12f)
        dotBottomMargin = DensityUtil.dp2px(context, 12f)
        dotPadding = DensityUtil.dp2px(context, 4f)
        columnCount = 4
        rowCount = 2
        columnMargin = DensityUtil.dp2px(context, 8f)
        rowMargin = null
        pagerTopMargin = DensityUtil.dp2px(context, 18f)
        contentPaddingStart = DensityUtil.dp2px(context, 12f)
        contentPaddingEnd = DensityUtil.dp2px(context, 12f)
        indicatorSpacing = DensityUtil.dp2px(context, 12f)
        isWrap = true
        showIndicator = true
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet) {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.GridMenuView,
            0,
            0
        )

        try {
            dotHeight = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_dotHeight,
                0
            )
            if (dotHeight == 0) {
                dotHeight = null
            }
            isWrap = typedArray.getBoolean(R.styleable.GridMenuView_isWrap, true)
            showIndicator = typedArray.getBoolean(R.styleable.GridMenuView_showIndicator, true)
            labelLines = typedArray.getInt(R.styleable.GridMenuView_labelLines, Int.MAX_VALUE)
            dotBottomMargin = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_dotBottomMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotLeftMargin = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_dotLeftMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotRightMargin = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_dotRightMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotPadding = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_dotPadding,
                DensityUtil.dp2px(context, 4f)
            )
            columnCount = typedArray.getInt(R.styleable.GridMenuView_columnCount, 4)
            rowCount = typedArray.getInt(R.styleable.GridMenuView_rowCount, 2)
            pagerTopMargin = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_pagerTopMargin,
                DensityUtil.dp2px(context, 18f)
            )
            contentPaddingStart = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_contentPaddingStart,
                DensityUtil.dp2px(context, 12f)
            )
            contentPaddingEnd = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_contentPaddingEnd,
                DensityUtil.dp2px(context, 12f)
            )
            indicatorSpacing = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_indicatorSpacing,
                DensityUtil.dp2px(context, 12f)
            )
            columnMargin = typedArray.getDimensionPixelSize(
                R.styleable.GridMenuView_columnMargin,
                DensityUtil.dp2px(context, 8f)
            )
            if (typedArray.hasValue(R.styleable.GridMenuView_rowMargin)) {
                rowMargin = typedArray.getDimensionPixelSize(
                    R.styleable.GridMenuView_rowMargin,
                    columnMargin
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_iconWidth)) {
                defaultIconWidth = typedArray.getDimensionPixelSize(
                    R.styleable.GridMenuView_iconWidth,
                    0
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_iconHeight)) {
                defaultIconHeight = typedArray.getDimensionPixelSize(
                    R.styleable.GridMenuView_iconHeight,
                    0
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_labelTextColor)) {
                defaultLabelTextColor = typedArray.getColor(
                    R.styleable.GridMenuView_labelTextColor,
                    0
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_labelTextSize)) {
                defaultLabelTextSize = typedArray.getDimension(
                    R.styleable.GridMenuView_labelTextSize,
                    0f
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_labelIconMargin)) {
                defaultLabelIconMargin = typedArray.getDimensionPixelSize(
                    R.styleable.GridMenuView_labelIconMargin,
                    0
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_dotSelectedDrawable)) {
                drawableResCurrent = ContextCompat.getDrawable(
                    context,
                    typedArray.getResourceId(R.styleable.GridMenuView_dotSelectedDrawable, 0)
                )
            }
            if (typedArray.hasValue(R.styleable.GridMenuView_dotNormalDrawable)) {
                drawableResNormal = ContextCompat.getDrawable(
                    context,
                    typedArray.getResourceId(R.styleable.GridMenuView_dotNormalDrawable, 0)
                )
            }
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * 绑定生命周期和 FragmentManager
     */
    fun bindLifecycle(lifecycleOwner: LifecycleOwner, fragmentManager: FragmentManager) {
        this.lifecycleOwner = lifecycleOwner
        this.fragmentManager = fragmentManager
    }

    fun <T : GridMenuBean> initData(menuList: List<T>?) {
        if (menuList.isNullOrEmpty()) {
            return
        }
        if (fragmentManager == null) {
            throw IllegalArgumentException("fragmentManager is null")
        }

        if (lifecycleOwner == null) {
            throw IllegalArgumentException("lifecycleOwner is null")
        }
        val newList = menuList.chunked(columnCount * rowCount)
        initImageRounds(newList)
        adapter = GridMenuViewPager2Adapter(
            this,
            newList
        )
        menuViewPager.adapter = adapter
        menuViewPager.setCurrentItem(0, false)
    }

    /**
     * 计算 ViewPager 底部小圆点
     */
    private fun <T : GridMenuBean> initImageRounds(menuList: List<List<T>>?) {
        dotsLayout.removeAllViews()
        val pageCount = menuList?.size ?: 0
        dotsLayout.visibility = if (showIndicator && pageCount > 1) {
            VISIBLE
        } else {
            GONE
        }
        lastPos = 0
        if (pageCount <= 1) {
            return
        }
        for (i in 0 until pageCount) {
            val round = AppCompatImageView(context)
            round.background = if (i == 0) {
                drawableResCurrent ?: defaultDrawableResCurrent
            } else {
                drawableResNormal ?: defaultDrawableResNormal
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (i > 0) {
                params.leftMargin = dotPadding
            }
            dotsLayout.addView(round, params)
        }
    }

    /**
     * 监听滑动实现底部点的显示
     */
    protected val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val pageSize = adapter?.pagerInfo?.size ?: 1
                if (pageSize <= 0 || dotsLayout.childCount == 0) {
                    return
                }
                val realPos: Int = position % pageSize
                dotsLayout.getChildAt(realPos)?.background =
                    drawableResCurrent ?: defaultDrawableResCurrent
                if (lastPos >= 0 && lastPos < dotsLayout.size && lastPos != realPos) {
                    dotsLayout.getChildAt(lastPos)?.background =
                        drawableResNormal ?: defaultDrawableResNormal
                }
                lastPos = realPos
            }
        }

    private val adapterListener: PagingAdapterListener<GridMenuBean> =
        object : PagingAdapterListener<GridMenuBean> {
            override fun onItemClick(
                view: View?,
                item: GridMenuBean?,
                position: Int
            ) {
                onMenuClickListener?.onMenuClick(
                    view,
                    adapter?.getItem(menuViewPager.currentItem),
                    item
                )
            }

            override fun onItemLongClick(
                view: View?,
                item: GridMenuBean?,
                position: Int
            ) {
                onMenuClickListener?.onMenuLongClick(
                    view,
                    adapter?.getItem(menuViewPager.currentItem),
                    item
                )
            }
        }

    fun getFragmentManager(): FragmentManager? {
        return fragmentManager
    }

    fun getLifecycleOwner(): LifecycleOwner? {
        return lifecycleOwner
    }

    fun getMenuListener(): OnMenuClickListener? {
        return onMenuClickListener
    }

    fun getColumnCount(): Int {
        return columnCount
    }

    fun getRowMarginOrDefault(): Int {
        return rowMargin ?: columnMargin
    }

    fun getAdapterListener(): PagingAdapterListener<GridMenuBean>? {
        return adapterListener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!pageCallbackRegistered) {
            menuViewPager.registerOnPageChangeCallback(onPageChangeCallback)
            pageCallbackRegistered = true
        }
    }

    override fun onDetachedFromWindow() {
        if (pageCallbackRegistered) {
            menuViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
            pageCallbackRegistered = false
        }
        super.onDetachedFromWindow()
    }
}
