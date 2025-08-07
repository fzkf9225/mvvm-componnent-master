//package com.casic.titan.commonui.adapter;
//
//import android.graphics.drawable.Drawable;
//import android.widget.Toast;
//
//import androidx.core.content.ContextCompat;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//import pers.fz.mvvm.bean.AttachmentBean;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import pers.fz.mvvm.R;
//import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
//import pers.fz.mvvm.base.BaseViewHolder;
//import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;
//
///**
// * Created by fz on 2024/11/12.
// * describe：图片列表
// */
//public class FormImageShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ImageShowItemBinding> {
//
//    private float radius = 8;
//    protected Drawable placeholderImage;
//    protected Drawable errorImage;
//
//    public FormImageShowAdapter() {
//        super();
//    }
//
//    public void setRadius(float radius) {
//        this.radius = radius;
//    }
//
//    public void setPlaceholderImage(Drawable placeholderImage) {
//        this.placeholderImage = placeholderImage;
//    }
//
//    public void setErrorImage(Drawable errorImage) {
//        this.errorImage = errorImage;
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.adapter_image_show_item;
//    }
//
//    @Override
//    public void onBindHolder(BaseViewHolder<ImageShowItemBinding> viewHolder, int pos) {
//        viewHolder.getBinding().cornerImage.setRadius((int) this.radius);
//        Glide.with(viewHolder.getBinding().cornerImage.getContext())
//                .load(mList.get(pos).getUrl())
//                .apply(new RequestOptions().placeholder(placeholderImage ==null? ContextCompat.getDrawable(viewHolder.itemView.getContext(),R.mipmap.ic_default_image) :placeholderImage)
//                .error(errorImage ==null? ContextCompat.getDrawable(viewHolder.itemView.getContext(),R.mipmap.ic_default_image) :errorImage))
//                .into(viewHolder.getBinding().cornerImage);
//        viewHolder.getBinding().cornerImage.setOnClickListener(v -> {
//                try{
//                    new PreviewPhotoDialog(v.getContext(),createImageInfo(mList),pos).show();
//                }catch (Exception e){
//                    e.printStackTrace();
//                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
//                }
//        });
//    }
//
//    public static List<Object> createImageInfo(List<AttachmentBean> images) {
//        if (images == null) {
//            return null;
//        }
//        List<Object> imageInfos = new ArrayList<>();
//        for (AttachmentBean item : images) {
//            imageInfos.add(item.getUrl());
//        }
//        return imageInfos;
//    }
//}
