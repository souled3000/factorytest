package com.vision.factorytest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 时间工具
 * 
 * @author yangle
 */
public class TimeUtils {

	private static Timer timer;

	/**
	 * 时间转换成字符串，默认为"yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param time
	 *            时间
	 */
	public static String dateToString(long time) {
		return dateToString(time, "yyyy.MM.dd HH:mm:ss");
	}

	/**
	 * 时间转换成字符串，指定格式
	 * 
	 * @param time
	 *            时间
	 * @param format
	 *            时间格式
	 */
	public static String dateToString(long time, String format) {
		Date date = new Date(time);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * 启动计时器
	 * 
	 * @param delay
	 *            超时时间
	 * @param iOvertimeExecute
	 *            回调接口
	 */
	public static void startTimer(long delay, final IOvertimeExecute iOvertimeExecute) {
		LogUtils.d("定时器", "启动计时器");

		stopTimer();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				iOvertimeExecute.execute();
			}
		}, delay);
	}

	/**
	 * 启动计时器
	 * 
	 * @param delay
	 *            超时时间
	 * @param period
	 *            循环执行间隔
	 * @param iOvertimeExecute
	 *            回调接口
	 */
	public static void startTimer(long delay, long period, final IOvertimeExecute iOvertimeExecute) {
		LogUtils.d("定时器", "启动计时器");

		stopTimer();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				iOvertimeExecute.execute();
			}
		}, delay, period);
	}

	/**
	 * 停止计时器
	 */
	public static void stopTimer() {
		LogUtils.d("定时器", "关闭计时器");

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	/**
	 * 操作超时动作执行接口
	 */
	public interface IOvertimeExecute {

		/**
		 * 执行操作
		 */
		void execute();
	}
}
