package pers.fz.mvvm.bean;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * created by fz on 2024/11/6 13:29
 * describe:
 */
@Entity
public abstract class BaseDaoBean extends BaseObservable {

    // 用于标识表中的主键以及主键自增
    @PrimaryKey
    @NonNull
    private String id = UUID.randomUUID().toString().replace("-", "");

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }
}

