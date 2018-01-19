package com.vision.factorytest.bean;

import java.util.ArrayList;

/**
 * 测试结果
 * 
 * @author yangle
 */
public class TestResult extends BaseBean {

	private ArrayList<Result> result;

	public class Result {
		/**
		 * MAC地址
		 */
		private String mac;
		/**
		 * 芯片型号
		 */
		private String chip;
		/**
		 * 当前测试项目
		 */
		private String currentItem;
		/**
		 * 测试是否成功
		 */
		private String isSuccess;
		/**
		 * 时间
		 */
		private long ts;

		public String getMac() {
			return mac;
		}

		public void setMac(String mac) {
			this.mac = mac;
		}

		public String getChip() {
			return chip;
		}

		public void setChip(String chip) {
			this.chip = chip;
		}

		public String getCurrentItem() {
			return currentItem;
		}

		public void setCurrentItem(String currentItem) {
			this.currentItem = currentItem;
		}

		public String getIsSuccess() {
			return isSuccess;
		}

		public void setIsSuccess(String isSuccess) {
			this.isSuccess = isSuccess;
		}

		public long getTs() {
			return ts;
		}

		public void setTs(long ts) {
			this.ts = ts;
		}

		@Override
		public String toString() {
			return "Result [mac=" + mac + ", chip=" + chip + ", currentItem=" + currentItem + ", isSuccess=" + isSuccess
					+ ", ts=" + ts + "]";
		}
	}

	public ArrayList<Result> getResult() {
		return result;
	}

	public void setResult(ArrayList<Result> result) {
		this.result = result;
	}
}
