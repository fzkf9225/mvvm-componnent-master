package pers.fz.mvvm.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

    public void startActivity(Context context, Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(context, toClx);
        ContextCompat.startActivity(context, intent, bundle);
    }

    public void startActivity(Context context, Class<?> toClx) {
        startActivity(context, toClx, null);
    }
}

