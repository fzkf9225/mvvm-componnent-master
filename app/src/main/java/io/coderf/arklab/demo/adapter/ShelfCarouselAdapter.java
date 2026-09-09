package io.coderf.arklab.demo.adapter;

import com.bumptech.glide.Glide;

import java.util.List;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ItemCarouselShelfBinding;

/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/9 9:06
 */
public class ShelfCarouselAdapter extends BaseRecyclerViewAdapter<String, ItemCarouselShelfBinding> {
    public ShelfCarouselAdapter() {
    }

    public ShelfCarouselAdapter(List<String> list) {
        super(list);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemCarouselShelfBinding> holder, int pos) {
        Glide.with(holder.getBinding().carouselImage).load(mList.get(pos)).into(holder.getBinding().carouselImage);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_carousel_shelf;
    }
}

