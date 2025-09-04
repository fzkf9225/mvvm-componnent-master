package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.casic.titan.commonui.fragment.CalendarMonthFragment;
import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityWightBinding;
import com.casic.titan.demo.enums.UseCaseEnum;
import com.casic.titan.demo.viewmodel.WightViewModel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.adapter.ImageShowAdapter;
import pers.fz.mvvm.adapter.VideoShowAdapter;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.bean.HomeMenuBean;
import pers.fz.mvvm.bean.base.ToolbarConfig;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.listener.OnMenuClickListener;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.util.common.RxView;
import pers.fz.mvvm.widget.customview.Code;
import pers.fz.mvvm.widget.customview.utils.NumberTextWatcher;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;
import pers.fz.mvvm.widget.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.widget.recyclerview.GridSpacingItemDecoration;


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
                new BannerBean("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42"),
                new BannerBean(R.mipmap.ic_launcher)));
        binding.cornersImageView.setOnClickListener(v -> new PreviewPhotoDialog(this)
                .createImageInfo(imageUrl)
                .currentPosition(0)
                .show());
        binding.imageCode.setImageBitmap(Code.getInstance().createBitmap());
        binding.imageCode.setOnClickListener(v -> binding.imageCode.setImageBitmap(Code.getInstance().createBitmap()));
        RxView.setOnClickListener(binding.cornerButton, 3000, "你点的太快了", view -> showToast(DateUtil.getDateTimeFromMillis(System.currentTimeMillis())));
        binding.numberFormatEditText.addTextChangedListener(new NumberTextWatcher(binding.numberFormatEditText, false));

        binding.circleProgressBar.setProgress(80);
        binding.horizontalProgressBar.setProgress(80);
        List<AttachmentBean> attachmentBeanList = new ArrayList<>();
        AttachmentBean attachmentBean1 = new AttachmentBean();
        attachmentBean1.setPath("https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800");
        attachmentBean1.setFileName("1.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean2 = new AttachmentBean();
        attachmentBean2.setPath("https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg");
        attachmentBean2.setFileName("2.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean3 = new AttachmentBean();
        attachmentBean3.setPath("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42");
        attachmentBean3.setFileName("3.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean4 = new AttachmentBean();
        attachmentBean4.setPath("https://q8.itc.cn/images01/20240208/45d5ee19361f4f8fa824e93ebfc42a8a.jpeg");
        attachmentBean4.setFileName("4.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean5 = new AttachmentBean();
        attachmentBean5.setPath("https://ww1.sinaimg.cn/mw690/008vmhs1ly1hrhly2i2jtj30j616nq9v.jpg");
        attachmentBean5.setFileName("5.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean6 = new AttachmentBean();
        attachmentBean6.setPath("https://img2.baidu.com/it/u=2380808412,3135171519&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1199");
        attachmentBean6.setFileName("6.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        AttachmentBean attachmentBean7 = new AttachmentBean();
        attachmentBean7.setPath(DrawableUtil.resourceToBase64(Config.getInstance().getApplication(), R.mipmap.ic_launcher));
        attachmentBean6.setFileName("7.jpg");
        attachmentBean1.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        attachmentBeanList.add(attachmentBean1);
        attachmentBeanList.add(attachmentBean2);
        attachmentBeanList.add(attachmentBean3);
        attachmentBeanList.add(attachmentBean4);
        attachmentBeanList.add(attachmentBean5);
        attachmentBeanList.add(attachmentBean6);
        attachmentBeanList.add(attachmentBean7);

        imageShowAdapter = new ImageShowAdapter();
        imageShowAdapter.setList(attachmentBeanList);
        binding.mRecyclerviewImage.setAdapter(imageShowAdapter);
        binding.mRecyclerviewImage.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.mRecyclerviewImage.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(this, 8), 0x00000000));

        List<AttachmentBean> videoList = new ArrayList<>();
        AttachmentBean attachmentBean8 = new AttachmentBean();
        attachmentBean8.setPath("http://alvideo.ippzone.com/zyvd/98/90/b753-55fe-11e9-b0d8-00163e0c0248");
        attachmentBean8.setFileName("8.mp4");
        attachmentBean8.setFileType(AttachmentTypeEnum.VIDEO.typeValue);
        videoList.add(attachmentBean8);
        videoShowAdapter = new VideoShowAdapter();
        videoShowAdapter.setList(videoList);
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
        shapeDrawable.getPaint().setColor(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green));
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
        Glide.with(this)
                .load("https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800")
                .into(binding.circleShapeableImageView);
        Glide.with(this)
                .load("https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg")
                .into(binding.roundedShapeableImageView);
//        binding.customBannerPicture.setOnViewPagerSelectedListener(position -> LogUtil.show(TAG, "当前选中页：" + position));
        binding.homeMenuView.bindLifecycle(this,getSupportFragmentManager());
        binding.homeMenuView.setDrawableResCurrent(DrawableUtil.createCircleDrawable(
                ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green),
                DensityUtil.dp2px(this, 5f)
        ));
        binding.homeMenuView.setOnMenuClickListener(menuClickListener);
    }

    OnMenuClickListener menuClickListener = new OnMenuClickListener() {
        @Override
        public void onMenuLongClick(@Nullable View v, @Nullable Fragment fragment, @Nullable HomeMenuBean menuBean) {

        }

        @Override
        public void onMenuClick(@Nullable View v, @Nullable Fragment fragment, @Nullable HomeMenuBean menuBean) {
            if (menuBean == null || TextUtils.isEmpty(menuBean.getComponentName())) {
                showToast("此页面正在研发中请稍后再试！");
                return;
            }
            Bundle bundle = new Bundle();
            UseCase useCase = new UseCase();
            useCase.setName(menuBean.getTitle());
            bundle.putParcelable("args", useCase);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClassName(getPackageName(), menuBean.getComponentName());
            startActivity(intent);
        }
    };

    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this)
                .setLightMode(true)
                .setTitle(setTitleBar())
                .setTextColor(R.color.white)
                .setBackIconRes(pers.fz.mvvm.R.mipmap.icon_fh_white)
                .setBgColor(pers.fz.mvvm.R.color.themeColor)
                .applyStatusBar();
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        List<UseCase> useCaseList = UseCaseEnum.toUseCaseList();
        List<HomeMenuBean> homeMenuBeanList = useCaseList.stream().map(item -> {
            HomeMenuBean homeMenuBean = new HomeMenuBean(0, R.mipmap.ic_launcher, item.getName(), item.getDescribe(), item.getClx().getName());
            if ("自定义组件".equalsIgnoreCase(item.getName())) {
                homeMenuBean.setGray(true);
            }
            return homeMenuBean;
        }).collect(Collectors.toList());
        binding.homeMenuView.initData(homeMenuBeanList);
    }


}