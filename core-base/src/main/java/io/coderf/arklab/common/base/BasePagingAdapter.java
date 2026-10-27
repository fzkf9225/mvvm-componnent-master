package io.coderf.arklab.common.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import io.coderf.arklab.common.listener.PagingAdapterListener;
import io.coderf.arklab.common.widget.recyclerview.SimpleItemTouchHelperCallback;

/**
 * Paging 列表；可选 Concat 前置业务 Header（见 {@link #setPagingHeaderAdapter}）。
 *
 * <p>列表数据由 {@link #submitData} 驱动；单条增删请走数据源（如 Room insert 后
 * {@code refreshData}/{@code adapter.refresh()}），勿在本 Adapter 内伪造 itemCount。</p>
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/10/27 移除 Live 内存段，单条插入改由数据源驱动
 */
public abstract class BasePagingAdapter<T, VDB extends ViewDataBinding> extends PagingDataAdapter<T, BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    public PagingAdapterListener<T> onPagingAdapterListener;

    @Nullable
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingHeaderAdapter;

    public BasePagingAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void setPagingHeaderAdapter(@Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingHeaderAdapter) {
        this.pagingHeaderAdapter = pagingHeaderAdapter;
    }

    @Nullable
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getPagingHeaderAdapter() {
        return pagingHeaderAdapter;
    }

    /**
     * Concat 中位于本 PagingAdapter 之前的条目数（业务 Header）。
     */
    public int getPagingLeadingExtraItemCount() {
        return pagingHeaderAdapter != null ? pagingHeaderAdapter.getItemCount() : 0;
    }

    /**
     * 内容条数（不含业务 Header / LoadState），用于空态。
     */
    public int getContentItemCount() {
        return getItemCount();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int pos) {
        onBindHolder(baseViewHolder, getItem(pos), pos);
    }

    public abstract void onBindHolder(BaseViewHolder<VDB> holder, T item, int pos);

    @NotNull
    @Override
    public BaseViewHolder<VDB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutId(), parent, false));
    }

    protected BaseViewHolder<VDB> createViewHold(VDB binding) {
        return new BaseViewHolder<>(binding, this);
    }

    protected abstract int getLayoutId();

    public T getAdapterItem(int pos) {
        return getItem(pos);
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int from, int to) {
        notifyItemMoved(from, to);
    }

    public void setOnAdapterListener(PagingAdapterListener<T> l) {
        onPagingAdapterListener = l;
    }
}
