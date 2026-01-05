package com.casic.otitan.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.BaseSearchActivityBinding;
import com.casic.otitan.common.widget.customview.CustomSearchEditText;


public abstract class BaseSearchActivity<VM extends BaseViewModel> extends BaseActivity<VM, BaseSearchActivityBinding> implements View.OnClickListener,
        CustomSearchEditText.OnInputSubmitListener {
    private final MutableLiveData<String> keywordsLiveData = new MutableLiveData<>();

    public MutableLiveData<String> getKeywordsLiveData() {
        return keywordsLiveData;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_search_activity;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.searchView.inputSearch.setOnInputSubmitListener(this);
        binding.searchView.search.setOnClickListener(this);
    }

    /**
     * 给search添加子菜单
     * @param hint item提示文字
     * @param onClickListener 点击事件回调
     */
    public void addMenuView(String hint, View.OnClickListener onClickListener) {
        View view = LayoutInflater.from(this).inflate(R.layout.option_item, null);
        binding.searchView.llMenu.addView(view);
        TextView tvOptionMenu = view.findViewById(R.id.tv_option_menu);
        tvOptionMenu.setHint(hint);
        tvOptionMenu.setOnClickListener(onClickListener);
    }

    @Override
    public void onClick(View v) {
        if (R.id.search == v.getId()) {
            keywordsLiveData.setValue(binding.searchView.inputSearch.getText() == null ? null : binding.searchView.inputSearch.getText().toString());
        }
    }

    @Override
    public void onInputSubmit(String query) {
        keywordsLiveData.setValue(query);
    }

    @Override
    public void onInputClear() {
        keywordsLiveData.setValue(null);
    }

}
