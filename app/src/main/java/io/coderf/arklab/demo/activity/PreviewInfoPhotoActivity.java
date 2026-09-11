package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.gallery.PreviewInfoBean;
import io.coderf.arklab.common.widget.gallery.PreviewInfoConfig;
import io.coderf.arklab.common.widget.gallery.PreviewInfoPhotoDialog;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityPreviewInfoPhotoBinding;

/**
 * 信息大图预览演示：定位图标、三行文案、仿微信「查看全部」相册。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
@AndroidEntryPoint
public class PreviewInfoPhotoActivity extends BaseActivity<EmptyViewModel, ActivityPreviewInfoPhotoBinding> {

    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_preview_info_photo;
    }

    @Override
    public String setTitleBar() {
        return "信息大图预览";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.btnDefault.setOnClickListener(v -> showDefault());
        binding.btnLocationOff.setOnClickListener(v -> showLocationOff());
        binding.btnLocationCallback.setOnClickListener(v -> showLocationCallback());
        binding.btnLocationSelectable.setOnClickListener(v -> showLocationSelectable());
        binding.btnTextStyle.setOnClickListener(v -> showCustomTextStyle());
        binding.btnCustomIcon.setOnClickListener(v -> showCustomIcon());
        binding.btnViewAllCallback.setOnClickListener(v -> showViewAllCallback());
        binding.btnInfoLineOff.setOnClickListener(v -> showInfoLineOff());
        binding.btnCircleBgOff.setOnClickListener(v -> showCircleBgOff());
        binding.btnSingle.setOnClickListener(v -> showSingle());
        binding.btnOldPreview.setOnClickListener(v -> showOldPreview());
    }

    @Override
    public void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private void showDefault() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setLocationEnabled(true)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveLocationText()))
                .show();
    }

    private void showLocationOff() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setLocationEnabled(false)
                .show();
    }

    private void showLocationCallback() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(1)
                .setLocationEnabled(true)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast("定位回调：第 " + (position + 1) + " 张，" +
                                (item == null ? "" : item.resolveLocationText())))
                .show();
    }

    private void showLocationSelectable() {
        List<PreviewInfoBean> list = buildDemoList();
        list.get(0).setLocationIconSelected(true);
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(list)
                .currentPosition(0)
                .setLocationCanSelected(true)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast((selected ? "选中" : "取消选中") + "定位：第 " + (position + 1) + " 张，"
                                + (item == null ? "" : item.resolveLocationText())))
                .show();
    }

    private void showCustomTextStyle() {
        PreviewInfoConfig config = PreviewInfoConfig.defaults()
                .setTitleTextColor(0xFFFFE082)
                .setTimeTextColor(0xFF80CBC4)
                .setLocationTextColor(0xFF90CAF9)
                .setTitleTimeSpacingPx(16)
                .setTimeLocationSpacingPx(12)
                .setBackgroundColor(0xFF1A1A1A);
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setConfig(config)
                .setTitleTextSizeSp(18f)
                .setTimeTextSizeSp(12f)
                .setLocationTextSizeSp(12f)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveTitle()))
                .show();
    }

    private void showCustomIcon() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setLocationIcon(io.coderf.arklab.common.R.drawable.ic_camera)
                .setPrevIcon(io.coderf.arklab.common.R.drawable.ic_flash_off)
                .setNextIcon(io.coderf.arklab.common.R.drawable.ic_flash_on)
                .setViewAllIcon(io.coderf.arklab.common.R.drawable.ic_camera)
                .setOnLocationClickListener((dialog, item, position, selected) -> showToast("自定义定位图标"))
                .show();
    }

    private void showViewAllCallback() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveLocationText()))
                .setOnViewAllClickListener((dialog, items, position) ->
                        showToast("自定义查看全部：共 " + items.size() + " 张，当前第 " + (position + 1) + " 张"))
                .show();
    }

    private void showInfoLineOff() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setTitleEnabled(true)
                .setTimeEnabled(false)
                .setLocationTextEnabled(false)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveLocationText()))
                .show();
    }

    private void showCircleBgOff() {
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(buildDemoList())
                .currentPosition(0)
                .setNavCircleBackgroundEnabled(false)
                .setViewAllCircleBackgroundEnabled(false)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveLocationText()))
                .show();
    }

    private void showSingle() {
        List<PreviewInfoBean> list = new ArrayList<>();
        list.add(buildDemoList().get(0));
        new PreviewInfoPhotoDialog(this)
                .setInfoImages(list)
                .currentPosition(0)
                .setOnLocationClickListener((dialog, item, position, selected) ->
                        showToast(item == null ? "定位" : item.resolveLocationText()))
                .show();
    }

    private void showOldPreview() {
        List<AttachmentBean> attachments = new ArrayList<>();
        for (PreviewInfoBean item : buildDemoList()) {
            attachments.add(item.toAttachment());
        }
        new PreviewPhotoDialog(this, attachments, 0).show();
    }

    private List<PreviewInfoBean> buildDemoList() {
        List<PreviewInfoBean> list = new ArrayList<>();
        list.add(PreviewInfoBean.of(
                imageAttachment("https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
                        "河道1.jpg", 1769580000000L, 30.2590, 120.1485),
                "河道巡检照片 · 可见光",
                "2026-01-28 14:40:00",
                "杭州市西湖区北山路沿湖段"));
        list.add(PreviewInfoBean.of(
                imageAttachment("https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg",
                        "河道2.jpg", 1769583600000L, 30.2741, 120.1552),
                "河道巡检照片 · 红外",
                "2026-01-28 15:40:00",
                "杭州市西湖区断桥附近"));
        list.add(PreviewInfoBean.of(
                imageAttachment("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42",
                        "河道3.jpg", 1769587200000L, 30.2428, 120.1480),
                "堤岸护栏检查",
                "2026-01-28 16:40:00",
                "杭州市西湖区苏堤春晓"));
        list.add(PreviewInfoBean.of(
                imageAttachment("https://q8.itc.cn/images01/20240208/45d5ee19361f4f8fa824e93ebfc42a8a.jpeg",
                        "河道4.jpg", 1769590800000L, 30.2501, 120.1392),
                "水体颜色观测",
                "2026-01-28 17:40:00",
                "杭州市西湖区花港观鱼"));
        return list;
    }

    private AttachmentBean imageAttachment(String path, String fileName, long createTime,
                                           double latitude, double longitude) {
        AttachmentBean bean = new AttachmentBean();
        bean.setPath(path);
        bean.setRelativePath(path);
        bean.setFileName(fileName);
        bean.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        bean.setCreateTime(createTime);
        bean.setLatitude(latitude);
        bean.setLongitude(longitude);
        return bean;
    }
}
