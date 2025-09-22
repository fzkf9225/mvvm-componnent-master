package com.casic.otitan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.casic.otitan.common.base.BaseRecyclerViewModel;
import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.bean.base.PageBean;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class RecyclerViewSampleViewModel extends BaseRecyclerViewModel<BaseRepository<BaseView>,BaseView, PopupWindowBean> {

    public RecyclerViewSampleViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected BaseRepository<BaseView> createRepository() {
        return null;
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
