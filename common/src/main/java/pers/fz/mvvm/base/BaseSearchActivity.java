package pers.fz.mvvm.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;
import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.BaseSearchActivityBinding;
import pers.fz.mvvm.wight.customlayout.CustomSearchEditText;


public abstract class BaseSearchActivity<VM extends BaseViewModel> extends BaseActivity<VM, BaseSearchActivityBinding> implements View.OnClickListener,
        CustomSearchEditText.OnInputSubmitListener{
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
        binding.baseSearchView.inputEdit.setOnInputSubmitListener(this);
        binding.baseSearchView.buttonSearch.setOnClickListener(this);
    }

    /**
     * 给search添加子菜单
     * @param hint item提示文字
     * @param onClickListener 点击事件回调
     */
    public void addMenuView(String hint,View.OnClickListener onClickListener) {
        View view = LayoutInflater.from(this).inflate(R.layout.option_item, null);
        binding.baseSearchView.llMenuOption.addView(view);
        TextView tvOptionMenu = view.findViewById(R.id.tv_option_menu);
        tvOptionMenu.setHint(hint);
        tvOptionMenu.setOnClickListener(onClickListener);
    }

    @Override
    public void onClick(View v) {
        if (R.id.button_search == v.getId()) {
            keywordsLiveData.postValue(binding.baseSearchView.inputEdit.getText().toString());
        }
    }

    @Override
    public void onInputSubmit(String query) {
        keywordsLiveData.postValue(binding.baseSearchView.inputEdit.getText().toString());
    }

    @Override
    public void onInputClear() {
        keywordsLiveData.postValue(null);
    }

}
