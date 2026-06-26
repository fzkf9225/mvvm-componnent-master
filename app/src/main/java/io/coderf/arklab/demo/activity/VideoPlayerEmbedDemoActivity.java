package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;

import java.util.Arrays;

import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.viewmodel.VideoPlayerViewModel;
import io.coderf.arklab.common.widget.video.VideoPlayerClarityOption;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerController;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityVideoPlayerEmbedBinding;

/**
 * 嵌入模式播放器 Demo：View 嵌入 Activity，非全屏仅全屏按钮。
 */
public class VideoPlayerEmbedDemoActivity extends BaseActivity<VideoPlayerViewModel, ActivityVideoPlayerEmbedBinding> {

    private static final String DEMO_URL =
        "http://alvideo.ippzone.com/zyvd/98/90/b753-55fe-11e9-b0d8-00163e0c0248";

    private VideoPlayerController controller;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_player_embed;
    }

    @Override
    public String setTitleBar() {
        return "嵌入播放器";
    }

    @Override
    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).applyStatusBar();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
    }

    @Override
    public void initData(Bundle bundle) {
        UseCase useCase;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
        VideoPlayerConfig config = VideoPlayerConfig.embedDefaults()
            .setCornerRadiusDp(12f)
            .setClarityOptions(Arrays.asList(
                new VideoPlayerClarityOption("原画", DEMO_URL, "最高画质"),
                new VideoPlayerClarityOption("流畅", DEMO_URL, "节省流量")
            ));
        VideoPlayerViewHelper.setupPlayer(
            binding.embedVideoPlayer, DEMO_URL, false, "嵌入播放 Demo", DEMO_URL, config);
        controller = new VideoPlayerController(this, binding.embedVideoPlayer, config);
        controller.attach(this::finish, () -> {
            createdToolbarConfig().applyStatusBar();
            applyToolbarHeight();
        });
        controller.bindLifecycle(this);
        controller.bindBackPressed(getOnBackPressedDispatcher(), this);
        VideoPlayerViewHelper.startPlay(binding.embedVideoPlayer);
    }
}
