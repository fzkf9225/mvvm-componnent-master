package pers.fz.mvvm.util.common;

import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


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
}
