package com.casic.otitan.demo.repository

import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.bean.AttachmentBean
import com.casic.otitan.common.repository.AttachmentRepositoryImpl
import com.casic.otitan.common.repository.RoomRepositoryImpl
import com.casic.otitan.demo.bean.Person
import com.casic.otitan.demo.database.PersonDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
class RoomPagingRepositoryImpl :
    RoomRepositoryImpl<Person, PersonDao, BaseView> {

    var attachmentRepositoryImpl: AttachmentRepositoryImpl?=null
    constructor(
        roomDao: PersonDao,
        attachmentRepositoryImpl: AttachmentRepositoryImpl,
        baseView: BaseView
    ) : super(roomDao, baseView){
        this.attachmentRepositoryImpl = attachmentRepositoryImpl
    }
    constructor(
        roomDao: PersonDao,
        baseView: BaseView
    ) : super(roomDao, baseView){

    }
    fun saveOrUpdateInfo(
        person: Person,
        imageList: List<AttachmentBean>?
    ): Completable {
        return getRoomDao().insert(person)
            .andThen(
                Completable.fromAction {
                    imageList?.forEach { item ->
//                        item.mainId = person.mobile
                    }
                    // 这里直接调用，不期望返回值
                    attachmentRepositoryImpl!!.saveOrUpdate(
                        imageList,
                        UUID.randomUUID().toString().replace("-", "")
                    )
                }
            )
            .timeout(30, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onSubscribe ->
                addDisposable(onSubscribe)
                baseView?.showLoading("正在保存,请稍后...", true)
            }
            .doFinally {
                baseView?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}
