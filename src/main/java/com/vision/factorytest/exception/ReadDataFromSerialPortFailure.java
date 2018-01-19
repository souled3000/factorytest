package com.vision.factorytest.exception;

public class ReadDataFromSerialPortFailure extends Exception {

	private static final long serialVersionUID = 1L;

	public ReadDataFromSerialPortFailure() {
	}

	@Override
	public String toString() {
		return "从串口读取数据失败！";
	}
}
