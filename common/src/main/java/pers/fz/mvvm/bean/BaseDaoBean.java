package pers.fz.mvvm.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * created by fz on 2024/11/6 13:29
 * describe:
 */
@Entity
public abstract class BaseDaoBean extends BaseObservable {

    // 用于标识表中的主键以及主键自增
    @PrimaryKey(autoGenerate = true)
    private long id;

    @Bindable
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

