package pers.fz.mvvm.wight.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.ItemPicShowBinding;
import pers.fz.mvvm.util.common.ThreadExecutorBounded;
import pers.fz.mvvm.util.download.DownLoadImageService;
import pers.fz.mvvm.util.download.ImageDownLoadCallBack;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.dialog.ImageSaveDialog;

/**
 * created by fz on 2024/12/20 14:09
 * describe:
 */
public class PreviewViewPagerAdapter extends BaseRecyclerViewAdapter<Object, ItemPicShowBinding> {
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
        return new ViewHolder(binding);
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemPicShowBinding> holder, int pos) {
        holder.getBinding().picPv.setAllowParentInterceptOnEdge(true);
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(mList.get(pos))
                .apply(new RequestOptions().error(R.mipmap.ic_default_image))
                .into(holder.getBinding().picPv);
    }

    private class ViewHolder extends BaseViewHolder<ItemPicShowBinding> {

        public ViewHolder(@NotNull ItemPicShowBinding binding) {
            super(binding);
            binding.picPv.setOnLongClickListener(v -> {
                if (!previewPhotoDialog.isCanSaveImage()) {
                    return false;
                }
                new ImageSaveDialog(itemView.getContext())
                        .setOnImageSaveListener(dialog -> {
                            dialog.dismiss();
                            if (mList.get(getAbsoluteAdapterPosition()) instanceof String ||
                                    mList.get(getAbsoluteAdapterPosition()) instanceof Integer ||
                                    mList.get(getAbsoluteAdapterPosition()) instanceof Uri) {
                                downloadImage(itemView.getContext(), mList.get(getAbsoluteAdapterPosition()));
                            } else {
                                Toast.makeText(itemView.getContext(), "图片缓存失败", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build()
                        .show();
                return true;
            });
            binding.picPv.setOnPhotoTapListener((v,x,y) -> previewPhotoDialog.dismiss());
        }
    }

    private void downloadImage(Context context, Object path) {
        ThreadExecutorBounded.getInstance().execute(new DownLoadImageService(context, path,
                "image", new ImageDownLoadCallBack() {
            @Override
            public void onDownLoadSuccess(File file) {
                handler.post(() -> Toast.makeText(context, "图片已保存至" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDownLoadFailed(String errorMsg) {
                handler.post(() -> Toast.makeText(context, TextUtils.isEmpty(errorMsg) ? "图片保存失败" : errorMsg, Toast.LENGTH_SHORT).show());
            }
        }));
    }
}

