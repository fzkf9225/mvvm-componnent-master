package io.coderf.arklab.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseSearchTitleActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityToolbarSearchDemoBinding;

/**
 * 搜索标题栏：{@link ToolbarConfig#setTitleHint(String)} 驱动 hint，点击搜索回写关键字。
 */
@AndroidEntryPoint
public class ToolbarSearchDemoActivity extends BaseSearchTitleActivity<EmptyViewModel, ActivityToolbarSearchDemoBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_toolbar_search_demo;
    }

    @Override
    public String setTitleBar() {
        return "";
    }

    @Override
    protected String getSearchHint() {
        return "输入关键字，点右侧搜索";
    }

    @Override
    public ToolbarConfig createdToolbarConfig() {
        return super.createdToolbarConfig().setElevationDp(2f);
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    public void initData(Bundle bundle) {
        getKeywordsLiveData().observe(this, keywords -> {
            if (TextUtils.isEmpty(keywords)) {
                binding.tvResult.setText("已清空");
            } else {
                binding.tvResult.setText("搜索：" + keywords);
            }
        });
    }
}
