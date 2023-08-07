package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.wight.pickertime.PickValueView;
import pers.fz.mvvm.util.log.LogUtil;

import java.util.List;

/**
 * Created by fz on 2017/10/18.
 * 单选弹框
 */

public class PickStringDialog<T extends PopupWindowBean> implements View.OnClickListener, PickValueView.onSelectedChangeListener {

    public Context context;
    private Dialog customDialog;
    private TextView submitBtn, tvSelected;
    private View pickerView;
    private LinearLayout pvLayout;
    private PickValueView pickString;
    private OnPickSelectedListener onPickSelectedListener;
    private String defaultString;
    private List<T> details;

    public PickStringDialog(Context context, List<T> details, OnPickSelectedListener onPickSelectedListener) {
        this.context = context;
        this.details = details;
        this.onPickSelectedListener = onPickSelectedListener;
    }

    public Dialog create(String defaultString) {
        this.defaultString = defaultString;

        customDialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        pickerView = LayoutInflater.from(context).inflate(
                R.layout.numberpicker, null);
        pvLayout = (LinearLayout) pickerView.findViewById(R.id.Main_pvLayout);
        tvSelected = (TextView) pickerView.findViewById(R.id.Main_tvSelected);
        pickString = (PickValueView) pickerView.findViewById(R.id.pickString);
        pickString.setVisibility(View.VISIBLE);

        if (defaultString == null || defaultString.equals("")) {
            tvSelected.setHint("请选择数量");
        } else {
            tvSelected.setText(defaultString);
        }

        pickString.setOnSelectedChangeListener(this);

        submitBtn = (TextView) pickerView.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(this);
        initPickData();
        customDialog.setContentView(pickerView); // 将布局设置给Dialog
        Window dialogWindow = customDialog.getWindow();
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);// 设置dialog宽度
        dialogWindow.setGravity(Gravity.BOTTOM);
        return customDialog;
    }

    private void initPickData() {
        if (details == null) {
            return;
        }
        String[] strs = new String[details.size()];
        for (int i = 0; i < details.size(); i++) {
            strs[i] = details.get(i).getName();
        }
        if (strs.length < 3) {
            pickString.setShowCount(strs.length);
        }

        if (defaultString == null || defaultString.equals("")) {
            pickString.setValueData(strs, strs.length < 1 ? "" : strs[0]);
        } else {
            pickString.setValueData(strs, defaultString);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submitBtn) {
            if (customDialog != null && customDialog.isShowing()) {
                customDialog.dismiss();
            }
            if (onPickSelectedListener != null) {
                if (TextUtils.isEmpty(tvSelected.getText().toString())) {
                    for (int i = 0; i < details.size(); i++) {
                        if (details.get(i).getName().equals(pickString.getLeftDefaultString())) {
                            onPickSelectedListener.pickSelected(this,
                                    i, pickString.getLeftDefaultString());
                        }
                    }
                } else {
                    onPickSelectedListener.pickSelected(this,
                            (Integer) tvSelected.getTag(), tvSelected.getText().toString());
                }
            }
        }
    }

    @Override
    public void onSelected(PickValueView view, Object leftValue, Object middleValue, Object rightValue) {
        if (view == pickString && leftValue != null) {
            tvSelected.setText(leftValue.toString());
            for (T contents : details) {
                if (contents.getName().equals(leftValue.toString())) {
                    tvSelected.setTag(contents.getId());
                }
            }
        }
    }

    public interface OnPickSelectedListener {
        void pickSelected(PickStringDialog mCurrentPick, Integer position, String showText);
    }
}
