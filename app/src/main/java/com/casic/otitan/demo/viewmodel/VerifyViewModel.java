package com.casic.otitan.demo.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.database.AttachmentDatabase;
import com.casic.otitan.common.repository.AttachmentRepositoryImpl;
import com.casic.otitan.common.utils.common.PropertiesUtil;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.Person;
import com.casic.otitan.demo.database.PersonDatabase;
import com.casic.otitan.demo.repository.RoomPagingRepositoryImpl;

import io.reactivex.rxjava3.disposables.Disposable;
import com.casic.otitan.common.api.ApiRetrofit;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.utils.log.LogUtil;

import java.util.List;

/**
 * created by fz on 2024/11/6 10:57
 * describe:
 */
public class VerifyViewModel extends BaseViewModel<RoomPagingRepositoryImpl, BaseView> {

    public MutableLiveData<Boolean> liveData = new MutableLiveData<>();

    private AttachmentRepositoryImpl attachmentRoomRepositoryImpl;
    public VerifyViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RoomPagingRepositoryImpl createRepository() {
        String attachmentDatabaseName =  PropertiesUtil.getInstance().loadConfig(
                getApplication(),
                ContextCompat.getString(getApplication(), R.string.app_config_file)
        ).getPropertyValue("ATTACHMENT_DATA_BASE", "");
        attachmentRoomRepositoryImpl = new AttachmentRepositoryImpl(
                AttachmentDatabase.getInstance(
                        getApplication(),
                        attachmentDatabaseName
                ).getAttachmentDao(), baseView
        );
        return new RoomPagingRepositoryImpl(
                PersonDatabase.getInstance(getApplication()).getPersonDao(),
                attachmentRoomRepositoryImpl,
                baseView);
    }

    public void add(Person person, List<AttachmentBean> imageList) {
        Disposable disposable = iRepository.saveOrUpdateInfo(person, imageList)
                .subscribe(
                () -> {
                    liveData.postValue(true);
                }, throwable -> {
                    LogUtil.show(ApiRetrofit.TAG,"错误："+throwable);
                    baseView.showToast(throwable.getMessage());
                    liveData.postValue(false);
                });
    }
}
