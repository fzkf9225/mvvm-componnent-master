package com.casic.titan.mqttcomponent;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by fz on 2023/7/5 17:22
 * describe :
 */
public class HelperUtil {

    public static String createMqttClientId(Context mContext) {
        String cacheClientId = CloudDataHelper.getClientId();
        if (!TextUtils.isEmpty(cacheClientId)) {
            return cacheClientId;
        }
        String androidId = getDeviceId(mContext);
        if (TextUtils.isEmpty(androidId)) {
            androidId = UUID.randomUUID().toString();
        }
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("mqtt_android_");
        try {
            stringBuffer.append(MD5Util.md5Encode(androidId));
        } catch (Exception e) {
            e.printStackTrace();
            stringBuffer.append(UUID.randomUUID().toString());
        }
        stringBuffer.append(Constants.VERSION);
        CloudDataHelper.saveClientId(stringBuffer.toString());
        return stringBuffer.toString();
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context mContext) {
        try {
            TelephonyManager mTm = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
            if (mTm != null) {
                String imei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imei = Settings.System.getString(
                            mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = mTm.getImei();
                } else {
                    imei = mTm.getDeviceId();
                }
                return imei == null ? "" : imei;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
