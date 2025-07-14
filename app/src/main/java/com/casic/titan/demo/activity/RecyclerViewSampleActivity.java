package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;

import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityRecyclerViewSampleBinding;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.viewmodel.EmptyViewModel;
import pers.fz.mvvm.wight.popupwindow.TreePopupView;
import pers.fz.mvvm.wight.popupwindow.MultiPopupView;
import pers.fz.mvvm.wight.popupwindow.PopupView;

@AndroidEntryPoint
public class RecyclerViewSampleActivity extends BaseActivity<EmptyViewModel, ActivityRecyclerViewSampleBinding> {
    private UseCase useCase;

    private TreePopupView<PopupWindowBean<PopupWindowBean<?>>> sexPopupWindow;

    private PopupView<PopupWindowBean<?>> cityPopupWindow;

    private MultiPopupView<PopupWindowBean<?>> qualityPopupWindow;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recycler_view_sample;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        sexPopupWindow = new TreePopupView<PopupWindowBean<PopupWindowBean<?>>>(this, Arrays.asList(
                new PopupWindowBean("", "不限", List.of(
                        new PopupWindowBean("", "不限")
                )),
                new PopupWindowBean("1", "男", Arrays.asList(
                        new PopupWindowBean("1", "性别男爱好女"),
                        new PopupWindowBean("2", "性别男爱好男"),
                        new PopupWindowBean("2", "性别男爱好男和女"),
                        new PopupWindowBean("2", "无所畏惧")
                )),
                new PopupWindowBean("2", "女", Arrays.asList(
                        new PopupWindowBean("1", "性别女爱好女"),
                        new PopupWindowBean("2", "性别女爱好男"),
                        new PopupWindowBean("2", "性别女爱好男和女"),
                        new PopupWindowBean("2", "无所畏惧")
                ))
        ), true, sexSelectCategory);
        binding.tvSex.setOnClickListener(v -> sexPopupWindow.showAsDropDown(v, 0, 0, Gravity.TOP));

        cityPopupWindow = new PopupView<PopupWindowBean<?>>(this, Arrays.asList(
                new PopupWindowBean("", "不限"),
                new PopupWindowBean("1", "北京"),
                new PopupWindowBean("2", "上海"),
                new PopupWindowBean("3", "武汉"),
                new PopupWindowBean("4", "长沙"),
                new PopupWindowBean("5", "南京"),
                new PopupWindowBean("6", "合肥"),
                new PopupWindowBean("7", "秦皇岛"),
                new PopupWindowBean("8", "扬州"),
                new PopupWindowBean("9", "镇江"),
                new PopupWindowBean("10", "芜湖"),
                new PopupWindowBean("11", "马鞍山"),
                new PopupWindowBean("12", "池州"),
                new PopupWindowBean("13", "六安"),
                new PopupWindowBean("14", "黄山"),
                new PopupWindowBean("15", "淮北")
        ), citySelectCategory);
        binding.tvCity.setOnClickListener(v -> cityPopupWindow.showAsDropDown(v, 0, 0, Gravity.TOP));

        qualityPopupWindow = new MultiPopupView<PopupWindowBean<?>>(this, Arrays.asList(
                new PopupWindowBean("", "不限"),
                new PopupWindowBean("1", "316不锈钢"),
                new PopupWindowBean("2", "塑料"),
                new PopupWindowBean("3", "铝合金"),
                new PopupWindowBean("4", "合成金属"),
                new PopupWindowBean("5", "艾德曼合金"),
                new PopupWindowBean("6", "振金")
        ), qualitySelectCategory);
        qualityPopupWindow.setColumnCount(3);
        qualityPopupWindow.setSubmitTextColor(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green));
        qualityPopupWindow.setSelectBgDrawable(DrawableUtil.createShapeDrawable(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green),
                DensityUtil.dp2px(this, 5f)));
        binding.tvQuality.setOnClickListener(v -> qualityPopupWindow.showAsDropDown(v, 0, 0, Gravity.TOP));
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
    }

    private final TreePopupView.SelectedListener<PopupWindowBean<PopupWindowBean<?>>> sexSelectCategory = (popupWindow, dataList, parentSelectPosition, childrenSelectPosition) ->
            binding.tvSex.setText(dataList.get(parentSelectPosition).getChildList().get(childrenSelectPosition).getPopupName());


    private final PopupView.SelectedListener<PopupWindowBean<?>> citySelectCategory = (popupWindow, dataList, selectPosition) ->
            binding.tvCity.setText(dataList.get(selectPosition).getPopupName());

    private final MultiPopupView.SelectedListener<PopupWindowBean<?>> qualitySelectCategory = (popupWindow, dataList) ->
            binding.tvQuality.setText("已选" + dataList.size() + "项");
}