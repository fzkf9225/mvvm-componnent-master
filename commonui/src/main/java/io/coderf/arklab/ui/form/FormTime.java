package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;

import io.coderf.arklab.common.enums.DateMode;
import io.coderf.arklab.common.utils.common.DateUtil;
import io.coderf.arklab.common.utils.common.NumberUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.DatePickDialog;
import io.coderf.arklab.ui.R;

/**
 * FormTime 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/12/26 16:27
 */
public class FormTime extends FormSelection {
    /**
     * 时间格式分隔符，默认为 ":"，也就是HH:mm:ss中间的":"
     */
    protected String separator;
    /**
     * 时间格式，默认为HH:mm:ss
     */
    protected String format;
    /**
     * 确认按钮背景色
     */
    protected int confirmTextColor;
    /**
     * 日期选择器模式，参考DateMode
     */
    protected int datePickModel;
    /**
     * 日期选择器dialog
     */
    protected DatePickDialog datePickDialog;
    protected int datePickerUi;

    public FormTime(Context context) {
        super(context);
    }

    public FormTime(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        TypedArray typedArray = null;
        boolean showClearButton;
        if (attrs != null) {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            separator = typedArray.getString(R.styleable.FormUI_separator);
            format = typedArray.getString(R.styleable.FormUI_format);
            datePickModel = typedArray.getInt(R.styleable.FormUI_datePickModel, DateMode.HOUR_MINUTE_SECOND.model);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext()));
            showClearButton = typedArray.getBoolean(R.styleable.FormUI_showClearButton, true);
            datePickerUi = typedArray.getInt(R.styleable.FormUI_datePickerUi, FormMaterialPickers.UI_WHEEL);
        } else {
            datePickModel = DateMode.HOUR_MINUTE_SECOND.model;
            confirmTextColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.onSurface(getContext());
            datePickerUi = FormMaterialPickers.UI_WHEEL;
            showClearButton = true;
        }
        if (TextUtils.isEmpty(separator)) {
            separator = ":";
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_FORMAT_TIME;
        }
        datePickDialog = new DatePickDialog(getContext())
                .setPositiveTextColor(this.confirmTextColor)
                .setTodayTextColor(this.confirmTextColor)
                .setDateMode(DateMode.getMode(this.datePickModel))
                .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                    String text = NumberUtil.formatMonthOrDay(hour) + separator + NumberUtil.formatMonthOrDay(minute) + separator + NumberUtil.formatMonthOrDay(second);
                    if (DateUtil.DEFAULT_FORMAT_TIME.equals(this.format)) {
                        ((MaterialTextView) tvSelection).setText(text);
                        return;
                    }
                    ((MaterialTextView) tvSelection).setText(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_TIME));
                })
                .setOnClearClickListener(dialog -> ((MaterialTextView) tvSelection).setText(null));
        if (typedArray != null) {
            FormDatePickDialogHelper.applyFormStyle(getContext(), typedArray, datePickDialog);
            typedArray.recycle();
        } else {
            FormDatePickDialogHelper.applyDefaultStyle(getContext(), confirmTextColor, showClearButton, datePickDialog);
        }
        datePickDialog.builder();
    }

    public DatePickDialog getDatePickDialog() {
        return datePickDialog;
    }

    @Override
    public void createText() {
        super.createText();
        tvSelection.setOnClickListener(v -> {
            MaterialTextView textView = (MaterialTextView) tvSelection;
            try {
                if (!TextUtils.isEmpty(textView.getText())) {
                    Date date = DateUtil.getDateByFormat(textView.getText().toString(), this.format);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    datePickDialog.setDefaultHour(calendar.get(Calendar.HOUR_OF_DAY));
                    datePickDialog.setDefaultMinute(calendar.get(Calendar.MINUTE));
                    datePickDialog.setDefaultMinute(calendar.get(Calendar.SECOND));
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.logger(TAG, "解析默认日志异常:" + e);
            }
            if (datePickerUi == FormMaterialPickers.UI_CALENDAR) {
                Calendar initial = Calendar.getInstance();
                try {
                    if (!TextUtils.isEmpty(textView.getText())) {
                        Date date = DateUtil.getDateByFormat(textView.getText().toString(), this.format);
                        initial.setTime(date);
                    }
                } catch (Exception ignored) {
                }
                FormMaterialPickers.showTime(getContext(), labelString, initial, true, calendar -> {
                    String text = NumberUtil.formatMonthOrDay(calendar.get(Calendar.HOUR_OF_DAY))
                            + separator + NumberUtil.formatMonthOrDay(calendar.get(Calendar.MINUTE))
                            + separator + NumberUtil.formatMonthOrDay(calendar.get(Calendar.SECOND));
                    if (DateUtil.DEFAULT_FORMAT_TIME.equals(this.format)) {
                        textView.setText(text);
                    } else {
                        textView.setText(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_TIME));
                    }
                });
                return;
            }
            datePickDialog.show();
        });
    }


}

