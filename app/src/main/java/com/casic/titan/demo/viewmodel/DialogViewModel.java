package com.casic.titan.demo.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.PopupWindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.casic.titan.commonui.dialog.TickViewMessageDialog;
import com.casic.titan.commonui.widght.calendar.DateRangePickDialog;
import com.casic.titan.demo.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.mvvm.base.BaseRepository;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.enums.DateMode;
import pers.fz.mvvm.repository.RepositoryImpl;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.widget.dialog.BottomSheetDialog;
import pers.fz.mvvm.widget.dialog.ConfirmDialog;
import pers.fz.mvvm.widget.dialog.LoadingProgressDialog;
import pers.fz.mvvm.widget.dialog.DatePickDialog;
import pers.fz.mvvm.widget.dialog.EditAreaDialog;
import pers.fz.mvvm.widget.dialog.InputDialog;
import pers.fz.mvvm.widget.dialog.MenuDialog;
import pers.fz.mvvm.widget.dialog.MessageDialog;
import pers.fz.mvvm.widget.dialog.ProgressBarDialog;
import pers.fz.mvvm.widget.dialog.UpdateMessageDialog;
import pers.fz.mvvm.widget.dialog.bean.ProgressBarSetting;
import pers.fz.mvvm.widget.popupwindow.CascadeMultiPopupWindow;
import pers.fz.mvvm.widget.popupwindow.CascadeSinglePopupWindow;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class DialogViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {
    List<PopupWindowBean> dataList = Arrays.asList(
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
    );

    public DialogViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    public void onClick(View view) {
        if (R.id.bottomSheetDialog == view.getId()) {
            new BottomSheetDialog<>(view.getContext())
                    .setData(dataList)
                    .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                        dialog.dismiss();
                        baseView.showToast(list.get(pos).getPopupName());
                    })
                    .builder()
                    .show();
        } else if (R.id.confirmDialog == view.getId()) {
            new ConfirmDialog(view.getContext())
                    .setMessage("这是确认弹框的示例")
                    .setNegativeText("再想想")
                    .setPositiveText("确认")
                    .setNegativeTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.nv_bg_color))
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.themeColor))
                    .setOnPositiveClickListener(dialog -> {
                        dialog.dismiss();
                        baseView.showToast("您点击的是确认按钮！");
                    })
                    .setOnNegativeClickListener(dialog -> {
                        dialog.dismiss();
                        baseView.showToast("您点击的是取消按钮！");
                    })
                    .builder()
                    .show();
        } else if (R.id.customProgressDialog == view.getId()) {
            new LoadingProgressDialog(view.getContext())
                    .setMessage("加载中...")
                    .setCanCancel(true)
                    .builder()
                    .show();
        } else if (R.id.inputDialog == view.getId()) {
            new InputDialog(view.getContext())
                    .setDefaultStr("北京")
                    .setHintStr("请填写城市名称")
                    .setTipsStr("城市")
                    .setOnPositiveClickListener((dialog, inputString) -> baseView.showToast("您输入的内容是：" + inputString))
                    .builder()
                    .show();
        } else if (R.id.editAreaDialog == view.getId()) {
            new EditAreaDialog(view.getContext())
                    .setDefaultStr("北京")
                    .setHintStr("请填写城市名称")
                    .setTipsStr("城市")
                    .setOnPositiveClickListener((dialog, inputString) -> baseView.showToast("您输入的内容是：" + inputString))
                    .builder()
                    .show();
        } else if (R.id.menuDialog == view.getId()) {
            new MenuDialog<>(view.getContext())
                    .setData(dataList)
                    .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                        dialog.dismiss();
                        baseView.showToast(list.get(pos).getPopupName());
                    })
                    .builder()
                    .show();
        } else if (R.id.messageDialog == view.getId()) {
            new MessageDialog(view.getContext())
                    .setMessage("这是MessageDialog内容")
                    .setOnPositiveClickListener(dialog -> baseView.showToast("这是MessageDialog"))
                    .builder()
                    .show();
        } else if (R.id.openImageDialog == view.getId()) {
            new OpenImageDialog(view.getContext())
                    .setMediaType(OpenImageDialog.CAMERA_ALBUM)
                    .setOnOpenImageClickListener(mediaType -> {
                        //这里结合MediaHelper去实现

                    })
                    .builder()
                    .show();
        } else if (R.id.openShootDialog == view.getId()) {
            new OpenShootDialog(view.getContext())
                    .setMediaType(OpenShootDialog.CAMERA_ALBUM)
                    .setOnOpenVideoClickListener(mediaType -> {
                        //这里结合MediaHelper去实现

                    })
                    .builder()
                    .show();
        } else if (R.id.updateMessageDialog == view.getId()) {
            new UpdateMessageDialog(view.getContext())
                    .setOnUpdateListener(v -> baseView.showToast("点击这个按钮可以开始下载操作了"))
                    .setButtonText("下载")
                    .setUpdateMsgString("修复已知问题，更多更新内容请查看 \nhttps://www.baidu.com")
                    .setVersionName("1.0.1")
                    .builder()
                    .show();
        } else if (R.id.tickViewMessageDialog == view.getId()) {
            new TickViewMessageDialog(view.getContext())
                    .setMessage("成功")
                    .setOnTickCheckedChangeListener((tickView, isCheck) -> baseView.showToast("isCheck:" + isCheck))
                    .setOnTickViewHideListener(() -> baseView.showToast("onTickViewHide"))
                    .setCountDown(3000)
                    .builder()
                    .show();
        } else if (R.id.circleProgressBarToPosition == view.getId()) {
            ProgressBarSetting progressBarSetting = new ProgressBarSetting(view.getContext());
            progressBarSetting.setMaxProgress(200);
            progressBarSetting.setFontPercent(0);
            progressBarSetting.setFontSize(DensityUtil.sp2px(view.getContext(), 24));
            progressBarSetting.setFontColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_orange));
            ProgressBarDialog progressBarDialog = new ProgressBarDialog(view.getContext())
                    .setProgressBarType(ProgressBarDialog.CIRCLE_PROGRESS_BAR)
                    .setProgressBarSetting(progressBarSetting)
                    .setMessageType("提示")
                    .setContent("正在下载中...")
                    .setShowButton(false)
                    .setCanCancel(true)
                    .builder();
            progressBarDialog.setProcess(80);
            progressBarDialog.show();
        } else if (R.id.horizontalProgressBarToPosition == view.getId()) {
            ProgressBarSetting progressBarSetting = new ProgressBarSetting(view.getContext());
            progressBarSetting.setMaxProgress(200);
            progressBarSetting.setProgressColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_red));
            ProgressBarDialog progressBarDialog = new ProgressBarDialog(view.getContext())
                    .setProgressBarType(ProgressBarDialog.HORIZONTAL_PROGRESS_BAR)
                    .setProgressBarSetting(progressBarSetting)
                    .setMessageType("提示")
                    .setContent("正在下载中...")
                    .setButtonText("关闭")
                    .setButtonColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_red))
                    .setOnProgressEndListener(() -> {
                        LogUtil.show(TAG, "---------------加载完成----------------");
                    })
                    .setCanCancel(true)
                    .builder();
            progressBarDialog.setProcess(80);
            progressBarDialog.show();
        } else if (R.id.circleProgressBar == view.getId()) {
            circleProgress = 0;
            circleHandler.post(circleRunnable = new CircleRunnable(view.getContext()));
        } else if (R.id.horizontalProgressBar == view.getId()) {
            horizontalProgress = 0;
            horizontalHandler.post(horizontalRunnable = new HorizontalRunnable(view.getContext()));
        } else if (R.id.datePick == view.getId()) {
            new DatePickDialog(view.getContext())
                    .setStartYear(1990)
                    .setEndYear(2030)
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setDateMode(DateMode.YEAR_MONTH_DAY)
                    .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                        String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day);
                        Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
                    })
                    .builder()
                    .show();
        } else if (R.id.timePick == view.getId()) {
            new DatePickDialog(view.getContext())
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setDateMode(DateMode.HOUR_MINUTE_SECOND)
                    .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                        String text = NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                        Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
                    })
                    .builder()
                    .show();
        } else if (R.id.dateTimePick == view.getId()) {
            new DatePickDialog(view.getContext())
                    .setStartYear(1990)
                    .setEndYear(2030)
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setDateMode(DateMode.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
                    .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                        String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day) + " " +
                                NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                        Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
                    })
                    .builder()
                    .show();
        } else if (R.id.customDateTimePick == view.getId()) {
            new DatePickDialog(view.getContext())
                    .setStartYear(1990)
                    .setEndYear(2030)
                    .setDateLabel(" - ", " - ", "\u3000")
                    .setTimeLabel(" : ", "\u3000", "\u3000")
                    .setGravity(Gravity.CENTER)
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setDateMode(DateMode.YEAR_MONTH_DAY_HOUR_MINUTE)
                    .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                        String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day) + " " +
                                NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                        Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT).show();
                    })
                    .builder()
                    .show();
        } else if (R.id.dateRangePickDialog == view.getId()) {
            FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
            Lifecycle lifecycle = ((AppCompatActivity) view.getContext()).getLifecycle();
            new DateRangePickDialog(view.getContext())
                    .setGravity(Gravity.BOTTOM)
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setOnPositiveClickListener((startDate, endDate) -> baseView.showToast(startDate + "~" + endDate))
                    .builder(fragmentManager, lifecycle)
                    .show();
        } else if (R.id.customDateRangePickDialog == view.getId()) {
            FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
            Lifecycle lifecycle = ((AppCompatActivity) view.getContext()).getLifecycle();
            ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
            shapeDrawableSelected.getPaint().setColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green));
            ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
            shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.transparent));

            // 动态创建一个红色的圆角矩形 ShapeDrawable
            GradientDrawable shapeDrawableBg = new GradientDrawable();
            shapeDrawableBg.setShape(GradientDrawable.RECTANGLE);
            shapeDrawableBg.setColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.default_background));
            shapeDrawableBg.setCornerRadius(view.getContext().getResources().getDimension(pers.fz.mvvm.R.dimen.radius_l)); // 设置圆角半径，单位为像素
            int daysInMonthCount = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH); // 获取本月最大天数
            new DateRangePickDialog(view.getContext())
                    .setStartDate(DateUtil.getCurrentYear() + "-" + NumberUtils.formatMonthOrDay(DateUtil.getCurrentMonth()) + "-01")
                    .setEndDate(DateUtil.getCurrentYear() + "-" + NumberUtils.formatMonthOrDay(DateUtil.getCurrentMonth()) + "-" + daysInMonthCount)
                    .setSelectedTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.white))
                    .setWeekTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_red))
                    .setWorkingDayTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.auto_color))
                    .setDotWidth(DensityUtil.dp2px(view.getContext(), 6))
                    .setDotHeight(DensityUtil.dp2px(view.getContext(), 6))
                    .setItemWidth(DensityUtil.dp2px(view.getContext(), 42))
                    .setItemHeight(DensityUtil.dp2px(view.getContext(), 42))
                    .setTextSize((float) DensityUtil.sp2px(view.getContext(), 13f))
                    .setSelectedBg(shapeDrawableSelected)
                    .setNormalBg(shapeDrawableNormal)
                    .setClearTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setGravity(Gravity.CENTER)
                    .setBgDrawable(shapeDrawableBg)
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), com.casic.titan.commonui.R.color.theme_green))
                    .setOnPositiveClickListener((startDate, endDate) -> baseView.showToast(startDate + "~" + endDate))
                    .builder(fragmentManager, lifecycle)
                    .show();
        } else if (R.id.popupSingleDialog == view.getId()) {
            String json = loadJSONFromAsset(view.getContext(), "aor.json");
            List<PopupWindowBean> dataList = new GsonBuilder()
                    .registerTypeAdapter(PopupWindowBean.class, new PopupWindowDeserializer())
                    .create()
                    .fromJson(json, new TypeToken<List<PopupWindowBean>>() {
                    }.getType());
            CascadeSinglePopupWindow<?> cascadeSinglePopupWindow = new CascadeSinglePopupWindow(
                    (Activity) view.getContext(),
                    dataList,
                    (CascadeSinglePopupWindow.SelectedListener<PopupWindowBean>) (popupWindow, dataList1) -> baseView.showToast("选中：" + dataList1.get(dataList1.size() - 1).getPopupName())
            );
            cascadeSinglePopupWindow.setSelectedStyle(
                    ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green),
                    ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.white),
                    DensityUtil.dp2px(view.getContext(), 6f)
            );
            cascadeSinglePopupWindow.setConfirmTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green));
            PopupWindowCompat.showAsDropDown(
                    cascadeSinglePopupWindow, view, 0, 0, Gravity.CENTER
            );
        } else if (R.id.popupMultiDialog == view.getId()) {
            String json = loadJSONFromAsset(view.getContext(), "aor.json");
            List<PopupWindowBean<?>> dataList = new GsonBuilder()
                    .registerTypeAdapter(PopupWindowBean.class, new PopupWindowDeserializer())
                    .create()
                    .fromJson(json, new TypeToken<List<PopupWindowBean<?>>>() {
                    }.getType());
            CascadeMultiPopupWindow<PopupWindowBean<?>> cascadeMultiPopupWindow = new CascadeMultiPopupWindow<>(
                    (Activity) view.getContext(),
                    dataList,
                    (popupWindow, dataList2) -> baseView.showToast("选中：" + new Gson().toJson(dataList2.stream().map(PopupWindowBean::getPopupName).collect(Collectors.toList())))
            );
            cascadeMultiPopupWindow.setCheckedDrawable(
                    DrawableUtil.createCheckedDrawable(view.getContext(),
                            ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green),
                            DensityUtil.dp2px(view.getContext(), 16f))
            );
            cascadeMultiPopupWindow.setUncheckedDrawable(
                    DrawableUtil.createUncheckedDrawable(
                            DensityUtil.dp2px(view.getContext(), 1),
                            ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green),
                            DensityUtil.dp2px(view.getContext(), 16))
            );
            cascadeMultiPopupWindow.setSelectedBgStyle(
                    ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green),
                    ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.white),
                    DensityUtil.dp2px(view.getContext(), 6f)
                    );
            cascadeMultiPopupWindow.setConfirmTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.theme_green));
            PopupWindowCompat.showAsDropDown(
                    cascadeMultiPopupWindow, view, 0, 0, Gravity.CENTER
            );
        }
    }

    public class PopupWindowDeserializer implements JsonDeserializer<PopupWindowBean<?>> {
        @Override
        public PopupWindowBean<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            PopupWindowBean<PopupWindowBean<?>> item = new PopupWindowBean<>();

            if (jsonObject.has("popupId") && !jsonObject.get("popupId").isJsonNull()) {
                item.setPopupId(jsonObject.get("popupId").getAsString());
            }

            if (jsonObject.has("popupName") && !jsonObject.get("popupName").isJsonNull()) {
                item.setPopupName(jsonObject.get("popupName").getAsString());
            }

            if (jsonObject.has("parentPopupId") && !jsonObject.get("parentPopupId").isJsonNull()) {
                item.setParentPopupId(jsonObject.get("parentPopupId").getAsString());
            }

            // 递归解析子列表（添加空值和类型检查）
            if (jsonObject.has("childList") && !jsonObject.get("childList").isJsonNull()) {
                JsonElement childElement = jsonObject.get("childList");
                if (childElement.isJsonArray()) {
                    JsonArray childArray = childElement.getAsJsonArray();
                    List<PopupWindowBean<?>> children = new ArrayList<>();
                    for (JsonElement child : childArray) {
                        children.add(context.deserialize(child, PopupWindowBean.class));
                    }
                    item.setChildList(children);
                }
            }

            return item;
        }
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
        return json;
    }

    private ProgressBarDialog circleProgressBarDialog;
    private int circleProgress = 0;
    private final Handler circleHandler = new Handler(Looper.getMainLooper());

    private final class CircleRunnable implements Runnable {
        private final Context context;

        public CircleRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if (circleProgress > 100) {
                circleHandler.removeCallbacks(this);
                return;
            }
            if (circleProgressBarDialog == null) {
                ProgressBarSetting progressBarSetting = new ProgressBarSetting(context);
                progressBarSetting.setFontPercent(0);
                progressBarSetting.setFontSize(DensityUtil.sp2px(context, 24));
                progressBarSetting.setFontColor(ContextCompat.getColor(context, pers.fz.mvvm.R.color.theme_orange));
                circleProgressBarDialog = new ProgressBarDialog(context)
                        .setProgressBarType(ProgressBarDialog.CIRCLE_PROGRESS_BAR)
                        .setProgressBarSetting(progressBarSetting)
                        .setMessageType("提示")
                        .setContent("正在下载中...")
                        .setOnButtonClickListener(v -> {
                            circleProgressBarDialog.dismiss();
                            circleProgressBarDialog = null;
                            circleHandler.removeCallbacks(this);
                        })
                        .setOnProgressEndListener(() -> LogUtil.show(TAG, "---------------加载结束----------------"))
                        .setCanCancel(true)
                        .builder();
                circleProgressBarDialog.show();
            }
            circleProgressBarDialog.postProcess(circleProgress);
            circleProgress++;
            circleHandler.postDelayed(this, 300);
        }
    }

    private Runnable circleRunnable = null;

    private ProgressBarDialog horizontalProgressBarDialog;
    private int horizontalProgress = 0;
    private final Handler horizontalHandler = new Handler(Looper.getMainLooper());

    private class HorizontalRunnable implements Runnable {
        private Context context;

        public HorizontalRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if (horizontalProgress > 100) {
                horizontalHandler.removeCallbacks(this);
                return;
            }
            if (horizontalProgressBarDialog == null) {
                ProgressBarSetting progressBarSetting = new ProgressBarSetting(context);
                progressBarSetting.setProgressColor(ContextCompat.getColor(context, pers.fz.mvvm.R.color.theme_red));
                horizontalProgressBarDialog = new ProgressBarDialog(context)
                        .setProgressBarType(ProgressBarDialog.HORIZONTAL_PROGRESS_BAR)
                        .setProgressBarSetting(progressBarSetting)
                        .setMessageType("提示")
                        .setContent("正在下载中...")
                        .setOnButtonClickListener(v -> {
                            horizontalProgressBarDialog.dismiss();
                            horizontalProgressBarDialog = null;
                            horizontalHandler.removeCallbacks(this);
                        })
                        .setOnProgressEndListener(() -> LogUtil.show(TAG, "---------------加载结束----------------"))
                        .setCanCancel(true)
                        .builder();
                horizontalProgressBarDialog.show();
            }
            horizontalProgressBarDialog.postProcess(horizontalProgress);
            horizontalProgress++;
            horizontalHandler.postDelayed(this, 300);
        }
    }

    private Runnable horizontalRunnable = null;

}
