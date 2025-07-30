package com.casic.titan.commonui.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.databinding.ItemFileShowBinding;
import com.casic.titan.commonui.utils.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.FileUtil;

/**
 * Created by fz on 2024/2/26.
 * describe：文件展示
 */
public class FileShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ItemFileShowBinding> {
    private int bgColor = Color.WHITE;
    private int textColor = 0x333333;
    private float radius = 5;
    private Drawable fileDrawable;
    private boolean isShowFileDrawable = true;

    public FileShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_file_show;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setFileDrawable(Drawable fileDrawable) {
        this.fileDrawable = fileDrawable;
    }

    public void setShowFileDrawable(boolean showFileDrawable) {
        isShowFileDrawable = showFileDrawable;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemFileShowBinding> viewHolder, int pos) {
        viewHolder.getBinding().tvFile.setTextColor(textColor);
        viewHolder.getBinding().layout.setBgColorAndRadius(bgColor, radius);
        if (isShowFileDrawable) {
            viewHolder.getBinding().imageFile.setVisibility(View.VISIBLE);
            if (fileDrawable == null) {
                viewHolder.getBinding().imageFile.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.mipmap.icon_file));
            } else {
                viewHolder.getBinding().imageFile.setImageDrawable(fileDrawable);
            }
        } else {
            viewHolder.getBinding().imageFile.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            viewHolder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getPath()));
        } else {
            viewHolder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getFileName()));
        }
        viewHolder.getBinding().tvFile.setOnClickListener(v -> AttachmentUtil.viewFile(v.getContext(), mList.get(pos).getPath()));
    }

}
