package com.vision.factorytest.manager;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.factorytest.Constant;
import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.bean.TestResult;
import com.vision.factorytest.device.DeviceConstant;
import com.vision.factorytest.ui.BaseFrame;
import com.vision.factorytest.ui.DeviceUpgradeFrame;
import com.vision.factorytest.utils.ByteUtils;
import com.vision.factorytest.utils.CommUtils;
import com.vision.factorytest.utils.ExcelUtils;
import com.vision.factorytest.utils.FormCheckUtils;
import com.vision.factorytest.utils.JsonUtils;
import com.vision.factorytest.utils.ShowUtils;
import com.vision.factorytest.utils.TestResultAnalyze;
import com.vision.factorytest.utils.TimeUtils;
import com.vision.factorytest.utils.TimeUtils.IOvertimeExecute;

import gnu.io.SerialPort;

/**
 * 接收数据
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class ReceiveMessageManager {
	private static final Logger log = LoggerFactory.getLogger(ReceiveMessageManager.class);
	private static ReceiveMessageManager receiveMessageManager;
	private SendMessageManager sendMessageManager;

	// 是否可以写入License
	private boolean isLicenseReady = true;
	// License是否写入成功
	private boolean isWriteLicenseOk = false;
	// 是否已进入测试模式
	private boolean isRec = false;
	// 是否已经重启
	private boolean isReboot = false;
	// WiFi是否连接
	private boolean isWiFiConn = false;
	// 是否正在计时
	private boolean isTimerWork = false;
	// 是否可以升级
	private boolean isUpgradeReady = true;
	// 是否恢复出厂
	private boolean isResetDevice = false;

	// 用于存储2630设备信息
	private StringBuilder builder = new StringBuilder();

	/**
	 * 获取ReceiveMessageManage实例
	 * 
	 * @return ReceiveMessageManage实例
	 */
	public static ReceiveMessageManager defaultManage() {
		return receiveMessageManager == null ? receiveMessageManager = new ReceiveMessageManager() : receiveMessageManager;
	}

	private ReceiveMessageManager() {
		sendMessageManager = SendMessageManager.defaultManage();
	}

	/**
	 * 解析串口返回数据
	 * 
	 * @param serialPortData
	 *            串口数据
	 */
	public void SerialPortDataResponse(byte[] serialPortData) {
		log.info("串口返回数据{}", ByteUtils.byteArrayToHexString(serialPortData));

		// 当前测试项目
		String testItem = GlobalVariable.currentTestItem;
		// 窗口对象
		BaseFrame frame = BaseFrame.getFrame();
		// 芯片型号
		String chip = GlobalVariable.currentChip;
		// 设备MAC
		String mac = frame.macInputField.getText();
		// 串口
		final SerialPort serialPort = frame.serialPort;
		// 设备回复信息
		String serialPortStr = "";
		try {
			serialPortStr = new String(serialPortData, "utf-8").replaceAll("\r\n", "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!FormCheckUtils.isMac(mac)) {
			return;
		}

		if (Constant.TestItem.WRITE_FACTORY_DISTRICT.equals(testItem)) {
			// 写模块工厂区
			writeFactoryDistrict(frame, chip, mac, serialPort, serialPortData, serialPortStr);

		} else if (Constant.TestItem.RSSI_TEST.equals(testItem)) {
			// 通讯质量测试
			RSSITest(frame, chip, mac, serialPort, serialPortData, serialPortStr);

		} else if (Constant.TestItem.DEVICE_UPGRADE.equals(testItem)) {
			// 模块程序升级
			deviceUpgrade(frame, chip, mac, serialPort, serialPortData, serialPortStr);
		} else if (Constant.TestItem.DEVICE_RESET.equals(testItem)) {
			// 模块程序升级
			deviceReset(frame, chip, mac, serialPort, serialPortData, serialPortStr);
		}
		// 设备MAC

	}

	private void deviceReset(BaseFrame frame, String chip, String mac, SerialPort serialPort, byte[] serialPortData, String str) {
		log.info("模块恢复出厂{}", str);
		if (DeviceConstant.Chip._7681.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7681.POWER_ON)) {
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.REC);

			} else if (str.contains(DeviceConstant.RMessage_7681.REC_ENTER) || str.contains(DeviceConstant.RMessage_7681.REC_INTO) || str.contains(DeviceConstant.RMessage_7681.AUTH_OK)) {
				frame.clearDataView();
				frame.setDataView("进入模块恢复出厂模式成功");
				frame.setDataView("发送恢复出厂设置指令");
				frame.setDataView("等待恢复出厂");
				isRec = true;
				isResetDevice = true;
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.RESET_DEVICE);
			} else if (str.contains(DeviceConstant.RMessage_7681.RESET_DEVICE_OK) && isResetDevice) {
				isResetDevice = false;
				frame.setDataView("\r\n恢复出厂成功");
				// SerialPortManager.isRefuseReadData = true;
				sendMessageManager.uploadTestResult(mac, true, 4, chip);
			}
		} else if (DeviceConstant.Chip._7688.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7688.POWER_ON)) {
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.REC);

			} else if ((str.contains(DeviceConstant.RMessage_7688.REC_ENTER) || str.contains(DeviceConstant.RMessage_7688.REC_INTO)) && !isRec) {
				frame.clearDataView();
				frame.setDataView("进入模块恢复出厂模式成功");
				frame.setDataView("发送恢复出厂设置指令");
				frame.setDataView("等待恢复出厂");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.RESET_DEVICE);
				isRec = true;
				isResetDevice = true;
			} else if (str.contains(DeviceConstant.RMessage_7688.RESET_DEVICE_OK) && isResetDevice) {
				isResetDevice = false;
				isRec = false;
				frame.setDataView("\r\n恢复出厂成功");
				sendMessageManager.uploadTestResult(mac, true, 4, chip);
			}
		}
	}

	/**
	 * 解析Http返回数据
	 * 
	 * @param content
	 *            http返回内容
	 */
	public void HttpResponse(String content, String urlOrigin) {
		log.info("http接收：{}", "urlOrigin:" + urlOrigin + "___content:" + content);

		String testItem = GlobalVariable.currentTestItem;
		BaseFrame frame = BaseFrame.getFrame();

		if (Constant.UrlOrigin.upload_test_result.equals(urlOrigin)) { // 上传测试结果
			if (content.contains("\"y\"")) {
				frame.setDataView("结果上传成功");

				// 清空MAC地址输入栏
				frame.macInputField.setText("");
			}

		} else if (Constant.UrlOrigin.get_test_result.equals(urlOrigin)) { // 获取测试结果
			if (content.length() == 2) {
				return;
			}

			TestResult testResult = JsonUtils.fromJsonString("{\"result\":" + content + "}", TestResult.class);
			// 芯片型号
			String chip = GlobalVariable.currentChip;
			boolean isSuccess = "1".equals(testResult.getResult().get(0).getIsSuccess()) ? true : false;

			log.info("获取测试结果{}", "chip:" + chip + "___isSuccess:" + isSuccess);

			if (Constant.TestItem.RSSI_TEST.equals(testItem)) {
				// 通讯质量测试
				if (DeviceConstant.Chip._2530.equals(chip)) {

				} else if (DeviceConstant.Chip._2630.equals(chip)) {

				} else if (DeviceConstant.Chip._7681.equals(chip)) {

				} else if (DeviceConstant.Chip._7688.equals(chip)) {

				}

			} else if (Constant.TestItem.DEVICE_UPGRADE.equals(testItem)) {
				// 模块程序升级

			} else {
				// 结果导出
				if (!testResult.getResult().isEmpty()) {
					ExcelUtils.createExcel(testResult.getResult());
					ShowUtils.message("测试结果导出成功");
				}
			}
		}
	}

	/**
	 * 写模块工厂区
	 * 
	 * @param frame
	 *            当前窗口
	 * @param chip
	 *            芯片型号
	 * @param mac
	 *            MAC地址
	 * @param serialPort
	 *            串口
	 * @param data
	 *            数据(字节)
	 * @param str
	 *            数据(字符串)
	 */
	private void writeFactoryDistrict(BaseFrame frame, String chip, String mac, final SerialPort serialPort, byte[] data, String str) {
		log.info("{}写模块工厂区开始,RECV:{}", chip, str);

		if (DeviceConstant.Chip._2630.equals(chip) || DeviceConstant.Chip._1310_device.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_1310.POWER_ON)) {
				log.info("from 1310：{}", ".");
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.REC);
				log.info("to 1310：{}", "rec");

			} else if (DeviceConstant.RMessage_1310.REC.equals(str)) {
				log.info("from 1310：{}", "#");
				frame.setDataView("进入写模块工厂区模式成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.WRITE_MAC);
				log.info("to 1310：{}", "0x0d");

			} else if (str.contains(DeviceConstant.RMessage_1310.WRITE_MAC)) {
				log.info("from 1310：{}", "Write");
				sendMessageManager.sendData_ZigBee(serialPort, mac);
				log.info("to 1310：{}", mac);

			} else if (str.contains(DeviceConstant.RMessage_1310.WRITE_MAC_OK)) {
				log.info("from 1310：{}", "write mac ok!!!");
				frame.setDataView("写入MAC地址成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.WRITE_LICENSE);
				log.info("to 1310：{}", "0x09");

			} else if (DeviceConstant.RMessage_1310.WRITE_LICENSE.equals(str)) {
				log.info("from 1310：{}", "C");
				isLicenseReady = false;
				sendMessageManager.sendData_ZigBee(serialPort, CommUtils.buildLicensePath(mac));
				log.info("to 1310：{}", "xmodel");

			} else if (str.contains(DeviceConstant.RMessage_1310.WRITE_LICENSE_OK)) {
				log.info("from 1310：{}", "write licence");
				frame.setDataView("写入License成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.GOAPP);
				log.info("to 1310：{}", "0x02");
			} else if (str.contains(DeviceConstant.RMessage_1310.ZIGBEE_TEST_IS_OK)) {
				sendMessageManager.getDeviceInfo_ZigBee(serialPort);
				log.info("to 1310：{}", "GET　DEVICE INFORMATION");
			} else if (ByteUtils.byteArrayToHexString(data).contains("6D64")) {
				// 获取2630设备信息，有时数据分开返回，特殊处理

				String result = TestResultAnalyze.writeFactoryDistrictAuth(data, mac);
				frame.setDataView(result);
				builder.delete(0, builder.length());
				// 上传测试结果
				if (!result.contains("失败")) {
					sendMessageManager.uploadTestResult(mac, true, 1, chip);
				} else {
					sendMessageManager.uploadTestResult(mac, false, 1, chip);
				}
			}

		} else if (DeviceConstant.Chip._7681.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7681.POWER_ON) && !isReboot) {
				log.info("7681{}", "进入写模块工厂区模式");
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.REC);

			} else if ((str.contains(DeviceConstant.RMessage_7681.REC_ENTER) || str.contains(DeviceConstant.RMessage_7681.REC_INTO)) && !isRec) {
				frame.setDataView("进入写模块工厂区模式成功");
				sendMessageManager.sendData_768(serialPort, DeviceConstant.Chip._7681, mac);
				isRec = true;

			} else if (str.contains("[0x17009]=[0x" + mac.substring(14).toLowerCase()) && isRec && !isReboot) {
				frame.setDataView("写入MAC地址成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.WRITE_LICENSE);

			} else if (str.contains(DeviceConstant.RMessage_7681.WRITE_LICENSE) && isLicenseReady && isRec) {
				log.info("7681{}", "开始写入License文件");
				sendMessageManager.sendData_768(serialPort, DeviceConstant.Chip._7681, CommUtils.buildLicensePath(mac));
				isLicenseReady = false;

			} else if ((str.contains(DeviceConstant.RMessage_7681.WRITE_LICENSE_OK) || str.contains(DeviceConstant.RMessage_7681._WRITE_LICENSE_OK)) && !isWriteLicenseOk && isRec) {
				isWriteLicenseOk = true;
				frame.setDataView("写入License成功");
				frame.setDataView("正在重启设备，请稍等...");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.REBOOT);
				isReboot = true;

			} else if (str.contains(DeviceConstant.RMessage_7681.AUTH_OK) && isRec) {
				frame.setDataView("设备重启完成");
				frame.setDataView("MAC地址 & License写入成功");
				frame.setDataView("\r\n写模块工厂区成功");
				isRec = false;
				isLicenseReady = true;
				isWriteLicenseOk = false;
				isReboot = false;

				// 上传测试结果
				sendMessageManager.uploadTestResult(mac, true, 1, chip);

			} else if (str.contains(DeviceConstant.RMessage_7681.AUTH_ERROR) && isRec) {
				frame.setDataView("设备重启完成");
				frame.setDataView("MAC地址 & License写入失败");
				frame.setDataView("\r\n写模块工厂区失败");
				isRec = false;
				isLicenseReady = true;
				isWriteLicenseOk = false;
				isReboot = false;

				sendMessageManager.uploadTestResult(mac, false, 1, chip);
			}

		} else if (DeviceConstant.Chip._7688.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7688.POWER_ON) && !isReboot) {
				log.info("7688{}", "进入写模块工厂区模式");
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.REC);

			} else if ((str.contains(DeviceConstant.RMessage_7688.REC_ENTER) || str.contains(DeviceConstant.RMessage_7688.REC_INTO)) && !isRec) {
				frame.setDataView("进入写模块工厂区模式成功");
				sendMessageManager.sendData_768(serialPort, DeviceConstant.Chip._7688, mac);
				isRec = true;

			} else if (str.contains(mac.substring(14)) && isRec && !isReboot) {
				frame.setDataView("写入MAC地址成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.WRITE_LICENSE);

			} else if (str.contains(DeviceConstant.RMessage_7688.WRITE_LICENSE) && isLicenseReady && isRec) {
				log.info("7688{}", "开始写入License文件");
				sendMessageManager.sendData_768(serialPort, DeviceConstant.Chip._7688, CommUtils.buildLicensePath(mac));
				isLicenseReady = false;

			} else if (str.contains(DeviceConstant.RMessage_7688.WRITE_LICENSE_OK) && isRec) {
				frame.setDataView("写入License成功");
				frame.setDataView("正在重启设备，请稍等...");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.REBOOT);
				isReboot = true;

			} else if (str.contains(DeviceConstant.RMessage_7688.AUTH_OK) && isRec) {
				frame.setDataView("设备重启完成");
				frame.setDataView("MAC地址 & License写入成功");
				frame.setDataView("\r\n写模块工厂区成功");
				isRec = false;
				isLicenseReady = true;
				isReboot = false;

				// 上传测试结果
				sendMessageManager.uploadTestResult(mac, true, 1, chip);

			} else if (str.contains(DeviceConstant.RMessage_7688.AUTH_ERROR) && isRec) {
				frame.setDataView("设备重启完成");
				frame.setDataView("MAC地址 & License写入失败");
				frame.setDataView("\r\n写模块工厂区失败");
				isRec = false;
				isLicenseReady = true;
				isReboot = false;

				sendMessageManager.uploadTestResult(mac, false, 1, chip);
			}
		}
	}

	/**
	 * 通讯质量测试
	 * 
	 * @param frame
	 *            当前窗口
	 * @param chip
	 *            芯片型号
	 * @param mac
	 *            MAC地址
	 * @param serialPort
	 *            串口
	 * @param data
	 *            数据(字节)
	 * @param str
	 *            数据(字符串)
	 */
	private void RSSITest(final BaseFrame frame, final String chip, final String mac, final SerialPort serialPort, byte[] data, String str) {
		log.info("通讯质量测试{}", str);

		if (DeviceConstant.Chip._1310_device.equals(chip) || DeviceConstant.Chip._1310_gateway.equals(chip) || DeviceConstant.Chip._2530.equals(chip) || DeviceConstant.Chip._2630.equals(chip)) {
			log.info("ZigBee通讯质量测试{}", ByteUtils.byteArrayToHexString(data));
			if (DeviceConstant.Chip._1310_device.equals(chip)) {
				if (str.contains("ZIGBEE TEST IS OK")) {
					frame.clearDataView();
					frame.setDataView("进入通讯质量测试模式成功");
					sendMessageManager.RSSITest_ZigBee(serialPort, chip);
				}

			} else if (DeviceConstant.Chip._1310_gateway.equals(chip)) {
				if ("1013".equals(ByteUtils.byteArrayToHexString(data))) {
					frame.clearDataView();
					frame.setDataView("进入通讯质量测试模式成功");
					sendMessageManager.RSSITest_ZigBee(serialPort, chip);
				}

			} else if (DeviceConstant.Chip._2530.equals(chip)) {
				if ("3025".equals(ByteUtils.byteArrayToHexString(data))) {
					frame.clearDataView();
					frame.setDataView("进入通讯质量测试模式成功");
					sendMessageManager.RSSITest_ZigBee(serialPort, chip);
				}

			} else {
				if (str.contains(DeviceConstant.RMessage_1310.ZIGBEE_TEST_IS_OK)) {
					frame.clearDataView();
					frame.setDataView("进入通讯质量测试模式成功");
					sendMessageManager.RSSITest_ZigBee(serialPort, chip);
				}
			}

			// 头数据命令字
			byte headOpcode = data[5];

			if (DeviceConstant.HeadOpcode.ZIGBEE == headOpcode) {
				// 接收数据显示
				frame.setDataView("接收数据——RSSI：" + data[6] + "dBm");

				// 记录测试数据
				if (GlobalVariable.RSSITestNumber == 1) {
					GlobalVariable.RSSITestData.clear();
				}
				GlobalVariable.RSSITestData.add((int) data[6]);
			}

		} else if (DeviceConstant.Chip._7681.equals(chip)) {
			if ((str.contains(DeviceConstant.RMessage_7681.REC_ENTER) || str.contains(DeviceConstant.RMessage_7681.REC_INTO)) && !isRec) {
				frame.clearDataView();
				frame.setDataView("进入通讯质量测试模式成功\r\n正在连接WiFi...");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.RSSI_TEST_START);
				isRec = true;

				TimeUtils.startTimer(1000 * 30, new IOvertimeExecute() {

					@Override
					public void execute() {
						frame.setDataView("WiFi连接失败");
						frame.setDataView("\r\n通讯质量测试失败");

						// 上传测试结果
						sendMessageManager.uploadTestResult(mac, false, 2, chip);

						TimeUtils.stopTimer();
						isRec = false;
					}
				});

			} else if (str.contains(DeviceConstant.RMessage_7681.WIFI_CONN_OK) && !isWiFiConn) {
				frame.setDataView("WiFi连接成功\r\n开始测试，请稍等...");
				isWiFiConn = true;

				TimeUtils.startTimer(1000 * 20, new IOvertimeExecute() {

					@Override
					public void execute() {
						frame.setDataView("\r\n通讯质量测试失败");

						// 上传测试结果
						sendMessageManager.uploadTestResult(mac, false, 2, chip);

						TimeUtils.stopTimer();
						isRec = false;
						isWiFiConn = false;
					}
				});

			} else if (str.contains("SI-")) {
				int len = str.length();
				String content = str.substring(len - 3, len);
				int rssi = Integer.parseInt(content);

				// 记录测试数据
				GlobalVariable.RSSITestData.add(rssi);

				int dataLen = GlobalVariable.RSSITestData.size();

				if (!isTimerWork) {
					isTimerWork = true;

					TimeUtils.startTimer(3000 * 10, new IOvertimeExecute() {

						@Override
						public void execute() {
							if (isRec) {
								RSSITestResult(frame, chip);
							}
						}
					});
				}

				if (dataLen == 5) {
					if (isRec) {
						RSSITestResult(frame, chip);
					}
				}
			}

		} else if (DeviceConstant.Chip._7688.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7688.POWER_ON)) {
				log.info("7688{}", "进入通讯质量测试模式");
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.REC);

			} else if ((str.contains(DeviceConstant.RMessage_7688.REC_ENTER) || str.contains(DeviceConstant.RMessage_7688.REC_INTO)) && !isRec) {
				frame.setDataView("进入通讯质量测试模式成功\r\n正在连接WiFi...");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.RSSI_TEST_START);
				isRec = true;

				TimeUtils.startTimer(1000 * 30, new IOvertimeExecute() {

					@Override
					public void execute() {
						frame.setDataView("WiFi连接失败");
						frame.setDataView("\r\n通讯质量测试失败");

						// 上传测试结果
						sendMessageManager.uploadTestResult(mac, false, 2, chip);

						TimeUtils.stopTimer();
						isRec = false;
					}
				});

			} else if (str.contains(DeviceConstant.RMessage_7688.WIFI_CONN_OK) && !isWiFiConn) {
				frame.setDataView("WiFi连接成功\r\n开始测试，请稍等...");
				isWiFiConn = true;

				TimeUtils.startTimer(1000 * 20, new IOvertimeExecute() {

					@Override
					public void execute() {
						frame.setDataView("\r\n通讯质量测试失败");

						// 上传测试结果
						sendMessageManager.uploadTestResult(mac, false, 2, chip);

						TimeUtils.stopTimer();
						isRec = false;
						isWiFiConn = false;
					}
				});

			} else if (str.contains("SI-")) {
				int len = str.length();
				String content = str.substring(len - 3, len);
				int rssi = Integer.parseInt(content);

				// 记录测试数据
				GlobalVariable.RSSITestData.add(rssi);

				int dataLen = GlobalVariable.RSSITestData.size();

				if (!isTimerWork) {
					isTimerWork = true;

					TimeUtils.startTimer(3000 * 10, new IOvertimeExecute() {

						@Override
						public void execute() {
							if (isRec) {
								RSSITestResult(frame, chip);
							}
						}
					});
				}

				if (dataLen == 5) {
					if (isRec) {
						RSSITestResult(frame, chip);
					}
				}
			}
		}
	}

	private void RSSITestResult(BaseFrame frame, String chip) {
		TimeUtils.stopTimer();

		log.info("通讯质量测试数据{}", GlobalVariable.RSSITestData.toString());

		// 设备MAC
		String mac = frame.macInputField.getText();
		frame.setDataView("通讯质量测试完成");

		if (TestResultAnalyze.RSSITest(GlobalVariable.RSSITestData)) {
			frame.setDataView("\r\n通讯质量测试成功");

			sendMessageManager.uploadTestResult(mac, true, 2, chip);
		} else {
			frame.setDataView("\r\n通讯质量测试失败");

			sendMessageManager.uploadTestResult(mac, false, 2, chip);
		}

		GlobalVariable.RSSITestData.clear();

		isRec = false;
		isTimerWork = false;
		isWiFiConn = false;
	}

	/**
	 * 模块程序升级
	 * 
	 * @param frame
	 *            当前窗口
	 * @param chip
	 *            芯片型号
	 * @param mac
	 *            MAC地址
	 * @param serialPort
	 *            串口
	 * @param data
	 *            数据(字节)
	 * @param str
	 *            数据(字符串)
	 */
	private void deviceUpgrade(final BaseFrame frame, final String chip, final String mac, final SerialPort serialPort, byte[] data, String str) {
		log.info("模块程序升级{}", str);

		if (DeviceConstant.Chip._2530.equals(chip)) {
			if (DeviceConstant.RMessage_2530.BOOT.equals(str) && !isRec) {
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2530.UPGRADE_INIT);

			} else if (DeviceConstant.RMessage_2530.UPGRADE_INIT.equals(str) && !isRec) {
				frame.setDataView("进入模块升级模式成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2530.UPGRADE);
				isRec = true;

			} else if (DeviceConstant.RMessage_2530.UPGRADE.equals(str) && isUpgradeReady) {
				frame.setDataView("正在进行模块升级,请稍等...");
				sendMessageManager.sendData_ZigBee(serialPort, CommUtils.buildUpgradePath());
				isUpgradeReady = false;

			} else if (str.contains(DeviceConstant.RMessage_2530.UPGRADE_OK)) {
				// 2530升级完成后会重启启动，打印信息Boot 0%会导致再次进入升级模式
				// 接收到升级成功指令后，延时3s处理
				TimeUtils.startTimer(3000, new IOvertimeExecute() {

					@Override
					public void execute() {
						frame.setDataView("\r\n模块升级成功");
						((DeviceUpgradeFrame) frame).setTx(630);

						// 上传测试结果
						sendMessageManager.uploadTestResult(mac, true, 3, chip);

						isRec = false;
						isUpgradeReady = true;
					}
				});
			}

		} else if (DeviceConstant.Chip._2630.equals(chip) || DeviceConstant.Chip._1310_device.equals(chip)) {
			if (DeviceConstant.RMessage_1310.POWER_ON.equals(str)) {
				log.info("{} update strated. recv:{}", chip, ".");
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.REC);
				log.info("to {} :{}", chip, "rec");
			} else if (DeviceConstant.RMessage_1310.REC.equals(str)) {
				log.info("from {} : {}", chip, "#");
				frame.setDataView("进入模块升级模式成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_2630.UPGRADE);
				log.info("to {} :{}", chip, "0x06");
			} else if (str.contains(DeviceConstant.RMessage_1310.UPGRADE)) {
				log.info("from {} : {}", chip, "recovery");
				frame.setDataView("正在进行模块升级,请稍等...");
				sendMessageManager.sendData_ZigBee(serialPort, CommUtils.buildUpgradePath());
				log.info("to {} :xmodel", chip);

			} else if (str.contains(DeviceConstant.RMessage_1310.UPGRADE_OK)) {
				log.info("from {} : {}", chip, "UPDATING OK");
				frame.setDataView("\r\n模块升级成功");
				((DeviceUpgradeFrame) frame).setTx(510);
				// 上传测试结果
				sendMessageManager.uploadTestResult(mac, true, 3, chip);
			}

		} else if (DeviceConstant.Chip._7681.equals(chip)) {
			if (str.contains(DeviceConstant.RMessage_7681.POWER_ON)) {
				frame.clearDataView();
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.REC);

			} else if ((str.contains(DeviceConstant.RMessage_7681.REC_ENTER) || str.contains(DeviceConstant.RMessage_7681.REC_INTO)) && !isRec) {
				frame.setDataView("进入模块升级模式成功");
				sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7681.UPGRADE);
				isRec = true;

			} else if (str.contains(DeviceConstant.RMessage_7681.WRITE_LICENSE) && isLicenseReady) {
				frame.setDataView("开始进行模块升级,请稍等...");
				sendMessageManager.sendData_768(serialPort, DeviceConstant.Chip._7681, CommUtils.buildUpgradePath());
				isLicenseReady = false;
			} else if (str.contains(DeviceConstant.RMessage_7681.UPGRADE_OK)) {
				frame.setDataView("\r\n模块升级成功");
				((DeviceUpgradeFrame) frame).setTx(1999);
				// isResetDevice=false;
				// SerialPortManager.isRefuseReadData = false;
				// 上传测试结果
				sendMessageManager.uploadTestResult(mac, true, 3, chip);

				isRec = false;
				isLicenseReady = true;
			} /*
				 * else if (str.contains(DeviceConstant.RMessage_7681.AUTH_OK) && isReboot) { frame.setDataView("发送恢复出厂设置指令"); frame.setDataView("等待恢复出厂"); isReboot = false; isResetDevice=true; sendMessageManager.sendOrder(serialPort,
				 * DeviceConstant.Order_7681.RESET_DEVICE);
				 * 
				 * }else if(str.contains(DeviceConstant.RMessage_7681.AUTH_OK) && isResetDevice){ isResetDevice=false; frame.setDataView("\r\n恢复出厂成功"); SerialPortManager.isRefuseReadData = true;
				 * 
				 * sendMessageManager.uploadTestResult(mac, true, 3, chip); }
				 */
		} else if (DeviceConstant.Chip._7688.equals(chip)) {
			/*
			 * if (str.contains(DeviceConstant.RMessage_7688.POWER_ON)) { frame.clearDataView(); sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.REC);
			 * 
			 * } else if ((str.contains(DeviceConstant.RMessage_7688.REC_ENTER) || str .contains(DeviceConstant.RMessage_7688.REC_INTO)) && !isRec) { frame.setDataView("进入模块恢复出厂模式成功"); frame.setDataView("发送恢复出厂设置指令");
			 * frame.setDataView("等待恢复出厂"); sendMessageManager.sendOrder(serialPort, DeviceConstant.Order_7688.RESET_DEVICE); isRec = true; isResetDevice=true; }else if(str.contains(DeviceConstant.RMessage_7688.AUTH_OK) && isResetDevice){
			 * isResetDevice=false; isRec = false; frame.setDataView("\r\n恢复出厂成功"); sendMessageManager.uploadTestResult(mac, true, 4, chip); }
			 */
		}
	}

	/**
	 * 重置标记信息
	 */
	public void reset() {
		isLicenseReady = true;
		isWriteLicenseOk = false;
		isRec = false;
		isReboot = false;
		isWiFiConn = false;
		isTimerWork = false;
		isUpgradeReady = true;
		isResetDevice = false;
		builder = new StringBuilder();
	}
}
