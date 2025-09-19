package pers.fz.mvvm.utils.encode;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * created by fz on 2024/11/8 14:29
 * describe:
 */
public class SM3Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] hash(byte[] data) {
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] output = new byte[digest.getDigestSize()];
        digest.doFinal(output, 0);
        return output;
    }

    public static String hashToHex(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

