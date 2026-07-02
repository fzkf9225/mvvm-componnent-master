package io.coderf.arklab.user.activity;

import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.adapter.ImageAddAdapter;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.dialog.OpenImageDialog;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.module.ActivityMediaHelper;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.databinding.FeedbackBinding;


/**
 * Created by fz on 2018/1/22.
 * describe：问题反馈
 */
@AndroidEntryPoint
public class FeedBackActivity extends BaseActivity<EmptyViewModel, FeedbackBinding> implements ImageAddAdapter.ImageViewAddListener,
        ImageAddAdapter.ImageViewClearListener {
    private ImageAddAdapter imageAddAdapter;
    @Inject
    @ActivityMediaHelper
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
