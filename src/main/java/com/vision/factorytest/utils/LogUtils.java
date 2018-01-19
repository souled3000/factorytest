package com.vision.factorytest.utils;

import org.apache.log4j.Logger;

/**
 * 日志输出
 * 
 * @author yangle
 */
public class LogUtils {

	/**
	 * 输入debug级别的log日志
	 * 
	 * @param TAG
	 *            log标签
	 * @param content
	 *            log内容
	 */
	public static void d(String TAG, String content) {
		Logger.getLogger(TAG).debug(content);
	}

	/**
	 * 输出info级别的log
	 * 
	 * @param TAG
	 *            log标签
	 * @param content
	 *            log内容
	 */
	public static void i(String TAG, String content) {
		Logger.getLogger(TAG).info(content);
	}
}
