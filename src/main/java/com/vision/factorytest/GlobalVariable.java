package com.vision.factorytest;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局变量
 * 
 * @author yangle
 */
public class GlobalVariable {

	private GlobalVariable() {
	}

	/**
	 * 当前测试项目
	 */
	public static String currentTestItem = "";

	/**
	 * 当前芯片
	 */
	public static String currentChip = "";

	/**
	 * 通讯质量测试——数据包发送数量
	 */
	public static int RSSITestNumber = 0;

	/**
	 * 通讯质量测试——数据包
	 */
	public static List<Integer> RSSITestData = new ArrayList<Integer>();
}
