package com.vision.factorytest.ui;

import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;

import javax.swing.*;

/**
 * 模块升级
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class DeviceResetFrame extends BaseFrame {

	public DeviceResetFrame() {
		initFrame();
		initMenu();
		initComponents();

		setTitle("模块恢复出厂");
	}

}
