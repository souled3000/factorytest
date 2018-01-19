package com.vision.factorytest.device;

import com.smarthome.head.SmartHomeConstant;
import com.smarthome.head.SmartHomeData;
import com.smarthome.head.SmartHomeHead;

/**
 * 添加/解析数据头
 * 
 * @author yangle
 */
public class DeviceHeadData {

	/**
	 * 添加头数据
	 * 
	 * @param data
	 *            源数据
	 * @return 加头数据
	 */
	public static byte[] addHead(byte[] data) {
		SmartHomeData smartHoneData = new SmartHomeData();
		smartHoneData.msgID = 0x01;
		// 不加密
		smartHoneData.keyLevel = SmartHomeConstant.Key.NO_SECRET_KEY;
		// 时间戳
		smartHoneData.time = System.currentTimeMillis() / 1000;
		// 数据
		smartHoneData.data = data;

		try {
			return SmartHomeHead.addHead(smartHoneData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[] {};
	}

	/**
	 * 解析头数据
	 * 
	 * @param headData
	 *            加头数据
	 * @return 数据源
	 */
	public static byte[] parseHead(byte[] headData) {
		SmartHomeData smartHomeData = null;
		try {
			smartHomeData = SmartHomeHead.parseData(headData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (smartHomeData == null) {
			return new byte[] {};
		}
		return smartHomeData.data;
	}
}
