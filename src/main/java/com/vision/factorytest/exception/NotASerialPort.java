package com.vision.factorytest.exception;

public class NotASerialPort extends Exception {

	private static final long serialVersionUID = 1L;

	public NotASerialPort() {
	}

	@Override
	public String toString() {
		return "端口指向的设备不是串口类型！";
	}
}
