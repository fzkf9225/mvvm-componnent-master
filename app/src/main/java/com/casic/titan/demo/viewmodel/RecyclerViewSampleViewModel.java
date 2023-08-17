package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.base.BaseRecyclerViewModel;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.bean.base.PageBean;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class RecyclerViewSampleViewModel extends BaseRecyclerViewModel<BaseView, PopupWindowBean> {
    public RecyclerViewSampleViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadData(int mCurrentPage) {
        PageBean<PopupWindowBean> pageBean = new PageBean<>();

        List<PopupWindowBean> dataList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            dataList.add(new PopupWindowBean(String.valueOf(mCurrentPage * 20 + i), "这是" + (mCurrentPage * 20 + i) + "行的数据哦！！！"));
        }
        pageBean.setList(dataList);
        pageBean.setResponseCount(dataList.size());
        listLiveData.setValue(pageBean);
    }
}
