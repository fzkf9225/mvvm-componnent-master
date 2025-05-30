package pers.fz.mvvm.adapter

import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import pers.fz.mvvm.R
import pers.fz.mvvm.base.BasePagingAdapter
import pers.fz.mvvm.base.BaseViewHolder
import pers.fz.mvvm.bean.HomeMenuBean
import pers.fz.mvvm.databinding.AdapterHomeMenuItemBinding

/**
 * created by fz on 2024/10/10 14:39
 * describe:工作菜单
 */
class HomeMenuAdapter : BasePagingAdapter<HomeMenuBean, AdapterHomeMenuItemBinding>(
    COMPARATOR
) {
    override fun onBindHolder(
        holder: BaseViewHolder<AdapterHomeMenuItemBinding>,
        item: HomeMenuBean,
        pos: Int
    ) {
        Glide.with(holder.itemView.context).load(item.icon).into(holder.binding.imageMenuIcon)
        holder.binding.tvWorkMenuName.text = item.title
    }

    override fun getLayoutId(): Int {
        return R.layout.adapter_home_menu_item
    }


    companion object {
        private val COMPARATOR: DiffUtil.ItemCallback<HomeMenuBean> =
            object : DiffUtil.ItemCallback<HomeMenuBean>() {
                override fun areItemsTheSame(oldItem: HomeMenuBean, newItem: HomeMenuBean): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: HomeMenuBean, newItem: HomeMenuBean): Boolean {
                    return oldItem == newItem
                }
            }
    }
}