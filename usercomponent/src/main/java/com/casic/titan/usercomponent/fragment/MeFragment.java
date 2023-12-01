package com.casic.titan.usercomponent.fragment;

import android.os.Bundle;

import com.casic.titan.userapi.router.UserRouterService;
import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.activity.ModifyPasswordActivity;
import com.casic.titan.usercomponent.activity.PersonalCenterActivity;
import com.casic.titan.usercomponent.activity.SettingActivity;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.databinding.MeFragmentBinding;
import com.gyf.immersionbar.ImmersionBar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.util.media.MediaBuilder;
import pers.fz.mvvm.util.media.MediaHelper;
import pers.fz.mvvm.viewmodel.MainViewModel;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;

/**
 * Created by fz on 2020/03/26.
 * describe：我的页面
 */
@AndroidEntryPoint
public class MeFragment extends BaseFragment<MainViewModel, MeFragmentBinding> {
    private final String TAG = this.getClass().getSimpleName();
    private MediaHelper mediaHelper;
    @Inject
    UserRouterService userRouterService;
    @Override
    protected int getLayoutId() {
        return R.layout.me_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ImmersionBar.with(this)
                .autoStatusBarDarkModeEnable(true,0.2f)
                .statusBarColor(pers.fz.mvvm.R.color.themeColor)
                .init();
        mediaHelper = new MediaBuilder(this, this)
                .setImageMaxSelectedCount(1)
                .builder();
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
            userRouterService.toLogin(requireContext(),loginLauncher);
        });
        binding.tvSetting.setOnClickListener(v -> startActivity(SettingActivity.class));
        binding.tvModifyPassword.setOnClickListener(v -> startActivity(ModifyPasswordActivity.class));
        binding.headImg.setOnClickListener(v -> {
            if (!UserAccountHelper.isLogin()) {
                userRouterService.toLogin(requireContext(),loginLauncher);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaHelper != null) {
            mediaHelper.unregister(this);
        }
    }
}
