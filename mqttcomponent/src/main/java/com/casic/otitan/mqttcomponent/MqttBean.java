package com.casic.otitan.mqttcomponent;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Created by fz on 2023/4/28 18:21
 * describe :MqttBean配置
 */
public class MqttBean implements Parcelable {
    private String mqtt_addr;
    private String mqtt_password;
    private String mqtt_username;

    protected MqttBean(Parcel in) {
        mqtt_addr = in.readString();
        mqtt_password = in.readString();
        mqtt_username = in.readString();
    }

    public static final Creator<MqttBean> CREATOR = new Creator<MqttBean>() {
        @Override
        public MqttBean createFromParcel(Parcel in) {
            return new MqttBean(in);
        }

        @Override
        public MqttBean[] newArray(int size) {
            return new MqttBean[size];
        }
    };

    public String getMqtt_addr() {
        return mqtt_addr;
    }

    public void setMqtt_addr(String mqtt_addr) {
        this.mqtt_addr = mqtt_addr;
    }

    public String getMqtt_password() {
        return mqtt_password;
    }

    public void setMqtt_password(String mqtt_password) {
        this.mqtt_password = mqtt_password;
    }

    public String getMqtt_username() {
        return mqtt_username;
    }

    public void setMqtt_username(String mqtt_username) {
        this.mqtt_username = mqtt_username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mqtt_addr);
        dest.writeString(mqtt_password);
        dest.writeString(mqtt_username);
    }

    @Override
    public String toString() {
        return "MqttBean{" +
                "mqtt_addr='" + mqtt_addr + '\'' +
                ", mqtt_password='" + mqtt_password + '\'' +
                ", mqtt_username='" + mqtt_username + '\'' +
                '}';
    }
}
