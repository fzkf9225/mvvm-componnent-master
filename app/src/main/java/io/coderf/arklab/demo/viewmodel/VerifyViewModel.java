package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.database.AttachmentDatabase;
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl;
import io.coderf.arklab.common.utils.common.PropertiesUtil;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.database.PersonDatabase;
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl;

import io.reactivex.rxjava3.disposables.Disposable;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.utils.log.LogUtil;

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
