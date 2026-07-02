package io.coderf.arklab.common.utils.common;

import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 防止快速点击工具类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/2 10:28
 */
public class RxView {
    /**
     * 默认点击间隔时间，单位：毫秒
     */
    private static final long DEFAULT_CLICK_INTERVAL = 500;

    public static Disposable setOnClickListener(View view, Consumer<View> onClickListener) {
        return setOnClickListener(view, DEFAULT_CLICK_INTERVAL, onClickListener);
    }

    public static Disposable setOnClickListener(View view, long interval, Consumer<View> onClickListener) {
        return Observable.create((ObservableOnSubscribe<View>)
                        emitter -> view.setOnClickListener(emitter::onNext)
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .throttleFirst(interval, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onClickListener);
    }

    public static Disposable setOnClickListener(View view, long interval, String message, Consumer<View> onClickListener) {
        return Observable.create((ObservableOnSubscribe<View>)
                        emitter -> view.setOnClickListener(emitter::onNext)
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .throttleFirst(interval, TimeUnit.MILLISECONDS, Schedulers.computation(), v -> Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onClickListener, throwable -> Toast.makeText(view.getContext(), "操作发生异常", Toast.LENGTH_SHORT).show());
    }


}
