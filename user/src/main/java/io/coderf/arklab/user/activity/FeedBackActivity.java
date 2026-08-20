package io.coderf.arklab.user.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.adapter.ImageAddAdapter;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.databinding.FeedbackBinding;
import io.coderf.arklab.userapi.gateway.MediaGateway;

/**
 * Created by fz on 2018/1/22.
 * describe：问题反馈（经 MediaGateway 选图，不直接依赖 commonmedia）
 */
@AndroidEntryPoint
public class FeedBackActivity extends BaseActivity<EmptyViewModel, FeedbackBinding> implements ImageAddAdapter.ImageViewAddListener,
        ImageAddAdapter.ImageViewClearListener {
    private ImageAddAdapter imageAddAdapter;

    @Inject
    MediaGateway mediaGateway;

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
            List<Uri> uris = AttachmentUtil.toUriList(imageAddAdapter.getList());
            mediaGateway.compressImages(uris, compressed -> {
                // 压缩完成回调；业务可在此上传
            });
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
        mediaGateway.pickImages(9, uris -> {
            if (uris == null || uris.isEmpty()) {
                return;
            }
            imageAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(new ArrayList<>(uris)));
            imageAddAdapter.notifyDataSetChanged();
        });
    }
}
