package com.vision.factorytest.device;

import com.vision.factorytest.utils.ByteUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 设备常量
 * 
 * @author yangle
 */
public class DeviceConstant {

	/**
	 * 通用波特率
	 */
	public static final int BAUDRATE_115200 = 115200;

	/**
	 * 7688芯片波特率
	 */
	public static final int BAUDRATE_57600 = 57600;

	/**
	 * RSSI标准值
	 */
	public static final int RSSI_STANDARD = -80;

	/**
	 * 芯片型号
	 */
	public static class Chip {

		public static final String _1310_gateway = "1310网关";/**网关 same as 2530*/
		
		public static final String _1310_device = "1310设备";/**设备 same as 2630*/
		
		public static final String _2530 = "2530";

		public static final String _2630 = "2630";

		public static final String _7681 = "7681";

		public static final String _7688 = "7688";
	}

	/**
	 * 头数据命令字
	 */
	public static class HeadOpcode {
		/**
		 * ZigBee透传
		 */
		public static final byte ZIGBEE = (byte) 0xAF;
	}

	/**
	 * 2530发送指令
	 */
	public static class Order_2530 {
		/**
		 * 进入设备升级模式
		 */
		public static final byte[] UPGRADE_INIT = ByteUtils.hexStr2Byte("6D64080000D301020304E5");
		/**
		 * 设备升级
		 */
		public static final byte[] UPGRADE = "AT#MCUOK".getBytes();
	}

	/**
	 * 2530回复信息
	 */
	public static class RMessage_2530 {
		/**
		 * 2530启动
		 */
		public static final String BOOT = "0%";
		/**
		 * 设备升级模式
		 */
		public static final String UPGRADE_INIT = "AT#UpdateMCU";
		/**
		 * 设备升级
		 */
		public static final String UPGRADE = "C";
		/**
		 * 设备升级成功
		 */
		public static final String UPGRADE_OK = "OK";
	}

	/**
	 * 2630发送指令
	 */
	public static class Order_2630 {
		/**
		 * 进入测试模式
		 */
		public static final byte[] REC = "rec".getBytes();
		/**
		 * 写入MAC地址
		 */
		public static final byte[] WRITE_MAC = new byte[] { 0x0d };
		/**
		 * 写入License
		 */
		public static final byte[] WRITE_LICENSE = new byte[] { 0x09 };
		/**
		 * 重启设备
		 */
		public static final byte[] REBOOT = new byte[] { 0x0e };
		/**
		 * 读取设备有效信息
		 */
		public static final byte READ_DEVICE_INFO = 0x01;
		/**
		 * 设备升级
		 */
		public static final byte[] UPGRADE = new byte[] { 0x06 };
		/**
		 * 返回APP
		 */
		public static final byte[] GOAPP = new byte[] { 0x02 };
	}

	/**
	 * 2630回复信息
	 */
	public static class RMessage_1310 {
		/**
		 * 设备通电
		 */
		public static final String POWER_ON = ".";
		/**
		 * 进入测试模式成功
		 */
		public static final String REC = "#";
		/**
		 * 写入MAC地址
		 */
		public static final String WRITE_MAC = "Write";
		/**
		 * 写入MAC地址成功
		 */
		public static final String WRITE_MAC_OK = "ok!!!";
		/**
		 * 写入License
		 */
		public static final String WRITE_LICENSE = "C";
		/**
		 * 写入License成功
		 */
		public static final String WRITE_LICENSE_OK = "write license ok";
		/**
		 * 设备重启
		 */
		public static final String REBOOT = "reboot";
		/**
		 * 进入通讯质量测试模式
		 */
		public static final String ZIGBEE_TEST_IS_OK = "ZIGBEE TEST";
		/**
		 * 设备升级
		 */
		public static final String UPGRADE = "recovery";
		/**
		 * 设备升级成功
		 */
		public static final String UPGRADE_OK = "Ok";
	}

	/**
	 * 7681发送指令
	 */
	public static class Order_7681 {
		/**
		 * 进入校准模式
		 */
		public static final byte[] REC = "iwpriv ra0 set\r\n".getBytes();
		/**
		 * 写入License
		 */
		public static final byte[] WRITE_LICENSE = "iwpriv ra0 set license\r\n".getBytes();
		/**
		 * 设备升级
		 */
		public static final byte[] UPGRADE = "AT#UpdateFW\r\n".getBytes();
		/**
		 * 重启设备
		 */
		public static final byte[] REBOOT = "AT#Reboot\r\n".getBytes();
		/**
		 * 启动通讯质量测试
		 */
		public static final byte[] RSSI_TEST_START = "test12".getBytes();

		/**
		 * 恢复出厂设置指令
		 */
		// public static final byte[] RESET_DEVICE=parseHexStr2Byte("6D64050100B202BA");
		public static final byte[] RESET_DEVICE = parseHexStr2Byte("6D64050100B202BA");
	}

	/**
	 * 7681回复信息
	 */
	public static class RMessage_7681 {
		/**
		 * 设备通电
		 */
		public static final String POWER_ON = "Recovery";
		/**
		 * 进入测试模式成功
		 */
		public static final String REC_ENTER = "Enter";
		/**
		 * 进入测试模式成功
		 */
		public static final String REC_INTO = "into";
		/**
		 * 写入License
		 */
		public static final String WRITE_LICENSE = "C";
		/**
		 * 写入License成功
		 */
		public static final String WRITE_LICENSE_OK = "write device";
		/**
		 * 写入License成功
		 */
		public static final String _WRITE_LICENSE_OK = "successful";
		/**
		 * 写入MAC地址及License成功
		 */
		public static final String AUTH_OK = "Auth 0";
		/**
		 * 写入MAC地址及License失败
		 */
		public static final String AUTH_ERROR = "Err(1)";
		/**
		 * WiFi连接成功
		 */
		public static final String WIFI_CONN_OK = "OK!";
		/**
		 * WiFi连接失败
		 */
		public static final String WIFI_CONN_ERR = "ERR!";
		/**
		 * 升级成功
		 */
		public static final String UPGRADE_OK = "Reboot";
		/**
		 * 恢复出厂设置
		 */
		public static final String RESET_DEVICE = "reset";
		public static final String RESET_DEVICE_OK = "Reboot";
	}

	/**
	 * 7688发送指令
	 */
	public static class Order_7688 {
		/**
		 * 进入校准模式
		 */
		public static final byte[] REC = "gurubj\r\nwabjtam123\r\n".getBytes();
		/**
		 * 写入License
		 */
		public static final byte[] WRITE_LICENSE = "licence\r\n".getBytes();
		/**
		 * 重启设备
		 */
		public static final byte[] REBOOT = "reboot\r\n".getBytes();
		/**
		 * 启动通讯质量测试
		 */
		public static final byte[] RSSI_TEST_START = "comm_test\r\n".getBytes();

		/**
		 * 恢复出厂设置指令
		 */
		public static final byte[] RESET_DEVICE = parseHexStr2Byte("6D64050100B202BA");
	}

	/**
	 * 7688回复信息
	 */
	public static class RMessage_7688 {
		/**
		 * 设备通电
		 */
		public static final String POWER_ON = "SYSTEM INIT OK";
		/**
		 * 进入测试模式成功
		 */
		public static final String REC_ENTER = "Enter";
		/**
		 * 进入测试模式成功
		 */
		public static final String REC_INTO = "into";
		/**
		 * 写入License
		 */
		public static final String WRITE_LICENSE = "C";
		/**
		 * 写入License成功
		 */
		public static final String WRITE_LICENSE_OK = "ok!";
		/**
		 * 写入MAC地址及License成功
		 */
		public static final String AUTH_OK = "Auth 0";
		/**
		 * 写入MAC地址及License失败
		 */
		public static final String AUTH_ERROR = "Err(1)";
		/**
		 * WiFi连接成功
		 */
		public static final String WIFI_CONN_OK = "OK!";
		/**
		 * WiFi连接失败
		 */
		public static final String WIFI_CONN_ERR = "ERR!";
		public static final String RESET_DEVICE_OK = "Reboot";
	}

	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(Arrays.toString(parseHexStr2Byte("6D64050100B202BA")));
	}

}
