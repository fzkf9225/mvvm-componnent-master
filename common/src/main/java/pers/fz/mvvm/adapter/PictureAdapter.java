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

import pers.fz.mvvm.bean.BannerBean;

/**
 * Created by fz on 2024/11/22
 *
 */
public class PictureAdapter<T extends BannerBean> extends RecyclerView.Adapter<PictureAdapter.BannerViewHolder> {
    private final List<T> imageUrls;
    private final @DrawableRes int placeholderImage;
    public PictureAdapter(List<T> imageUrls,@DrawableRes int placeholderImage) {
        this.imageUrls = imageUrls;
        this.placeholderImage = placeholderImage;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int realPosition = position % imageUrls.size();
        Glide.with(holder.imageView.getContext())
                .load(imageUrls.get(realPosition).getBannerUrl())
                .apply(new RequestOptions().error(placeholderImage))
                .into(holder.imageView);
        holder.imageView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(realPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        // 返回一个很大的数，实现无限循环
        return Integer.MAX_VALUE;
    }

    protected static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
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

