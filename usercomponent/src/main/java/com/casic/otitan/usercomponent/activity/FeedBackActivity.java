package com.casic.otitan.usercomponent.activity;

import android.os.Bundle;
import android.view.View;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.databinding.FeedbackBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.media.MediaHelper;
import com.casic.otitan.media.dialog.OpenImageDialog;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.module.MediaModule;
import com.casic.otitan.common.adapter.ImageAddAdapter;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.utils.common.AttachmentUtil;
import com.casic.otitan.common.viewmodel.EmptyViewModel;
import com.casic.otitan.common.widget.recyclerview.FullyGridLayoutManager;


/**
 * Created by fz on 2018/1/22.
 * describe：问题反馈
 */
@AndroidEntryPoint
public class FeedBackActivity extends BaseActivity<EmptyViewModel, FeedbackBinding> implements ImageAddAdapter.ImageViewAddListener,
        ImageAddAdapter.ImageViewClearListener {
    private ImageAddAdapter imageAddAdapter;
    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.feedback;
    }

    @Override
    public String setTitleBar() {
        return "问题反馈";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                imageAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                imageAddAdapter.notifyDataSetChanged();
            }
        });
        imageAddAdapter = new ImageAddAdapter();
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        binding.feedBackRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.feedBackRecyclerView.setAdapter(imageAddAdapter);
        binding.feedBackBtn.setOnClickListener(v -> {
            mediaHelper.startCompressImage(AttachmentUtil.toUriList(imageAddAdapter.getList()));
        });

    }

    @Override
    public void initData(Bundle bundle) {

    }

    @Override
    public void imgClear(View view, int position) {
        imageAddAdapter.getList().remove(position);
        imageAddAdapter.notifyDataSetChanged();
    }

    @Override
    public void imgAdd(View view) {
        new OpenImageDialog(view.getContext())
                .setMediaType(OpenImageDialog.CAMERA_ALBUM)
                .setOnOpenImageClickListener(mediaHelper)
                .builder()
                .show();
    }
}
