package com.vision.factorytest.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.vision.factorytest.Constant;
import com.vision.factorytest.bean.TestResult.Result;

/**
 * 表格操作
 * 
 * @author yangle
 */
public class ExcelUtils {

	/**
	 * 创建Excel
	 * 
	 * @param list
	 *            数据
	 */
	public static void createExcel(List<Result> results) {
		// 创建一个Excel文件
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 创建一个工作表
		HSSFSheet sheet = workbook.createSheet("工厂测试");
		// 添加表头行
		HSSFRow hssfRow = sheet.createRow(0);
		// 设置单元格格式居中
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 设置单元格宽度
		sheet.setColumnWidth(0, 20 * 256);
		sheet.setColumnWidth(1, 20 * 256);
		sheet.setColumnWidth(2, 20 * 256);
		sheet.setColumnWidth(3, 20 * 256);
		sheet.setColumnWidth(4, 20 * 256);

		// 添加表头内容
		HSSFCell headCell = hssfRow.createCell(0);
		headCell.setCellValue("芯片型号");
		headCell.setCellStyle(cellStyle);

		headCell = hssfRow.createCell(1);
		headCell.setCellValue("MAC地址");
		headCell.setCellStyle(cellStyle);

		headCell = hssfRow.createCell(2);
		headCell.setCellValue("测试项目");
		headCell.setCellStyle(cellStyle);

		headCell = hssfRow.createCell(3);
		headCell.setCellValue("是否通过");
		headCell.setCellStyle(cellStyle);

		headCell = hssfRow.createCell(4);
		headCell.setCellValue("时间");
		headCell.setCellStyle(cellStyle);

		// 添加数据内容
		for (int i = 0; i < results.size(); i++) {
			hssfRow = sheet.createRow((int) i + 1);
			Result result = results.get(i);

			// 创建单元格，并设置值
			HSSFCell cell = hssfRow.createCell(0);
			cell.setCellValue(result.getChip());
			cell.setCellStyle(cellStyle);

			cell = hssfRow.createCell(1);
			cell.setCellValue(result.getMac());
			cell.setCellStyle(cellStyle);

			cell = hssfRow.createCell(2);
			String testItem = result.getCurrentItem();
			if (Constant.TestItem.WRITE_FACTORY_DISTRICT.equals(testItem)) {
				cell.setCellValue("写模块工厂区");
			} else if (Constant.TestItem.RSSI_TEST.equals(testItem)) {
				cell.setCellValue("通讯质量测试");
			} else if (Constant.TestItem.DEVICE_UPGRADE.equals(testItem)) {
				cell.setCellValue("模块升级");
			}
			cell.setCellStyle(cellStyle);

			cell = hssfRow.createCell(3);
			cell.setCellValue(result.getIsSuccess().equals("1") ? "通过" : "未通过");
			cell.setCellStyle(cellStyle);

			cell = hssfRow.createCell(4);
			cell.setCellValue(TimeUtils.dateToString(result.getTs()));
			cell.setCellStyle(cellStyle);
		}

		// 保存Excel文件
		try {
			OutputStream outputStream = new FileOutputStream("./工厂测试结果.xls");
			workbook.write(outputStream);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
