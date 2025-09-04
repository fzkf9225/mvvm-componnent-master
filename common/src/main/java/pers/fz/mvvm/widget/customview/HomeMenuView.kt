package pers.fz.mvvm.widget.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import pers.fz.mvvm.R
import pers.fz.mvvm.adapter.HomeMenuViewPager2Adapter
import pers.fz.mvvm.bean.HomeMenuBean
import pers.fz.mvvm.listener.CustomHomeMenuAdapterCallback
import pers.fz.mvvm.listener.OnMenuClickListener
import pers.fz.mvvm.listener.PagingAdapterListener
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.util.common.DrawableUtil
import java.util.function.IntConsumer
import java.util.stream.IntStream

/**
 * created by fz on 2025/4/27 15:59
 * describe:
 */
open class HomeMenuView : ConstraintLayout {
    private var fragmentManager: FragmentManager? = null

    /**
     * 指示器高度
     */
    private var dotHeight: Int? = null

    /**
     * 指示器与底部距离
     */
    private var dotBottomMargin = 0

    /**
     * 指示器与左侧边距
     */
    private var dotLeftMargin: Int = 0

    /**
     * 指示器与右侧边距
     */
    private var dotRightMargin: Int = 0

    /**
     * 指示器内部margin，点与点之间的间距
     */
    private var dotPadding: Int = 0

    /**
     * 每行显示几个
     */
    private var columnCount = 4

    /**
     * 一共几行
     */
    private var rowCount = 2

    /**
     * viewPager的topMargin
     */
    private var topMargin = 0

    /**
     * label显示行数
     */
    var labelLines = Int.MAX_VALUE

    /**
     * 是否自适应高度
     */
    private var isWrap = true

    /**
     * viewPager的leftMargin
     */
    var startMargin = 0

    /**
     * viewPager的rightMargin
     */
    var endMargin = 0

    /**
     * 指示器与viewPager的间隔
     */
    private var bottomMargin = 0

    /**
     * 列间距
     */
    private var columnMargin = 0

    /**
     * 背景样式资源，与下面二选一
     */
    private var backgroundDrawableRes: Drawable? = null

    /**
     * 背景颜色，与上面二选一
     */
    private var backgroundColor: Int? = null

    /**
     * 背景圆角，与上面二选一
     */
    private var backgroundCornerRadius: Float? = 0f

    /**
     * 选中时圆点样式
     */
    private val defaultDrawableResCurrent: Drawable by lazy {
        DrawableUtil.createCircleDrawable(
            ContextCompat.getColor(context, R.color.white),
            DensityUtil.dp2px(context, 5f)
        )
    }

    /**
     * 未选中时圆点样式
     */
    private val defaultDrawableResNormal: Drawable by lazy {
        DrawableUtil.createCircleDrawable(
            ContextCompat.getColor(context, R.color.gray),
            DensityUtil.dp2px(context, 5f)
        )
    }

    /**
     * 选中原点样式
     */
    var drawableResCurrent: Drawable? = null

    /**
     * 未选中原点样式
     */
    var drawableResNormal: Drawable? = null

    /**
     * 菜单item点击事件
     */
    var onMenuClickListener: OnMenuClickListener? = null

    /**
     * 自定义item回调
     */
    var customHomeMenuAdapterCallback: CustomHomeMenuAdapterCallback? = null


    private val menuViewPager by lazy {
        ViewPager2(context).apply {
            id = generateViewId()
            registerOnPageChangeCallback(onPageChangeCallback)
        }
    }

    private val viewPagerLayoutParams by lazy {
        LayoutParams(
            0,
            if (isWrap) {
                LayoutParams.WRAP_CONTENT
            } else 0
        ).apply {
            // viewPager的定位
            topToTop = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
            bottomToTop = dotsLayout.id
            if (!isWrap) {
                verticalWeight = 1f
            }
            topMargin = this@HomeMenuView.topMargin
        }
    }

    private var adapter: HomeMenuViewPager2Adapter<HomeMenuBean>? = null

    private val dotsLayout by lazy {
        // 初始化指针
        LinearLayout(context).apply {
            id = generateViewId()
            setVerticalGravity(Gravity.CENTER)
            setHorizontalGravity(Gravity.CENTER)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    private val dotLayoutParams by lazy {
        LayoutParams(
            LayoutParams.WRAP_CONTENT,
            dotHeight ?: LayoutParams.WRAP_CONTENT,
        ).apply {
            leftMargin = dotLeftMargin
            rightMargin = dotRightMargin
            bottomMargin = dotBottomMargin
            topToBottom = menuViewPager.id
            topMargin = this@HomeMenuView.bottomMargin
            endToEnd = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
        }
    }

    /**
     * 上一次索引位置
     */
    private var lastPos = 0

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
        // 设置指针和ViewPager定位
        removeAllViews()
        addView(menuViewPager, viewPagerLayoutParams)
        addView(dotsLayout, dotLayoutParams)
        // 设置背景（优先级：drawable > 颜色 > 默认）
        background = when {
            backgroundDrawableRes != null -> backgroundDrawableRes
            backgroundColor != null -> {
                DrawableUtil.createRectDrawable(
                    backgroundColor!!,
                    0, 0, backgroundCornerRadius ?: 0f
                )
            }

            else -> ContextCompat.getDrawable(context, R.drawable.rounded_white_16)
        }
    }

    private fun setDefaultValues() {
        // 设置代码创建时的默认值
        dotLeftMargin = DensityUtil.dp2px(context, 12f)
        dotRightMargin = DensityUtil.dp2px(context, 12f)
        dotBottomMargin = DensityUtil.dp2px(context, 12f)
        dotPadding = DensityUtil.dp2px(context, 4f)
        columnCount = 4
        rowCount = 2
        columnMargin = DensityUtil.dp2px(context, 8f)
        backgroundCornerRadius = DensityUtil.dp2px(context, 16f).toFloat()
        // 新增的margin属性默认值
        topMargin = DensityUtil.dp2px(context, 18f)
        startMargin = DensityUtil.dp2px(context, 12f)
        endMargin = DensityUtil.dp2px(context, 12f)
        bottomMargin = DensityUtil.dp2px(context, 12f)
        isWrap = true
    }

    /**
     * 获取资源配置
     */
    private fun parseAttributes(context: Context, attrs: AttributeSet) {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.HomeMenuView,
            0,
            0
        )

        try {
            dotHeight = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_dotHeight,
                0
            )
            if (dotHeight == 0) {
                dotHeight = null;
            }
            isWrap = typedArray.getBoolean(R.styleable.HomeMenuView_isWrap, true)
            labelLines = typedArray.getInt(R.styleable.HomeMenuView_labelLines, Int.MAX_VALUE)
            dotBottomMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_dotBottomMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotLeftMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_dotLeftMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotRightMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_dotRightMargin,
                DensityUtil.dp2px(context, 12f)
            )
            dotPadding = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_dotPadding,
                DensityUtil.dp2px(context, 4f)
            )
            columnCount = typedArray.getInt(R.styleable.HomeMenuView_columnCount, 4)
            rowCount = typedArray.getInt(R.styleable.HomeMenuView_rowCount, 2)
            topMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_topMargin,
                DensityUtil.dp2px(context, 18f)
            )
            // 解析新增的margin属性
            startMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_startMargin,
                DensityUtil.dp2px(context, 12f)
            )
            endMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_endMargin,
                DensityUtil.dp2px(context, 12f)
            )
            bottomMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_bottomMargin,
                DensityUtil.dp2px(context, 12f)
            )
            columnMargin = typedArray.getDimensionPixelSize(
                R.styleable.HomeMenuView_columnMargin,
                DensityUtil.dp2px(context, 8f)
            )
            // 其他属性解析...
            backgroundCornerRadius = typedArray.getDimension(
                R.styleable.HomeMenuView_backgroundCornerRadius,
                DensityUtil.dp2px(context, 16f).toFloat()
            )

            // 解析背景颜色
            if (typedArray.hasValue(R.styleable.HomeMenuView_backgroundColor)) {
                backgroundColor = typedArray.getColor(R.styleable.HomeMenuView_backgroundColor, 0)
            }

            // 解析背景drawable
            if (typedArray.hasValue(R.styleable.HomeMenuView_backgroundDrawable)) {
                backgroundDrawableRes = ContextCompat.getDrawable(
                    context,
                    typedArray.getResourceId(R.styleable.HomeMenuView_backgroundDrawable, 0)
                )
            }
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * 绑定FragmentManager
     */
    public fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun <T : HomeMenuBean> initData(menuList: List<T>?) {
        if (menuList.isNullOrEmpty()) {
            return
        }
        if (fragmentManager == null) {
            throw IllegalArgumentException("fragmentManager is null")
        }
        val newList = menuList.chunked(columnCount * rowCount)
        initImageRounds(newList)
        adapter = HomeMenuViewPager2Adapter(
            this,
            newList
        )
        menuViewPager.adapter = adapter
        menuViewPager.setCurrentItem(0, false)
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private fun <T : HomeMenuBean> initImageRounds(menuList: List<List<T>>?) {
        dotsLayout.removeAllViews()
        /*
         *当轮播图大于1张时小圆点显示
         */
        if ((menuList?.size ?: 0) > 1) {
            dotsLayout.visibility = VISIBLE
        } else {
            dotsLayout.visibility = INVISIBLE
        }
        lastPos = 0
        /*
         * 默认让第一张图片显示深颜色的圆点
         */
        IntStream.range(0, (menuList?.size ?: 0)).forEach(IntConsumer { i: Int ->
            val round = AppCompatImageView(context)
            if (i == 0) {
                round.background = drawableResCurrent ?: defaultDrawableResCurrent
            } else {
                round.background = drawableResNormal ?: defaultDrawableResNormal
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2)
            params.leftMargin = dotPadding
            dotsLayout.addView(round, params)
        })
    }

    /**
     * 监听滑动实现底部点的显示
     */
    protected val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val realPos: Int = position % (adapter?.pagerInfo?.size ?: 1)
                dotsLayout.getChildAt(realPos).background =
                    drawableResCurrent ?: defaultDrawableResCurrent
                if (lastPos >= 0 && lastPos < dotsLayout.size && lastPos != realPos) {
                    dotsLayout.getChildAt(lastPos).background =
                        drawableResNormal ?: defaultDrawableResNormal
                }
                lastPos = realPos
            }
        }

    private val adapterListener: PagingAdapterListener<HomeMenuBean> =
        object : PagingAdapterListener<HomeMenuBean> {
            override fun onItemClick(
                view: View?,
                item: HomeMenuBean?,
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
                item: HomeMenuBean?,
                position: Int
            ) {
                onMenuClickListener?.onMenuLongClick(
                    view,
                    adapter?.getItem(menuViewPager.currentItem),
                    item
                )
            }
        }

    // 提供设置背景的方法
    fun setMenuBackground(drawable: Drawable?) {
        backgroundDrawableRes = drawable
        background = drawable
    }

    fun setMenuBackgroundColor(color: Int) {
        backgroundColor = color
        background = DrawableUtil.createRectDrawable(color, 0, 0, backgroundCornerRadius ?: 0f)
    }

    fun setMenuBackgroundCornerRadius(radius: Float) {
        backgroundCornerRadius = radius
        if (backgroundColor != null) {
            background = DrawableUtil.createRectDrawable(backgroundColor!!, 0, 0, radius)
        }
    }

    fun getFragmentManager(): FragmentManager? {
        return fragmentManager
    }

    fun getMenuListener(): OnMenuClickListener? {
        return onMenuClickListener
    }

    fun getColumnCount(): Int {
        return columnCount
    }

    fun getAdapterListener(): PagingAdapterListener<HomeMenuBean>? {
        return adapterListener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        menuViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }
}