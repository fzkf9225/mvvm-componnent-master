package com.casic.titan.commonui.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.databinding.ItemFileShowBinding;
import com.casic.titan.commonui.utils.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.FileUtils;


/**
 * Created by fz on 2024/2/26.
 * describe：文件展示
 */
public class FileShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, ItemFileShowBinding> {

    public FileShowAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_file_show;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemFileShowBinding> viewHolder, int pos) {
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            viewHolder.getBinding().tvFile.setText(FileUtils.getFileName(mList.get(pos).getPath()));
        } else {
            viewHolder.getBinding().tvFile.setText(FileUtils.getFileName(mList.get(pos).getFileName()));
        }
        viewHolder.getBinding().tvFile.setOnClickListener(v -> AttachmentUtil.viewFile(v.getContext(),mList.get(pos).getPath()));
    }

}