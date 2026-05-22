package io.coderf.arklab.demo.repository

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.AttachmentBean
import io.coderf.arklab.common.repository.AttachmentRepositoryImpl
import io.coderf.arklab.common.repository.RoomRepositoryImpl
import io.coderf.arklab.demo.bean.Person
import io.coderf.arklab.demo.database.PersonDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Demo：人员表仓库，继承 [RoomRepositoryImpl] 并扩展「人员 + 附件」联合保存。
 *
 * ## 用法
 * ```java
 * RoomPagingRepositoryImpl repo = RepositoryFactory.create(
 *     RoomPagingRepositoryImpl.class,
 *     PersonDatabase.getInstance(context).getPersonDao(),
 *     baseView
 * );
 * // 分页
 * new RxRoomPagingSource<>(repo, "id");
 * // 保存
 * repo.saveOrUpdateInfo(person, attachments).subscribe(...);
 * ```
 *
 * Loading 通过 [getRequestUi] 展示，由 ViewModel 注入，勿使用 baseView.showLoading。
 *
 * @author fz
 * @see io.coderf.arklab.common.datasource.RxRoomPagingSource
 */
class RoomPagingRepositoryImpl :
    RoomRepositoryImpl<Person, PersonDao, BaseView> {

    /** 可选：保存人员时同步写入附件表 */
    var attachmentRepositoryImpl: AttachmentRepositoryImpl? = null

    /**
     * @param roomDao 人员 Dao
     * @param attachmentRepositoryImpl 附件仓库
     * @param baseView 页面（用于 BaseRepository 绑定，UI 走 RequestUi）
     */
    constructor(
        roomDao: PersonDao,
        attachmentRepositoryImpl: AttachmentRepositoryImpl,
        baseView: BaseView
    ) : super(roomDao, baseView) {
        this.attachmentRepositoryImpl = attachmentRepositoryImpl
    }

    /** 仅人员表，不涉及附件 */
    constructor(
        roomDao: PersonDao,
        baseView: BaseView
    ) : super(roomDao, baseView)

    /**
     * 保存或更新人员信息，并可选保存附件列表。
     * 先 insert Person，再在同一事务链中调用附件仓库。
     *
     * @param person 人员实体
     * @param imageList 附件列表，可为 null
     * @return Completable 在主线程回调
     */
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
                getRequestUi()?.showLoading("正在保存,请稍后...", true)
            }
            .doFinally {
                getRequestUi()?.hideLoading()
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}
