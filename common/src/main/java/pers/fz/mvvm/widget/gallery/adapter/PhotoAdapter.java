package pers.fz.mvvm.widget.gallery.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.utils.common.ThreadExecutorBounded;
import pers.fz.mvvm.utils.download.DownLoadImageService;
import pers.fz.mvvm.utils.download.ImageDownLoadCallBack;
import pers.fz.mvvm.widget.dialog.ImageSaveDialog;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;
import pers.fz.mvvm.widget.gallery.impl.PhotoView;

/**
 * created by fz on 2024/12/26 16:01
 * describe:
 */
public class PhotoAdapter extends PagerAdapter {
    private List<Object> imageInfoList;
    private final PreviewPhotoDialog previewPhotoDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public PhotoAdapter(PreviewPhotoDialog previewPhotoDialog, List<Object> imageInfoList) {
        this.previewPhotoDialog = previewPhotoDialog;
        this.imageInfoList = imageInfoList;
    }

    @Override
    public int getCount() {
        return this.imageInfoList == null ? 0 : this.imageInfoList.size();
    }

    public void setList(List<Object> imageInfoList) {
        this.imageInfoList = imageInfoList;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        Glide.with(container.getContext())
                .asBitmap()
                .load(imageInfoList.get(position))
                .apply(new RequestOptions().error(R.mipmap.ic_default_image))
                .into(photoView);
        photoView.setOnLongClickListener(v -> {
            if (!previewPhotoDialog.isCanSaveImage()) {
                return false;
            }
            new ImageSaveDialog(container.getContext())
                    .setOnImageSaveListener(dialog -> {
                        dialog.dismiss();
                        if (imageInfoList.get(position) instanceof String ||
                                imageInfoList.get(position) instanceof Integer ||
                                imageInfoList.get(position) instanceof Uri) {
                            downloadImage(container.getContext(), imageInfoList.get(position));
                        } else {
                            Toast.makeText(container.getContext(), "图片缓存失败", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build()
                    .show();
            return true;
        });
        photoView.setOnPhotoTapListener((v, x, y) -> previewPhotoDialog.dismiss());
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
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

