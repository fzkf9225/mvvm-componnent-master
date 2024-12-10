package pers.fz.mvvm.adapter;

import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.ImageShowItemBinding;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;

/**
 * Created by fz on 2017/10/20.
 * 视频列表
 */
public class ImageShowAdapter extends BaseRecyclerViewAdapter<String, ImageShowItemBinding> {

    public ImageShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.image_show_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ImageShowItemBinding> viewHolder, int pos) {
        Glide.with(viewHolder.getBinding().cornerImage.getContext())
                .load(mList.get(pos))
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(viewHolder.getBinding().cornerImage);
        viewHolder.getBinding().cornerImage.setOnClickListener(v -> {
                try{
                    new PicShowDialog(v.getContext(),PicShowDialog.createImageInfo(mList),pos).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
        });
    }
}
