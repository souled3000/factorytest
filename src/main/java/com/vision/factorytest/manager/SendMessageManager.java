package com.vision.factorytest.manager;

import gnu.io.SerialPort;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vision.factorytest.Constant;
import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;
import com.vision.factorytest.device.SmartDevice;
import com.vision.factorytest.net.HttpChannel;
import com.vision.factorytest.ui.BaseFrame;
import com.vision.factorytest.ui.RSSITestFrame;
import com.vision.factorytest.utils.ByteUtils;
import com.vision.factorytest.utils.CommUtils;
import com.vision.factorytest.utils.FormCheckUtils;
import com.vision.factorytest.utils.XModem;

/**
 * 发送数据
 * 
 * @author yangle
 */
public class SendMessageManager {

	private static SendMessageManager sendMessageManager;

	/**
	 * 获取SendMessageManager实例
	 * 
	 * @return SendMessageManager实例
	 */
	public static SendMessageManager defaultManage() {
		return sendMessageManager == null ? sendMessageManager = new SendMessageManager() : sendMessageManager;
	}

	private SendMessageManager() {

	}

	/**
	 * 2530、2630、7681、7688发送指令
	 * 
	 * @param serialPort
	 *            串口
	 * @param order
	 *            指令
	 */
	public void sendOrder(SerialPort serialPort, byte[] order) {
		SerialPortManager.sendToPort(serialPort, order);
	}

	/**
	 * 2530、2630发送数据
	 * 
	 * @param serialPort
	 *            串口
	 * @param str
	 *            MAC地址、bin文件路径
	 */
	public void sendData_ZigBee(SerialPort serialPort, String str) {
		if (FormCheckUtils.isMac(str)) {
			SerialPortManager.sendToPort(serialPort, ByteUtils.hexStr2Byte(str));

		} else if (str.contains("license") || str.contains("upgrade")) {
			try {
				SerialPortManager.isRefuseReadData = true;
				XModem xModem = new XModem(serialPort.getInputStream(), serialPort.getOutputStream(),
						new PrintWriter(System.err));
				xModem.send(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 2630读取设备信息
	 * 
	 * @param serialPort
	 *            串口
	 */
	public void getDeviceInfo_ZigBee(SerialPort serialPort) {
		SmartDevice smartDevice = new SmartDevice();
		smartDevice.opcode = DeviceConstant.Order_2630.READ_DEVICE_INFO;
		byte[] data = smartDevice.buildData();
		SerialPortManager.sendToPort(serialPort, data);
	}

	/**
	 * 2530、2630通讯质量测试
	 *
	 * @param serialPort
	 *            串口
	 * @param chip
	 */
	public void RSSITest_ZigBee(final SerialPort serialPort, final String chip) {
		final byte[] data = ByteUtils.hexStr2Byte("6D64040000AFB3");

		new Thread() {
			public void run() {
				for (int i = 0; i < 10; i++) {
					SerialPortManager.sendToPort(serialPort, data);
					GlobalVariable.RSSITestNumber = i + 1;

					// 显示数据包发送数量
					RSSITestFrame frame = (RSSITestFrame) BaseFrame.getFrame();
					frame.setDataView("已发送第" + (i + 1) + "包数据");
					try {
						sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// 10包数据发送完成，分析测试结果并显示
					// 因为不确定是否可以收到最后一包，所以以发送包数做为标准
					if (i == 9) {
						frame.RSSITestResult(chip);
					}
				}
			};
		}.start();
	}

	/**
	 * 7681、7688发送数据
	 * 
	 * @param serialPort
	 *            串口
	 * @param str
	 *            MAC地址、bin文件路径
	 */
	public void sendData_768(final SerialPort serialPort, String chip, String str) {
		if (FormCheckUtils.isMac(str)) {
			if (DeviceConstant.Chip._7681.equals(chip)) {
				final List<byte[]> macList_7681 = CommUtils.getMAC_7681(str);

				new Thread() {
					public void run() {
						try {
							for (int i = 0; i < macList_7681.size(); i++) {
								SerialPortManager.sendToPort(serialPort, macList_7681.get(i));
								sleep(500);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
				}.start();

			} else if (DeviceConstant.Chip._7688.equals(chip)) {
				final List<byte[]> macList_7688 = CommUtils.getMAC_7688(str);

				new Thread() {
					public void run() {
						try {
							for (int i = 0; i < macList_7688.size(); i++) {
								SerialPortManager.sendToPort(serialPort, macList_7688.get(i));
								sleep(500);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
				}.start();
			}

		} else if (str.contains("license") || str.contains("upgrade")) {
			try {
				SerialPortManager.isRefuseReadData = true;
				XModem xModem = new XModem(serialPort.getInputStream(), serialPort.getOutputStream(),
						new PrintWriter(System.err));
				xModem.send(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 上传测试结果
	 * 
	 * @param mac
	 *            MAC地址
	 * @param isSuccess
	 *            测试是否成功 type 1:工厂 2通讯 3生产
	 */
	public void uploadTestResult(String mac, boolean isSuccess, int type, String chip) {
		BaseFrame frame = BaseFrame.getFrame();
		frame.macInputField.setText("");
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("mac", mac);
		// map.put("chip", GlobalVariable.currentChip);
		// map.put("currentItem", GlobalVariable.currentTestItem);
		// map.put("isSuccess", isSuccess == true ? "1" : "0");
		String ts = "";
		String sc = isSuccess == true ? "成功" : "失败";
		switch (type) {
		case 1:
			ts = "工厂";
			break;
		case 2:
			ts = "通讯";
			break;
		case 3:
			ts = "升级";
			break;
		case 4:
			ts = "恢复";
			break;
		}

		String dir1 = "MacResult/";

		String filename = chip + ".txt";
		File file = new File(dir1);
		if (!file.exists()) {
			file.mkdirs();
		}
		File file2 = new File(dir1, filename);
		if (!file2.exists()) {
			try {
				file2.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir1 + filename, true)));
			out.write(mac + "," + ts + "," + sc + "," + chip + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// HttpChannel.sendMessageGet(Constant.UrlOrigin.upload_test_result, map);
	}

	/**
	 * 获取单个项目测试结果
	 * 
	 * @param mac
	 *            MAC地址
	 * @param testItem
	 *            测试项目
	 */
	public void getSingleTestResult(String mac, String testItem) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("mac", mac);
		map.put("chip", GlobalVariable.currentChip);
		map.put("currentItem", testItem);

		HttpChannel.sendMessageGet(Constant.UrlOrigin.get_test_result, map);
	}

	/**
	 * 获取所有项目测试结果
	 * 
	 * @param mac
	 *            MAC地址
	 */
	public void getTestResult(String mac) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("mac", mac);
		map.put("chip", GlobalVariable.currentChip);

		HttpChannel.sendMessageGet(Constant.UrlOrigin.get_test_result, map);
	}

	/**
	 * 获取所有测试结果
	 */
	public void getTestResult() {
		HttpChannel.sendMessageGet(Constant.UrlOrigin.get_test_result, null);
	}
}
