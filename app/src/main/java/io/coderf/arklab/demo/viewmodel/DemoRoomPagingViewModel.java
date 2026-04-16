package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.database.PersonDatabase;
import io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.api.RepositoryFactory;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.datasource.RxRoomPagingSource;
import io.coderf.arklab.common.viewmodel.PagingViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class DemoRoomPagingViewModel extends PagingViewModel<RoomPagingRepositoryImpl, Person, BaseView> {

    public final Map<String,Object> queryParams = new HashMap<>();

    public final Set<String> keywordsKey = new HashSet<>();
    private String keywords;

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Inject
    public DemoRoomPagingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PagingData<Person>> createPagingData() {
        return PagingLiveData.getLiveData(new Pager<>(getPagingConfig(), () -> new RxRoomPagingSource<>(iRepository,queryParams,keywordsKey,keywords)));
    }

    @Override
    protected RoomPagingRepositoryImpl createRepository() {
        return RepositoryFactory.create(RoomPagingRepositoryImpl.class,PersonDatabase.getInstance(getApplication()).getPersonDao(), baseView);
//        return new RoomPagingRepositoryImpl(PersonDatabase.getInstance(getApplication()).getPersonDao(), baseView);
    }

    @Override
    public int getStartPage() {
        return 0;
    }
}
