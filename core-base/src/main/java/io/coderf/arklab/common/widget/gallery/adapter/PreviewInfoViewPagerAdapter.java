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
import io.coderf.arklab.common.databinding.ItemPicShowBinding;
import io.coderf.arklab.common.databinding.ItemPreviewVideoBinding;
import io.coderf.arklab.common.utils.common.LivePhotoInfo;
import io.coderf.arklab.common.utils.download.DownLoadImageService;
import io.coderf.arklab.common.utils.download.ImageDownLoadCallBack;
import io.coderf.arklab.common.widget.gallery.PreviewInfoPhotoDialog;
import io.coderf.arklab.common.widget.gallery.PreviewMediaBinder;
import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;
import io.coderf.arklab.common.widget.video.ArkVideoPlayerView;
import io.coderf.arklab.core.utils.CoreExecutors;

/**
 * 信息大图预览 ViewPager 适配器。
 * <p>
 * 与 {@link PreviewViewPagerAdapter} 对称：图片/实况与视频分 layout；
 * 预览中实况仅胶囊播放，视频页内播且左上角关闭。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 * @updated 2026/11/25 视频页内播 + 实况预览手势拆分
 */
public class PreviewInfoViewPagerAdapter extends BaseRecyclerViewAdapter<PreviewInfoBean, ViewDataBinding> {

    private final PreviewInfoPhotoDialog previewInfoPhotoDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int currentPage = 0;
    private RecyclerView recyclerView;

    public PreviewInfoViewPagerAdapter(PreviewInfoPhotoDialog previewInfoPhotoDialog) {
        this.previewInfoPhotoDialog = previewInfoPhotoDialog;
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
        PreviewInfoBean item = mList.get(dataPos);
        Context ctx = recyclerView != null ? recyclerView.getContext() : previewInfoPhotoDialog.getContext();
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
        PreviewInfoBean item = mList.get(pos);
        if (holder instanceof VideoViewHolder videoHolder) {
            bindVideo(videoHolder, item, pos);
        } else if (holder instanceof ImageViewHolder imageHolder) {
            bindImage(imageHolder, item, pos);
        }
    }

    private void bindImage(ImageViewHolder holder, PreviewInfoBean item, int pos) {
        ItemPicShowBinding binding = holder.imageBinding;
        binding.clPic.setBackgroundColor(Color.TRANSPARENT);
        Drawable placeholder = previewInfoPhotoDialog.getPlaceholderImage() == null
                ? Config.getInstance().getDefaultPlaceholderDrawable(holder.itemView.getContext())
                : previewInfoPhotoDialog.getPlaceholderImage();
        Drawable error = previewInfoPhotoDialog.getErrorImage() == null
                ? Config.getInstance().getDefaultErrorImageDrawable(holder.itemView.getContext())
                : previewInfoPhotoDialog.getErrorImage();

        PreviewMediaBinder.bindImageOrLive(
                binding.livePhoto,
                item.getPath(),
                LivePhotoInfo.from(item),
                placeholder,
                error,
                previewInfoPhotoDialog.getEffectiveZoomConfig(),
                previewInfoPhotoDialog.isCanSaveImage(),
                previewInfoPhotoDialog.getEffectiveImageSaveDialogConfig(),
                () -> {
                    int adapterPos = holder.getAbsoluteAdapterPosition();
                    if (adapterPos < 0 || adapterPos >= mList.size()) {
                        return;
                    }
                    PreviewInfoBean current = mList.get(adapterPos);
                    downloadImage(holder.itemView.getContext(), current.getPath(), current.getFileType());
                },
                (v, x, y) -> previewInfoPhotoDialog.dismiss());
    }

    private void bindVideo(VideoViewHolder holder, PreviewInfoBean item, int pos) {
        ItemPreviewVideoBinding binding = holder.videoBinding;
        binding.clPreviewVideo.setBackgroundColor(Color.BLACK);
        boolean autoPlay = pos == currentPage;
        PreviewMediaBinder.bindVideoPlayer(
                binding.previewVideoPlayer,
                item.getPath(),
                item.getFileName(),
                autoPlay);
        PreviewMediaBinder.bindVideoCloseButton(
                binding.btnPreviewVideoClose,
                previewInfoPhotoDialog::dismiss);
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
        if (recyclerView == null) {
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

        ImageViewHolder(@NotNull ItemPicShowBinding binding, PreviewInfoViewPagerAdapter adapter) {
            super(binding, adapter);
            this.imageBinding = binding;
        }
    }

    private static class VideoViewHolder extends BaseViewHolder<ViewDataBinding> {
        final ItemPreviewVideoBinding videoBinding;

        VideoViewHolder(@NotNull ItemPreviewVideoBinding binding, PreviewInfoViewPagerAdapter adapter) {
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
        PreviewInfoBean item = mList.get(position);
        if (showConfirm) {
            new io.coderf.arklab.common.widget.dialog.ImageSaveDialog(ctx)
                    .setConfig(previewInfoPhotoDialog.getEffectiveImageSaveDialogConfig())
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
