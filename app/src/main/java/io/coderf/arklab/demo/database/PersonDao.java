package io.coderf.arklab.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;

import io.coderf.arklab.common.annotation.RoomObservedEntity;
import io.coderf.arklab.demo.bean.Person;

/**
 * 人员表 Room Dao 示例（Demo 模块）。
 *
 * <p><b>接入要点：</b></p>
 * <ul>
 *   <li>{@link RoomObservedEntity} 指定实体，KSP 生成 {@link PersonDaoRawQueryBridge}；</li>
 *   <li>本类继承 Bridge，只需实现 {@link #getTableName()}；</li>
 *   <li>CRUD / 动态查询 / 分页见 {@link io.coderf.arklab.common.dao.BaseRoomDao}；</li>
 *   <li>仓库层使用 {@link io.coderf.arklab.demo.repository.RoomPagingRepositoryImpl}，
 *       分页列表使用 {@link io.coderf.arklab.common.datasource.RxRoomPagingSource}。</li>
 * </ul>
 *
 * <p>模块需配置：{@code ksp project(':room-processor')}</p>
 *
 * @author fz
 * @see PersonDatabase
 */
@Dao
@RoomObservedEntity(Person.class)
public abstract class PersonDao extends PersonDaoRawQueryBridge {

    /**
     * 表名须与 {@link Person} 的 @Entity(tableName) 一致；
     * 未指定 tableName 时为类名 "Person"。
     */
    @NonNull
    @Override
    public String getTableName() {
        return Person.class.getSimpleName();
    }
}
