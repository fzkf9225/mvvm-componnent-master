package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.wight.pickertime.PickTimeView;
import pers.fz.mvvm.util.apiUtil.DateUtil;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;


/**
 * Created by fz on 2017/10/18.
 * 单选弹框
 */

public class PickTimeViewDialog extends Dialog implements View.OnClickListener, PickTimeView.onSelectedChangeListener {

    private Context context;
    private long stTime = System.currentTimeMillis();
    private String sdfDate = DateUtil.DEFAULT_FORMAT_DATE;
    private String defaultDate;
    private String defaultHint = "请选择时间";
    private OnPickTimeDateSelectedListener onPickTimeDateSelectedListener;
    private OnDialogInterfaceClickListener onDialogInterfaceClickListener;
    private int pickerViewType = PickTimeView.TYPE_PICK_DATE;
    private boolean isShowUnLimitedButton = false;

    public PickTimeViewDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public PickTimeViewDialog setSimpleDateFormat(String format) {
        if (StringUtil.isEmpty(format)) {
            sdfDate = DateUtil.DEFAULT_FORMAT_DATE;
        }
        this.sdfDate = format;
        return this;
    }

    /**
     * 是否展示时间不限按钮，作为筛选条件的时候需要，作为时间选择的时候则不需要
     *
     * @param isShowUnLimitedButton 是否隐藏按钮
     * @return this
     */
    public PickTimeViewDialog setIsShowUnLimitedButton(boolean isShowUnLimitedButton) {
        this.isShowUnLimitedButton = isShowUnLimitedButton;
        return this;
    }

    public PickTimeViewDialog setOnPickTimeDateSelectedListener(OnPickTimeDateSelectedListener onPickTimeDateSelectedListener) {
        this.onPickTimeDateSelectedListener = onPickTimeDateSelectedListener;
        return this;
    }

    /**
     * 设置时间不限按钮的点击事件
     *
     * @param onDialogInterfaceClickListener 监听
     * @return this
     */
    public PickTimeViewDialog setOnUnLimitedButtonClickListener(OnDialogInterfaceClickListener onDialogInterfaceClickListener) {
        this.onDialogInterfaceClickListener = onDialogInterfaceClickListener;
        return this;
    }

    public PickTimeViewDialog setDefaultDate(String defaultDate) {
        try {
            this.defaultDate = defaultDate;
            stTime = DateUtil.stringToLong(defaultDate, sdfDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public PickTimeViewDialog setDefaultHint(String defaultHint) {
        this.defaultHint = defaultHint;
        return this;
    }

    public PickTimeViewDialog build() {
        initView();
        return this;
    }

    public PickTimeViewDialog setViewType(int type) {
        this.pickerViewType = type;
        return this;
    }

    private void initView() {
        View pickerView = LayoutInflater.from(context).inflate(R.layout.picktime_view, null);
        PickTimeView pickDate = pickerView.findViewById(R.id.pickDate);
        TextView submitBtn = pickerView.findViewById(R.id.submitBtn);
        TextView tvSelected = pickerView.findViewById(R.id.Main_tvSelected);
        TextView tvUnlimited = pickerView.findViewById(R.id.tv_unlimited);
        tvUnlimited.setVisibility(isShowUnLimitedButton ? View.VISIBLE : View.GONE);
        tvUnlimited.setOnClickListener(v -> {
            if (onDialogInterfaceClickListener != null) {
                onDialogInterfaceClickListener.onDialogClick(this);
            }
        });
        submitBtn.setOnClickListener(this);
        pickDate.setOnSelectedChangeListener(this);
        pickDate.setViewType(pickerViewType);
        tvSelected.setHint(defaultHint);
        try {
            pickDate.setTimeMillis(DateUtil.stringToLong(defaultDate, sdfDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(pickerView); // 将布局设置给Dialog
        Window dialogWindow = getWindow();
        if (dialogWindow != null){
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 设置dialog宽度
            dialogWindow.setGravity(Gravity.BOTTOM);
        }
    }

    @Override
    public void onSelected(PickTimeView view, long timeMillis) {
        stTime = timeMillis;
    }

    public interface OnPickTimeDateSelectedListener {
        void onPickDateSelect(PickTimeViewDialog pickTimeViewDialog, long date);
    }

    @Override
    public void onClick(View v) {
        if (onPickTimeDateSelectedListener != null) {
            onPickTimeDateSelectedListener.onPickDateSelect(this, stTime);
        }
    }
}
