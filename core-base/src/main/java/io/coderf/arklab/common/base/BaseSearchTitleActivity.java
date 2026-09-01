package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.BaseActivityTitleSearchBinding;
import io.coderf.arklab.common.utils.theme.EdgeToEdgeHelper;
import io.coderf.arklab.common.widget.customview.CustomSearchEditText;

/**
 * 带搜索框的标题栏 Activity。
 * <p>
 * 外壳布局 {@code base_activity_title_search}，正文通过 {@link #getLayoutId()} 嵌入
 * {@code searcher_title_container}。MaterialToolbar 样式统一走 {@link ToolbarConfig}。
 * </p>
 */
public abstract class BaseSearchTitleActivity<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseActivity<VM, VDB> {

    protected BaseActivityTitleSearchBinding searchBinding;

    protected MutableLiveData<String> keywordsLiveData = new MutableLiveData<>();

    @Override
    protected boolean hasToolBar() {
        // 使用独立 search 外壳，不走 base_activity_constraint
        return false;
    }

    @Override
    protected void initToolbar() {
        searchBinding = DataBindingUtil.setContentView(this, R.layout.base_activity_title_search);
        searchBinding.setLifecycleOwner(this);
        searchBinding.setContext(this);
        binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutId(),
                searchBinding.searcherTitleContainer, true);
        binding.setLifecycleOwner(this);

        ToolbarConfig config = createdToolbarConfig();
        searchBinding.setToolbarConfig(config);
        applySearchToolbarHeight(config);

        if (shouldApplyEdgeToEdge()) {
            // 正文只吃导航栏；状态栏由 MaterialToolbar insets 承担
            EdgeToEdgeHelper.applyNavigationBarInsets(
                    searchBinding.searcherTitleContainer, shouldAdjustBottomForIme());
        }

        searchBinding.searchToolBar.setNavigationOnClickListener(
                v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * 将 {@link ToolbarConfig#getHeight()} 应用到搜索 MaterialToolbar；Edge-to-Edge 时叠加状态栏高度。
     */
    protected void applySearchToolbarHeight(ToolbarConfig config) {
        if (searchBinding == null || config == null) {
            return;
        }
        int height = config.getHeight();
        boolean customHeight = config.hasCustomHeight();
        if (shouldApplyEdgeToEdge()) {
            EdgeToEdgeHelper.applyToolbarInsets(searchBinding.searchToolBar, height, customHeight);
        } else {
            EdgeToEdgeHelper.applyToolbarHeight(searchBinding.searchToolBar, height, customHeight);
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        searchBinding.inputEdit.setOnInputSubmitListener(onInputSubmitListener);
        searchBinding.tvSearch.setOnClickListener(onSearchClickListener);
    }

    protected View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            keywordsLiveData.setValue(searchBinding.inputEdit.getText() == null
                    ? null
                    : searchBinding.inputEdit.getText().toString());
        }
    };

    protected CustomSearchEditText.OnInputSubmitListener onInputSubmitListener =
            new CustomSearchEditText.OnInputSubmitListener() {
                @Override
                public void onInputSubmit(String query) {
                    keywordsLiveData.setValue(query);
                }

                @Override
                public void onInputClear() {
                    keywordsLiveData.setValue(null);
                }
            };

    /**
     * 默认主题色背景 + 白色返回/文字（浅色状态栏图标）。
     * 子类可 override 后链式改 hint、字号等，末尾保留 {@link ToolbarConfig#applyStatusBar()}。
     */
    @Override
    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this)
                .setTitle(setTitleBar())
                .setTitleHint(getSearchHint())
                .setTextColor(R.color.white)
                .setBackIconRes(R.drawable.icon_fh)
                .setBgColor(R.color.themeColor)
                .setLightMode(true)
                .setShowBackButton(true)
                .applyStatusBar();
    }

    /**
     * 搜索框默认 hint；子类可 override。
     */
    protected String getSearchHint() {
        return getString(R.string.please_enter);
    }

    public MutableLiveData<String> getKeywordsLiveData() {
        return keywordsLiveData;
    }
}

