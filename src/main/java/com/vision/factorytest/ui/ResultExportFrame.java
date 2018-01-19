package com.vision.factorytest.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.vision.factorytest.manager.SendMessageManager;

/**
 * 结果导出
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class ResultExportFrame extends BaseFrame {

	private JButton resultExport = new JButton("导出测试结果");

	public ResultExportFrame() {
		initFrame();

		resultExport.setFocusable(false);
		resultExport.setBackground(Color.LIGHT_GRAY);
		resultExport.setBounds(150, 180, 300, 30);
		add(resultExport);

		resultExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SendMessageManager.defaultManage().getTestResult();
			}
		});
	}

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				initGlobalFont();
				new ResultExportFrame().setVisible(true);
			}
		});
	}
}
