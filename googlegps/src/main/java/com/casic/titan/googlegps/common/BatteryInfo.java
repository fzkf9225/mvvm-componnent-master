package com.casic.titan.googlegps.common;

public class BatteryInfo {
    public int BatteryLevel;
    public boolean IsCharging;
    public BatteryInfo(int percentage, boolean isCharging){
        BatteryLevel = percentage;
        IsCharging = isCharging;
    }
}
