package pers.fz.mvvm.helper;

import androidx.recyclerview.widget.RecyclerView;

import pers.fz.mvvm.base.BasePagingAdapter;

/**
 * created by fz on 2024/10/31 16:14
 * describe:
 */
public class HeaderAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    private final BasePagingAdapter<?, ?> pagingDataAdapter;

    public HeaderAdapterDataObserver(BasePagingAdapter<?,?> pagingDataAdapter) {
        this.pagingDataAdapter = pagingDataAdapter;
    }

    @Override
    public void onChanged() {
        // 数据集变化时重新计算总项数
        pagingDataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        // 插入新项时重新计算总项数
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        // 移除项时重新计算总项数
        pagingDataAdapter.notifyItemRangeRemoved(positionStart + 1, itemCount);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        // 移动项时重新计算总项数
        pagingDataAdapter.notifyItemMoved(fromPosition + 1, toPosition + 1);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        // 项变化时重新计算总项数
        pagingDataAdapter.notifyItemRangeChanged(positionStart + 1, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        // 项变化时重新计算总项数
        pagingDataAdapter.notifyItemRangeChanged(positionStart + 1, itemCount, payload);
    }
}

