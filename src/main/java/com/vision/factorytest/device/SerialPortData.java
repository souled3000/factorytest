package com.vision.factorytest.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.factorytest.utils.ByteUtils;


/**
 * 建立串口发送的数据
 * 
 * @author yangle
 */
public class SerialPortData {
	private static final Logger log = LoggerFactory.getLogger(SerialPortData.class);
	/**
	 * 建立串口发送的数据（无数据内容）
	 * 
	 * @param headOpcode
	 *            头数据命令字
	 * @return 串口发送的数据
	 */
	public static byte[] build(byte headOpcode) {
		ByteBuffer data = ByteBuffer.allocate(8);
		data.put((byte) 0x6D);
		data.put((byte) 0x64);
		data.put((byte) (data.array().length - 3));
		byte[] b = { 0x01, 0x00, headOpcode, (byte) 0xDF };
		data.put(b);
		data.put(check(data.array(), 2, data.array().length));
		log.info("串口发送数据", ByteUtils.byteArrayToHexString(data.array()));
		return data.array();
	}

	/**
	 * 建立串口发送的数据（有数据内容）
	 * 
	 * @param headData
	 *            加头数据
	 * @return 串口发送的数据
	 */
	public static byte[] build(byte[] headData) {
		ByteBuffer data = ByteBuffer.allocate(15 + headData.length + 1);
		data.put((byte) 0x6D);
		data.put((byte) 0x64);
		data.put((byte) ((byte) 13 + headData.length));
		byte[] b = { 0x01, 0x00, (byte) 0xAD, (byte) 0xDF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		data.put(b);
		data.put(headData);
		data.put(check(data.array(), 2, data.array().length));
		log.info("串口发送数据{}", ByteUtils.byteArrayToHexString(data.array()));
		return data.array();
	}

	/**
	 * 解析串口接收的数据（有数据内容）
	 * 
	 * @param data
	 *            串口接收的数据
	 * @return 加头数据
	 */
	public static byte[] parse(byte[] data) {
		if (data.length < 1) {
			return new byte[] {};
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

		// 数据校验
		byte temp = byteBuffer.get(data.length - 1);
		if (temp != check(data, 2, data.length)) {
			// LogUtils.d("串口接收数据", "数据校验错误");
			// return new byte[] {};
		}

		byte[] headData = new byte[byteBuffer.get(2) - 13];
		System.arraycopy(data, 15, headData, 0, headData.length);
		return headData;
	}

	/**
	 * 计算校验位
	 * 
	 * @param data
	 *            数据
	 * @param start
	 *            开始位置
	 * @param length
	 *            校验的数据长度
	 * @return 校验位
	 */
	private static byte check(byte[] data, int start, int length) {
		int check = 0;
		for (int i = start; i < length - 2; i++) {
			check += data[i] & 0xff;
			check &= 0xff;
		}
		return (byte) (check & 0xff);
	}
}
