package io.coderf.arklab.common.base

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 通用 ViewHolder。
 *
 * 单击和长按在创建时绑到 itemView，不在 onBind 里反复注册，避免滑动时重复分配监听器。
 *
 * 两套列表的坐标含义不同，按 Adapter 类型分开绑，不能合成一套：
 * - [BaseRecyclerViewAdapter] 把头布局和内容放在同一个 Adapter 里，回调下标是数据下标
 *   （有头时为 absoluteAdapterPosition - 1）。
 * - [BasePagingAdapter] 的业务 Header 是 ConcatAdapter 里的前置 Adapter，回调下标用
 *   bindingAdapterPosition，并带上当前位置的 item。
 *
 * 对外构造与原先一致，子类仍按 `(binding)` / `(binding, adapter)` / `(binding, isClickable, adapter)` 调用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/20 收敛重复的点击绑定，行为与原 Java 构造保持一致
 */
open class BaseViewHolder<VDB : ViewDataBinding>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var bindingOrNull: VDB? = null

    /**
     * item 的 DataBinding。只传入裸 [View] 的头布局没有 binding，此时与原先一样为 null。
     * 声明成非空是为了保持原来 Java 字段的调用方式，Kotlin 侧不用被迫加空判断。
     */
    var binding: VDB
        get() {
            @Suppress("UNCHECKED_CAST")
            return bindingOrNull as VDB
        }
        private set(value) {
            bindingOrNull = value
        }

    constructor(binding: VDB) : this(binding.root) {
        this.binding = binding
    }

    constructor(
        headerView: View,
        isClickable: Boolean,
        adapter: BaseRecyclerViewAdapter<*, *>,
    ) : this(headerView) {
        if (isClickable) {
            bindHeaderClicks(adapter)
        }
    }

    constructor(
        binding: VDB,
        isClickable: Boolean,
        adapter: BaseRecyclerViewAdapter<*, *>,
    ) : this(binding.root, isClickable, adapter) {
        this.binding = binding
    }

    constructor(
        binding: VDB,
        adapter: BaseRecyclerViewAdapter<*, VDB>,
    ) : this(binding.root) {
        this.binding = binding
        bindRecyclerItemClicks(adapter)
    }

    constructor(
        binding: VDB,
        adapter: BasePagingAdapter<*, VDB>,
    ) : this(binding.root) {
        this.binding = binding
        bindPagingItemClicks(adapter)
    }

    /** 头布局：有监听才消费长按，避免没有监听时抢走父级的长按。 */
    private fun bindHeaderClicks(adapter: BaseRecyclerViewAdapter<*, *>) {
        itemView.setOnClickListener { v ->
            adapter.onHeaderViewClickListener?.onHeaderViewClick(v)
        }
        itemView.setOnLongClickListener { v ->
            val listener = adapter.onHeaderViewClickListener ?: return@setOnLongClickListener false
            listener.onHeaderViewLongClick(v)
            true
        }
    }

    /**
     * 普通列表项。脱离窗口（[RecyclerView.NO_POSITION]）时单击直接忽略；
     * 长按仍返回 true，避免事件落到一个已经失效的位置上。
     */
    private fun bindRecyclerItemClicks(adapter: BaseRecyclerViewAdapter<*, VDB>) {
        itemView.setOnClickListener { v ->
            val pos = recyclerItemPosition(adapter)
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            adapter.mOnItemClickListener?.onItemClick(v, pos)
        }
        itemView.setOnLongClickListener { v ->
            val pos = recyclerItemPosition(adapter)
            if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
            val listener = adapter.mOnItemLongClickListener ?: return@setOnLongClickListener false
            listener.onItemLongClick(v, pos)
            true
        }
    }

    /**
     * [BaseRecyclerViewAdapter] 的点击下标是 [BaseRecyclerViewAdapter.mList] 的下标。
     * 头布局占第 0 位时，内容项的 absoluteAdapterPosition 比数据下标大 1。
     */
    private fun recyclerItemPosition(adapter: BaseRecyclerViewAdapter<*, *>): Int {
        val absPos = absoluteAdapterPosition
        if (absPos == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION
        return if (adapter.hasHeaderView()) absPos - 1 else absPos
    }

    @Suppress("UNCHECKED_CAST")
    private fun bindPagingItemClicks(adapter: BasePagingAdapter<*, VDB>) {
        // 监听器和 item 共用同一个 T，擦除后这个转换是安全的。
        bindPagingItemClicksTyped(adapter as BasePagingAdapter<Any, VDB>)
    }

    /**
     * Paging 项。业务 Header / Footer 在 ConcatAdapter 的其他段里，
     * 所以用 bindingAdapterPosition，不再手动减头布局。
     * item 为 null（例如占位）时不回调；只要装了监听器，长按就消费掉。
     */
    private fun <T> bindPagingItemClicksTyped(adapter: BasePagingAdapter<T, VDB>) {
        itemView.setOnClickListener { v ->
            val listener = adapter.onPagingAdapterListener ?: return@setOnClickListener
            val pagingPos = bindingAdapterPosition
            if (pagingPos == RecyclerView.NO_POSITION) return@setOnClickListener
            val item = adapter.getAdapterItem(pagingPos) ?: return@setOnClickListener
            listener.onItemClick(v, item, pagingPos)
        }
        itemView.setOnLongClickListener { v ->
            val listener = adapter.onPagingAdapterListener ?: return@setOnLongClickListener false
            val pagingPos = bindingAdapterPosition
            if (pagingPos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
            val item = adapter.getAdapterItem(pagingPos)
            if (item != null) {
                listener.onItemLongClick(v, item, pagingPos)
            }
            true
        }
    }
}
