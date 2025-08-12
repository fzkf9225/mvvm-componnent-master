package pers.fz.mvvm.widget.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import pers.fz.mvvm.R
import pers.fz.mvvm.adapter.Viewpager2MenuAdapter
import pers.fz.mvvm.bean.HomeMenuBean
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
open class HomeMenuView : ConstraintLayout, DefaultLifecycleObserver {
    private var lifecycleOwner: LifecycleOwner? = null
    private var fragmentManager: FragmentManager? = null

    private var dotHeight = DensityUtil.dp2px(context, 26f)
    private var dotBottomMargin = 0
    private var dotLeftMargin = DensityUtil.dp2px(context, 12f)
    private var dotRightMargin = DensityUtil.dp2px(context, 12f)
    private var dotPadding = DensityUtil.dp2px(context, 4f)

    /**
     * 选中时圆点样式
     */
    private val defaultDrawableResCurrent: Drawable by lazy {
        createDrawable(
            ContextCompat.getColor(context, R.color.white),
            DensityUtil.dp2px(context, 10f),
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

    var drawableResCurrent: Drawable? = null

    var drawableResNormal: Drawable? = null

    companion object {
        /**
         * 一行有多少列
         */
        const val COLUMN = 4

        /**
         * 一共多少航
         */
        const val ROW = 2
    }

    var onMenuClickListener: OnMenuClickListener? = null

    private val menuViewPager by lazy {
        ViewPager2(context).apply {
            setId(generateViewId())
            registerOnPageChangeCallback(onPageChangeCallback)
        }
    }

    private val viewPagerLayoutParams by lazy {
        LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            //viewPager的定位
            topToTop = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
            topMargin = DensityUtil.dp2px(context, 18f)
        }
    }

    private val adapter by lazy {
        Viewpager2MenuAdapter(
            fragmentManager!!,
            lifecycleOwner?.lifecycle!!,
            menuList,
            COLUMN,
            adapterListener
        )
    }

    private val dotsLayout by lazy {
        //初始化指针
        LinearLayout(context).apply {
            setId(generateViewId())
            setVerticalGravity(Gravity.CENTER)
            setHorizontalGravity(Gravity.CENTER)
            orientation = LinearLayout.HORIZONTAL
        }
    }

    private val dotLayoutParams by lazy {
        LayoutParams(
            LayoutParams.WRAP_CONTENT,
            dotHeight.toInt()
        ).apply {
            leftMargin = dotLeftMargin.toInt()
            rightMargin = dotRightMargin.toInt()
            bottomMargin = dotBottomMargin.toInt()
            topToBottom = menuViewPager.id
            endToEnd = LayoutParams.PARENT_ID;
            startToStart = LayoutParams.PARENT_ID;
        }
    }


    /**
     * 上一次索引位置
     */
    private var lastPos = -1

    /**
     * 菜单数据
     */
    private var menuList: MutableList<MutableList<HomeMenuBean>>? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    public fun bindLifecycle(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        lifecycleOwner.lifecycle.addObserver(this)
    }

    public fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun createDrawable(
        color: Int,
        width: Int,
        height: Int,
        cornerRadius: Float = DensityUtil.dp2px(context, 10f).toFloat()
    ): ShapeDrawable {
        val shape = RoundRectShape(
            floatArrayOf(
                cornerRadius, cornerRadius, // 左上角
                cornerRadius, cornerRadius, // 右上角
                cornerRadius, cornerRadius, // 右下角
                cornerRadius, cornerRadius  // 左下角
            ), null, null
        )
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.setColor(color)
        shapeDrawable.paint.isAntiAlias = true // 启用抗锯齿
        shapeDrawable.setIntrinsicWidth(width)
        shapeDrawable.setIntrinsicHeight(height)
        return shapeDrawable
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        //设置指针和ViewPager定位
        removeAllViews()
        addView(menuViewPager, viewPagerLayoutParams)
        addView(dotsLayout, dotLayoutParams)
        background = ContextCompat.getDrawable(context, R.drawable.rounded_white_16)
    }

    fun initData(
        menuList: MutableList<HomeMenuBean>
    ) {
        this.menuList = menuList.chunked(COLUMN * ROW) as MutableList<MutableList<HomeMenuBean>>?
        if (this.menuList.isNullOrEmpty()) {
            return
        }
        initImageRounds()
        menuViewPager.setAdapter(adapter)
        menuViewPager.setCurrentItem(0, false)
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private fun initImageRounds() {
        dotsLayout.removeAllViews()
        /*
         *当轮播图大于1张时小圆点显示
         */
        if ((this.menuList?.size ?: 0) > 1) {
            dotsLayout.visibility = VISIBLE
        } else {
            dotsLayout.visibility = INVISIBLE
        }
        lastPos = 0
        /*
         * 默认让第一张图片显示深颜色的圆点
         */
        IntStream.range(0, (this.menuList?.size ?: 0)).forEach(IntConsumer { i: Int ->
            val round = AppCompatImageView(context)
            if (i == 0) {
                round.background = drawableResCurrent ?: defaultDrawableResCurrent
            } else {
                round.background = drawableResNormal ?: defaultDrawableResNormal
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2)
            params.leftMargin = dotPadding.toInt()
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
                val realPos: Int = position % (menuList?.size ?: 1)
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
                    adapter.getItem(menuViewPager.currentItem),
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
                    adapter.getItem(menuViewPager.currentItem),
                    item
                )
            }

        }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        menuViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }
}