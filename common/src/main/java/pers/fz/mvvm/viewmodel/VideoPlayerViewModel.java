package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;

/**
 * Created by fz on 2021/5/26 14:16
 * describe:
 */
public class VideoPlayerViewModel<BV extends BaseView> extends BaseViewModel<BV> {
    public VideoPlayerViewModel(@NonNull Application application) {
        super(application);
    }

}
