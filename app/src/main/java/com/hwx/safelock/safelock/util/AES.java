package com.hwx.safelock.safelock.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-128-CBC加解密模式
 * @author libai
 */
public class AES {
	
	/**
	 * 加密
	 * @param encData 要加密的数据
	 * @param secretKey 密钥 ,16位的数字和字母
	 * @param vector 初始化向量,16位的数字和字母
	 * @return
	 * @throws Exception
	 */
	public static String Encrypt(String encData ,String secretKey,String vector) throws Exception {
		
		if(secretKey == null) {
			return null;
		}
		if(secretKey.length() != 16) {
			return null;
		}
		byte[] raw = secretKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec iv = new IvParameterSpec(vector.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(encData.getBytes());
//		System.out.println(new BASE64Encoder().encode(encrypted));
		return encodeBytes( encrypted );
	}

	/**
	 * 解密
	 * @param decData
	 * @param secretKey 密钥 ,16位的数字和字母
	 * @param vector 初始化向量,16位的数字和字母
	 * @return
	 * @throws Exception
	 */
	public static String Decrypt(String decData ,String secretKey,String vector) throws Exception{
		
		if(secretKey == null) {
			return null;
		}
		if(secretKey.length() != 16) {
			return null;
		}
		byte[] raw = secretKey.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(vector.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] encrypted1 = decodeBytes(decData);
		byte[] original = cipher.doFinal(encrypted1);
		String originalString = new String(original);
		return originalString;
	}

	/**
	 * 转16进制
	 * @param bytes
	 * @return
	 */
	public static String encodeBytes(byte[] bytes) {
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
			strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
		}
		return strBuf.toString();
	}

	/**
	 * 转字节数组
	 * @param str
	 * @return
	 */
	public static byte[] decodeBytes(String str) {
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i += 2) {
			char c = str.charAt(i);
			bytes[i / 2] = (byte) ((c - 'a') << 4);
			c = str.charAt(i + 1);
			bytes[i / 2] += (c - 'a');
		}
		return bytes;
	}

	public static String jiami(String data){
		try {
			return AES.Encrypt(data, "abcdefguhyjhddda", "1234567098716351");
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e("加密失败");
			return "";
		}
	}
	public static String jiemi(String data){
		try {
			return AES.Decrypt(data, "abcdefguhyjhddda", "1234567098716351");
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e("解密失败");
			return "";
		}
	}

	public static void main(String[] args) throws Exception {
		String data = "{\"service_code\":\"FS0014\",\"phone_id\":\"18022887432\",\"start_time\":\"20141015000000\",\"end_time\":\"2014101610000000\"}";
		String res = AES.Encrypt(data, "abcdefguhyjhddda", "1234567098716351");
		System.out.println("加密后"+res);
		String res1 = AES.Decrypt(res, "abcdefguhyjhddda", "1234567098716351");
		System.out.println("解密后"+res1);
		System.out.println("1".getBytes().toString());
		System.out.println(encodeBytes("11".getBytes()));
		System.out.println((int) 'a');
	}
	
}
