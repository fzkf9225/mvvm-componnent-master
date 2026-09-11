package io.coderf.arklab.common.widget.gallery.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.databinding.ItemPreviewAlbumBinding;
import io.coderf.arklab.common.widget.gallery.PreviewAlbumDialog;
import io.coderf.arklab.common.widget.gallery.PreviewInfoBean;

/**
 * 仿微信相册宫格适配器。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
public class PreviewAlbumAdapter extends BaseRecyclerViewAdapter<PreviewInfoBean, ItemPreviewAlbumBinding> {

    private final PreviewAlbumDialog previewAlbumDialog;
    private int selectedPosition;

    public PreviewAlbumAdapter(PreviewAlbumDialog previewAlbumDialog) {
        this.previewAlbumDialog = previewAlbumDialog;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_preview_album;
    }

    @Override
    protected BaseViewHolder<ItemPreviewAlbumBinding> createViewHold(ItemPreviewAlbumBinding binding) {
        return new ViewHolder(binding, this);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemPreviewAlbumBinding> holder, int pos) {
        PreviewInfoBean item = mList.get(pos);
        Glide.with(holder.itemView.getContext())
                .load(item.getPath())
                .apply(new RequestOptions()
                        .placeholder(previewAlbumDialog.getPlaceholderImage() == null
                                ? Config.getInstance().getDefaultPlaceholderDrawable(holder.itemView.getContext())
                                : previewAlbumDialog.getPlaceholderImage())
                        .error(previewAlbumDialog.getErrorImage() == null
                                ? Config.getInstance().getDefaultErrorImageDrawable(holder.itemView.getContext())
                                : previewAlbumDialog.getErrorImage()))
                .into(holder.getBinding().ivPreviewAlbum);
        holder.getBinding().viewPreviewAlbumSelected.setVisibility(
                pos == selectedPosition ? View.VISIBLE : View.GONE);
    }

    private static class ViewHolder extends BaseViewHolder<ItemPreviewAlbumBinding> {

        public <T> ViewHolder(@NotNull ItemPreviewAlbumBinding binding, PreviewAlbumAdapter adapter) {
            super(binding, adapter);
            binding.getRoot().setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                PreviewAlbumDialog.OnAlbumItemClickListener listener = adapter.previewAlbumDialog.getOnItemClickListener();
                if (listener != null) {
                    listener.onAlbumItemClick(adapter.previewAlbumDialog, adapter.getList().get(pos), pos);
                }
            });
        }
    }
}
