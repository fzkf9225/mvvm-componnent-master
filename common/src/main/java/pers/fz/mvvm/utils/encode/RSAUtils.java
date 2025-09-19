package pers.fz.mvvm.utils.encode;

import android.content.Context;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Created by fz on 2017/7/28.
 * RSA加解密
 */

public class RSAUtils {

    //加密
    public static String enCode(Context context,String data){
        String source = data;
        try {
            // 从字符串中得到公钥
//			PublicKey publicKey = RSAUtilsHelper.loadPublicKey(RSAUtilsHelper.PUCLIC_KEY);
            // 从文件中得到公钥
            InputStream inPublic = context.getResources().getAssets().open("rsa_public_key.pem");
            PublicKey publicKey = RSAUtilsHelper.loadPublicKey(inPublic);
            // 加密
            byte[] encryptByte = RSAUtilsHelper.encryptData(source.getBytes("UTF-8"), publicKey);
            // 为了方便观察吧加密后的数据用base64加密转一下，要不然看起来是乱码,所以解密是也是要用Base64先转换
//            BASE64Encoder encoder = new BASE64Encoder();
//            String ss = Base64Util.encode(encryptByte);
            return Base64Util.encode(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
            return "加密错误";
        }
    }
    //解密
    public static String deCode(Context context, String data) {
        try {
            // 从字符串中得到私钥
            // PrivateKey privateKey = RSAUtilsHelper
            // .loadPrivateKey(RSAUtilsHelper.PRIVATE_KEY);
            // 从文件中得到私钥
            InputStream inPrivate = context.getResources().getAssets().open("rsa_private_key.pem");
            PrivateKey privateKey = RSAUtilsHelper.loadPrivateKey(inPrivate);
            // 因为RSA加密后的内容经Base64再加密转换了一下，所以先Base64解密回来再给RSA解密
            byte[] b1 = Base64.getDecoder().decode(data);
            byte[] decryptByte = RSAUtilsHelper.decryptData(b1, privateKey);
            String decryptStr = new String(decryptByte);
            return decryptStr;
        } catch (Exception e) {
            e.printStackTrace();
            return "解密错误";
        }
    }
}
