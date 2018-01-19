package com.vision.factorytest.utils;

import java.util.List;

import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;

/**
 * 测试结果分析
 * 
 * @author yangle
 */
public class TestResultAnalyze {

	/**
	 * 通讯质量测试
	 * 
	 * @param RSSITestData
	 *            测试数据
	 * @return 是否通过
	 */
	public static boolean RSSITest(List<Integer> RSSITestData) {
		// 芯片型号
		String chip = GlobalVariable.currentChip;
		int dataCount = 5;

		if (DeviceConstant.Chip._2530.equals(chip) || DeviceConstant.Chip._2630.equals(chip)) {
			dataCount = 8;
		} else if (DeviceConstant.Chip._7681.equals(chip) || DeviceConstant.Chip._7688.equals(chip)) {
			dataCount = 5;
		}

		if (RSSITestData == null || RSSITestData.size() < dataCount) {
			return false;
		}
		int i = 0;
		for (int RSSI : RSSITestData) {
			if (RSSI < DeviceConstant.RSSI_STANDARD) {
				i++;
			}
		}
		if (i > 2) {
			return false;
		}
		return true;
	}

	/**
	 * 2530、2630写工厂区验证
	 * 
	 * @param data
	 *            串口返回数据
	 * @param mac
	 *            MAC地址
	 * @return 验证结果
	 */
	public static String writeFactoryDistrictAuth(byte[] data, String mac) {
		StringBuffer stringBuffer = new StringBuffer();
		byte licenseAuth = data[8];
		byte flashAuth = data[9];
		byte[] macAuth = new byte[8];
		System.arraycopy(data, 10, macAuth, 0, macAuth.length);
		boolean isLicenseAuthOK;
		boolean isFlashAuthOK;
		boolean isMacAuthOK;

		if (ByteUtils.byteArrayToHexString(macAuth).equals(mac)) {
			stringBuffer.append("MAC地址写入成功\r\n");
			isLicenseAuthOK = true;
		} else {
			stringBuffer.append("MAC地址写入失败\r\n");
			isLicenseAuthOK = false;
		}

		if (0x01 == licenseAuth) {
			stringBuffer.append("License认证成功\r\n");
			isFlashAuthOK = true;
		} else {
			stringBuffer.append("License认证失败\r\n");
			isFlashAuthOK = false;
		}

		if (0x01 == flashAuth) {
			stringBuffer.append("Flash认证成功\r\n");
			isMacAuthOK = true;
		} else {
			stringBuffer.append("Flash认证失败\r\n");
			isMacAuthOK = false;
		}

		if (isLicenseAuthOK && isFlashAuthOK && isMacAuthOK) {
			stringBuffer.append("\r\n写模块工厂区成功");
		} else {
			stringBuffer.append("\r\n写模块工厂区失败");
		}

		return stringBuffer.toString();
	}
}
