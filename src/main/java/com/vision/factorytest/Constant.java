package com.vision.factorytest;

/**
 * 常量
 * 
 * @author yangle
 */
public class Constant {

	/**
	 * 版本号
	 */
	public static final String VERSION = "1.5";

	/**
	 * 网络访问地址
	 */
	public static final String SERVER_URL = "http://193.168.1.115:7070";

	/**
	 * 网络访问接口
	 */
	public static class UrlOrigin {
		/**
		 * 上传测试结果
		 */
		public static final String upload_test_result = "/backstage/xav/imp";
		/**
		 * 获取测试结果
		 */
		public static final String get_test_result = "/backstage/xav/query";
	}

	/**
	 * 测试项目
	 */
	public static class TestItem {
		/**
		 * 写模块工厂区
		 */
		public static final String WRITE_FACTORY_DISTRICT = "writeFactoryDistrict";
		/**
		 * 通信质量测试
		 */
		public static final String RSSI_TEST = "RSSITest";
		/**
		 * 模块程序升级
		 */
		public static final String DEVICE_UPGRADE = "deviceUpgrade";

		public static final String DEVICE_RESET = "deviceReset";
	}
}
