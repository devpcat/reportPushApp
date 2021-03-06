package com.dc.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 生成excel文件
 * 可以指定2003或者2007版本
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-2-23
 *    fix->1.
 *         2.
 * </pre>
 */
public class ExcelWriter {

	private Workbook wb;
	private Sheet sheet;
	private CellStyle style;
	private int rownum;
	private List<Integer> colMaxLenList;

	//private int maxColNum = -1;

	public ExcelWriter(boolean excel2003) {

		//SXSSFWorkbook workbook=new SXSSFWorkbook();

		// 创建一个excel文件
		wb = getWorkbok(excel2003);
		// 创建一个样式对象
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);// 水平居中
		style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直居中
		// style.setWrapText(true);//自动换行
		DataFormat df = wb.createDataFormat();
		style.setDataFormat(df.getFormat("@"));
	}

	public ExcelWriter(CellStyle style, boolean excel2003) {
		// 创建一个excel文件
		wb = getWorkbok(excel2003);
		// 创建一个sheet
		this.style = style;
	}

	/**
	 * 根据指定的模板文件生成对象
	 *
	 * @param templetefile
	 */
	public ExcelWriter(String templetefile) {
		wb = getWorkbok(templetefile);
	}

	/**
	 * 创建一个表单
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午03:58:41
	 * @param sheetName
	 */
	public void addSheet(String sheetName) {

		// 第二次调用时设置前一次
		autoSizeColumn();

		// 创建一个sheet
		sheet = wb.createSheet(sheetName);
		rownum = 0;
		//maxColNum = -1;// 复位
		colMaxLenList = new ArrayList<Integer>();
	}

	private void autoSizeColumn() {
		if (null == this.colMaxLenList)
			return;
		if (sheet instanceof SXSSFSheet)
			((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
		for (int i = 0; i < this.colMaxLenList.size(); i++) {
			sheet.autoSizeColumn(i);
			int currwid = sheet.getColumnWidth(i);
			int nextwid = 275 * this.colMaxLenList.get(i);
			if (currwid < nextwid && nextwid < 5000)
				sheet.setColumnWidth(i, nextwid);
		}
	}

	/**
	 * 增加一行记录
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午04:02:17
	 * @param strarr
	 */
	public void addRow(String[] strarr) {
		// 创建一行
		Row row = sheet.createRow(this.rownum);

		int j = 0;
		for (String elem2 : strarr) {
			// 创建一个单元格
			Cell cell = row.createCell(j);
			cell.setCellStyle(style);
			cell.setCellValue(elem2);
			j++;

			//设置宽度
			int len = null == elem2 ? 0 : elem2.getBytes().length;
			if (j > this.colMaxLenList.size()) {
				this.colMaxLenList.add(len);
			} else {
				if (len > this.colMaxLenList.get(j - 1)) {
					this.colMaxLenList.set(j - 1, len);
				}
			}
		}
		this.rownum++;

		//		if (j > maxColNum)
		//			maxColNum = j;
	}

	/**
	 * 增加多行记录
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午04:09:24
	 * @param list
	 */
	public void addRow(List<String[]> list) {
		for (String elem[] : list)
			addRow(elem);
	}

	/**
	 * 输出excel2007（xlsx）文件
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午04:17:48
	 * @param filePath
	 * @throws IOException
	 */
	public void write(String filePath) throws IOException {

		// 最后一个sheet的自适应
		autoSizeColumn();

		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath);
			wb.write(outputStream);
			outputStream.flush();
		} finally {
			if (null != outputStream)
				outputStream.close();
		}
	}

	public static final String EXCEL_2003 = "xls";
	public static final String EXCEL_2007 = "xlsx";

	public Workbook getWorkbok(String filename) {
		Workbook wb = null;
		if (null == filename) {
			wb = new SXSSFWorkbook();
		} else if (filename.endsWith(EXCEL_2003)) { // Excel?0?22003
			wb = new HSSFWorkbook();
		} else if (filename.endsWith(EXCEL_2007)) { // Excel 2007/2010
			wb = new SXSSFWorkbook();
		} else {
			wb = new SXSSFWorkbook();
		}
		return wb;
	}

	public Workbook getWorkbok(boolean excel2003) {
		Workbook wb = null;
		if (excel2003) { // Excel?0?22003
			wb = new HSSFWorkbook();
		} else { // Excel 2007/2010
			wb = new SXSSFWorkbook(1000);//由XSSFWorkbook改为SXSSFWorkbook
		}
		return wb;
	}

}
