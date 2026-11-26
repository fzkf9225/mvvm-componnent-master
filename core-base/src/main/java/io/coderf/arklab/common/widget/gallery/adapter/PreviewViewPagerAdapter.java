package io.coderf.arklab.common.widget.gallery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.databinding.ItemPicShowBinding;
import io.coderf.arklab.common.databinding.ItemPreviewVideoBinding;
import io.coderf.arklab.common.utils.common.LivePhotoInfo;
import io.coderf.arklab.common.utils.download.DownLoadImageService;
import io.coderf.arklab.common.utils.download.ImageDownLoadCallBack;
import io.coderf.arklab.common.widget.gallery.PreviewMediaBinder;
import io.coderf.arklab.common.widget.gallery.PreviewItemActionBinder;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;
import io.coderf.arklab.common.widget.video.ArkVideoPlayerView;
import io.coderf.arklab.core.utils.CoreExecutors;

/**
 * 普通大图预览翻页适配器。
 * <p>
 * 图片 / 实况使用 {@link ItemPicShowBinding}；视频使用 {@link ItemPreviewVideoBinding} 页内播放。
 * 实况在预览中关闭按住播，仅左下角胶囊可播；视频单击暂停/播放，左上角关闭整页预览。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/12/20 14:09
 * @updated 2026/11/25 视频页内播 + 实况预览手势拆分
 */
public class PreviewViewPagerAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ViewDataBinding> {

    private final PreviewPhotoDialog previewPhotoDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int currentPage = 0;
    private RecyclerView recyclerView;

    public PreviewViewPagerAdapter(PreviewPhotoDialog previewPhotoDialog) {
        this.previewPhotoDialog = previewPhotoDialog;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(0, currentPage);
    }

    public void onPreviewPageSelected(int position) {
        int old = currentPage;
        currentPage = position;
        if (recyclerView == null) {
            return;
        }
        if (old >= 0 && old < getItemCount() && old != position) {
            pausePlayerAt(old);
        }
        if (position >= 0 && position < getItemCount()) {
            startPlayerAt(position);
        }
    }

    public void releaseAllPlayers() {
        if (recyclerView != null) {
            for (int i = 0; i < getItemCount(); i++) {
                releasePlayerAt(i);
            }
        }
        PreviewMediaBinder.releaseAllGsyVideos();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeaderView() && position == 0) {
            return TYPE_HEAD;
        }
        int dataPos = hasHeaderView() ? position - 1 : position;
        if (dataPos < 0 || dataPos >= mList.size()) {
            return PreviewMediaBinder.VIEW_TYPE_IMAGE;
        }
        AttachmentBean item = mList.get(dataPos);
        Context ctx = recyclerView != null ? recyclerView.getContext() : previewPhotoDialog.getContext();
        if (PreviewMediaBinder.isVideo(ctx, item.getFileType(), item.getPath())) {
            return PreviewMediaBinder.VIEW_TYPE_VIDEO;
        }
        return PreviewMediaBinder.VIEW_TYPE_IMAGE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_pic_show;
    }

    @NotNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            return super.onCreateViewHolder(parent, viewType);
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == PreviewMediaBinder.VIEW_TYPE_VIDEO) {
            ItemPreviewVideoBinding binding = DataBindingUtil.inflate(
                    inflater, R.layout.item_preview_video, parent, false);
            return new VideoViewHolder(binding, this);
        }
        ItemPicShowBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.item_pic_show, parent, false);
        return new ImageViewHolder(binding, this);
    }

    @Override
    protected BaseViewHolder<ViewDataBinding> createViewHold(ViewDataBinding binding) {
        return new BaseViewHolder<>(binding, this);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ViewDataBinding> holder, int pos) {
        AttachmentBean item = mList.get(pos);
        if (holder instanceof VideoViewHolder videoHolder) {
            bindVideo(videoHolder, item, pos);
        } else if (holder instanceof ImageViewHolder imageHolder) {
            bindImage(imageHolder, item, pos);
        }
    }

    private void bindImage(ImageViewHolder holder, AttachmentBean item, int pos) {
        ItemPicShowBinding binding = holder.imageBinding;
        binding.clPic.setBackgroundColor(Color.BLACK);
        Drawable placeholder = previewPhotoDialog.getPlaceholderImage() == null
                ? Config.getInstance().getDefaultPlaceholderDrawable(holder.itemView.getContext())
                : previewPhotoDialog.getPlaceholderImage();
        Drawable error = previewPhotoDialog.getErrorImage() == null
                ? Config.getInstance().getDefaultErrorImageDrawable(holder.itemView.getContext())
                : previewPhotoDialog.getErrorImage();

        PreviewMediaBinder.bindImageOrLive(
                binding.livePhoto,
                item.getPath(),
                LivePhotoInfo.from(item),
                placeholder,
                error,
                previewPhotoDialog.getEffectiveZoomConfig(),
                previewPhotoDialog.isCanSaveImage(),
                previewPhotoDialog.getEffectiveImageSaveDialogConfig(),
                () -> {
                    int adapterPos = holder.getAbsoluteAdapterPosition();
                    if (adapterPos < 0 || adapterPos >= mList.size()) {
                        return;
                    }
                    AttachmentBean current = mList.get(adapterPos);
                    downloadImage(holder.itemView.getContext(), current.getPath(), current.getFileType());
                },
                (v, x, y) -> previewPhotoDialog.dismiss());
        PreviewItemActionBinder.bind(holder.itemView, previewPhotoDialog, pos);
    }

    private void bindVideo(VideoViewHolder holder, AttachmentBean item, int pos) {
        ItemPreviewVideoBinding binding = holder.videoBinding;
        binding.clPreviewVideo.setBackgroundColor(Color.BLACK);
        boolean autoPlay = pos == currentPage && previewPhotoDialog.isVideoAutoPlayEnabled();
        PreviewMediaBinder.bindVideoPlayer(
                binding.previewVideoPlayer,
                item.getPath(),
                item.getFileName(),
                autoPlay);
        PreviewMediaBinder.bindVideoCloseButton(
                binding.btnPreviewVideoClose,
                previewPhotoDialog::dismiss);
        PreviewItemActionBinder.bind(holder.itemView, previewPhotoDialog, pos);
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder<ViewDataBinding> holder) {
        if (holder instanceof ImageViewHolder imageHolder) {
            imageHolder.imageBinding.livePhoto.release();
        } else if (holder instanceof VideoViewHolder videoHolder) {
            PreviewMediaBinder.releaseVideoPlayer(videoHolder.videoBinding.previewVideoPlayer);
        }
        super.onViewRecycled(holder);
    }

    private void pausePlayerAt(int position) {
        if (recyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
        if (vh instanceof VideoViewHolder videoHolder) {
            PreviewMediaBinder.pauseVideoPlayer(videoHolder.videoBinding.previewVideoPlayer);
        } else if (vh instanceof ImageViewHolder imageHolder) {
            imageHolder.imageBinding.livePhoto.stopMotion();
        }
    }

    private void startPlayerAt(int position) {
        if (recyclerView == null || !previewPhotoDialog.isVideoAutoPlayEnabled()) {
            return;
        }
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
        if (vh instanceof VideoViewHolder videoHolder) {
            ArkVideoPlayerView player = videoHolder.videoBinding.previewVideoPlayer;
            try {
                player.startPlayLogic();
            } catch (Exception ignore) {
            }
        }
    }

    private void releasePlayerAt(int position) {
        if (recyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(position);
        if (vh instanceof VideoViewHolder videoHolder) {
            PreviewMediaBinder.releaseVideoPlayer(videoHolder.videoBinding.previewVideoPlayer);
        } else if (vh instanceof ImageViewHolder imageHolder) {
            imageHolder.imageBinding.livePhoto.release();
        }
    }

    private static class ImageViewHolder extends BaseViewHolder<ViewDataBinding> {
        final ItemPicShowBinding imageBinding;

        ImageViewHolder(@NotNull ItemPicShowBinding binding, PreviewViewPagerAdapter adapter) {
            super(binding, adapter);
            this.imageBinding = binding;
        }
    }

    private static class VideoViewHolder extends BaseViewHolder<ViewDataBinding> {
        final ItemPreviewVideoBinding videoBinding;

        VideoViewHolder(@NotNull ItemPreviewVideoBinding binding, PreviewViewPagerAdapter adapter) {
            super(binding, adapter);
            this.videoBinding = binding;
        }
    }

    /**
     * 右下角下载 / 外部触发。
     *
     * @param position    当前页下标
     * @param showConfirm true 先弹 ImageSaveDialog；false 直接下载（默认）
     */
    public void saveCurrentItem(int position, boolean showConfirm) {
        if (position < 0 || position >= mList.size()) {
            return;
        }
        if (recyclerView == null) {
            return;
        }
        Context ctx = recyclerView.getContext();
        AttachmentBean item = mList.get(position);
        if (showConfirm) {
            new io.coderf.arklab.common.widget.dialog.ImageSaveDialog(ctx)
                    .setConfig(previewPhotoDialog.getEffectiveImageSaveDialogConfig())
                    .setOnImageSaveListener(dialog -> {
                        dialog.dismiss();
                        downloadImage(ctx, item.getPath(), item.getFileType());
                    })
                    .build()
                    .show();
        } else {
            downloadImage(ctx, item.getPath(), item.getFileType());
        }
    }

    /** 兼容：默认按确认弹窗逻辑（一般给外部旧调用） */
    public void saveCurrentItem(int position) {
        saveCurrentItem(position, true);
    }

    private void downloadImage(Context context, String path, String fileType) {
        CoreExecutors.bounded().execute(new DownLoadImageService(context, path,
                TextUtils.isEmpty(fileType) ? "image" : fileType, new ImageDownLoadCallBack() {
            @Override
            public void onDownLoadSuccess(File file) {
                handler.post(() -> Toast.makeText(context, "文件已保存至" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDownLoadFailed(String errorMsg) {
                handler.post(() -> Toast.makeText(context, TextUtils.isEmpty(errorMsg) ? "保存失败" : errorMsg, Toast.LENGTH_SHORT).show());
            }
        }));
    }
}
