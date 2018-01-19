package com.vision.factorytest.ui;

import com.vision.factorytest.GlobalVariable;
import com.vision.factorytest.manager.SendMessageManager;
import com.vision.factorytest.utils.LogUtils;
import com.vision.factorytest.utils.TestResultAnalyze;

/**
 * 通讯质量测试
 * 
 * @author yangle
 */
@SuppressWarnings("all")
public class RSSITestFrame extends BaseFrame {

	public RSSITestFrame() {
		initFrame();
		initMenu();
		initComponents();

		setTitle("通信质量测试");
	}

	public void RSSITestResult(String chip) {
		LogUtils.d("通讯质量测试数据", GlobalVariable.RSSITestData.toString());

		// 设备MAC
		String mac = macInputField.getText();

		if (TestResultAnalyze.RSSITest(GlobalVariable.RSSITestData)) {
			setDataView("\r\n通讯质量测试成功");

			// 上传测试结果
			SendMessageManager.defaultManage().uploadTestResult(mac, true, 2, chip);
		} else {
			setDataView("\r\n通讯质量测试失败");

			SendMessageManager.defaultManage().uploadTestResult(mac, false, 2, chip);
		}

		GlobalVariable.RSSITestNumber = 0;
		GlobalVariable.RSSITestData.clear();
	}
}
