package com.smarthome.head;

import com.vision.factorytest.utils.ByteUtils;

/**
 * 解析时会添加的数据 FIN OPCODE SRCID DataFormat MsgID Data
 * 
 * @author Lal
 * 
 */
public class SmartHomeData {

	/**
	 * 标识数据包是否是最后一包，0-不是；1-最后一包数据
	 */
	public boolean isFin;
	/**
	 * 是读取还是写入
	 */
	public boolean isRead;
	/**
	 * 标识是否是应答数据
	 */
	public boolean isACK;
	/**
	 * 标识是否隐藏帧头数据
	 */
	public boolean isMask = false;
	/**
	 * 数据格式 0x0：二进制数据格式。 0x1：JSON 格式数据。 0x2：XML 格式数据。
	 */
	public byte dataFormat;
	/**
	 * 密钥级别，0-不加密，1-随机密钥（登录之前使用），2-密证，3-密证+随机密钥（登录之后使用）。
	 */
	public byte keyLevel;
	/**
	 * 加密类型，0-RC4 加密，1-DES 加密，2-AES 加密。
	 */
	public byte encryptType;
	/**
	 * 操作识别码 0：控制终端与设备终端局域网通讯数据。 1: 控制终端与设备终端广域网直接通讯数据。 2：终端设备与平台通讯数据。
	 * 3：终端设备需要平台代理转发数据。
	 */
	public byte opcode;
	/**
	 * 消息ID（协议ID）
	 */
	public short msgID;
	/**
	 * 数据包序号 每个会话的每个协议保存一个数据包序号。 发送一次加1 只取一个字节
	 */
	public short dataSequ;
	/**
	 * 会话ID，登录设备后由设备返回
	 */
	public byte[] sessionId;
	/**
	 * 单个回话发送计数次回话每发送一包数据需要将序号加1 只取两字节
	 */
	public int sequence;
	/**
	 * 绝对时间戳，秒级数值，表示1970 年到现在的秒数 只取四字节
	 */
	public long time;
	/**
	 * 指令到达的目标设备ID 号码
	 */
	public long dstID;
	/**
	 * 指令到达的目标设备ID 号码(通过websocket转发时使用 内容为设备ID) 当为接收数据时此字段为空d
	 */
	public long wsDstID;
	/**
	 * 指令发出的源设备ID 号码 如果有用户登录填写用户ID
	 */
	public long srcID;
	/**
	 * 发送或接收到的实际数据
	 */
	public byte[] data;
	/**
	 * 包解析结果识别码
	 */
	public int code;
	public long datID;

	@Override
	public String toString() {
		return "SmartHoneData{" + "isFin=" + isFin + ", isRead=" + isRead + ", isACK=" + isACK + ", isMask=" + isMask
				+ ", dataFormat=" + dataFormat + ", keyLevel=" + keyLevel + ", encryptType=" + encryptType + ", opcode="
				+ opcode + ", msgID=" + msgID + ", dadaSeuq=" + dataSequ + ", sessionId="
				+ ByteUtils.byteArrayToHexString(sessionId) + ", sequence=" + sequence + ", time=" + time + ", dstID="
				+ dstID + ", wsDstID=" + wsDstID + ", srcID=" + srcID + ", data=" + ByteUtils.byteArrayToHexString(data)
				+ ", code=" + code + ", datID=" + datID + '}';
	}
}
