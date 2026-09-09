package io.coderf.arklab.demo.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.List;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ItemSearchSuggestionBinding;

/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/9 9:03
 */
public class SuggestionAdapter extends BaseRecyclerViewAdapter<String, ItemSearchSuggestionBinding> {

    public SuggestionAdapter(OnPick onPick) {
        this.onPick = onPick;
    }

    public interface OnPick {
        void onPick(@NonNull String text);
    }

    private final OnPick onPick;

    @SuppressLint("NotifyDataSetChanged")
    public void submit(@NonNull List<String> data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemSearchSuggestionBinding> holder, int pos) {
        String text = mList.get(pos);
        holder.getBinding().tvSuggestion.setText(text);
        holder.getBinding().tvSuggestion.setOnClickListener(v -> onPick.onPick(text));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_search_suggestion;
    }
}

