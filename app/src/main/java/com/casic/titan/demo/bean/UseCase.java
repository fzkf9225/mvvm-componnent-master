package com.casic.titan.demo.bean;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;

/**
 * Created by fz on 2023/8/14 10:19
 * describe :
 */
public class UseCase extends BaseObservable implements Parcelable {
    private Class<?> clx;
    private String name;
    private String describe;
    private Bundle args;

    public UseCase() {
    }

    public UseCase(Class<?> clx, String name, String describe, Bundle args) {
        this.clx = clx;
        this.name = name;
        this.describe = describe;
        this.args = args;
    }

    protected UseCase(Parcel in) {
        name = in.readString();
        describe = in.readString();
        args = in.readBundle();
    }

    public static final Creator<UseCase> CREATOR = new Creator<UseCase>() {
        @Override
        public UseCase createFromParcel(Parcel in) {
            return new UseCase(in);
        }

        @Override
        public UseCase[] newArray(int size) {
            return new UseCase[size];
        }
    };

    public Class<?> getClx() {
        return clx;
    }

    public void setClx(Class<?> clx) {
        this.clx = clx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public Bundle getArgs() {
        return args;
    }

    public void setArgs(Bundle args) {
        this.args = args;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(describe);
        dest.writeBundle(args);
    }
}
