package io.coderf.arklab.common.utils.encode;

import android.content.Context;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 基于 assets 中 PEM 密钥文件的 RSA 加解密门面。
 * <p>
 * 依赖 {@link RSAUtilsHelper} 完成实际加解密；公钥/私钥分别从
 * {@code assets/rsa_public_key.pem}、{@code assets/rsa_private_key.pem} 加载。
 * 密文以 Base64 字符串形式出入，便于传输与展示。
 * </p>
 * <p>
 * 推荐使用 {@link #encrypt(Context, String)} / {@link #decrypt(Context, String)}；
 * 历史方法 {@link #enCode} / {@link #deCode} 已过时。
 * </p>
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026-08-27 13:44
 */
public final class RSAUtils {

    private static final String ASSET_PUBLIC_KEY = "rsa_public_key.pem";
    private static final String ASSET_PRIVATE_KEY = "rsa_private_key.pem";
    private static final String ERROR_ENCRYPT = "加密错误";
    private static final String ERROR_DECRYPT = "解密错误";

    private RSAUtils() {
        // no instance
    }

    /**
     * 使用 assets 中的公钥加密，返回 Base64 密文。
     *
     * @param context 用于读取 assets，不可为 null
     * @param data    明文，不可为 null
     * @return Base64 密文；失败时返回 {@code "加密错误"}
     */
    public static String encrypt(Context context, String data) {
        if (context == null || data == null) {
            return ERROR_ENCRYPT;
        }
        try {
            InputStream inPublic = context.getResources().getAssets().open(ASSET_PUBLIC_KEY);
            PublicKey publicKey = RSAUtilsHelper.loadPublicKey(inPublic);
            byte[] encryptByte = RSAUtilsHelper.encryptData(
                    data.getBytes(StandardCharsets.UTF_8), publicKey);
            if (encryptByte == null) {
                return ERROR_ENCRYPT;
            }
            // 与解密侧统一使用 Base64Util，避免 java.util.Base64 与自实现 Base64 混用
            return Base64Util.encode(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_ENCRYPT;
        }
    }

    /**
     * 使用 assets 中的私钥解密 Base64 密文。
     *
     * @param context 用于读取 assets，不可为 null
     * @param data    Base64 密文，不可为 null
     * @return 明文；失败时返回 {@code "解密错误"}
     */
    public static String decrypt(Context context, String data) {
        if (context == null || data == null) {
            return ERROR_DECRYPT;
        }
        try {
            InputStream inPrivate = context.getResources().getAssets().open(ASSET_PRIVATE_KEY);
            PrivateKey privateKey = RSAUtilsHelper.loadPrivateKey(inPrivate);
            // 与加密侧统一：Base64Util 解码
            byte[] encrypted = Base64Util.decode(data);
            if (encrypted == null || encrypted.length == 0) {
                return ERROR_DECRYPT;
            }
            byte[] decryptByte = RSAUtilsHelper.decryptData(encrypted, privateKey);
            if (decryptByte == null) {
                return ERROR_DECRYPT;
            }
            return new String(decryptByte, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_DECRYPT;
        }
    }

    /**
     * 公钥加密，返回 Base64 密文。
     *
     * @param context Context
     * @param data    明文
     * @return Base64 密文或 {@code "加密错误"}
     * @deprecated 请改用 {@link #encrypt(Context, String)}
     */
    @Deprecated
    public static String enCode(Context context, String data) {
        return encrypt(context, data);
    }

    /**
     * 私钥解密 Base64 密文。
     *
     * @param context Context
     * @param data    Base64 密文
     * @return 明文或 {@code "解密错误"}
     * @deprecated 请改用 {@link #decrypt(Context, String)}
     */
    @Deprecated
    public static String deCode(Context context, String data) {
        return decrypt(context, data);
    }
}
