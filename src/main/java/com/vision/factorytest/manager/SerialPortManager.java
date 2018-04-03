package com.vision.factorytest.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.factorytest.exception.NoSuchPort;
import com.vision.factorytest.exception.NotASerialPort;
import com.vision.factorytest.exception.PortInUse;
import com.vision.factorytest.exception.ReadDataFromSerialPortFailure;
import com.vision.factorytest.exception.SendDataToSerialPortFailure;
import com.vision.factorytest.exception.SerialPortInputStreamCloseFailure;
import com.vision.factorytest.exception.SerialPortOutputStreamCloseFailure;
import com.vision.factorytest.exception.SerialPortParameterFailure;
import com.vision.factorytest.exception.TooManyListeners;
import com.vision.factorytest.utils.ArrayUtils;
import com.vision.factorytest.utils.ByteUtils;
import com.vision.factorytest.utils.ShowUtils;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * 串口管理
 * 
 * @author yangle
 */
public class SerialPortManager {
	private static final Logger log = LoggerFactory.getLogger(SerialPortManager.class);
	public static boolean isRefuseReadData = false;

	/**
	 * 查找所有可用端口
	 * 
	 * @return 可用端口名称列表
	 */
	@SuppressWarnings("unchecked")
	public static final ArrayList<String> findPort() {
		// 获得当前所有可用串口
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> portNameList = new ArrayList<String>();
		// 将可用串口名添加到List并返回该List
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}

	/**
	 * 打开串口
	 * 
	 * @param portName
	 *            端口名称
	 * @param baudrate
	 *            波特率
	 * @return 串口对象
	 */
	public static final SerialPort openPort(String portName, int baudrate) {
		try {
			// 通过端口名识别端口
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(portName, 2000);
			// 判断是不是串口
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				try {
					// 设置一下串口的波特率等参数
					serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				} catch (UnsupportedCommOperationException e) {
					new SerialPortParameterFailure().printStackTrace();
				}
				return serialPort;
			} else {
				// 不是串口
				new NotASerialPort().printStackTrace();
			}
		} catch (NoSuchPortException e1) {
			new NoSuchPort().printStackTrace();
		} catch (PortInUseException e2) {
			new PortInUse().printStackTrace();
			ShowUtils.warningMessage(new PortInUse().toString());
		}
		return null;
	}

	/**
	 * 关闭串口
	 * 
	 * @param serialport
	 *            待关闭的串口对象
	 */
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
		}
	}

	/**
	 * 向串口发送数据
	 * 
	 * @param serialPort
	 *            串口对象
	 * @param order
	 *            待发送数据
	 */
	public static void sendToPort(SerialPort serialPort, byte[] order) {
		OutputStream out = null;
		try {
			out = serialPort.getOutputStream();
			out.write(order);
			out.flush();
			log.info("向串口发送数据{}", ByteUtils.byteArrayToHexString(order) + "___" + Arrays.toString(order));
		} catch (IOException e) {
			new SendDataToSerialPortFailure().printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
			} catch (IOException e) {
				new SerialPortOutputStreamCloseFailure().printStackTrace();
			}
		}
	}

	/**
	 * 从串口读取数据
	 * 
	 * @param serialPort
	 *            当前已建立连接的SerialPort对象
	 * @return 读取到的数据
	 */
	public static byte[] readFromPort(SerialPort serialPort) {
		InputStream in = null;
		byte[] bytes = {};
		try {
			isRefuseReadData = true;
			in = serialPort.getInputStream();
			// 缓冲区大小为一个字节
			byte[] readBuffer = new byte[1];
			int bytesNum = in.read(readBuffer);
			while (bytesNum > 0) {
				bytes = ArrayUtils.concat(bytes, readBuffer);
				bytesNum = in.read(readBuffer);
			}
		} catch (IOException e) {
			new ReadDataFromSerialPortFailure().printStackTrace();
		} finally {
			isRefuseReadData = false;
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				new SerialPortInputStreamCloseFailure().printStackTrace();
			}
		}
		return bytes;
	}

	/**
	 * 添加监听器
	 * 
	 * 串口对象
	 */
	public static void addListener(SerialPort serialPort) {
		try {
			// 给串口添加监听器
			serialPort.addEventListener(new SerialListener(serialPort));
			// 设置当有数据到达时唤醒监听接收线程
			serialPort.notifyOnDataAvailable(true);
			// 设置当通信中断时唤醒中断线程
			serialPort.notifyOnBreakInterrupt(true);
		} catch (TooManyListenersException e) {
			new TooManyListeners().printStackTrace();
		}
	}

	private static class SerialListener implements SerialPortEventListener {

		private SerialPort serialPort;

		public SerialListener(SerialPort serialPort) {
			this.serialPort = serialPort;
		}

		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {
			case SerialPortEvent.BI: // 10 通讯中断
				// ShowUtils.errorMessage("与串口设备通讯中断");
				break;

			case SerialPortEvent.OE: // 7 溢位（溢出）错误

			case SerialPortEvent.FE: // 9 帧错误

			case SerialPortEvent.PE: // 8 奇偶校验错误

			case SerialPortEvent.CD: // 6 载波检测

			case SerialPortEvent.CTS: // 3 清除待发送数据

			case SerialPortEvent.DSR: // 4 待发送数据准备好了

			case SerialPortEvent.RI: // 5 振铃指示

			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
				break;

			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				if (!isRefuseReadData) {
					byte[] data = null;
					if (serialPort == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					} else {
						// 读取串口数据
						data = SerialPortManager.readFromPort(serialPort);
						ReceiveMessageManager.defaultManage().SerialPortDataResponse(data);
					}
				}
				break;
			}
		}
	}
}
