package io.coderf.arklab.usercomponent.fragment;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.userapi.router.UserRouterService;
import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.activity.ModifyPasswordActivity;
import io.coderf.arklab.usercomponent.activity.PersonalCenterActivity;
import io.coderf.arklab.usercomponent.activity.SettingActivity;
import io.coderf.arklab.usercomponent.api.UserAccountHelper;
import io.coderf.arklab.usercomponent.databinding.MeFragmentBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.dialog.OpenImageDialog;
import io.coderf.arklab.media.module.MediaModule;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.utils.theme.ThemeUtils;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;

/**
 * Created by fz on 2020/03/26.
 * describe：我的页面
 */
@AndroidEntryPoint
public class MeFragment extends BaseFragment<EmptyViewModel, MeFragmentBinding> {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    UserRouterService userRouterService;

    @Inject
    @MediaModule.FragmentMediaHelper
    MediaHelper mediaHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.me_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ThemeUtils.setupStatusBar(requireActivity(), ContextCompat.getColor(requireContext(), io.coderf.arklab.common.R.color.themeColor),true);
        mediaHelper.getMediaBuilder().setImageMaxSelectedCount(1);
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
//            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
//                if (mediaBean.getMediaList() == null || mediaBean.getMediaList().size() == 0) {
//                    return;
//                }
//                UserInfo userInfo = UserAccountHelper.getUser();
//                userInfo.setAvatar(mediaBean.getMediaList().get(0));
//                UserAccountHelper.saveLoginState(userInfo, true);
//                binding.setUser(userInfo);
//            }
        });
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
                new OpenImageDialog(requireActivity())
                        .setMediaType(OpenImageDialog.CAMERA_ALBUM)
                        .setOnOpenImageClickListener(mediaHelper)
                        .builder()
                        .show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.setUser(UserAccountHelper.getUser());
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    /**
     * 检查登录状态
     */
    private void checkUserInfo() {

    }
}
