package pers.fz.mvvm.utils.common;

import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by fz on 2023/9/6 17:35
 * describe :
 */
public class RxMenuView {
    /**
     * 默认点击间隔时间，单位：毫秒
     */
    private static final long DEFAULT_CLICK_INTERVAL = 500;

    public static Disposable setOnMenuItemClickListener(Toolbar toolbar, Consumer<Toolbar> onMenuItemClickListener) {
        return setOnMenuItemClickListener(toolbar, DEFAULT_CLICK_INTERVAL, onMenuItemClickListener);
    }

    public static Disposable setOnMenuItemClickListener(Toolbar toolbar, long interval, Consumer<Toolbar> onMenuItemClickListener) {
        return Observable.create((ObservableOnSubscribe<Toolbar>)
                        emitter -> toolbar.setOnMenuItemClickListener(item -> {
                            emitter.onNext(toolbar);
                            return false;
                        })
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .throttleFirst(interval, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onMenuItemClickListener);
    }

    public static Disposable setOnMenuItemClickListener(Toolbar toolbar, long interval,  String message, Consumer<Toolbar> onMenuItemClickListener) {
        return Observable.create((ObservableOnSubscribe<Toolbar>)
                        emitter -> toolbar.setOnMenuItemClickListener(item -> {
                            emitter.onNext(toolbar);
                            return false;
                        })
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .throttleFirst(interval, TimeUnit.MILLISECONDS, Schedulers.computation(), v -> Toast.makeText(toolbar.getContext(), message, Toast.LENGTH_SHORT).show())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onMenuItemClickListener, throwable -> Toast.makeText(toolbar.getContext(), "操作发生异常", Toast.LENGTH_SHORT).show());
    }
}
