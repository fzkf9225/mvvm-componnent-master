package io.coderf.arklab.demo.adapter;

import com.bumptech.glide.Glide;

import java.util.List;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ItemCarouselBannerBinding;

/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/9 9:01
 */
public class HeroCarouselAdapter extends BaseRecyclerViewAdapter<String, ItemCarouselBannerBinding> {
    public HeroCarouselAdapter(List<String> list) {
        super(list);
    }

    public HeroCarouselAdapter() {
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemCarouselBannerBinding> holder, int pos) {
        Glide.with(holder.getBinding().carouselImage.getContext()).load(mList.get(pos)).into(holder.getBinding().carouselImage);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_carousel_banner;
    }
}

