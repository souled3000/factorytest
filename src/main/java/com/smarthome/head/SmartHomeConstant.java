package com.smarthome.head;

/**
 * Created by zhaoqing on 2016/8/1.
 */
public class SmartHomeConstant {

	public static class Key {
		// 无密钥
		public static final byte NO_SECRET_KEY = 0;
		// 随机密钥（登录之前使用）
		public static final byte RANDOM_SECRET_KEY = 1;
		// 密证
		public static final byte KEY_CERTIFICATE = 2;
		// 密证+随机密钥（登录之后使用）
		public static final byte LOGIN_SECRET_KEY = 3;
	}

	public static class Encrypt {
		// 加密类型，0-RC4 加密，1-DES 加密，2-AES 加密
		public static final byte ENCRYPT_TYPE_RC4 = 0;
		public static final byte ENCRYPT_TYPE_DES = 1;
		public static final byte ENCRYPT_TYPE_AES = 2;
	}

	// 二进制数据格式
	public static final byte BINARY = 0;
	// JSON 格式数据
	public static final byte Json = 1;
	// XML 格式数据
	public static final byte XML = 2;
}
