package com.casic.titan.usercomponent.activity;

import android.os.Bundle;
import android.view.View;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.databinding.FeedbackBinding;

import javax.inject.Inject;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.viewmodel.EmptyViewModel;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;


/**
 * Created by fz on 2018/1/22.
 * describe：问题反馈
 */
public class FeedBackActivity extends BaseActivity<EmptyViewModel, FeedbackBinding> implements ImageAddAdapter.ImageViewAddListener,
        ImageAddAdapter.ImageViewClearListener {
    private ImageAddAdapter imageAddAdapter;
    @Inject
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
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
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
            mediaHelper.startCompressImage(imageAddAdapter.getList());
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
