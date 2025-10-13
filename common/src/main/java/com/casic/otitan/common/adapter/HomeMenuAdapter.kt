package com.casic.otitan.common.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.TextUtils
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.casic.otitan.common.R
import com.casic.otitan.common.base.BasePagingAdapter
import com.casic.otitan.common.base.BaseViewHolder
import com.casic.otitan.common.bean.HomeMenuBean
import com.casic.otitan.common.databinding.AdapterHomeMenuItemBinding
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.widget.customview.HomeMenuView

/**
 * created by fz on 2024/10/10 14:39
 * describe:工作菜单
 */
class HomeMenuAdapter(val homeMenuView: HomeMenuView?) :
    BasePagingAdapter<HomeMenuBean, AdapterHomeMenuItemBinding>(
        COMPARATOR
    ) {
    override fun onBindHolder(
        holder: BaseViewHolder<AdapterHomeMenuItemBinding>,
        item: HomeMenuBean,
        pos: Int
    ) {
        holder.binding.imageMenuIcon.setImageResource(item.icon)
        holder.binding.tvWorkMenuName.text = item.title
        val iconLayoutParams =
            holder.binding.imageMenuIcon.layoutParams as ConstraintLayout.LayoutParams
        iconLayoutParams.width =
            item.iconWidth?.toInt() ?: DensityUtil.dp2px(holder.itemView.context, 27f)
        iconLayoutParams.height =
            item.iconHeight?.toInt() ?: DensityUtil.dp2px(holder.itemView.context, 27f)
        holder.binding.imageMenuIcon.layoutParams = iconLayoutParams

        holder.binding.tvWorkMenuName.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            item.labelSize ?: DensityUtil.sp2px(holder.itemView.context, 14f).toFloat()
        )

        holder.binding.tvWorkMenuName.setTextColor(
            item.labelColor ?: ContextCompat.getColor(holder.itemView.context, R.color.autoColor)
        )

        val textLayoutParams =
            holder.binding.tvWorkMenuName.layoutParams as ConstraintLayout.LayoutParams
        textLayoutParams.topMargin =
            item.iconTextMargin?.toInt() ?: DensityUtil.dp2px(holder.itemView.context, 10f)
        holder.binding.tvWorkMenuName.layoutParams = textLayoutParams

        if (item.isGray == true) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f) // 0表示完全灰度，1表示原色
            val colorFilter = ColorMatrixColorFilter(matrix)
            holder.binding.imageMenuIcon.colorFilter = colorFilter
//            holder.binding.tvWorkMenuName.paint.colorFilter = colorFilter
//            holder.binding.tvWorkMenuName.invalidate()
        } else {
            holder.binding.imageMenuIcon.clearColorFilter()
//            holder.binding.tvWorkMenuName.paint.colorFilter = null
//            holder.binding.tvWorkMenuName.invalidate()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.adapter_home_menu_item
    }

    override fun createViewHold(binding: AdapterHomeMenuItemBinding): BaseViewHolder<AdapterHomeMenuItemBinding> {
        return ViewHolder(binding, this)
    }

    public class ViewHolder : BaseViewHolder<AdapterHomeMenuItemBinding> {
        constructor(
            binding: AdapterHomeMenuItemBinding,
            adapter: HomeMenuAdapter
        ) : super(binding, adapter) {
            if (adapter.homeMenuView?.labelLines == 1) {
                // 设置为单行显示
                binding.tvWorkMenuName.maxLines = 1
                binding.tvWorkMenuName.ellipsize = TextUtils.TruncateAt.END
            } else {
                binding.tvWorkMenuName.maxLines =
                    adapter.homeMenuView?.labelLines ?: Int.MAX_VALUE
                binding.tvWorkMenuName.ellipsize = null // 清除省略号
            }
        }
    }


    companion object {
        private val COMPARATOR: DiffUtil.ItemCallback<HomeMenuBean> =
            object : DiffUtil.ItemCallback<HomeMenuBean>() {
                override fun areItemsTheSame(
                    oldItem: HomeMenuBean,
                    newItem: HomeMenuBean
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: HomeMenuBean,
                    newItem: HomeMenuBean
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}