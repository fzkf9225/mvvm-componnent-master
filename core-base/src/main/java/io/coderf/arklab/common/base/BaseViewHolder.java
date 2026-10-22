package io.coderf.arklab.common.base;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * updated by fz on 2024/12/28.
 * RecyclerView的通用ViewHolder，此次修改主要把单击和长按事件放在了ViewHolder中，减少滑动带来的资源消耗
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class BaseViewHolder<VDB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private VDB binding;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public BaseViewHolder(@NotNull VDB binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public <T, HVDB extends ViewDataBinding> BaseViewHolder(@NonNull View headerView, boolean isClickable, BaseRecyclerViewAdapter<T, HVDB> adapter) {
        super(headerView);
        if (!isClickable) {
            return;
        }
        itemView.setOnClickListener(v -> {
            if (adapter.onHeaderViewClickListener != null) {
                adapter.onHeaderViewClickListener.onHeaderViewClick(v);
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.onHeaderViewClickListener != null) {
                adapter.onHeaderViewClickListener.onHeaderViewLongClick(v);
                return true;
            }
            return false;
        });
    }

    public <T, HVDB extends ViewDataBinding> BaseViewHolder(@NotNull VDB binding, boolean isClickable, BaseRecyclerViewAdapter<T, HVDB> adapter) {
        this(binding.getRoot(),isClickable,adapter);
        this.binding = binding;
    }

    public <T> BaseViewHolder(@NotNull VDB binding, BaseRecyclerViewAdapter<T, VDB> adapter) {
        this(binding, adapter, false);
    }

    /**
     * @param useBindingAdapterPosition true 时用于 ConcatAdapter 子段（如 {@link io.coderf.arklab.common.adapter.PagingLiveAdapter}），
     *                                  点击回调使用 bindingAdapterPosition，避免 Header/Paging 段偏移。
     */
    public <T> BaseViewHolder(@NotNull VDB binding, BaseRecyclerViewAdapter<T, VDB> adapter, boolean useBindingAdapterPosition) {
        super(binding.getRoot());
        this.binding = binding;
        itemView.setOnClickListener(v -> {
            int pos = resolveRecyclerPosition(adapter, useBindingAdapterPosition);
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            if (adapter instanceof io.coderf.arklab.common.adapter.PagingLiveAdapter) {
                @SuppressWarnings("unchecked")
                io.coderf.arklab.common.adapter.PagingLiveAdapter<T, VDB> live =
                        (io.coderf.arklab.common.adapter.PagingLiveAdapter<T, VDB>) adapter;
                if (live.onPagingAdapterListener != null) {
                    T item = live.getLiveItem(pos);
                    if (item != null) {
                        live.onPagingAdapterListener.onItemClick(v, item, pos);
                    }
                    return;
                }
            }
            if (adapter.mOnItemClickListener != null) {
                adapter.mOnItemClickListener.onItemClick(v, pos);
            }
        });
        itemView.setOnLongClickListener(v -> {
            int pos = resolveRecyclerPosition(adapter, useBindingAdapterPosition);
            if (pos == RecyclerView.NO_POSITION) {
                return true;
            }
            if (adapter instanceof io.coderf.arklab.common.adapter.PagingLiveAdapter) {
                @SuppressWarnings("unchecked")
                io.coderf.arklab.common.adapter.PagingLiveAdapter<T, VDB> live =
                        (io.coderf.arklab.common.adapter.PagingLiveAdapter<T, VDB>) adapter;
                if (live.onPagingAdapterListener != null) {
                    T item = live.getLiveItem(pos);
                    if (item != null) {
                        live.onPagingAdapterListener.onItemLongClick(v, item, pos);
                    }
                    return true;
                }
            }
            if (adapter.mOnItemLongClickListener != null) {
                adapter.mOnItemLongClickListener.onItemLongClick(v, pos);
                return true;
            }
            return false;
        });
    }

    private <T> int resolveRecyclerPosition(BaseRecyclerViewAdapter<T, ?> adapter, boolean useBindingAdapterPosition) {
        if (useBindingAdapterPosition) {
            return getBindingAdapterPosition();
        }
        int absPos = getAbsoluteAdapterPosition();
        if (absPos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }
        return adapter.hasHeaderView() ? (absPos - 1) : absPos;
    }

    public <T> BaseViewHolder(@NonNull VDB binding, BasePagingAdapter<T, VDB> adapter) {
        super(binding.getRoot());
        this.binding = binding;
        // ConcatAdapter 下使用 bindingAdapterPosition，自动忽略 Header/Tail 段偏移
        itemView.setOnClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                int pagingPos = getBindingAdapterPosition();
                if (pagingPos == RecyclerView.NO_POSITION) {
                    return;
                }
                T item = adapter.getAdapterItem(pagingPos);
                if (item != null) {
                    adapter.onPagingAdapterListener.onItemClick(v, item, pagingPos);
                }
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (adapter.onPagingAdapterListener != null) {
                int pagingPos = getBindingAdapterPosition();
                if (pagingPos == RecyclerView.NO_POSITION) {
                    return true;
                }
                T item = adapter.getAdapterItem(pagingPos);
                if (item != null) {
                    adapter.onPagingAdapterListener.onItemLongClick(v, item, pagingPos);
                }
                return true;
            }
            return false;
        });
    }

    public VDB getBinding() {
        return binding;
    }

}
