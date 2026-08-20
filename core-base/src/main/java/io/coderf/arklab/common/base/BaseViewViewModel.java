package io.coderf.arklab.common.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

/**
 * created by fz on 2025/6/26 9:38
 * describe:
 */
public class BaseViewViewModel<BV extends BaseView> extends AndroidViewModel {
    protected final String TAG = this.getClass().getSimpleName();

    protected BV baseView;

    public BaseViewViewModel(@NonNull Application application) {
        super(application);
    }

    public void setBaseView(BV baseView) {
        this.baseView = baseView;
    }

    public BV getBaseView() {
        return baseView;
    }

    /**
     * 启动页面。{@code extras} 写入 Intent extras（不是 ActivityOptions）。
     */
    public void startActivity(@NonNull Context context, @NonNull Class<?> toClx, @Nullable Bundle extras) {
        Intent intent = new Intent(context, toClx);
        if (extras != null) {
            intent.putExtras(extras);
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void startActivity(@NonNull Context context, @NonNull Class<?> toClx) {
        startActivity(context, toClx, null);
    }
}
