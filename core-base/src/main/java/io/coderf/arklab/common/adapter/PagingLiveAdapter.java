package io.coderf.arklab.common.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.listener.PagingAdapterListener;

/**
 * Paging3 列表「实时最新」Adapter：会话内动态插入、与历史分页同类型的 item。
 *
 * <p>与 {@link io.coderf.arklab.common.base.BasePagingAdapter#setPagingLiveAdapter} 配合，
 * Concat 顺序为：<b>Header? → Live(本 Adapter) → Paging(历史) → LoadState Footer</b>。</p>
 *
 * <p>{@link #append} 插入到 <b>index 0</b>（最新在第一条）。刷新前建议 {@link #clearLive()}。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/22
 */
public abstract class PagingLiveAdapter<T, VDB extends ViewDataBinding>
        extends BaseRecyclerViewAdapter<T, VDB> {

    @Nullable
    public PagingAdapterListener<T> onPagingAdapterListener;

    @Override
    public final Integer getHeaderViewId() {
        return null;
    }

    @Override
    public final void setHeaderView(View headerView) {
        // no-op
    }

    @Override
    public final boolean hasHeaderView() {
        return false;
    }

    /**
     * 插入一条最新数据到 index 0。
     */
    public void append(@Nullable T item) {
        if (item == null) {
            return;
        }
        mList.add(0, item);
        notifyItemInserted(0);
    }

    /**
     * 在最前插入多条。
     */
    public void appendAll(@Nullable List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        mList.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
    }

    /**
     * 清空实时段。
     */
    public void clearLive() {
        int size = mList.size();
        if (size == 0) {
            return;
        }
        mList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public int removeIf(@NonNull Predicate<T> predicate) {
        int removed = 0;
        for (int i = mList.size() - 1; i >= 0; i--) {
            if (predicate.test(mList.get(i))) {
                mList.remove(i);
                notifyItemRemoved(i);
                removed++;
            }
        }
        return removed;
    }

    public void submitList(@Nullable List<T> list) {
        mList.clear();
        if (list != null && !list.isEmpty()) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setOnPagingAdapterListener(@Nullable PagingAdapterListener<T> listener) {
        this.onPagingAdapterListener = listener;
    }

    @Override
    protected BaseViewHolder<VDB> createViewHold(VDB binding) {
        return new BaseViewHolder<>(binding, this, true);
    }

    @Nullable
    public T getLiveItem(int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    @NonNull
    public List<T> snapshot() {
        return new ArrayList<>(mList);
    }

    public boolean anyMatch(@NonNull Predicate<T> predicate) {
        for (T t : mList) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(@Nullable T item) {
        if (item == null) {
            return false;
        }
        for (T t : mList) {
            if (Objects.equals(t, item)) {
                return true;
            }
        }
        return false;
    }
}
