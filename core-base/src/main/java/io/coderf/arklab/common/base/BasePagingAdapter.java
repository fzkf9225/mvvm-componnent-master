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
 * Paging 列表；可选 Concat 前置 Header / Live（见
 * {@link #setPagingHeaderAdapter} / {@link #setPagingLiveAdapter}）。
 *
 * <p>历史由 {@link #submitData} 驱动；会话内动态数据用
 * {@link io.coderf.arklab.common.adapter.PagingLiveAdapter}，勿在本 Adapter 内伪造 itemCount。</p>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/10/22 Live 命名与 Concat 前置
 */
public abstract class BasePagingAdapter<T, VDB extends ViewDataBinding> extends PagingDataAdapter<T, BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    public PagingAdapterListener<T> onPagingAdapterListener;

    @Nullable
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingHeaderAdapter;

    @Nullable
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingLiveAdapter;

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
     * 会话内实时最新段，默认 null。
     * 组装顺序：Header? → <b>Live</b> → Paging → LoadState；
     * 新数据插在 Live 的 index 0（见 {@link io.coderf.arklab.common.adapter.PagingLiveAdapter#append}）。
     */
    public void setPagingLiveAdapter(@Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingLiveAdapter) {
        this.pagingLiveAdapter = pagingLiveAdapter;
    }

    @Nullable
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getPagingLiveAdapter() {
        return pagingLiveAdapter;
    }

    /**
     * Concat 中位于本 PagingAdapter 之前的条目数（业务 Header + Live）。
     */
    public int getPagingLeadingExtraItemCount() {
        int count = 0;
        if (pagingHeaderAdapter != null) {
            count += pagingHeaderAdapter.getItemCount();
        }
        if (pagingLiveAdapter != null) {
            count += pagingLiveAdapter.getItemCount();
        }
        return count;
    }

    /**
     * 实时 Live 段条目数。
     */
    public int getPagingLiveItemCount() {
        return pagingLiveAdapter != null ? pagingLiveAdapter.getItemCount() : 0;
    }

    /**
     * 分页 + Live 总条数（不含业务 Header / LoadState），用于空态。
     */
    public int getContentItemCount() {
        return getItemCount() + getPagingLiveItemCount();
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
