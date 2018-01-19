package com.vision.factorytest.utils;

/**
 * 格式校验
 * 
 * @author yangle
 */
public class FormCheckUtils {

	/**
	 * 是否为MAC地址
	 * 
	 * @param address
	 *            地址
	 * @return 是否为MAC地址
	 */
	public static boolean isMac(String address) {
		if (address != null && address.length() == 16) {
			return true;
		}
		return false;
	}
}
