package com.vision.factorytest.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vision.factorytest.Constant;
import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.device.DeviceConstant;
import com.vision.factorytest.manager.ReceiveMessageManager;
import com.vision.factorytest.manager.SerialPortManager;
import com.vision.factorytest.utils.CommUtils;
import com.vision.factorytest.utils.FormCheckUtils;
import com.vision.factorytest.utils.ShowUtils;

import gnu.io.SerialPort;

/**
 * Frame基类
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class BaseFrame extends JFrame {
	private static final Logger log = LoggerFactory.getLogger(BaseFrame.class);
	/**
	 * 程序界面宽度
	 */
	private final int WIDTH = 600;

	/**
	 * 程序界面高度
	 */
	private final int HEIGHT = 420;

	public JTextArea dataView = new JTextArea();
	public JScrollPane scrollDataView = new JScrollPane(dataView);
	public JLabel chipLabel = new JLabel("芯片型号");
	public JComboBox chipChoice = new JComboBox();
	public JLabel macLabel = new JLabel("MAC地址");
	public JTextField macInputField = new JTextField();
	public JSeparator separator = new JSeparator();
	public JLabel serialportStatusLabel = new JLabel();
	public JLabel chipModelLabel = new JLabel();
	public SerialportSetDialog serialportSetDialog = new SerialportSetDialog();

	public SerialPort serialPort;
	private String mac = "";

	private static BaseFrame frame;

	public static BaseFrame getFrame() {
		return frame;
	}

	public BaseFrame() {
		frame = this;

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				if (serialPort != null) {
					// 程序退出时关闭串口释放资源
					System.out.println("程序退出关闭了串口");
					SerialPortManager.closePort(serialPort);
				}
				System.exit(0);
			}
		});
	}

	/**
	 * 初始化窗口
	 */
	public void initFrame() {
		// 关闭窗口
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		setLayout(null);

		setTitle("工厂测试 v" + Constant.VERSION);
	}

	/**
	 * 初始化菜单栏
	 */
	public void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu setMenu = new JMenu(" 设置");
		JMenuItem serialportSet = new JMenuItem("串口设置");
		JMenuItem clearView = new JMenuItem("初始化");
		JMenuItem exit = new JMenuItem("退出");

		serialportSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
		setMenu.add(serialportSet);
		setMenu.addSeparator();
		clearView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
		setMenu.add(clearView);
		setMenu.addSeparator();
		setMenu.add(exit);

		menuBar.add(setMenu);
		menuBar.setBounds(0, 0, 600, 30);
		add(menuBar);

		// 点串口设置时，显示dialog,设置了芯片型号，波特率
		serialportSet.addActionListener(new SerialportConnectListener());

		clearView.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dataView.setText("");
				macInputField.setText("");
				ReceiveMessageManager.defaultManage().reset();
			}
		});

		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
	}

	/**
	 * 初始化控件
	 */
	public void initComponents() {
		// 数据显示
		dataView.setFocusable(false);
		dataView.setMargin(new Insets(5, 5, 5, 5));
		scrollDataView.setBounds(10, 30, 575, 280);
		add(scrollDataView);

		chipLabel.setBounds(10, 325, 70, 20);
		add(chipLabel);

		chipChoice.setFocusable(false);
		chipChoice.setBounds(85, 325, 100, 20);
		add(chipChoice);

		macLabel.setBounds(210, 325, 70, 20);
		add(macLabel);

		macInputField.setBounds(285, 325, 160, 20);
		add(macInputField);

		separator.setForeground(Color.lightGray);
		separator.setBounds(10, 360, 575, 1);
		add(separator);

		serialportStatusLabel.setBounds(10, 368, 100, 20);
		add(serialportStatusLabel);

		chipModelLabel.setBounds(120, 368, 200, 20);
		add(chipModelLabel);

		chipChoice.addItem("未选择");
		if (Constant.TestItem.DEVICE_RESET.equals(GlobalVariable.currentTestItem)) {
			chipChoice.addItem(DeviceConstant.Chip._7681);
			chipChoice.addItem(DeviceConstant.Chip._7688);
		} else {
			// 2530无写模块工厂区工序
			if (!Constant.TestItem.WRITE_FACTORY_DISTRICT.equals(GlobalVariable.currentTestItem)) {
				chipChoice.addItem(DeviceConstant.Chip._2530);
			}
			chipChoice.addItem(DeviceConstant.Chip._2630);
			chipChoice.addItem(DeviceConstant.Chip._7681);
			chipChoice.addItem(DeviceConstant.Chip._7688);
			chipChoice.addItem(DeviceConstant.Chip._1310_device);
		}
		if (Constant.TestItem.RSSI_TEST.equals(GlobalVariable.currentTestItem)) {
			chipChoice.addItem(DeviceConstant.Chip._1310_gateway);
		}
		chipChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String chip = (String) chipChoice.getSelectedItem();
				if (!chip.equals("未选择")) {
					setClipStatus(chip);
					GlobalVariable.currentChip = chip;
					// 获取串口名称
					String commName = (String) serialportSetDialog.getCommName();

					// 检查串口名称是否获取正确
					if (commName == null || commName.equals("")) {
						ShowUtils.warningMessage("没有搜索到有效串口！");
					} else {
						// 当前测试项目
						String currentTestItem = GlobalVariable.currentTestItem;
						// 当前芯片型号
						String currentChip = GlobalVariable.currentChip;

						if (serialPort != null) {
							// 程序退出时关闭串口释放资源
							SerialPortManager.closePort(serialPort);
						}
						// 2530芯片、7688芯片波特率57600
						// 通信质量测试，7681芯片波特率为57600
						// 2630芯片、7681芯片波特率为115200
						if (DeviceConstant.Chip._1310_gateway.equals(currentChip) || DeviceConstant.Chip._2530.equals(currentChip) || DeviceConstant.Chip._7688.equals(currentChip)
								|| (Constant.TestItem.RSSI_TEST.equals(currentTestItem) && DeviceConstant.Chip._7681.equals(currentChip))) {
							serialPort = SerialPortManager.openPort(commName, DeviceConstant.BAUDRATE_57600);
							log.info("波特率：{}", "57600");
						}
						if (DeviceConstant.Chip._1310_device.equals(currentChip) || DeviceConstant.Chip._2630.equals(currentChip) || (!Constant.TestItem.RSSI_TEST.equals(currentTestItem) && DeviceConstant.Chip._7681.equals(currentChip))) {
							serialPort = SerialPortManager.openPort(commName, DeviceConstant.BAUDRATE_115200);
							log.info("波特率：{}", "115200");
						}
						if (serialPort != null) {
							setSerialportStatus(true, commName);
						}
					}

					SerialPortManager.addListener(serialPort);

				} else {
					setClipStatus("");
				}
				macInputField.setText("");
			}
		});

		macInputField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				mac = macInputField.getText();

				if (mac.length() == 24) { // 二维码数据
					mac = CommUtils.getMacFromCode(mac);
					if (FormCheckUtils.isMac(mac)) {
						new Thread() {
							public void run() {
								macInputField.setText(mac);
								if (Constant.TestItem.WRITE_FACTORY_DISTRICT.equals(GlobalVariable.currentTestItem) && !CommUtils.isFileExists(mac)) {
									ShowUtils.errorMessage("未找到License文件");
								}
							};
						}.start();
					}
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

			}
		});

		setSerialportStatus(false, null);
		setClipStatus("");
	}

	/**
	 * 连接串口设备监听
	 */
	public class SerialportConnectListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			serialportSetDialog.show();
			serialportSetDialog.SerialportConnect(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// 获取串口名称
					String commName = (String) serialportSetDialog.getCommName();

					// 关闭串口设置dialog
					serialportSetDialog.dispose();

					// 检查串口名称是否获取正确
					if (commName == null || commName.equals("")) {
						ShowUtils.warningMessage("没有搜索到有效串口！");
					} else {
						// 当前测试项目
						String currentTestItem = GlobalVariable.currentTestItem;
						// 当前芯片型号
						String currentChip = GlobalVariable.currentChip;

						if (serialPort != null) {
							// 程序退出时关闭串口释放资源
							SerialPortManager.closePort(serialPort);
						}
						// 2530芯片、7688芯片波特率57600
						// 通信质量测试，7681芯片波特率为57600
						// 2630芯片、7681芯片波特率为115200
						int baud = 0;
						if ( DeviceConstant.Chip._1310_gateway.equals(currentChip)
								|| (Constant.TestItem.RSSI_TEST.equals(currentTestItem) && DeviceConstant.Chip._7681.equals(currentChip))
								|| (Constant.TestItem.DEVICE_RESET.equals(currentTestItem) && DeviceConstant.Chip._7681.equals(currentChip))) {
							log.info("波特率：{}", "57600");
							baud = DeviceConstant.BAUDRATE_57600;
						}
						if (DeviceConstant.Chip._1310_device.equals(currentChip) ||DeviceConstant.Chip._2630.equals(currentChip)
								|| (!Constant.TestItem.RSSI_TEST.equals(currentTestItem) && !Constant.TestItem.DEVICE_RESET.equals(currentTestItem) && DeviceConstant.Chip._7681.equals(currentChip))) {
							baud = DeviceConstant.BAUDRATE_115200;
							log.info("波特率：{}", "115200");
						}
						serialPort = SerialPortManager.openPort(commName, baud);
						if (serialPort != null) {
							setSerialportStatus(true, commName);
						}
					}

					SerialPortManager.addListener(serialPort);
				}
			});
		}
	}

	/**
	 * 设置串口状态
	 * 
	 * @param isConnection
	 *            串口是否连接
	 */
	public void setSerialportStatus(boolean isConnection, String commName) {
		if (isConnection) {
			serialportStatusLabel.setText(commName + "已连接");
			serialportStatusLabel.setForeground(Color.black);
		} else {
			serialportStatusLabel.setText("串口未连接");
			serialportStatusLabel.setForeground(Color.red);
		}
	}

	/**
	 * 设置当前芯片型号
	 * 
	 * @param chipModel
	 *            芯片型号
	 */
	public void setClipStatus(String chipModel) {
		if (!chipModel.equals("")) {
			chipModelLabel.setText("芯片型号" + chipModel);
			chipModelLabel.setForeground(Color.black);
		} else {
			chipModelLabel.setText("未选择芯片型号");
			chipModelLabel.setForeground(Color.red);
		}
	}

	/**
	 * 数据显示
	 * 
	 * @param content
	 *            数据
	 */
	public void setDataView(String content) {
		dataView.append(content + "\r\n");
		dataView.setCaretPosition(dataView.getText().length());
	}

	/**
	 * 清除数据显示
	 */
	public void clearDataView() {
		dataView.setText("");
	}

	/**
	 * 统一设置字体
	 */
	public static void initGlobalFont() {
		FontUIResource fontRes = new FontUIResource(new Font("微软雅黑", Font.PLAIN, 15));
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, fontRes);
			}
		}
	}
}
