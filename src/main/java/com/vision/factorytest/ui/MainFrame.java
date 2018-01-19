package com.vision.factorytest.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.JButton;

import com.vision.factorytest.Constant;
import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.utils.LogUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * 程序主界面，用于选择测试内容
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class MainFrame extends BaseFrame {

	private JButton writeFactoryDistrict = new JButton("写模块工厂区");
	private JButton RSSITest = new JButton("通讯质量测试");
	private JButton deviceUpgrade = new JButton("模块升级区");
	private JButton deviceReset = new JButton("模块恢复出厂区");

	public MainFrame() {
		initFrame();
		initView();
		actionListener();
	}

	private void initView() {
		writeFactoryDistrict.setFocusable(false);
		writeFactoryDistrict.setBackground(Color.LIGHT_GRAY);
		writeFactoryDistrict.setBounds(150, 70, 300, 30);
		add(writeFactoryDistrict);

		RSSITest.setFocusable(false);
		RSSITest.setBackground(Color.LIGHT_GRAY);
		RSSITest.setBounds(150, 140, 300, 30);
		add(RSSITest);

		deviceUpgrade.setFocusable(false);
		deviceUpgrade.setBackground(Color.LIGHT_GRAY);
		deviceUpgrade.setBounds(150, 210, 300, 30);
		add(deviceUpgrade);

		deviceReset.setFocusable(false);
		deviceReset.setBackground(Color.LIGHT_GRAY);
		deviceReset.setBounds(150, 280, 300, 30);
		add(deviceReset);
	}

	private void actionListener() {
		writeFactoryDistrict.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						// 写模块工厂区
						GlobalVariable.currentTestItem = Constant.TestItem.WRITE_FACTORY_DISTRICT;
						WriteFactoryDistrictFrame writeFactoryDistrictFrame = new WriteFactoryDistrictFrame();
						writeFactoryDistrictFrame.setVisible(true);
//						dispose();
					}
				});
			}
		});

		RSSITest.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						// 通信质量测试
						GlobalVariable.currentTestItem = Constant.TestItem.RSSI_TEST;
						RSSITestFrame rSSITestFrame = new RSSITestFrame();
						rSSITestFrame.setVisible(true);
//						dispose();
					}
				});
			}
		});

		deviceUpgrade.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						// 模块升级
						GlobalVariable.currentTestItem = Constant.TestItem.DEVICE_UPGRADE;
						DeviceUpgradeFrame deviceUpgradeFrame = new DeviceUpgradeFrame();
						deviceUpgradeFrame.setVisible(true);
//						dispose();
					}
				});
			}
		});

		deviceReset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						// 模块恢复
						GlobalVariable.currentTestItem = Constant.TestItem.DEVICE_RESET;
						DeviceResetFrame deviceResetFrame = new DeviceResetFrame();
						deviceResetFrame.setVisible(true);
//						dispose();
					}
				});
			}
		});
	}

	public static void main(String args[]) {
		System.out.println(System.getProperty("java.library.path"));
		System.out.println(System.getProperty("user.dir") + "\\log4j.properties");
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log4j.properties");
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				initGlobalFont();
				new MainFrame().setVisible(true);
			}
		});
	}
}
