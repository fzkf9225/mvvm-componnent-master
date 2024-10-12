package com.casic.titan.commonui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.commonui.databinding.FileAddItemBinding;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;


/**
 * Created by fz on 2024/3/11
 * describe：添加文件
 */
public class FileAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, FileAddItemBinding> {
    public FileClearListener fileClearListener;

    public FileAddAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.file_add_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<FileAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setOnClickListener(v -> {
            if (fileClearListener != null) {
                fileClearListener.fileClear(v, pos);
            }
        });
//        holder.getBinding().ivImageShow.setOnClickListener(v -> {
//            try {
//                new PicShowDialog(mContext, PicShowDialog.createUriImageInfo(mList), pos).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            holder.getBinding().tvFile.setText(mList.get(pos).getPath());
        } else {
            holder.getBinding().tvFile.setText(mList.get(pos).getFileName());
        }

//        //下划线
//        holder.getBinding().tvFile.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//        //抗锯齿
//        holder.getBinding().tvFile.getPaint().setAntiAlias(true);
        holder.getBinding().tvFile.setOnClickListener(v -> {
            Toast.makeText(mContext, "暂不支持预览！", Toast.LENGTH_SHORT).show();
        });
    }

    private String getMimeType(String url) {
        String mimeType = "*/*";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    public void setFileClearListener(FileClearListener fileClearListener) {
        this.fileClearListener = fileClearListener;
    }

    public interface FileClearListener {
        void fileClear(View view, int position);
    }

}
