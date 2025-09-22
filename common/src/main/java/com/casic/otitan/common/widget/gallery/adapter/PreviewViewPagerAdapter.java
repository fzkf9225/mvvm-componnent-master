package com.casic.otitan.common.widget.gallery.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import com.casic.otitan.common.R;
import com.casic.otitan.common.activity.VideoPlayerActivity;
import com.casic.otitan.common.adapter.MediaShowAdapter;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.databinding.ItemPicShowBinding;
import com.casic.otitan.common.enums.AttachmentTypeEnum;
import com.casic.otitan.common.utils.common.AttachmentUtil;
import com.casic.otitan.common.utils.common.FileUtil;
import com.casic.otitan.common.utils.common.ThreadExecutorBounded;
import com.casic.otitan.common.utils.download.DownLoadImageService;
import com.casic.otitan.common.utils.download.ImageDownLoadCallBack;
import com.casic.otitan.common.utils.log.LogUtil;
import com.casic.otitan.common.widget.dialog.ImageSaveDialog;
import com.casic.otitan.common.widget.gallery.PreviewPhotoDialog;

/**
 * created by fz on 2024/12/20 14:09
 * describe:
 */
public class PreviewViewPagerAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ItemPicShowBinding> {
    private final PreviewPhotoDialog previewPhotoDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public PreviewViewPagerAdapter(PreviewPhotoDialog previewPhotoDialog) {
        this.previewPhotoDialog = previewPhotoDialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_pic_show;
    }

    @Override
    protected BaseViewHolder<ItemPicShowBinding> createViewHold(ItemPicShowBinding binding) {
        return new ViewHolder(binding,this);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemPicShowBinding> holder, int pos) {
        holder.getBinding().picPv.setAllowParentInterceptOnEdge(true);
        AttachmentTypeEnum attachmentTypeEnum = AttachmentUtil.getMediaType(holder.getBinding().getRoot().getContext(), mList.get(pos).getFileType(), mList.get(pos).getPath());
        if (AttachmentTypeEnum.VIDEO == attachmentTypeEnum) {
            holder.getBinding().videoPlay.setVisibility(View.VISIBLE);
        } else if (AttachmentTypeEnum.IMAGE == attachmentTypeEnum) {
            holder.getBinding().videoPlay.setVisibility(View.GONE);
        } else {
            holder.getBinding().videoPlay.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(mList.get(pos).getPath())
                .apply(new RequestOptions().placeholder(previewPhotoDialog.getPlaceholderImage() == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : previewPhotoDialog.getPlaceholderImage())
                        .error(previewPhotoDialog.getErrorImage() == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : previewPhotoDialog.getErrorImage()))
                .into(holder.getBinding().picPv);
    }

    private class ViewHolder extends BaseViewHolder<ItemPicShowBinding> {

        public <T> ViewHolder(@NotNull ItemPicShowBinding binding, PreviewViewPagerAdapter adapter) {
            super(binding, adapter);
            binding.picPv.setOnLongClickListener(v -> {
                if (!previewPhotoDialog.isCanSaveImage()) {
                    return false;
                }
                new ImageSaveDialog(itemView.getContext())
                        .setOnImageSaveListener(dialog -> {
                            dialog.dismiss();
                            try {
                                downloadImage(itemView.getContext(), mList.get(getAbsoluteAdapterPosition()).getPath(),
                                        mList.get(getAbsoluteAdapterPosition()).getFileType());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .build()
                        .show();
                return true;
            });
            binding.picPv.setOnPhotoTapListener((v, x, y) -> previewPhotoDialog.dismiss());
            binding.videoPlay.setOnClickListener(v -> {
                try {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE,
                            TextUtils.isEmpty(mList.get(getAbsoluteAdapterPosition()).getFileName())?
                            FileUtil.getFileName(adapter.getList().get(getAbsoluteAdapterPosition()).getPath())
                            :mList.get(getAbsoluteAdapterPosition()).getFileName());
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, adapter.getList().get(getAbsoluteAdapterPosition()).getPath());
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

