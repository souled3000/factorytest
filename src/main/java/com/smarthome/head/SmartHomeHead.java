package com.smarthome.head;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.vision.factorytest.utils.AESUtils;

public class SmartHomeHead {

	public static byte[] addHead(SmartHomeData smartHomeData) throws Exception {
		int crcLength = 2;
		ByteBuffer bb;

		if (smartHomeData.keyLevel != SmartHomeConstant.Key.NO_SECRET_KEY) {
			crcLength = smartHomeData.data.length + 2;

			byte j = (byte) (16 - (crcLength % 16));
			if (j > 0 && j < 16) {
				crcLength += j;
			}

			bb = ByteBuffer.allocate(crcLength - 2 + 8 + 10).order(ByteOrder.LITTLE_ENDIAN);
		} else {
			bb = ByteBuffer.allocate(18).order(ByteOrder.LITTLE_ENDIAN);
		}

		byte temp = 0;
		temp |= smartHomeData.isFin ? (byte) (1 << 7) : 0;
		temp |= smartHomeData.isMask ? (byte) (1 << 6) : 0;
		temp |= (byte) (1 << 3);
		temp |= smartHomeData.opcode & 0x7;
		bb.put(temp);
		temp = 0;
		bb.put(temp);
		bb.putShort((short) (smartHomeData.sequence & 0xffff));
		bb.putInt((int) (smartHomeData.time & 0xffffffff));
		temp = 0;
		temp |= smartHomeData.isRead ? (byte) (1 << 7) : 0;
		temp |= smartHomeData.isACK ? (byte) (1 << 6) : 0;
		temp |= (smartHomeData.dataFormat & 0x3) << 4;
		temp |= (smartHomeData.keyLevel & 0x3) << 2;
		temp |= smartHomeData.encryptType & 0x03;
		bb.put(temp);
		bb.put((byte) (smartHomeData.dataSequ & 0xff));
		bb.putShort((short) (smartHomeData.msgID & 0xffff));
		bb.putShort((short) ((crcLength - 2) & 0xffff));

		byte[] headTemp = new byte[14];
		System.arraycopy(bb.array(), 0, headTemp, 0, 14);
		bb.put(crc8Caculate(headTemp));

		byte[] data = ByteBuffer.allocate(smartHomeData.data.length + 2).put(new byte[2]).put(smartHomeData.data)
				.array();

		bb.put(crc8Caculate(data));

		if (smartHomeData.keyLevel == 1) {
			byte[] mac = new byte[] {};
			byte[] key = getRandomKey((int) smartHomeData.time, mac);

			if (smartHomeData.encryptType == 0) {
				data = HloveyRc4(data, key);
			} else if (smartHomeData.encryptType == 1) {
				data = encryptUseDes(data, key);
			} else if (smartHomeData.encryptType == 2) {
				data = AESUtils.encrypt(data, key);
			}

			bb.put(data);
		}

		return bb.array();
	}

	public static SmartHomeData parseData(byte[] data) throws Exception {
		SmartHomeData shd = new SmartHomeData();

		byte[] headTemp = new byte[14];
		System.arraycopy(data, 0, headTemp, 0, 14);
		byte headCheck = crc8Caculate(headTemp);

		if (headCheck != (byte) (data[14] & 0xff)) {
			throw new Exception("头校验不正确");
		}
		if ((data[12] & 0xff) != (data.length - 18)) {
			throw new Exception("数据长度验不正确");
		}
		shd.isFin = ((data[0] >> 7) & 0x01) == 1;
		shd.isMask = ((data[0] >> 6) & 0x01) == 1;
		shd.opcode = (byte) (data[0] & 0x03);

		shd.sequence = ((data[3] << 8) | (data[2])) & 0xffff;
		shd.time = ((data[7] << 24) & 0xff000000) | ((data[6] << 16) & 0xff0000) | ((data[5] << 8) & 0xff00)
				| (data[4] & 0xff);

		shd.isRead = ((data[8] >> 7) & 0x01) == 1;
		shd.isACK = ((data[8] >> 6) & 0x01) == 1;
		shd.dataFormat = (byte) ((data[8] >> 4) & 0x03);
		shd.keyLevel = (byte) ((data[8] >> 2) & 0x03);
		shd.encryptType = (byte) (data[8] & 0x03);
		shd.dataSequ = (short) (data[9] & 0xff);
		shd.msgID = (short) (data[10] & 0xff);

		byte[] dataTemp = new byte[data.length - 16];
		System.arraycopy(data, 16, dataTemp, 0, dataTemp.length);

		if (shd.keyLevel == 1) {
			byte[] mac = new byte[] {};
			byte[] key = getRandomKey((int) shd.time, mac);

			if (shd.encryptType == 0) {
				dataTemp = HloveyRc4(dataTemp, key);
			} else if (shd.encryptType == 1) {
				dataTemp = decryptUseDes(dataTemp, key);
			} else if (shd.encryptType == 2) {
				dataTemp = AESUtils.decrypt(dataTemp, key);
			}
		}

		byte dataCheck = crc8Caculate(dataTemp);

		if (data.length < 2 || (dataCheck != (byte) (data[15] & 0xff))) {
			throw new Exception("数据校验不正确");
		}
		shd.data = new byte[dataTemp.length - 2];
		System.arraycopy(dataTemp, 2, shd.data, 0, shd.data.length);

		return shd;
	}

	private static byte[] getRandomKey(int time, byte[] mac) {
		byte[] pTime = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(time).array();
		byte timeXor = (byte) (pTime[0] ^ pTime[1] ^ pTime[2] ^ pTime[3]);

		int ntime = ~time;
		byte[] id = mac;

		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.put(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(time).array());
		bb.put(id);
		bb.put(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ntime).array());

		byte[] key = bb.array();
		for (int i = 0; i < ((timeXor & 0x03) + 1); i++) {
			for (int n = 0; n < 16; n++) {
				key[n] ^= id[(timeXor + (i + n)) & 0x07] ^ pTime[((i + n) & 0x03)];
			}
		}
		return key;
	}

	private static byte[] HloveyRc4(byte[] plaintext, byte[] key) {
		byte[] S = new byte[256];
		byte[] T = new byte[256];
		int keylen;

		if (key.length < 1 || key.length > 256) {
			return null;
		} else {
			keylen = key.length;
			for (int i = 0; i < 256; i++) {
				S[i] = (byte) i;
				T[i] = (byte) (key[i % keylen] & 0xff);
			}
			int j = 0;
			for (int i = 0; i < 256; i++) {
				j = (j + S[i] + T[i]) % 256;
				j &= 0xff;
				S[i] ^= S[j];
				S[j] ^= S[i];
				S[i] ^= S[j];
			}
		}

		byte[] ciphertext = new byte[plaintext.length];
		int i = 0, j = 0, k, t;
		for (int counter = 0; counter < plaintext.length; counter++) {
			i = (i + 1) % 256;
			j = (j + S[i]) % 256;
			j &= 0xff;
			S[i] ^= S[j];
			S[j] ^= S[i];
			S[i] ^= S[j];
			t = (S[i] + S[j]) % 256;
			t &= 0xff;
			k = S[t];
			ciphertext[counter] = (byte) (plaintext[counter] ^ k);
		}
		return ciphertext;
	}

	private static byte[] encryptUseDes(byte[] content, byte[] key) {
		try {
			if (content == null) {
				return new byte[0];
			}
			int length = content.length;
			byte j = (byte) (8 - (length % 8));

			if (j > 0 && j < 8) {
				j += 8;
				byte temp[] = new byte[length + j];
				for (int i = length; i < temp.length; i++) {
					temp[i] = j;
				}
				System.arraycopy(content, 0, temp, 0, content.length);
				;
				content = temp;
			}
			for (int i = 0; i < key.length; i++) {
				key[i] &= 0x7f;
			}
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] decryptUseDes(byte[] content, byte[] key) {
		try {
			for (int i = 0; i < key.length; i++) {
				key[i] &= 0x7f;
			}
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			byte[] result = cipher.doFinal(content);
			byte j = result[result.length - 1];
			if (j > 0 && j < 16) {
				byte temp[] = new byte[j];
				System.arraycopy(result, result.length - j, temp, 0, j);
				for (byte b : temp) {
					if (b != j) {
						return result;
					}
				}
				temp = new byte[result.length - j];
				System.arraycopy(result, 0, temp, 0, temp.length);
				result = temp;
			}
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private static short POLY = 0x131;

	private static byte crc8Caculate(byte[] data) {
		byte crc = 0, bit = 8;
		byte i;

		for (i = 0; i < data.length; i++) {
			crc ^= (data[i]);
			for (bit = 8; bit > 0; bit--) {
				if ((crc & 0x80) != 0) {
					crc = (byte) ((crc << 1) ^ POLY);
				} else {
					crc = (byte) (crc << 1);
				}
			}
		}
		return crc;
	}
}
