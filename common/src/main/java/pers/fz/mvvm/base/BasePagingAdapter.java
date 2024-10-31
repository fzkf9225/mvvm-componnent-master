package pers.fz.mvvm.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.listener.PagingAdapterListener;
import pers.fz.mvvm.wight.recyclerview.SimpleItemTouchHelperCallback;

/**
 * updated by fz on 2024/10/31
 * describe：paging分页，添加头布局的时候有bug，暂时搞不定
 */
public abstract class BasePagingAdapter<T, VDB extends ViewDataBinding> extends PagingDataAdapter<T, BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    protected final String TAG = this.getClass().getSimpleName();
    protected RecyclerView mRecyclerView;

    public PagingAdapterListener<T> onPagingAdapterListener;

    public BasePagingAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder baseViewHolder, final int pos) {
        baseViewHolder.getBinding().getRoot().setOnClickListener(v -> {
            if (onPagingAdapterListener != null) {
                onPagingAdapterListener.onItemClick(v, getItem(pos), pos);
            }
        });
        baseViewHolder.getBinding().getRoot().setOnLongClickListener(v -> {
            if (onPagingAdapterListener != null) {
                onPagingAdapterListener.onItemLongClick(v, getItem(pos), pos);
                return true;
            }
            return false;
        });
        onBindHolder(baseViewHolder, getItem(pos), pos);
    }

    /**
     * 设置数据
     *
     * @param holder
     * @param pos
     */
    public abstract void onBindHolder(final BaseViewHolder<VDB> holder, T item, final int pos);


    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutId(), parent, false));
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @param binding item布局
     * @return BaseViewHolder
     */
    protected BaseViewHolder<VDB> createViewHold(VDB binding) {
        return new BaseViewHolder<>(binding);
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
    public void onViewRecycled(@NonNull BaseViewHolder<VDB> holder) {
        super.onViewRecycled(holder);
        try {
            if (holder.getBinding() == null) {
                holder.itemView.setOnClickListener(null);
                return;
            }
            holder.getBinding().getRoot().setOnClickListener(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int from, int to) {
        notifyItemMoved(from, to);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public void setOnAdapterListener(PagingAdapterListener<T> l) {
        onPagingAdapterListener = l;
    }

}
