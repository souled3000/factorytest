package com.vision.factorytest.ui;

import javax.swing.JLabel;

import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;

/**
 * 模块升级
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class DeviceUpgradeFrame extends BaseFrame {

	public JLabel txLabel = new JLabel();

	public DeviceUpgradeFrame() {
		initFrame();
		initMenu();
		initComponents();

		setTitle("模块升级");
		txLabel.setBounds(545, 368, 50, 20);
		add(txLabel);
	}

	public void setTx(int count) {
		String chip = GlobalVariable.currentChip;
		if (DeviceConstant.Chip._2530.equals(chip)) {
			txLabel.setText((int) (count / 6.3) + "%");

		} else if (DeviceConstant.Chip._2630.equals(chip)) {
			txLabel.setText((int) (count / 5.1) + "%");

		} else if (DeviceConstant.Chip._7681.equals(chip)) {
			txLabel.setText((int) (count / 19.99) + "%");
		}
	}
}
