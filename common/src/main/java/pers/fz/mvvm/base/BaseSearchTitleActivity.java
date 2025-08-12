package pers.fz.mvvm.base;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.databinding.BaseActivityTitleSearchBinding;
import pers.fz.mvvm.widget.customview.CustomSearchEditText;

/**
 * created by fz on 2024/10/24 14:04
 * describe:
 */
public abstract class BaseSearchTitleActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseActivity<VM, VDB> {
    protected BaseActivityTitleSearchBinding searchBinding;

    protected MutableLiveData<String> keywordsLiveData = new MutableLiveData<>();

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected void initToolbar() {
        searchBinding = DataBindingUtil.setContentView(this, R.layout.base_activity_title_search);
        searchBinding.setLifecycleOwner(this);
        searchBinding.setContext(this);
        binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutId(), searchBinding.searcherTitleContainer, true);
        binding.setLifecycleOwner(this);
        setSupportActionBar(searchBinding.searchToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        searchBinding.setToolbarConfig(createdToolbarConfig());
        searchBinding.searchToolBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        searchBinding.inputEdit.setOnInputSubmitListener(onInputSubmitListener);
        searchBinding.tvSearch.setOnClickListener(onSearchClickListener);
    }

    protected View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            keywordsLiveData.setValue(searchBinding.inputEdit.getText().toString());
        }
    };

    protected CustomSearchEditText.OnInputSubmitListener onInputSubmitListener = new CustomSearchEditText.OnInputSubmitListener() {
        @Override
        public void onInputSubmit(String query) {
            keywordsLiveData.setValue(searchBinding.inputEdit.getText().toString());
        }

        @Override
        public void onInputClear() {
            keywordsLiveData.setValue(null);
        }
    };

    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this)
                .setTitle(setTitleBar())
                .setTextColor(R.color.white)
                .setBackIconRes(pers.fz.mvvm.R.mipmap.icon_fh_white)
                .setBgColor(pers.fz.mvvm.R.color.themeColor)
                .applyStatusBar();
    }

    public MutableLiveData<String> getKeywordsLiveData() {
        return keywordsLiveData;
    }
}

