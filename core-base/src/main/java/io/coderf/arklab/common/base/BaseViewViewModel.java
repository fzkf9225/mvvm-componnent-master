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
 * ViewModel 工具基类（启动页等），不含页面引用。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2025/6/26 9:38
 * @updated 2026/9/12
 */
public class BaseViewViewModel extends AndroidViewModel {
    protected final String TAG = this.getClass().getSimpleName();

    public BaseViewViewModel(@NonNull Application application) {
        super(application);
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
