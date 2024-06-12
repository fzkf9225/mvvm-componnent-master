package pers.fz.mvvm.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import pers.fz.mvvm.listener.OnHeaderViewClickListener;
import pers.fz.mvvm.wight.recyclerview.SimpleItemTouchHelperCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by fz on 2017/12/28.
 */
public abstract class BaseRecyclerViewAdapter<T, VDB extends ViewDataBinding> extends RecyclerView.Adapter<BaseViewHolder<VDB>> implements
        SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    protected final String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected List<T> mList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
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

    public BaseRecyclerViewAdapter(Context context) {
        mContext = context;
        setList(mList);
    }

    public BaseRecyclerViewAdapter(Context context, List<T> list) {
        mContext = context;
        mList = (list == null) ? new ArrayList<T>() : list;
        setList(mList);
    }

    public boolean hasHeaderView() {
        return getHeaderViewId() != -1 || headerView != null;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
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

    public int getHeaderViewId() {
        return -1;
    }


    @Override
    public void onBindViewHolder(final BaseViewHolder baseViewHolder, final int pos) {
        if (getItemViewType(pos) == TYPE_HEAD) {
            if (getHeaderViewId() != -1) {
                baseViewHolder.getBinding().getRoot().setOnClickListener(v -> {
                    if (onHeaderViewClickListener != null) {
                        onHeaderViewClickListener.onHeaderViewClick(v);
                    }
                });
                baseViewHolder.getBinding().getRoot().setOnLongClickListener(v -> {
                    if (onHeaderViewClickListener != null) {
                        onHeaderViewClickListener.onHeaderViewLongClick(v);
                        return true;
                    }
                    return false;
                });
            } else {
                headerView.setOnClickListener(v -> {
                    if (onHeaderViewClickListener != null) {
                        onHeaderViewClickListener.onHeaderViewClick(v);
                    }
                });
                headerView.setOnLongClickListener(v -> {
                    if (onHeaderViewClickListener != null) {
                        onHeaderViewClickListener.onHeaderViewLongClick(v);
                        return true;
                    }
                    return false;
                });
            }
            onBindHeaderHolder(baseViewHolder);
            return;
        }
        int realPosition = getRealPosition(baseViewHolder);
        baseViewHolder.getBinding().getRoot().setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getRealPosition(baseViewHolder));
            }
        });
        baseViewHolder.getBinding().getRoot().setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, getRealPosition(baseViewHolder));
                return true;
            }
            return false;
        });
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
        int count = (mList == null ? 0 : mList.size());
        if (getHeaderViewId() != -1) {
            return count + 1;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getHeaderViewId() == -1 && headerView == null) {
            return TYPE_NORMAL;
        }
        if (getHeaderViewId() != -1 && position == 0) {
            return TYPE_HEAD;
        }
        if (headerView != null && position == 0) {
            return TYPE_HEAD;
        }
        return TYPE_NORMAL;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD && getHeaderViewId() != -1) {
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
        return new BaseViewHolder<>(binding);
    }

    /**
     * 获取自定义viewHolder对象，如果不自定义借直接返回baseViewHolder
     *
     * @return BaseViewHolder
     */
    protected BaseViewHolder createHeaderViewHold(View headerView) {
        return new BaseViewHolder<>(headerView);
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

    public int getItemViewHeight() {
        return 0;
    }

    public void setList(List<T> list) {
        this.mList = list;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return (getHeaderViewId() == -1 && headerView == null) ? position : position - 1;
    }

    public List<T> getList() {
        return mList;
    }

    public void setList(T[] list) {
        ArrayList<T> arrayList = new ArrayList<T>(list == null ? 0 : list.length);
        if (list != null) {
            arrayList.addAll(Arrays.asList(list));
        }
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

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
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
