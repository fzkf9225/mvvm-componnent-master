package io.coderf.arklab.common.utils.encode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 摘要工具类。
 * <p>
 * 推荐使用 {@link #md5(String)} / {@link #md5(byte[])} 生成标准 32 位小写十六进制 MD5。
 * 其余历史方法因编码不规范或并非真正 MD5，已标记为过时，请勿再使用。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/27 13:44
 */
public final class MD5Util {

    private static final String ALGORITHM = "MD5";

    private MD5Util() {
        // no instance
    }

    /**
     * 对字符串做 MD5 摘要，使用 UTF-8 编码，返回 32 位小写十六进制字符串。
     * <p>推荐方法。失败时返回空字符串（不抛异常，便于调用方兼容旧逻辑）。</p>
     *
     * @param input 原始字符串，可为 null（返回空串）
     * @return 32 位小写 MD5，异常或 null 输入时返回 ""
     */
    public static String md5(String input) {
        if (input == null) {
            return "";
        }
        return md5(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对字节数组做 MD5 摘要，返回 32 位小写十六进制字符串。
     *
     * @param data 原始字节，可为 null（返回空串）
     * @return 32 位小写 MD5，异常或 null 输入时返回 ""
     */
    public static String md5(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] md5Bytes = digest.digest(data);
            return toHex(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            // MD5 在标准 JRE/Android 中必定存在，此处仅作兜底
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 对字符串做 MD5 摘要（UTF-8），返回 32 位小写十六进制字符串。
     * <p>与 {@link #md5(String)} 行为一致，保留方法名以兼容已有调用（如 FileUtil、ApiRetrofit）。</p>
     *
     * @param inStr 原始字符串
     * @return 32 位小写 MD5
     * @throws Exception 保留声明以兼容旧签名；实际内部已捕获算法异常，不会真正抛出
     * @deprecated 请改用 {@link #md5(String)}，语义更清晰且无受检异常
     */
    @Deprecated
    public static String md5Encode(String inStr) throws Exception {
        return md5(inStr);
    }

    /**
     * 历史 MD5 实现：将 char 直接强转为 byte，非 UTF-8，中文/多字节字符结果与标准 MD5 不一致。
     *
     * @param inStr 原始字符串
     * @return 32 位小写十六进制（仅对 ASCII 与 {@link #md5(String)} 一致）
     * @deprecated 编码方式错误，请改用 {@link #md5(String)}
     */
    @Deprecated
    public static String string2MD5(String inStr) {
        if (inStr == null) {
            return "";
        }
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance(ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        return toHex(md5Bytes);
    }

    /**
     * 简单异或“混淆”（与字符 't' 异或），并非 MD5。
     * <p>执行一次为“加密”，再执行一次可还原，仅适合极弱混淆场景，无安全性。</p>
     *
     * @param inStr 输入字符串
     * @return 异或后的字符串
     * @deprecated 不是 MD5，名称误导；如需可逆混淆请自行实现并明确命名
     */
    @Deprecated
    public static String convertMD5(String inStr) {
        if (inStr == null) {
            return null;
        }
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        return new String(a);
    }

    /**
     * 对 {@link #convertMD5(String)} 执行两次，用于“解密”上述异或结果。
     *
     * @param str 经 convertMD5 处理后的字符串
     * @return 还原后的字符串
     * @deprecated 配合 convertMD5 使用，同样不是 MD5
     */
    @Deprecated
    public static String md5Decode(String str) {
        return convertMD5(convertMD5(str));
    }

    /**
     * 字节数组转小写十六进制字符串。
     */
    private static String toHex(byte[] bytes) {
        StringBuilder hexValue = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int val = b & 0xff;
            if (val < 16) {
                hexValue.append('0');
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
