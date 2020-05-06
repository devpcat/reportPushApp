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
 * TODO �����������
 *
 * <pre>
 * ����excel�ļ�
 * ����ָ��2003����2007�汾
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

		// ����һ��excel�ļ�
		wb = getWorkbok(excel2003);
		// ����һ����ʽ����
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);// ˮƽ����
		style.setVerticalAlignment(VerticalAlignment.CENTER);// ��ֱ����
		// style.setWrapText(true);//�Զ�����
		DataFormat df = wb.createDataFormat();
		style.setDataFormat(df.getFormat("@"));
	}

	public ExcelWriter(CellStyle style, boolean excel2003) {
		// ����һ��excel�ļ�
		wb = getWorkbok(excel2003);
		// ����һ��sheet
		this.style = style;
	}

	/**
	 * ����ָ����ģ���ļ����ɶ���
	 *
	 * @param templetefile
	 */
	public ExcelWriter(String templetefile) {
		wb = getWorkbok(templetefile);
	}

	/**
	 * ����һ������
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 ����03:58:41
	 * @param sheetName
	 */
	public void addSheet(String sheetName) {

		// �ڶ��ε���ʱ����ǰһ��
		autoSizeColumn();

		// ����һ��sheet
		sheet = wb.createSheet(sheetName);
		rownum = 0;
		//maxColNum = -1;// ��λ
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
	 * ����һ�м�¼
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 ����04:02:17
	 * @param strarr
	 */
	public void addRow(String[] strarr) {
		// ����һ��
		Row row = sheet.createRow(this.rownum);

		int j = 0;
		for (String elem2 : strarr) {
			// ����һ����Ԫ��
			Cell cell = row.createCell(j);
			cell.setCellStyle(style);
			cell.setCellValue(elem2);
			j++;

			//���ÿ���
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
	 * ���Ӷ��м�¼
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 ����04:09:24
	 * @param list
	 */
	public void addRow(List<String[]> list) {
		for (String elem[] : list)
			addRow(elem);
	}

	/**
	 * ���excel2007��xlsx���ļ�
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 ����04:17:48
	 * @param filePath
	 * @throws IOException
	 */
	public void write(String filePath) throws IOException {

		// ���һ��sheet������Ӧ
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
			wb = new SXSSFWorkbook(1000);//��XSSFWorkbook��ΪSXSSFWorkbook
		}
		return wb;
	}

}