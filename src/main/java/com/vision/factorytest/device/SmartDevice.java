package com.vision.factorytest.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 设备
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class SmartDevice {

	/**
	 * 有效数据
	 */
	public byte[] data = new byte[] {};
	/**
	 * 命令字
	 */
	public byte opcode;

	/**
	 * 建立通讯数据
	 * 
	 * @return 通讯数据
	 */
	public byte[] buildData() {
		if (data.length > (0x00ff - 2)) {
			return null;
		}
		ByteBuffer bb = ByteBuffer.allocate(7 + data.length);
		bb.put((byte) 0x6D);
		bb.put((byte) 0x64);
		bb.put((byte) (data.length + 4));
		bb.put((byte) 0);
		bb.put((byte) 0);
		bb.put(opcode);
		bb.put(data);
		bb.put(check(bb.array(), 2, bb.array().length));
		return bb.array();
	}

	/**
	 * 解析通讯数据
	 * 
	 * @param data
	 *            通讯数据
	 * @return 有效数据&命令字
	 */
	public void parseData(byte[] originData) {
		if (originData.length < 1) {
			return;
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(originData).order(ByteOrder.LITTLE_ENDIAN);
		byte head_1 = byteBuffer.get(0);
		byte head_2 = byteBuffer.get(1);
		if (head_1 != 0x6D && head_2 != 0x64) {
			return;
		}
		byte head_3 = byteBuffer.get(2);
		if ((originData.length - 2 - 1) != (head_3 & 0xff)) {
			return;
		}

		// 版本号
		byte head_4 = byteBuffer.get(3);
		// 预留
		byte head_5 = byteBuffer.get(4);
		// 命令字
		byte head_6 = byteBuffer.get(5);
		// 校验字
		byte temp = byteBuffer.get(originData.length - 1);
		originData[originData.length - 1] = 0;

		if (temp != check(originData, 2, originData.length)) {
			return;
		}

		// 有效数据
		data = new byte[head_3 - 4];
		if (data.length > 0) {
			System.arraycopy(originData, 6, data, 0, data.length);
		}
		// 命令字
		opcode = head_6;
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
	private byte check(byte[] data, int start, int length) {
		int check = 0;
		for (int i = start; i < length; i++) {
			check += data[i] & 0xff;
			check &= 0xff;
		}
		return (byte) (check & 0xff);
	}
}
