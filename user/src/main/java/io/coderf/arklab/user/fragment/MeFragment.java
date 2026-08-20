package io.coderf.arklab.user.fragment;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.utils.theme.ThemeUtils;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.user.R;
import io.coderf.arklab.user.activity.ModifyPasswordActivity;
import io.coderf.arklab.user.activity.PersonalCenterActivity;
import io.coderf.arklab.user.activity.SettingActivity;
import io.coderf.arklab.user.api.UserAccountHelper;
import io.coderf.arklab.user.databinding.MeFragmentBinding;
import io.coderf.arklab.base.gateway.MediaGateway;
import io.coderf.arklab.userapi.router.UserRouterService;

/**
 * Created by fz on 2020/03/26.
 * describe：我的页面
 */
@AndroidEntryPoint
public class MeFragment extends BaseFragment<EmptyViewModel, MeFragmentBinding> {

    @Inject
    UserRouterService userRouterService;

    @Inject
    MediaGateway mediaGateway;

    @Override
    protected int getLayoutId() {
        return R.layout.me_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ThemeUtils.setupStatusBar(requireActivity(), ContextCompat.getColor(requireContext(), io.coderf.arklab.common.R.color.themeColor), true);
        binding.imagePersonalCenter.setOnClickListener(v -> startActivity(PersonalCenterActivity.class));
        binding.tvUserName.setOnClickListener(v -> {
            if (UserAccountHelper.isLogin()) {
                return;
            }
            userRouterService.toLogin(requireContext(), authManager.getLoginLauncher());
        });
        binding.tvSetting.setOnClickListener(v -> startActivity(SettingActivity.class));
        binding.tvModifyPassword.setOnClickListener(v -> startActivity(ModifyPasswordActivity.class));
        binding.headImg.setOnClickListener(v -> {
            if (!UserAccountHelper.isLogin()) {
                userRouterService.toLogin(requireContext(), authManager.getLoginLauncher());
            } else {
                mediaGateway.pickImages(1, uris -> {
                    // 头像上传等业务回调
                });
            }
        });
    }

    @Override
    protected void initData(Bundle bundle) {
        if (UserAccountHelper.isLogin()) {
            binding.setUser(UserAccountHelper.getUser());
        }
    }
}
