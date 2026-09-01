package io.coderf.arklab.common.utils.encode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * RSA 非对称加解密辅助工具。
 * <p>
 * 提供密钥对生成、公钥/私钥加载、加解密等能力。Android 端加解密统一使用
 * {@code RSA/ECB/PKCS1Padding}，以便与常见后端实现兼容。
 * </p>
 * <p>
 * 注意：
 * <ul>
 *   <li>单次加密数据长度受密钥长度限制（约为 keySize/8 - 11 字节），超长需分段或改用混合加密。</li>
 *   <li>默认密钥长度 1024 已偏弱，新业务建议使用 2048 及以上。</li>
 * </ul>
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/27 13:44
 */
public final class RSAUtilsHelper {

	private static final String RSA = "RSA";
	/** Android 与多数后端互通时使用的 transformation */
	private static final String RSA_ANDROID = "RSA/ECB/PKCS1Padding";
	/** 部分纯 Java 环境可能使用的 transformation（当前未默认使用） */
	@SuppressWarnings("unused")
	private static final String RSA_JAVA = "RSA/None/PKCS1Padding";

	/** 推荐的新密钥长度 */
	public static final int RECOMMENDED_KEY_LENGTH = 2048;
	/** 历史默认密钥长度（兼容旧逻辑） */
	public static final int DEFAULT_KEY_LENGTH = 1024;

	private RSAUtilsHelper() {
		// no instance
	}

	/**
	 * 随机生成 RSA 密钥对（密钥长度 1024）。
	 * <p>1024 位安全性不足，新业务请使用 {@link #generateRSAKeyPair(int)} 并传入 2048 或以上。</p>
	 *
	 * @return 密钥对，失败时返回 null
	 * @deprecated 默认长度过短，请改用 {@link #generateRSAKeyPair(int)} 并指定 {@link #RECOMMENDED_KEY_LENGTH}
	 */
	@Deprecated
	public static KeyPair generateRSAKeyPair() {
		return generateRSAKeyPair(DEFAULT_KEY_LENGTH);
	}

	/**
	 * 随机生成 RSA 密钥对。
	 *
	 * @param keyLength 密钥长度（bit），常见范围 512～4096，推荐 2048
	 * @return 密钥对，算法不可用时返回 null
	 */
	public static KeyPair generateRSAKeyPair(int keyLength) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
			kpg.initialize(keyLength);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用公钥加密。
	 * <p>每次加密的字节数不能超过密钥长度字节数减 11（PKCS1Padding）。</p>
	 *
	 * @param data      待加密数据
	 * @param publicKey 公钥
	 * @return 密文字节，失败返回 null
	 */
	public static byte[] encryptData(byte[] data, PublicKey publicKey) {
		if (data == null || publicKey == null) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(RSA_ANDROID);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用私钥解密。
	 *
	 * @param encryptedData 密文字节（由 {@link #encryptData} 产生）
	 * @param privateKey    私钥
	 * @return 明文，失败返回 null
	 */
	public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey) {
		if (encryptedData == null || privateKey == null) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(RSA_ANDROID);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 通过公钥编码字节（如 {@code publicKey.getEncoded()}）还原公钥（X.509）。
	 *
	 * @param keyBytes 公钥编码
	 * @return 公钥
	 */
	public static PublicKey getPublicKey(byte[] keyBytes)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 通过私钥编码字节还原私钥（PKCS#8）。
	 *
	 * @param keyBytes 私钥编码
	 * @return 私钥
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePrivate(keySpec);
	}

	/**
	 * 使用模数 N 与公钥指数 e 还原公钥。
	 *
	 * @param modulus        模数（十进制字符串）
	 * @param publicExponent 公钥指数（十进制字符串）
	 * @return 公钥
	 */
	public static PublicKey getPublicKey(String modulus, String publicExponent)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger bigIntModulus = new BigInteger(modulus);
		BigInteger bigIntPublicExponent = new BigInteger(publicExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPublicExponent);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 使用模数 N 与私钥指数 d 还原私钥。
	 * <p>历史实现误用了 {@link RSAPublicKeySpec}，已修正为 {@link RSAPrivateKeySpec}。</p>
	 *
	 * @param modulus         模数（十进制字符串）
	 * @param privateExponent 私钥指数（十进制字符串）
	 * @return 私钥
	 */
	public static PrivateKey getPrivateKey(String modulus, String privateExponent)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger bigIntModulus = new BigInteger(modulus);
		BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
		// 修复：原先错误使用 RSAPublicKeySpec，无法正确生成私钥
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(bigIntModulus, bigIntPrivateExponent);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePrivate(keySpec);
	}

	/**
	 * 从 Base64 字符串加载公钥（X.509 编码，可含 PEM 头时需先去掉）。
	 *
	 * @param publicKeyStr Base64 公钥数据
	 * @return RSA 公钥
	 * @throws Exception 算法不存在 / 密钥非法 / 数据为空
	 */
	public static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
		try {
			byte[] buffer = Base64.getDecoder().decode(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance(RSA);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法", e);
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法", e);
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空", e);
		} catch (IllegalArgumentException e) {
			throw new Exception("公钥 Base64 解码失败", e);
		}
	}

	/**
	 * 从 Base64 字符串加载私钥（PKCS#8 编码）。
	 *
	 * @param privateKeyStr Base64 私钥数据
	 * @return RSA 私钥
	 * @throws Exception 算法不存在 / 密钥非法 / 数据为空
	 */
	public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
		try {
			byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance(RSA);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法", e);
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法", e);
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空", e);
		} catch (IllegalArgumentException e) {
			throw new Exception("私钥 Base64 解码失败", e);
		}
	}

	/**
	 * 从输入流加载公钥（支持 PEM：自动跳过以 {@code -} 开头的头尾行）。
	 *
	 * @param in 公钥输入流
	 * @return 公钥
	 * @throws Exception 读取或解析失败
	 */
	public static PublicKey loadPublicKey(InputStream in) throws Exception {
		try {
			return loadPublicKey(readKey(in));
		} catch (IOException e) {
			throw new Exception("公钥数据流读取错误", e);
		} catch (NullPointerException e) {
			throw new Exception("公钥输入流为空", e);
		}
	}

	/**
	 * 从输入流加载私钥（支持 PEM：自动跳过以 {@code -} 开头的头尾行）。
	 *
	 * @param in 私钥输入流
	 * @return 私钥
	 * @throws Exception 读取或解析失败
	 */
	public static PrivateKey loadPrivateKey(InputStream in) throws Exception {
		try {
			return loadPrivateKey(readKey(in));
		} catch (IOException e) {
			throw new Exception("私钥数据读取错误", e);
		} catch (NullPointerException e) {
			throw new Exception("私钥输入流为空", e);
		}
	}

	/**
	 * 读取密钥文件内容，跳过 PEM 头尾（以 {@code -} 开头的行）。
	 */
	private static String readKey(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		String readLine;
		while ((readLine = br.readLine()) != null) {
			if (readLine.isEmpty() || readLine.charAt(0) == '-') {
				continue;
			}
			sb.append(readLine);
		}
		return sb.toString();
	}

	/**
	 * 打印公钥模数与指数信息（调试用）。
	 */
	public static void printPublicKeyInfo(PublicKey publicKey) {
		if (!(publicKey instanceof RSAPublicKey)) {
			System.out.println("Not an RSAPublicKey");
			return;
		}
		RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
		System.out.println("----------RSAPublicKey----------");
		System.out.println("Modulus.length=" + rsaPublicKey.getModulus().bitLength());
		System.out.println("Modulus=" + rsaPublicKey.getModulus().toString());
		System.out.println("PublicExponent.length=" + rsaPublicKey.getPublicExponent().bitLength());
		System.out.println("PublicExponent=" + rsaPublicKey.getPublicExponent().toString());
	}

	/**
	 * 打印私钥模数与指数信息（调试用）。
	 */
	public static void printPrivateKeyInfo(PrivateKey privateKey) {
		if (!(privateKey instanceof RSAPrivateKey)) {
			System.out.println("Not an RSAPrivateKey");
			return;
		}
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
		System.out.println("----------RSAPrivateKey ----------");
		System.out.println("Modulus.length=" + rsaPrivateKey.getModulus().bitLength());
		System.out.println("Modulus=" + rsaPrivateKey.getModulus().toString());
		System.out.println("PrivateExponent.length=" + rsaPrivateKey.getPrivateExponent().bitLength());
		System.out.println("PrivateExponent=" + rsaPrivateKey.getPrivateExponent().toString());
	}
}
