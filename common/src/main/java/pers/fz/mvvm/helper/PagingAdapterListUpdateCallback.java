package pers.fz.mvvm.helper;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

/**
 * created by fz on 2024/10/31 17:03
 * describe:
 */
public class PagingAdapterListUpdateCallback implements ListUpdateCallback {
    @NonNull
    private final RecyclerView.Adapter mAdapter;

    /**
     * Creates an AdapterListUpdateCallback that will dispatch update events to the given adapter.
     *
     * @param adapter The Adapter to send updates to.
     */
    public PagingAdapterListUpdateCallback(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    /** {@inheritDoc} */
    @Override
    public void onInserted(int position, int count) {
        mAdapter.notifyItemRangeInserted(position, count);
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoved(int position, int count) {
        mAdapter.notifyItemRangeRemoved(position, count);
    }

    /** {@inheritDoc} */
    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressLint("UnknownNullness") // b/240775049: Cannot annotate properly
    public void onChanged(int position, int count, Object payload) {
        mAdapter.notifyItemRangeChanged(position, count, payload);
    }
}

