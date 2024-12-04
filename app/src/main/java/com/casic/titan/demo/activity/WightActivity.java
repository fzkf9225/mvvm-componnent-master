package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.code.Code;
import com.casic.titan.commonui.fragment.CalendarMonthFragment;
import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityWightBinding;
import com.casic.titan.demo.viewmodel.WightViewModel;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.adapter.ImageShowAdapter;
import pers.fz.mvvm.adapter.VideoShowAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.util.common.RxView;
import pers.fz.mvvm.wight.customlayout.utils.NumberTextWatcher;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.wight.recyclerview.GridSpacingItemDecoration;


@AndroidEntryPoint
public class WightActivity extends BaseActivity<WightViewModel, ActivityWightBinding> {
    private UseCase useCase;
    private String imageUrl = "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800";
    private ImageShowAdapter imageShowAdapter;
    private VideoShowAdapter videoShowAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wight;
    }

    @Override
    public String setTitleBar() {
        return "组件示例";
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setImageUrl(imageUrl);
        binding.autoTextView.setText("这是AutoTextView测试文字");
        binding.starBar.setOnStarChangeListener(mark -> binding.tvStarBar.setText(String.valueOf(mark)));
        binding.customBannerPicture.initView(Arrays.asList(
                new BannerBean("https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800"),
                new BannerBean("https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg"),
                new BannerBean("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42")));
        binding.cornersImageView.setOnClickListener(v -> new PicShowDialog(this, List.of(
                imageUrl
        ), 0).show());
        binding.imageCode.setImageBitmap(Code.getInstance().createBitmap());
        binding.imageCode.setOnClickListener(v -> binding.imageCode.setImageBitmap(Code.getInstance().createBitmap()));
        RxView.setOnClickListener(binding.cornerButton, 3000,"你点的太快了", view -> showToast(DateUtil.getDateTimeFromMillis(System.currentTimeMillis())));
        binding.numberFormatEditText.addTextChangedListener(new NumberTextWatcher(binding.numberFormatEditText, false));

        binding.circleProgressBar.setProgress(80);
        binding.horizontalProgressBar.setProgress(80);

        imageShowAdapter = new ImageShowAdapter(this);
        imageShowAdapter.setList(Arrays.asList(
                "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
                "https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg",
                "https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42"
        ));
        binding.mRecyclerviewImage.setAdapter(imageShowAdapter);
        binding.mRecyclerviewImage.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.mRecyclerviewImage.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(this, 8), 0x00000000));

        videoShowAdapter = new VideoShowAdapter(this);
        videoShowAdapter.setList(Arrays.asList(
                "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
                "https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg",
                "https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42"
        ));
        binding.mRecyclerviewVideo.setAdapter(videoShowAdapter);
        binding.mRecyclerviewVideo.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.mRecyclerviewVideo.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(this, 8), 0x00000000));
        //单选日历
        binding.calendarViewSingle.initData(getLifecycle(), getSupportFragmentManager());
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        int width = DensityUtil.dp2px(this, 4f); // 宽度
        int height = DensityUtil.dp2px(this, 4f); // 高度
        shapeDrawable.setBounds(0, 0, width, height);
        shapeDrawable.getPaint().setColor(ContextCompat.getColor(this, pers.fz.media.R.color.theme_green));
        binding.calendarViewSingle.registerOnPageChangeCallback((calendarData, pos) -> {
            calendarData.getCalendarDataList().forEach(item -> item.setDrawable(shapeDrawable));
            CalendarMonthFragment fragment = binding.calendarViewSingle.getCalendarPagerAdapter().getItem(pos);
            if (fragment != null) {
                fragment.getAdapter().notifyDataSetChanged();
            }
            binding.tvCalendarViewSingle.setText(calendarData.getYear() + "-" + NumberUtils.formatMonthOrDay(calendarData.getMonth()) + "（单选模式）");
        });
        binding.calendarViewSingle.setOnSelectedChangedListener((startDate, endDate) -> showToast(startDate));
        //多选日历
        binding.calendarViewMulti.initData(getLifecycle(), getSupportFragmentManager());
        binding.calendarViewMulti.registerOnPageChangeCallback((calendarData, pos) -> {
            CalendarMonthFragment fragmentMulti = binding.calendarViewMulti.getCalendarPagerAdapter().getItem(pos);
            if (fragmentMulti != null) {
                fragmentMulti.getAdapter().notifyDataSetChanged();
            }
            binding.tvCalendarViewMulti.setText(calendarData.getYear() + "-" + NumberUtils.formatMonthOrDay(calendarData.getMonth()) + "（区间模式）");
        });
        binding.calendarViewMulti.setOnSelectedChangedListener((startDate, endDate) -> showToast(startDate + "~" + endDate));
    }

    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig().setTitle(setTitleBar())
                .setTextColor(R.color.white)
                .setBackIconRes(pers.fz.mvvm.R.mipmap.icon_fh_white)
                .setBgColor(pers.fz.mvvm.R.color.themeColor);
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


}