package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import pers.fz.mvvm.R;
import pers.fz.mvvm.wight.pickertime.PickValueView;

/**
 * Created by fz on 2017/10/18.
 * 单选弹框
 */

public class FloorsPickDialog implements View.OnClickListener, PickValueView.onSelectedChangeListener {

    public Context context;
    private Dialog customDialog;
    private TextView singleTextView, submitBtn, tvSelected;
    private View pickerView;
    private LinearLayout pvLayout;
    private PickValueView pickValue;
    private OnPickSelectedListener onPickSelectedListener;
    private String defaultString;

    public FloorsPickDialog(Context context, String defaultString, TextView singleTextView) {
        this.context = context;
        this.defaultString = defaultString;
        this.singleTextView = singleTextView;
    }

    public Dialog create() {
        customDialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        pickerView = LayoutInflater.from(context).inflate(
                R.layout.numberpicker, null);
        pvLayout = (LinearLayout) pickerView.findViewById(R.id.Main_pvLayout);
        tvSelected = (TextView) pickerView.findViewById(R.id.Main_tvSelected);
        pickValue = (PickValueView) pickerView.findViewById(R.id.pickValue2);
        pickValue.setVisibility(View.VISIBLE);
        if (defaultString == null || defaultString.equals("")) {
            tvSelected.setHint("请选择数量");
        } else {
            tvSelected.setText(defaultString);
        }
        pickValue.setOnSelectedChangeListener(this);

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
        Integer value[] = new Integer[50];
        for (int i = 0; i < value.length; i++) {
            value[i] = i - 4;
        }
        if (defaultString == null || defaultString.equals("")) {
            pickValue.setValueData(value, value[14]);
        } else {
            pickValue.setValueData(value, Integer.parseInt(defaultString));
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submitBtn) {
            if (tvSelected.getText().toString() == null || tvSelected.getText().equals("")) {
                singleTextView.setText("10");
//                    if(onPickSelectedListener!=null) onPickSelectedListener.pickSelected("1");
            } else {
                singleTextView.setText(tvSelected.getText().toString());
//                    if(onPickSelectedListener!=null) onPickSelectedListener.pickSelected(tvSelected.getText().toString());
            }
            if (customDialog != null && customDialog.isShowing()) {
                customDialog.dismiss();
            }
        }
    }

    @Override
    public void onSelected(PickValueView view, Object leftValue, Object middleValue, Object rightValue) {
        if (view == pickValue && leftValue != null) {
            tvSelected.setText(leftValue.toString());
        }
    }

    public void setOnPickSelectedListener(OnPickSelectedListener onPickSelectedListener) {
        this.onPickSelectedListener = onPickSelectedListener;
    }

    public interface OnPickSelectedListener {
        void pickSelected(String pickNum);
    }
}
