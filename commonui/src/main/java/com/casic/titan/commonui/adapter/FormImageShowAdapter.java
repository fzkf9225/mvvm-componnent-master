package com.casic.titan.commonui.adapter;

import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.casic.titan.commonui.bean.AttachmentBean;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.ImageShowItemBinding;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;

/**
 * Created by fz on 2024/11/12.
 * describe：图片列表
 */
public class FormImageShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ImageShowItemBinding> {

    public FormImageShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.image_show_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ImageShowItemBinding> viewHolder, int pos) {
        Glide.with(viewHolder.getBinding().cornerImage.getContext())
                .load(mList.get(pos).getUrl())
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(viewHolder.getBinding().cornerImage);
        viewHolder.getBinding().cornerImage.setOnClickListener(v -> {
                try{
                    new PicShowDialog(v.getContext(),createImageInfo(mList),pos).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
        });
    }

    public static List<Object> createImageInfo(List<AttachmentBean> images) {
        if (images == null) {
            return null;
        }
        List<Object> imageInfos = new ArrayList<>();
        for (AttachmentBean item : images) {
            imageInfos.add(item.getUrl());
        }
        return imageInfos;
    }
}
