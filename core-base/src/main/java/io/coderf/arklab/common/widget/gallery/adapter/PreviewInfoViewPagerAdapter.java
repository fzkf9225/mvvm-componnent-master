package io.coderf.arklab.common.widget.gallery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.adapter.MediaShowAdapter;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.databinding.ItemPicShowBinding;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.common.ThreadExecutorBounded;
import io.coderf.arklab.common.utils.download.DownLoadImageService;
import io.coderf.arklab.common.utils.download.ImageDownLoadCallBack;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.ImageSaveDialog;
import io.coderf.arklab.common.widget.gallery.PreviewInfoBean;
import io.coderf.arklab.common.widget.gallery.PreviewInfoPhotoDialog;

/**
 * 信息大图预览 ViewPager 适配器。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
public class PreviewInfoViewPagerAdapter extends BaseRecyclerViewAdapter<PreviewInfoBean, ItemPicShowBinding> {

    private final PreviewInfoPhotoDialog previewInfoPhotoDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public PreviewInfoViewPagerAdapter(PreviewInfoPhotoDialog previewInfoPhotoDialog) {
        this.previewInfoPhotoDialog = previewInfoPhotoDialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_pic_show;
    }

    @Override
    protected BaseViewHolder<ItemPicShowBinding> createViewHold(ItemPicShowBinding binding) {
        return new ViewHolder(binding, this);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemPicShowBinding> holder, int pos) {
        holder.getBinding().picPv.setAllowParentInterceptOnEdge(true);
        holder.getBinding().picPv.applyZoomConfig(previewInfoPhotoDialog.getEffectiveZoomConfig());
        holder.getBinding().clPic.setBackgroundColor(Color.TRANSPARENT);
        PreviewInfoBean item = mList.get(pos);
        AttachmentTypeEnum attachmentTypeEnum = AttachmentUtil.getMediaType(
                holder.getBinding().getRoot().getContext(), item.getFileType(), item.getPath());
        if (AttachmentTypeEnum.VIDEO == attachmentTypeEnum) {
            holder.getBinding().videoPlay.setVisibility(View.VISIBLE);
        } else {
            holder.getBinding().videoPlay.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(item.getPath())
                .apply(new RequestOptions().placeholder(previewInfoPhotoDialog.getPlaceholderImage() == null
                                ? Config.getInstance().getDefaultPlaceholderDrawable(holder.itemView.getContext())
                                : previewInfoPhotoDialog.getPlaceholderImage())
                        .error(previewInfoPhotoDialog.getErrorImage() == null
                                ? Config.getInstance().getDefaultErrorImageDrawable(holder.itemView.getContext())
                                : previewInfoPhotoDialog.getErrorImage()))
                .into(holder.getBinding().picPv);
    }

    private class ViewHolder extends BaseViewHolder<ItemPicShowBinding> {

        public <T> ViewHolder(@NotNull ItemPicShowBinding binding, PreviewInfoViewPagerAdapter adapter) {
            super(binding, adapter);
            binding.picPv.setOnLongClickListener(v -> {
                if (!previewInfoPhotoDialog.isCanSaveImage()) {
                    return false;
                }
                new ImageSaveDialog(itemView.getContext())
                        .setConfig(previewInfoPhotoDialog.getEffectiveImageSaveDialogConfig())
                        .setOnImageSaveListener(dialog -> {
                            dialog.dismiss();
                            try {
                                PreviewInfoBean item = mList.get(getAbsoluteAdapterPosition());
                                downloadImage(itemView.getContext(), item.getPath(), item.getFileType());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .build()
                        .show();
                return true;
            });
            binding.picPv.setOnPhotoTapListener((v, x, y) -> previewInfoPhotoDialog.dismiss());
            binding.videoPlay.setOnClickListener(v -> {
                try {
                    PreviewInfoBean item = adapter.getList().get(getAbsoluteAdapterPosition());
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE,
                            TextUtils.isEmpty(item.getFileName())
                                    ? FileUtil.getFileName(item.getPath())
                                    : item.getFileName());
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, item.getPath());
                    VideoPlayerActivity.show(v.getContext(), bundleVideo);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(MediaShowAdapter.class.getSimpleName(), "视频播放失败:" + e);
                    Toast.makeText(v.getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void downloadImage(Context context, String path, String fileType) {
        ThreadExecutorBounded.getInstance().execute(new DownLoadImageService(context, path,
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
