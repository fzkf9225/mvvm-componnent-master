package com.casic.otitan.common.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.casic.otitan.common.listener.OnHeaderViewClickListener;
import com.casic.otitan.common.widget.recyclerview.SimpleItemTouchHelperCallback;

/**
 * updated by fz on 2024/12/10.
 */
public abstract class BaseRecyclerViewAdapter<T, VDB extends ViewDataBinding> extends RecyclerView.Adapter<BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    protected final String TAG = this.getClass().getSimpleName();
    protected final List<T> mList = new ArrayList<>();
    /**
     * itemView的类型，头布局类型
     */
    public static final int TYPE_HEAD = 1;
    /**
     * itemView的类型，正常类型
     */
    public static final int TYPE_NORMAL = 0;

    public OnItemClickListener mOnItemClickListener;
    public OnItemLongClickListener mOnItemLongClickListener;

    public OnHeaderViewClickListener onHeaderViewClickListener;
    /**
     * 头布局，优先级没有直接getHeaderViewId高
     */
    private View headerView;

    public BaseRecyclerViewAdapter() {
    }

    public BaseRecyclerViewAdapter(List<T> list) {
        setList(list);
    }

    /**
     * 是否有头布局
     * @return true为有头布局
     */
    public boolean hasHeaderView() {
        return getHeaderViewId() != null || headerView != null;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    public View getHeaderView() {
        return headerView;
    }

    public void addAll(List<T> items) {
        if (items != null) {
            this.mList.addAll(items);
            notifyItemRangeInserted(this.mList.size(), items.size());
        }
    }

    public Integer getHeaderViewId() {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseViewHolder baseViewHolder, final int pos) {
        if (getItemViewType(pos) == TYPE_HEAD) {
            onBindHeaderHolder(baseViewHolder);
            return;
        }
        int realPosition = hasHeaderView() ? (pos - 1) : pos;
        onBindHolder(baseViewHolder, realPosition);
    }

    /**
     * 设置数据
     *
     * @param holder
     */
    public void onBindHeaderHolder(final BaseViewHolder holder) {

    }

    /**
     * 设置数据
     *
     * @param holder
     * @param pos
     */
    public abstract void onBindHolder(final BaseViewHolder<VDB> holder, final int pos);

    @Override
    public int getItemCount() {
        int count = mList.size();
        if (hasHeaderView()) {
            return count + 1;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeaderView() && position == 0) {
            return TYPE_HEAD;
        }
        return TYPE_NORMAL;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD && getHeaderViewId() != null) {
            return createHeaderViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getHeaderViewId(), parent, false));
        } else if (viewType == TYPE_HEAD && headerView != null) {
            return createHeaderViewHold(headerView);
        } else {
            return createViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getLayoutId(), parent, false));
        }
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @param binding item布局
     * @return BaseViewHolder
     */
    protected <HVDB extends ViewDataBinding> BaseViewHolder<HVDB> createHeaderViewHold(HVDB binding) {
        return new BaseViewHolder<>(binding, true, this);
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @return BaseViewHolder
     */
    protected BaseViewHolder createHeaderViewHold(View headerView) {
        return new BaseViewHolder<>(headerView, true, this);
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @param binding item布局
     * @return BaseViewHolder
     */
    protected BaseViewHolder<VDB> createViewHold(VDB binding) {
        return new BaseViewHolder<>(binding, this);
    }

    /**
     * itemView的资源布局
     *
     * @return 布局资源Id
     */
    protected abstract int getLayoutId();

    public void setList(List<T> list) {
        this.mList.clear();
        if (list != null && !list.isEmpty()) {
            this.mList.addAll(list);
        }
    }

    public List<T> getList() {
        return mList;
    }

    public void setList(T[] list) {
        ArrayList<T> arrayList = list != null ? new ArrayList<>(Arrays.asList(list)) : new ArrayList<>();
        setList(arrayList);
    }

    @Override
    public void onItemDismiss(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int from, int to) {
        Collections.swap(mList, from, to);
        notifyItemMoved(from, to);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        mOnItemLongClickListener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void setOnHeaderViewClickListener(OnHeaderViewClickListener onHeaderViewClickListener) {
        this.onHeaderViewClickListener = onHeaderViewClickListener;
    }
}
