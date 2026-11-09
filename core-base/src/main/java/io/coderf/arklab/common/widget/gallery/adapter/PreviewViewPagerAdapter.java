package io.coderf.arklab.common.widget.gallery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.adapter.MediaShowAdapter;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.databinding.ItemPicShowBinding;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.LivePhotoInfo;
import io.coderf.arklab.core.utils.FileUtil;
import io.coderf.arklab.core.utils.ThreadExecutorBounded;
import io.coderf.arklab.common.utils.download.DownLoadImageService;
import io.coderf.arklab.common.utils.download.ImageDownLoadCallBack;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.customview.LivePhotoView;
import io.coderf.arklab.common.widget.dialog.ImageSaveDialog;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;
import io.coderf.arklab.common.widget.gallery.impl.PhotoView;

/**
 * 普通大图预览的翻页适配器。单击关闭，长按保存，视频显示播放按钮；实况图按住播放动感片段。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/12/20 14:09
 * @updated 2026/11/9 接入 LivePhotoView
 */
public class PreviewViewPagerAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ItemPicShowBinding> {
    /**
     * 所属预览弹窗，用来读取缩放、占位图和是否允许保存
     */
    private final PreviewPhotoDialog previewPhotoDialog;
    /**
     * 保存结果回到主线程提示
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * @param previewPhotoDialog 所属预览弹窗
     */
    public PreviewViewPagerAdapter(PreviewPhotoDialog previewPhotoDialog) {
        this.previewPhotoDialog = previewPhotoDialog;
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
        LivePhotoView livePhoto = holder.getBinding().livePhoto;
        livePhoto.setAllowParentInterceptOnEdge(true);
        livePhoto.applyZoomConfig(previewPhotoDialog.getEffectiveZoomConfig());
        holder.getBinding().clPic.setBackgroundColor(Color.BLACK);

        AttachmentBean item = mList.get(pos);
        AttachmentTypeEnum attachmentTypeEnum = AttachmentUtil.getMediaType(
                holder.getBinding().getRoot().getContext(), item.getFileType(), item.getPath());
        LivePhotoInfo liveInfo = LivePhotoInfo.from(item);
        boolean isVideo = AttachmentTypeEnum.VIDEO == attachmentTypeEnum;

        holder.getBinding().videoPlay.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        Drawable placeholder = previewPhotoDialog.getPlaceholderImage() == null
                ? Config.getInstance().getDefaultPlaceholderDrawable(holder.itemView.getContext())
                : previewPhotoDialog.getPlaceholderImage();
        Drawable error = previewPhotoDialog.getErrorImage() == null
                ? Config.getInstance().getDefaultErrorImageDrawable(holder.itemView.getContext())
                : previewPhotoDialog.getErrorImage();

        PhotoView photoView = livePhoto.getPhotoView();
        View.OnLongClickListener saveListener = v -> {
            if (!previewPhotoDialog.isCanSaveImage()) {
                return false;
            }
            new ImageSaveDialog(holder.itemView.getContext())
                    .setConfig(previewPhotoDialog.getEffectiveImageSaveDialogConfig())
                    .setOnImageSaveListener(dialog -> {
                        dialog.dismiss();
                        try {
                            int adapterPos = holder.getAbsoluteAdapterPosition();
                            if (adapterPos < 0 || adapterPos >= mList.size()) {
                                return;
                            }
                            downloadImage(holder.itemView.getContext(),
                                    mList.get(adapterPos).getPath(),
                                    mList.get(adapterPos).getFileType());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .build()
                    .show();
            return true;
        };

        if (isVideo) {
            livePhoto.setOnMotionAvailabilityListener(null);
            livePhoto.bindStillOnly(item.getPath(), placeholder, error);
            photoView.setOnLongClickListener(saveListener);
        } else {
            // 无 otherInfo 时也会自动解析同名 MOV / 内嵌 Motion Photo
            livePhoto.setOnMotionAvailabilityListener(available ->
                    photoView.setOnLongClickListener(available ? null : saveListener));
            livePhoto.bind(item.getPath(), liveInfo, placeholder, error, true);
        }
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder<ItemPicShowBinding> holder) {
        holder.getBinding().livePhoto.release();
        super.onViewRecycled(holder);
    }

    private class ViewHolder extends BaseViewHolder<ItemPicShowBinding> {

        public <T> ViewHolder(@NotNull ItemPicShowBinding binding, PreviewViewPagerAdapter adapter) {
            super(binding, adapter);
            binding.livePhoto.getPhotoView().setOnPhotoTapListener((v, x, y) -> previewPhotoDialog.dismiss());
            binding.videoPlay.setOnClickListener(v -> {
                try {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos < 0 || pos >= adapter.getList().size()) {
                        return;
                    }
                    AttachmentBean item = adapter.getList().get(pos);
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

    /**
     * 把当前图片保存到本地，并在主线程提示结果
     *
     * @param context  上下文
     * @param path     图片地址
     * @param fileType 文件类型，为空时按图片处理
     */
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
