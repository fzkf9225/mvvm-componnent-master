package pers.fz.mvvm.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.SearchSmartrecyclerviewBinding;
import pers.fz.mvvm.wight.customlayout.CustomSearchEditText;

/**
 * Create by fz 2021-10-27
 * describe：
 */
public abstract class BaseSearchRecyclerViewFragment<VM extends BaseRecyclerViewModel, T> extends BaseRecyclerViewFragment<VM, SearchSmartrecyclerviewBinding,T>
        implements  CustomSearchEditText.OnInputSubmitListener {
    public MutableLiveData<String> keywordsLiveData = new MutableLiveData<>();

    public MutableLiveData<String> getKeywordsLiveData() {
        return keywordsLiveData;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.search_smartrecyclerview;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.searchLayout.inputSearch.setOnInputSubmitListener(this);
        binding.searchLayout.search.setOnClickListener(v-> keywordsLiveData.postValue(binding.searchLayout.inputSearch.getText().toString()));
    }

    /**
     * 给search添加子菜单
     * @param hint item提示文字
     * @param onClickListener 点击事件回调
     */
    public void addMenuView(String hint,View.OnClickListener onClickListener) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.option_item, null);
        binding.searchLayout.llMenu.addView(view);
        TextView tvOptionMenu = view.findViewById(R.id.tv_option_menu);
        tvOptionMenu.setHint(hint);
        tvOptionMenu.setOnClickListener(onClickListener);
    }

    @Override
    public void onInputSubmit(String query) {
        keywordsLiveData.postValue(binding.searchLayout.inputSearch.getText().toString());
    }

    @Override
    public void onInputClear() {
        keywordsLiveData.postValue(null);
    }
}
