package com.vision.factorytest.utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;

public class CommUtils {
	public static byte[] read6D64(InputStream is) throws Exception {
		byte[] w = null;
		while (true) {
			int l = is.read();
			if (l != 0x6D) {
				continue;
			} else {
				l = is.read();
				if (l != 0x64) {
					continue;
				} else {
					l = is.read();
					byte[] r = new byte[l];
					is.read(r);
					w = new byte[l + 3];
					w[0] = 0x6d;
					w[1] = 0x64;
					w[2] = (byte) l;
					System.arraycopy(r, 0, w, 3, l);
					break;
				}
			}
		}
		return w;
	}

	/**
	 * 获取License的地址路径
	 * 
	 * @param mac
	 *            MAC地址
	 * @return License的地址路径
	 */
	public static String buildLicensePath(String mac) {
		// 芯片型号
		String chip = GlobalVariable.currentChip;
		if (DeviceConstant.Chip._1310_device.equals(chip)) {
			return "./license/1310/" + mac + "-AES.bin";

		} else if (DeviceConstant.Chip._2630.equals(chip)) {
			return "./license/2630/" + mac + "-AES.bin";

		} else if (DeviceConstant.Chip._7681.equals(chip) || DeviceConstant.Chip._7688.equals(chip)) {
			return "./license/768x/" + mac + "-AES.bin";
		}

		return "";
	}

	/**
	 * 获取升级文件的地址路径
	 * 
	 * @param mac
	 *            MAC地址
	 * @return 升级文件的地址路径
	 */
	public static String buildUpgradePath() {
		String chip = GlobalVariable.currentChip;
		if (DeviceConstant.Chip._1310_device.equals(chip)) {
			return "./upgrade/update_1310.bin";

		} else if (DeviceConstant.Chip._2530.equals(chip)) {
			return "./upgrade/update_2530.bin";

		} else if (DeviceConstant.Chip._2630.equals(chip)) {
			return "./upgrade/update_2630.bin";

		} else if (DeviceConstant.Chip._7681.equals(chip)) {
			return "./upgrade/update_7681.bin";
		}

		return "";
	}

	/**
	 * 格式化7681芯片MAC地址
	 * 
	 * @param mac
	 *            MAC地址
	 * @return 格式化后的MAC地址
	 */
	public static List<byte[]> getMAC_7681(String mac) {
		List<byte[]> list = new ArrayList<byte[]>();
		char[] macArray = mac.substring(4).toCharArray();

		for (int i = 0; i < 6; i++) {
			String order = "iwpriv ra0 set flash 0x1700";
			order += (i + 4) + "=0x" + macArray[i * 2] + macArray[i * 2 + 1] + "\r\n";
			list.add(order.getBytes());
		}

		return list;
	}

	/**
	 * 格式化7688芯片MAC地址
	 * 
	 * @param mac
	 *            MAC地址
	 * @return 格式化后的MAC地址
	 */
	public static List<byte[]> getMAC_7688(String mac) {
		List<byte[]> list = new ArrayList<byte[]>();
		char[] macArray = mac.substring(4).toCharArray();

		for (int i = 0; i < 3; i++) {
			String order = "iwpriv ra0 e2p ";
			order += (i * 2 + 4) + "=" + macArray[i * 4 + 2] + macArray[i * 4 + 3] + macArray[i * 4] + macArray[i * 4 + 1] + "\r\n";
			list.add(order.getBytes());
		}

		return list;
	}

	/**
	 * 解析二维码中的MAC地址
	 * 
	 * @param str
	 *            二维码内容
	 * @return MAC地址
	 */
	public static String getMacFromCode(String str) {
		byte[] bytes = Base64Utils.decode(str);
		byte[] mac = new byte[8];
		System.arraycopy(bytes, 0, mac, 0, mac.length);
		return ByteUtils.byteArrayToHexString(mac);
	}

	/**
	 * 文件是否存在
	 * 
	 * @param mac
	 *            MAC地址
	 * @return 文件是否存在
	 */
	public static boolean isFileExists(String mac) {
		// 芯片型号
		String chip = GlobalVariable.currentChip;
		File file = null;
		if (DeviceConstant.Chip._1310_device.equals(chip)) {
			file = new File("./license/1310/", mac + "-AES.bin");

		} else if (DeviceConstant.Chip._2630.equals(chip)) {
			file = new File("./license/2630/", mac + "-AES.bin");

		} else if (DeviceConstant.Chip._7681.equals(chip) || DeviceConstant.Chip._7688.equals(chip)) {
			file = new File("./license/768x/", mac + "-AES.bin");
		}

		if (file != null) {
			return file.exists();

		}
		return false;
	}

}
