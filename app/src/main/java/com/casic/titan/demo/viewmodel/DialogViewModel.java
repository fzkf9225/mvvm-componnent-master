package com.casic.titan.demo.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;

import java.util.Arrays;
import java.util.List;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.util.apiUtil.DensityUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.dialog.BottomSheetDialog;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;
import pers.fz.mvvm.wight.dialog.CustomProgressDialog;
import pers.fz.mvvm.wight.dialog.InputDialog;
import pers.fz.mvvm.wight.dialog.MenuDialog;
import pers.fz.mvvm.wight.dialog.MessageDialog;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.dialog.OpenShootDialog;
import pers.fz.mvvm.wight.dialog.ProgressBarDialog;
import pers.fz.mvvm.wight.dialog.TickViewMessageDialog;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;
import pers.fz.mvvm.wight.dialog.bean.ProgressBarSetting;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class DialogViewModel extends BaseViewModel<BaseView> {
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

    public void onClick(View view) {
        if (R.id.bottomSheetDialog == view.getId()) {
            new BottomSheetDialog<>(view.getContext())
                    .setData(dataList)
                    .setOnOptionBottomMenuClickListener((dialog, list, pos) -> baseView.showToast(list.get(pos).getName()))
                    .builder()
                    .show();
        } else if (R.id.confirmDialog == view.getId()) {
            new ConfirmDialog(view.getContext())
                    .setMessage("这是确认弹框的示例")
                    .setCancelText("再想想")
                    .setSureText("确认")
                    .setNegativeTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.nv_bg_color))
                    .setPositiveTextColor(ContextCompat.getColor(view.getContext(), pers.fz.mvvm.R.color.themeColor))
                    .setOnSureClickListener(dialog -> {
                        dialog.dismiss();
                        baseView.showToast("您点击的是确认按钮！");
                    })
                    .setOnCancelClickListener(dialog -> {
                        dialog.dismiss();
                        baseView.showToast("您点击的是取消按钮！");
                    })
                    .builder()
                    .show();
        } else if (R.id.customProgressDialog == view.getId()) {
            new CustomProgressDialog(view.getContext())
                    .setMessage("加载中...")
                    .setCanCancel(true)
                    .builder()
                    .show();
        } else if (R.id.inputDialog == view.getId()) {
            new InputDialog(view.getContext())
                    .setDefaultStr("北京")
                    .setHintStr("请填写城市名称")
                    .setTipsStr("城市")
                    .setOnSureClickListener((dialog, inputString) -> baseView.showToast("您输入的内容是：" + inputString))
                    .builder()
                    .show();
        } else if (R.id.menuDialog == view.getId()) {
            new MenuDialog<>(view.getContext())
                    .setData(dataList)
                    .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                        dialog.dismiss();
                        baseView.showToast(list.get(pos).getName());
                    })
                    .builder()
                    .show();
        } else if (R.id.messageDialog == view.getId()) {
            new MessageDialog(view.getContext())
                    .setMessage("这是MessageDialog内容")
                    .setOnSureClickListener(dialog -> baseView.showToast("这是MessageDialog"))
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
        } else if (R.id.circleProgressBarToPostion == view.getId()) {
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
        } else if (R.id.horizontalProgressBarToPostion == view.getId()) {
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
        }
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
                        .setOnProgressEndListener(()-> LogUtil.show(TAG,"---------------加载结束----------------"))
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
                        .setOnProgressEndListener(()-> LogUtil.show(TAG,"---------------加载结束----------------"))
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
