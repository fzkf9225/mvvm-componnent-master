package com.casic.titan.commonui.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.casic.titan.commonui.bean.AttachmentBean
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import pers.fz.mvvm.database.BaseRoomDao

/**
 * created by fz on 2024/11/6 10:31
 * describe:
 */
@Dao
abstract class AttachmentDao : BaseRoomDao<AttachmentBean>() {
    /**
     * 这个必须重写
     * @return
     */
    public override fun getTableName(): String {
        return AttachmentBean::class.java.getSimpleName()
    }

    @Query("Delete FROM AttachmentBean WHERE mobileId = :mobileId")
    abstract fun deleteByMobileId(mobileId: String)

    @Query("Delete FROM AttachmentBean WHERE mainId = :mainId")
    abstract fun deleteByMainId(mainId: String)

    @Query("Delete FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName")
    abstract fun delete(mainId: String, fieldName: String)

    @Query("Select * FROM AttachmentBean")
    abstract fun queryAll(): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId")
    abstract fun queryByMainId(mainId: String): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName")
    abstract fun queryList(mainId: String, fieldName: String): List<AttachmentBean>

    /**
     * 下面这几个可以看情况重写，主要是observedEntities得用法观察数据，在base中已经使用了占位符
     */
    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFindLiveData(query: SupportSQLiteQuery): LiveData<AttachmentBean>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doQueryByParamsLiveData(query: SupportSQLiteQuery): LiveData<List<AttachmentBean>>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFind(query: SupportSQLiteQuery): Single<AttachmentBean>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doQueryByParams(query: SupportSQLiteQuery): Flowable<List<AttachmentBean>>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun findPageList(query: SupportSQLiteQuery): List<AttachmentBean>
}

