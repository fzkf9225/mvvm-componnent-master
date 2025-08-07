package pers.fz.mvvm.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.BannerBean;

/**
 * Created by fz on 2024/11/22
 */
public class PictureAdapter<T extends BannerBean> extends RecyclerView.Adapter<PictureAdapter.BannerViewHolder<T>> {
    private final List<T> imageUrls;
    private final @DrawableRes int placeholderImage;

    public PictureAdapter(List<T> imageUrls, @DrawableRes int placeholderImage) {
        this.imageUrls = imageUrls;
        this.placeholderImage = placeholderImage;
    }

    @NonNull
    @Override
    public BannerViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder<>(imageView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder<T> holder, int position) {
        int realPosition = position % imageUrls.size();
        Glide.with(holder.imageView.getContext())
                .load(imageUrls.get(realPosition).getBannerUrl())
                .apply(new RequestOptions().error(placeholderImage))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        // 返回一个很大的数，实现无限循环
        return Integer.MAX_VALUE;
    }

    protected static class BannerViewHolder<T extends BannerBean> extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public BannerViewHolder(@NonNull View itemView, PictureAdapter<T> pictureAdapter) {
            super(itemView);
            imageView = (ImageView) itemView;
            imageView.setOnClickListener(v -> {
                if (pictureAdapter.onItemClickListener == null) {
                    return;
                }
                int realPosition = getAbsoluteAdapterPosition() % pictureAdapter.imageUrls.size();
                pictureAdapter.onItemClickListener.onItemClick(realPosition);
            });
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public PictureAdapter<T> setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        return this;
    }

}

