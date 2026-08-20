package io.coderf.arklab.common.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.TextUtils
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import io.coderf.arklab.common.R
import io.coderf.arklab.common.base.BaseViewHolder
import io.coderf.arklab.common.bean.GridMenuBean
import io.coderf.arklab.common.databinding.AdapterGridMenuItemBinding
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.widget.customview.GridMenuView

/**
 * created by fz on 2024/10/10 14:39
 * describe:工作菜单
 */
class GridMenuAdapter(val gridMenuView: GridMenuView?) :
    io.coderf.arklab.common.base.BasePagingAdapter<GridMenuBean, AdapterGridMenuItemBinding>(
        COMPARATOR
    ) {
    override fun onBindHolder(
        holder: BaseViewHolder<AdapterGridMenuItemBinding>,
        item: GridMenuBean,
        pos: Int
    ) {
        holder.binding.imageMenuIcon.setImageResource(item.icon)
        holder.binding.tvWorkMenuName.text = item.title
        val iconLayoutParams =
            holder.binding.imageMenuIcon.layoutParams as ConstraintLayout.LayoutParams
        iconLayoutParams.width = item.iconWidth?.toInt()
            ?: gridMenuView?.defaultIconWidth
            ?: DensityUtil.dp2px(holder.itemView.context, 27f)
        iconLayoutParams.height = item.iconHeight?.toInt()
            ?: gridMenuView?.defaultIconHeight
            ?: DensityUtil.dp2px(holder.itemView.context, 27f)
        holder.binding.imageMenuIcon.layoutParams = iconLayoutParams

        holder.binding.tvWorkMenuName.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            item.labelSize
                ?: gridMenuView?.defaultLabelTextSize
                ?: DensityUtil.sp2px(holder.itemView.context, 14f).toFloat()
        )

        holder.binding.tvWorkMenuName.setTextColor(
            item.labelColor
                ?: gridMenuView?.defaultLabelTextColor
                ?: ContextCompat.getColor(holder.itemView.context, R.color.autoColor)
        )

        val textLayoutParams =
            holder.binding.tvWorkMenuName.layoutParams as ConstraintLayout.LayoutParams
        textLayoutParams.topMargin = item.iconTextMargin?.toInt()
            ?: gridMenuView?.defaultLabelIconMargin
            ?: DensityUtil.dp2px(holder.itemView.context, 10f)
        holder.binding.tvWorkMenuName.layoutParams = textLayoutParams

        if (item.isGray == true) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val colorFilter = ColorMatrixColorFilter(matrix)
            holder.binding.imageMenuIcon.colorFilter = colorFilter
        } else {
            holder.binding.imageMenuIcon.clearColorFilter()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.adapter_grid_menu_item
    }

    override fun createViewHold(binding: AdapterGridMenuItemBinding): BaseViewHolder<AdapterGridMenuItemBinding> {
        return ViewHolder(binding, this)
    }

    class ViewHolder : BaseViewHolder<AdapterGridMenuItemBinding> {
        constructor(
            binding: AdapterGridMenuItemBinding,
            adapter: GridMenuAdapter
        ) : super(binding, adapter) {
            val lines = adapter.gridMenuView?.labelLines ?: Int.MAX_VALUE
            if (lines == 1) {
                binding.tvWorkMenuName.maxLines = 1
                binding.tvWorkMenuName.ellipsize = TextUtils.TruncateAt.END
            } else if (lines < Int.MAX_VALUE) {
                binding.tvWorkMenuName.maxLines = lines
                binding.tvWorkMenuName.ellipsize = TextUtils.TruncateAt.END
            } else {
                binding.tvWorkMenuName.maxLines = Int.MAX_VALUE
                binding.tvWorkMenuName.ellipsize = null
            }
        }
    }


    companion object {
        private val COMPARATOR: DiffUtil.ItemCallback<GridMenuBean> =
            object : DiffUtil.ItemCallback<GridMenuBean>() {
                override fun areItemsTheSame(
                    oldItem: GridMenuBean,
                    newItem: GridMenuBean
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: GridMenuBean,
                    newItem: GridMenuBean
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
