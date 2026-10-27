package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.core.ui.delegate.ImeAdjustEnabled;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityDemoSmartPagingLiveBinding;
import io.coderf.arklab.demo.fragment.DemoSmartPagingLiveFragment;

/**
 * Room + Paging3 本地发消息演示：底部输入发送，写入本地库后刷新分页列表。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/10/22
 * @updated 2026/10/27 改为 Room insert 演示
 */
@AndroidEntryPoint
public class DemoSmartPagingLiveActivity extends BaseActivity<EmptyViewModel, ActivityDemoSmartPagingLiveBinding> {

    private UseCase useCase;

    {
        // Edge-to-Edge 下用 IME inset 顶起底部输入栏（勿依赖 Manifest adjustResize）
        imeInsetPolicy = ImeAdjustEnabled.INSTANCE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_demo_smart_paging_live;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.btnSend.setOnClickListener(v -> sendFromComposer());
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendFromComposer();
                return true;
            }
            return false;
        });
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        if (useCase != null && toolbarBind != null && toolbarBind.getToolbarConfig() != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private void sendFromComposer() {
        String text = binding.etMessage.getText() != null
                ? binding.etMessage.getText().toString()
                : "";
        if (TextUtils.isEmpty(text.trim())) {
            return;
        }
        DemoSmartPagingLiveFragment fragment = findChatFragment();
        if (fragment == null) {
            return;
        }
        fragment.sendLocalMessage(text);
        binding.etMessage.setText("");
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && binding.etMessage.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(binding.etMessage.getWindowToken(), 0);
        }
    }

    private DemoSmartPagingLiveFragment findChatFragment() {
        Fragment navHost = getSupportFragmentManager()
                .findFragmentById(R.id.demo_smart_paging_tail_fragment_view);
        if (!(navHost instanceof NavHostFragment)) {
            return null;
        }
        Fragment primary = ((NavHostFragment) navHost).getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary instanceof DemoSmartPagingLiveFragment) {
            return (DemoSmartPagingLiveFragment) primary;
        }
        for (Fragment f : ((NavHostFragment) navHost).getChildFragmentManager().getFragments()) {
            if (f instanceof DemoSmartPagingLiveFragment) {
                return (DemoSmartPagingLiveFragment) f;
            }
        }
        return null;
    }
}
