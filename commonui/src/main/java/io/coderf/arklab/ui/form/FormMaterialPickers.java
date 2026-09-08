package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.TimeZone;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.autosize.AutoSize;
import io.coderf.arklab.common.autosize.AutoSizeCompat;

/**
 * Form 日期/时间的 Material3 日历、时钟模式。
 * 默认仍走滚轮 {@link io.coderf.arklab.common.widget.dialog.DatePickDialog}，避免改排版。
 */
public final class FormMaterialPickers {

    public static final int UI_WHEEL = 0;
    public static final int UI_CALENDAR = 1;
    private static int unadaptDepth;

    private FormMaterialPickers() {
    }

    @Nullable
    static FragmentManager fragmentManager(@Nullable Context context) {
        FragmentActivity activity = activityOf(context);
        return activity == null ? null : activity.getSupportFragmentManager();
    }

    @Nullable
    static FragmentActivity activityOf(@Nullable Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof FragmentActivity activity) {
                return activity;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static void showDate(@NonNull Context context,
                         @Nullable CharSequence title,
                         @Nullable Calendar initial,
                         @NonNull OnDatePicked callback) {
        FragmentManager fm = fragmentManager(context);
        if (fm == null) {
            return;
        }
        long selection = utcMillis(initial != null ? initial : Calendar.getInstance());
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
                .setTitleText(TextUtils.isEmpty(title) ? null : title)
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(millis -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(localFromUtc(millis));
            callback.onPicked(calendar);
        });
        picker.addOnDismissListener(dialog -> restoreDensity(context));
        showUnadapted(context, () -> picker.show(fm, "form_material_date"));
    }

    public static void showTime(@NonNull Context context,
                         @Nullable CharSequence title,
                         @Nullable Calendar initial,
                         boolean withSeconds,
                         @NonNull OnDatePicked callback) {
        FragmentManager fm = fragmentManager(context);
        if (fm == null) {
            return;
        }
        Calendar seed = initial != null ? initial : Calendar.getInstance();
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTheme(R.style.ThemeOverlay_App_MaterialTimePicker)
                .setTitleText(TextUtils.isEmpty(title) ? "" : title)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(seed.get(Calendar.HOUR_OF_DAY))
                .setMinute(seed.get(Calendar.MINUTE))
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (initial != null) {
                calendar.setTimeInMillis(initial.getTimeInMillis());
            }
            calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
            calendar.set(Calendar.MINUTE, picker.getMinute());
            if (!withSeconds) {
                calendar.set(Calendar.SECOND, 0);
            }
            callback.onPicked(calendar);
        });
        picker.addOnDismissListener(dialog -> restoreDensity(context));
        showUnadapted(context, () -> picker.show(fm, "form_material_time"));
    }

    public static void showDateThenTime(@NonNull Context context,
                                 @Nullable CharSequence title,
                                 @Nullable Calendar initial,
                                 @NonNull OnDatePicked callback) {
        Calendar seed = initial != null ? (Calendar) initial.clone() : Calendar.getInstance();
        showDate(context, title, seed, date -> {
            seed.set(Calendar.YEAR, date.get(Calendar.YEAR));
            seed.set(Calendar.MONTH, date.get(Calendar.MONTH));
            seed.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
            showTime(context, title, seed, true, time -> {
                seed.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
                seed.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                callback.onPicked(seed);
            });
        });
    }

    /**
     * MaterialDatePicker 按系统 density 排 7 列格子。AutoSize 改过 density 后格子会偏右裁切。
     * 弹层期间取消适配，关掉后再还原。日期+时间连续弹层用计数，避免中间闪回适配。
     */
    private static void showUnadapted(@NonNull Context context, @NonNull Runnable show) {
        FragmentActivity activity = activityOf(context);
        if (activity != null && unadaptDepth == 0) {
            AutoSizeCompat.cancelAdapt(activity.getResources());
        }
        unadaptDepth++;
        show.run();
    }

    private static void restoreDensity(@NonNull Context context) {
        unadaptDepth = Math.max(0, unadaptDepth - 1);
        if (unadaptDepth > 0) {
            return;
        }
        FragmentActivity activity = activityOf(context);
        if (activity != null) {
            AutoSize.autoConvertDensityOfGlobal(activity);
        }
    }

    private static long utcMillis(@NonNull Calendar local) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        utc.set(Calendar.MILLISECOND, 0);
        return utc.getTimeInMillis();
    }

    private static long localFromUtc(long utcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(utcMillis);
        Calendar local = Calendar.getInstance();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        local.set(Calendar.MILLISECOND, 0);
        return local.getTimeInMillis();
    }

    public interface OnDatePicked {
        void onPicked(@NonNull Calendar calendar);
    }
}
