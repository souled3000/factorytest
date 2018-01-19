package com.vision.factorytest.ui;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.vision.factorytest.manager.SerialPortManager;
import com.vision.factorytest.utils.ShowUtils;

/**
 * 串口设置
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class SerialportSetDialog extends JDialog {

	/**
	 * Dialog界面宽度
	 */
	private static final int WIDTH = 300;

	/**
	 * Dialog界面高度
	 */
	private static final int HEIGHT = 200;

	private JLabel serialPortLabel;
	private JComboBox commChoice;
	private JButton scanButton;
	private JButton operateButton;

	private List<String> commList = null;

	public SerialportSetDialog() {
		initDialog();
		initComponents();
		initData();
		actionListener();
	}

	private void initDialog() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		setLayout(null);
		setTitle("串口设置");
	}

	private void initComponents() {
		serialPortLabel = new JLabel("串口");
		commChoice = new JComboBox();
		scanButton = new JButton("扫描");
		operateButton = new JButton("连接");

		serialPortLabel.setBounds(65, 50, 40, 20);
		add(serialPortLabel);

		commChoice.setFocusable(false);
		commChoice.setBounds(115, 50, 120, 20);
		add(commChoice);

		scanButton.setFocusable(false);
		scanButton.setBounds(65, 90, 80, 20);
		add(scanButton);

		operateButton.setFocusable(false);
		operateButton.setBounds(155, 90, 80, 20);
		add(operateButton);
	}

	private void initData() {
		// 检查是否有可用串口，有则加入选项中
		commList = SerialPortManager.findPort();
		if (commList == null || commList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			commChoice.removeAllItems();
			for (String s : commList) {
				commChoice.addItem(s);
			}
		}
	}

	private void actionListener() {
		scanButton.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				initData();
			}
		});
	}

	/**
	 * 串口连接
	 * 
	 * @param actionListener
	 *            连接按钮监听
	 */
	public void SerialportConnect(ActionListener actionListener) {
		operateButton.addActionListener(actionListener);
	}

	/**
	 * 获取串口名称
	 * 
	 * @return 串口名称
	 */
	public String getCommName() {
		return (String) commChoice.getSelectedItem();
	}
}
