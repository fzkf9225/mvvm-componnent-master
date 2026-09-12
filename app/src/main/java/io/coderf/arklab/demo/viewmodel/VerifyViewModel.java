package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import dagger.hilt.EntryPoints;
import io.coderf.arklab.base.api.AppPropertiesConfig;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.database.AttachmentDatabase;
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.database.PersonDatabase;
import io.coderf.arklab.demo.di.AppPropertiesEntryPoint;
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * VerifyViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/6 10:57
 * @updated 2026/9/12
 */
public class VerifyViewModel extends BaseViewModel<RoomPagingRepositoryImpl> {

    public MutableLiveData<Boolean> liveData = new MutableLiveData<>();

    private AttachmentRepositoryImpl attachmentRoomRepositoryImpl;

    public VerifyViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RoomPagingRepositoryImpl createRepository() {
        AppPropertiesConfig config = EntryPoints.get(
                getApplication(),
                AppPropertiesEntryPoint.class
        ).appPropertiesConfig();
        String attachmentDatabaseName = config.getAttachmentDataBase();
        attachmentRoomRepositoryImpl = new AttachmentRepositoryImpl(
                AttachmentDatabase.getInstance(
                        getApplication(),
                        attachmentDatabaseName
                ).getAttachmentDao()
        );
        return new RoomPagingRepositoryImpl(
                PersonDatabase.getInstance(getApplication()).getPersonDao(),
                attachmentRoomRepositoryImpl);
    }

    public void add(Person person, List<AttachmentBean> imageList) {
        Disposable disposable = iRepository.saveOrUpdateInfo(person, imageList)
                .subscribe(
                () -> {
                    liveData.postValue(true);
                }, throwable -> {
                    LogUtil.logger(ApiRetrofit.TAG, "错误：" + throwable);
                    getNetworkRequestUiHost().showToast(throwable.getMessage());
                    liveData.postValue(false);
                });
    }
}
