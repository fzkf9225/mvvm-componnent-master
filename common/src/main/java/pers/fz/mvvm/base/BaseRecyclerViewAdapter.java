package pers.fz.mvvm.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

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
    private int headerViewId;
    private View headerView;
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

    public BaseRecyclerViewAdapter(Context context) {
        mContext = context;
        mList = (mList == null) ? new ArrayList<>() : mList;
        setList(mList);
    }

    public BaseRecyclerViewAdapter(Context context, List<T> list) {
        mContext = context;
        mList = (list == null) ? new ArrayList<T>() : list;
        setList(mList);
    }

    public void setHeaderView(@LayoutRes int headerViewRes) {
        this.headerViewId = headerViewRes;
        headerView = LayoutInflater.from(mContext).inflate(headerViewRes, null);
        this.headerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
        notifyItemInserted(0);
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public void addAll(List<T> items) {
        if (items != null) {
            this.mList.addAll(items);
            notifyItemRangeInserted(this.mList.size(), items.size());
        }
    }

    public int getHeaderViewId() {
        return headerViewId;
    }

    public View getHeaderView() {
        return headerView;
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder baseViewHolder, final int pos) {
        if (getItemViewType(pos) == TYPE_HEAD) {
            if (baseViewHolder == null) {
                return;
            }
            onBindHeaderHolder(baseViewHolder, getRealPosition(baseViewHolder));
            return;
        }
        baseViewHolder.getBinding().getRoot().setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(baseViewHolder.getBinding().getRoot(), getRealPosition(baseViewHolder));
            }
        });
        baseViewHolder.getBinding().getRoot().setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(baseViewHolder.getBinding().getRoot(), getRealPosition(baseViewHolder));
                return true;
            }
            return false;
        });
        onBindHolder(baseViewHolder, getRealPosition(baseViewHolder));
    }

    /**
     * 设置数据
     *
     * @param holder
     * @param pos
     */
    public void onBindHeaderHolder(final BaseViewHolder holder, final int pos) {

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
        if (headerView != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (headerView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            return TYPE_HEAD;
        }
        return TYPE_NORMAL;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD && getHeaderView() != null) {
            return createHeaderViewHold(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), getHeaderViewId(), parent, false));
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
        return new BaseViewHolder<>(binding,true);
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
        return headerView == null ? position : position - 1;
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

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        mOnItemLongClickListener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
