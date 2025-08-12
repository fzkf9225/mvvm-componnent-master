package pers.fz.mvvm.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import pers.fz.mvvm.bean.AttachmentBean
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

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

    @Query("Select * FROM AttachmentBean where createUser = :createUser")
    abstract fun queryAllByUser(createUser:String?): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId")
    abstract fun queryByMainId(mainId: String): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName")
    abstract fun queryList(mainId: String, fieldName: String): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and createUser = :createUser")
    abstract fun queryByMainId(mainId: String,createUser:String?): List<AttachmentBean>

    @Query("Select * FROM AttachmentBean WHERE mainId = :mainId and fieldName = :fieldName and createUser = :createUser")
    abstract fun queryList(mainId: String, fieldName: String,createUser:String?): List<AttachmentBean>

    /**
     * 下面这几个可以看情况重写，主要是observedEntities得用法观察数据，在base中已经使用了占位符
     */
    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFindLiveData(query: SupportSQLiteQuery): LiveData<AttachmentBean>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFindListLiveData(query: SupportSQLiteQuery): LiveData<List<AttachmentBean>>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFind(query: SupportSQLiteQuery): Single<AttachmentBean>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doFindList(query: SupportSQLiteQuery): Single<List<AttachmentBean>>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doQueryFlowable(query: SupportSQLiteQuery): Flowable<List<AttachmentBean>>

    @RawQuery(observedEntities = [AttachmentBean::class])
    abstract override fun doQueryList(query: SupportSQLiteQuery): List<AttachmentBean>

}

