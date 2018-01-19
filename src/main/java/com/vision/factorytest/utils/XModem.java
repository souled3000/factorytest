package com.vision.factorytest.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.vision.factorytest.Constant;
import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;
import com.vision.factorytest.manager.SerialPortManager;
import com.vision.factorytest.ui.BaseFrame;
import com.vision.factorytest.ui.DeviceUpgradeFrame;

@SuppressWarnings("all")
public class XModem {

	// Debugging
	private static final String TAG = "XModem";
	// packet is per 128-byte
	private final int SECSIZE = 128;
	// retry for 10 times
	private final int MAXERRORS = 20;

	// Protocol bytes
	private final char SOH = 0x01;
	private final char STX = 0x02;
	private final char EOT = 0x04;
	private final char ACK = 0x06;
	// Base-10 value: 21
	private final char NAK = 0x15;
	// Base-10 value: 24
	private final char CAN = 0x18;
	// Base-10 value: 67
	private final char CRC = 0x43;

	// TLV bytes
	private final byte TAG_BYTE = (byte) 0xA5;
	private final byte INV_TAG_BYTE = (byte) 0x5A;

	protected InputStream inStream;
	protected OutputStream outStream;
	protected PrintWriter errStream;

	private final char crctable[] = { 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 0x8108, 0x9129,
			0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7,
			0x62d6, 0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 0x2462, 0x3443, 0x0420, 0x1401,
			0x64e6, 0x74c7, 0x44a4, 0x5485, 0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 0x3653,
			0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe,
			0xd79d, 0xc7bc, 0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823, 0xc9cc, 0xd9ed, 0xe98e,
			0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
			0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22,
			0x3c03, 0x0c60, 0x1c41, 0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49, 0x7e97, 0x6eb6,
			0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70, 0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59,
			0x8f78, 0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f, 0x1080, 0x00a1, 0x30c2, 0x20e3,
			0x5004, 0x4025, 0x7046, 0x6067, 0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e, 0x02b1,
			0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256, 0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f,
			0xd52c, 0xc50d, 0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 0xa7db, 0xb7fa, 0x8799,
			0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c, 0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
			0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab, 0x5844, 0x4865, 0x7806, 0x6827, 0x18c0,
			0x08e1, 0x3882, 0x28a3, 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 0x4a75, 0x5a54,
			0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8,
			0x8dc9, 0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 0xef1f, 0xff3e, 0xcf5d, 0xdf7c,
			0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };

	// File
	private String fileName;

	public XModem(InputStream is, OutputStream os, PrintWriter errs) {
		inStream = is;
		outStream = os;
		errStream = errs;
	}

	public XModem() {
		inStream = (DataInputStream) System.in;
		outStream = new DataOutputStream(System.out);
		errStream = new PrintWriter(System.err);
	}

	protected boolean gotChar;

	/**
	 * Send a file to the remote
	 */
	public boolean send(final String tfile) throws IOException {
		new Thread() {
			public void run() {
				try {
					fileName = tfile;
					char errorcount;
					byte blocknumber, character;
					int checksum;
					byte[] sector = new byte[SECSIZE];
					int nbytes;

					DataInputStream dis;
					dis = new DataInputStream(new FileInputStream(tfile));
					LogUtils.d(TAG, "file open, ready to send");
					errorcount = 0;
					blocknumber = 0x01;

					gotChar = false;

					int count = 0;
					while ((nbytes = dis.read(sector)) != 0) {
						if (nbytes <= -1) {
							break;
						}
						if (nbytes < SECSIZE)
							for (int i = nbytes; i < SECSIZE; i++)
								sector[i] = (byte) 0xff;

						errorcount = 0;
						boolean isSend = true;

						while (errorcount < MAXERRORS) {
							if (isSend) {
								errStream.println("{" + blocknumber + "}");
								StringBuffer buf = new StringBuffer().append(blocknumber);
								putChar(SOH, "SOH");
								putChar(blocknumber, "BlockNumber");
								putChar(~blocknumber, "Inverse BlockNumber");
								checksum = 0;
								checksum = crc16(sector) & 0x00ffff;
								putChar(sector, (short) checksum, "Sector + Checksum");
								outStream.flush();

								LogUtils.d(TAG,
										"SOH:" + (int) SOH + "___BlockNumber:" + blocknumber + "___Inverse BlockNumber:"
												+ ~blocknumber + "___Data:" + ByteUtils.byteArrayToHexString(sector));
							}

							String testItem = GlobalVariable.currentTestItem;
							String chip = GlobalVariable.currentChip;

							if (Constant.TestItem.WRITE_FACTORY_DISTRICT.equals(testItem)) {
								// 写模块工厂区
								if (DeviceConstant.Chip._7681.equals(chip) || DeviceConstant.Chip._7688.equals(chip)) {
									sleep(1000);
								}

								byte data = getChar();
								if (data == ACK) {
									LogUtils.d(TAG, "有应答___" + ByteUtils.byteToHex(data));
									break;

								} else if (data == 0x2E) {
									while (true) {
										LogUtils.d(TAG, ".");
										if (getChar() == ACK) {
											break;
										}
									}
									break;

								} else {
									++errorcount;
									LogUtils.d(TAG, "无应答___" + ByteUtils.byteToHex(data));
								}

							} else if (Constant.TestItem.DEVICE_UPGRADE.equals(testItem)) {
								if (count == 1) {
									// 初始化
									sleep(2000);
								}
								if (DeviceConstant.Chip._2530.equals(chip) || DeviceConstant.Chip._2630.equals(chip)) {
									sleep(10);
								}

								byte data = getChar();
								if (data == ACK) {
									isSend = true;
									LogUtils.d(TAG, "有应答___" + ByteUtils.byteToHex(data));
									break;

								} else {
									++errorcount;
									isSend = false;
									LogUtils.d(TAG, "无应答___" + ByteUtils.byteToHex(data));
								}
							}
						}
						if (errorcount == MAXERRORS)
							xError();
						blocknumber = (byte) ((++blocknumber) % 256);
						LogUtils.d(TAG, "发送包数:" + (++count));

						if (GlobalVariable.currentTestItem.equals(Constant.TestItem.DEVICE_UPGRADE)) {
							((DeviceUpgradeFrame) BaseFrame.getFrame()).setTx(count);
						}
					}

					boolean isAck = false;
					while (!isAck) {
						putChar(EOT, "EOT");
						isAck = getChar() == ACK;
					}

					SerialPortManager.isRefuseReadData = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();

		return true;
	}

	/**
	 * Receive a file from remote
	 */
	public boolean recv(String tfile) throws IOException {
		char errorcount;
		byte blocknumber, character = 0;
		int checksum;
		boolean gotChar;
		byte[] sector = new byte[SECSIZE];

		DataOutputStream dos;
		dos = new DataOutputStream(new FileOutputStream(tfile));
		gotChar = false;
		putChar((byte) 0x43, "C");
		errorcount = 0;
		blocknumber = 0x01;

		while (true) {
			if (errorcount > MAXERRORS) {
				dos.close();
				return false;
			}
			character = getChar();
			gotChar = true;
			if (character != EOT) {
				try {
					byte not_ch;
					if (character != SOH) {
						errorcount++;
						continue;
					}
					// blocknumber
					character = getChar();
					// ~blocknumber
					not_ch = (byte) ~getChar();

					if (character != not_ch) {
						errorcount++;
						continue;
					}
					if (character != blocknumber) {
						errorcount++;
						continue;
					}
					for (int i = 0; i < SECSIZE; i++) {
						sector[i] = getChar();
					}

					checksum = 0;
					checksum = (getChar() & 0xff) << 8;
					checksum |= (getChar() & 0xff);

					int crc = crc16(sector);
					if (crc != checksum) {
						errorcount++;
						continue;
					}
					putChar(ACK, "ACK");
					blocknumber++;
					try {
						dos.write(sector);
					} catch (IOException ioe) {
						errStream.println("--ERROR--write failed. Blocknumber " + blocknumber);
					}
				} finally {
					if (errorcount != 0)
						putChar(NAK, "NAK");
				}
			} else {
				break;
			}
		}

		dos.close();

		putChar(ACK, "ACK");
		putChar(ACK, "ACK");

		return true;
	}

	protected byte getChar() throws IOException {
		byte something = (byte) inStream.read();
		return something;
	}

	protected void putChar(int c, String debug) throws IOException {
		outStream.write((byte) c);
	}

	protected void putChar(byte[] b, String debug) throws IOException {
		outStream.write(b);
	}

	protected void putChar(byte[] b, short c, String debug) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(b.length + 2).order(ByteOrder.BIG_ENDIAN);
		bb.put(b);
		bb.putShort(c);
		outStream.write(bb.array());
	}

	protected char crc16(byte[] bytes) {
		char crc = 0x0000;
		for (byte b : bytes) {
			crc = (char) ((crc << 8) ^ crctable[((crc >> 8) ^ b) & 0x00ff]);
		}
		return (char) (crc);
	}

	protected void xError() {
		errStream.println("too many errors...aborting");
	}
}