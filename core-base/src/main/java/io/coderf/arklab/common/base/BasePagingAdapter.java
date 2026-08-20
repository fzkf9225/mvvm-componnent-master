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
 * updated by fz on 2024/10/31
 * describe：Paging 列表；可选在 ConcatAdapter 中前置自定义头（见 {@link #setPagingHeaderAdapter}）。
 */
public abstract class BasePagingAdapter<T, VDB extends ViewDataBinding> extends PagingDataAdapter<T, BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    public PagingAdapterListener<T> onPagingAdapterListener;

    @Nullable
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingHeaderAdapter;

    public BasePagingAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    /**
     * 在列表数据之前插入的 RecyclerView.Adapter（如横幅、筛选条等），默认 null 表示无头布局。
     * 需在 Fragment 将本 Adapter 与 Footer 组装为 {@link androidx.recyclerview.widget.ConcatAdapter} 之前设置
     * （见 {@link io.coderf.arklab.common.base.BaseSmartPagingFragment#initView} / {@link io.coderf.arklab.common.base.BasePagingFragment#initView}）。
     */
    public void setPagingHeaderAdapter(@Nullable RecyclerView.Adapter<? extends RecyclerView.ViewHolder> pagingHeaderAdapter) {
        this.pagingHeaderAdapter = pagingHeaderAdapter;
    }

    @Nullable
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getPagingHeaderAdapter() {
        return pagingHeaderAdapter;
    }

    /**
     * ConcatAdapter 中位于本 PagingAdapter 之前的条目数，用于点击回调里将绝对 position 换算为分页项下标。
     */
    public int getPagingLeadingExtraItemCount() {
        return pagingHeaderAdapter != null ? pagingHeaderAdapter.getItemCount() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int pos) {
        onBindHolder(baseViewHolder, getItem(pos), pos);
    }

    /**
     * 设置数据
     *
     * @param holder
     * @param pos
     */
    public abstract void onBindHolder(BaseViewHolder<VDB> holder, T item, int pos);

    @NotNull
    @Override
    public BaseViewHolder<VDB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutId(), parent, false));
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @param binding item布局
     * @return BaseViewHolder
     */
    protected BaseViewHolder<VDB> createViewHold(VDB binding) {
        return new BaseViewHolder<>(binding,this);
    }

    /**
     * itemView的资源布局
     *
     * @return 布局资源Id
     */
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
