package pers.fz.mvvm.util.common;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import pers.fz.mvvm.R;


/**
 * created by fz on 2023/4/23 10:05
 * describe:
 **/
public class PropertiesUtil extends Properties {
    private final static String TAG = PropertiesUtil.class.getSimpleName();

    private PropertiesUtil() {
    }

    private static final class PropertiesUtilHolder {
        static final PropertiesUtil PROPERTIES_UTIL = new PropertiesUtil();
    }

    public static PropertiesUtil getInstance() {
        return PropertiesUtilHolder.PROPERTIES_UTIL;
    }

    /**
     * 获取默认配置
     *
     * @param mContext 视图
     * @return
     */
    public PropertiesUtil loadConfig(Context mContext) {
        try {
            String configFile = mContext.getResources().getString(R.string.app_config_file);
            if (TextUtils.isEmpty(configFile)) {
                return this;
            }
            InputStream inputStream = mContext.getAssets().open(configFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            load(bufferedReader);
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public PropertiesUtil loadConfig(Context mContext, String configFile) {
        try {
            if (TextUtils.isEmpty(configFile)) {
                return this;
            }
            InputStream inputStream = mContext.getAssets().open(configFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            load(bufferedReader);
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 获取配置IP
     *
     * @return IP地址
     */
    public String getBaseUrl() {
        return getProperty("BASE_URL");
    }

    /**
     * 获取配置PORT
     *
     * @return 端口地址
     */
    public String getPort() {
        return getProperty("PORT");
    }

    /**
     * 获取AppKey
     *
     * @return
     */
    public String getAppKey() {
        return getProperty("APP_KEY");
    }

    /**
     * 获取AppId
     *
     * @return
     */
    public String getAppId() {
        return getProperty("APP_ID");
    }

    /**
     * 获取appSecret
     *
     * @return app密钥
     */
    public String getAppSecret() {
        return getProperty("APP_SECRET");
    }

    /**
     * 获取协议版本
     *
     * @return 默认1.0.0
     */
    public String getProtocolVersion() {
        return getProperty("PROTOCOL_VERSION", "1.0.0");
    }

    /**
     * 获取自定义getProperty
     *
     * @param key          key值
     * @param defaultValue 默认值
     * @return String
     */
    public String getPropertyValue(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

}
