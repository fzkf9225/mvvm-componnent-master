package com.casic.titan.commonui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.databinding.FileAddItemBinding;
import com.casic.titan.commonui.utils.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2024/3/11
 * describe：添加文件
 */
public class FileAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, FileAddItemBinding> {
    public FileClearListener fileClearListener;
    private int bgColor = Color.WHITE;
    private int textColor = 0x333333;
    private float radius;

    public FileAddAdapter(Context context) {
        super(context);
        radius = DensityUtil.dp2px(context,5);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.file_add_item;
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

    @Override
    public void onBindHolder(BaseViewHolder<FileAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setOnClickListener(v -> {
            if (fileClearListener != null) {
                fileClearListener.fileClear(v, pos);
            }
        });
        holder.getBinding().tvFile.setTextColor(textColor);
        holder.getBinding().layout.setBgColorAndRadius(bgColor,radius);
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            holder.getBinding().tvFile.setText(mList.get(pos).getPath());
        } else {
            holder.getBinding().tvFile.setText(mList.get(pos).getFileName());
        }
        holder.getBinding().tvFile.setOnClickListener(v ->
                AttachmentUtil.viewFile(v.getContext(), mList.get(pos).getPath()));
    }

    public void setFileClearListener(FileClearListener fileClearListener) {
        this.fileClearListener = fileClearListener;
    }

    public interface FileClearListener {
        void fileClear(View view, int position);
    }

}
