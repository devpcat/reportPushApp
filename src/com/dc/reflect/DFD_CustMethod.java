package com.dc.reflect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import com.dc.util.Tools;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.dc.repo.ConfigReader;
import com.dc.repo.ResultBean;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 大发电取暖费对账明细表
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-27
 *    fix->1.
 *         2.
 * </pre>
 */
public class DFD_CustMethod {

	private final static Logger log = Logger.getLogger(DFD_CustMethod.class);
	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";

	/**
	 * 0-日期 1-数据源 2-模板路径 3-输出路径
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-28 上午11:34:28
	 * @param args
	 * @throws Exception
	 */
	public static void CreateExcel(String[] args, ResultBean resBean) throws Exception {
		OutputStream out = null;
		JConnection jc = null;

		String workdate = com.dc.repo.Tools.translateString(args[0]);

		try {
			jc = new JConnection(args[1]);
			//读取模板
			Workbook workBook = getWorkbok(new File("./config/" + args[2]));
			Sheet sheet = workBook.getSheetAt(0);
			Row row1 = sheet.getRow(1);
			//修改交易日期
			row1.getCell(0).setCellValue("交易日期：" + workdate);
			CellStyle cellStyle2A = row1.getCell(0).getCellStyle();

			//将cellstyble获取并存储
			Row row3 = sheet.getRow(3);
			CellStyle cellStyleA = row3.getCell(0).getCellStyle();
			CellStyle cellStyleB = row3.getCell(1).getCellStyle();
			CellStyle cellStyleC = row3.getCell(2).getCellStyle();
			CellStyle cellStyleD = row3.getCell(3).getCellStyle();
			CellStyle cellStyleE = row3.getCell(4).getCellStyle();
			CellStyle cellStyleF = row3.getCell(5).getCellStyle();
			CellStyle cellStyleG = row3.getCell(6).getCellStyle();
			CellStyle cellStyleH = row3.getCell(7).getCellStyle();
			System.out.println(cellStyleA);

			//执行查询sql
			List<String[]> list = jc.queryAsList("select t.khh,t.nd,t.jfje,t.wyj,t.tfje,t.amount,t.yhlx "
					+ "from dldfahf_trxinfos t " + "where t.dzflag='0' and t.TRXMODE='0' and t.workdate='" + workdate + "'");
			list.remove(0);//删除标题头
			//写入excel数据
			int i = 3;

			int jmnum = 0;
			int gjnum = 0;
			int jmys = 0;
			int gjys = 0;
			int jmwy = 0;
			int gjwy = 0;
			int jmtt = 0;
			int gjtt = 0;
			int jmss = 0;
			int gjss = 0;

			for (String[] elem : list) {
				//0-用户号 1-年度 2-缴费金额【应收金额】 3-违约金 4-退费金额 5-实收金额 6-用户类型
				Row row4 = sheet.createRow(i++);
				Cell cellA = row4.createCell(0);
				Cell cellB = row4.createCell(1);
				Cell cellC = row4.createCell(2);
				Cell cellD = row4.createCell(3);
				Cell cellE = row4.createCell(4);
				Cell cellF = row4.createCell(5);
				Cell cellG = row4.createCell(6);
				Cell cellH = row4.createCell(7);
				cellA.setCellStyle(cellStyleA);
				cellB.setCellStyle(cellStyleB);
				cellC.setCellStyle(cellStyleC);
				cellD.setCellStyle(cellStyleD);
				cellE.setCellStyle(cellStyleE);
				cellF.setCellStyle(cellStyleF);
				cellG.setCellStyle(cellStyleG);
				cellH.setCellStyle(cellStyleH);
				cellA.setCellValue(i - 3);
				cellB.setCellValue(elem[0]);
				cellC.setCellValue(Integer.parseInt(elem[1]));
				cellD.setCellValue(Integer.parseInt(elem[2]) / 100.0);
				cellE.setCellValue(Integer.parseInt(elem[3]) / 100.0);
				cellF.setCellValue(Integer.parseInt(elem[4]) / 100.0);
				cellG.setCellValue(Integer.parseInt(elem[5]) / 100.0);
				cellH.setCellValue(elem[6]);
				//0-用户号 1-年度 2-缴费金额【应收金额】 3-违约金 4-退费金额 5-实收金额 6-用户类型
				if ("居民".equals(elem[6].trim())) {
					//居民
					jmnum++;
					jmys += Integer.parseInt(elem[2]);
					jmwy += Integer.parseInt(elem[3]);
					jmtt += Integer.parseInt(elem[4]);
					jmss += Integer.parseInt(elem[5]);
				} else {
					//公建
					gjnum++;
					gjys += Integer.parseInt(elem[2]);
					gjwy += Integer.parseInt(elem[3]);
					gjtt += Integer.parseInt(elem[4]);
					gjss += Integer.parseInt(elem[5]);
				}
			}//for

			if (3 == i)
				i++;

			//汇总第一行
			//居民应收金额合计：    居民违约金额合计：    居民退费金额合计：    居民实收合计：
			Row row4 = sheet.createRow(i++);
			Cell cell1 = row4.createCell(0);//居民应收金额合计：
			Cell cell2 = row4.createCell(2);//居民违约金额合计：
			Cell cell3 = row4.createCell(4);// 居民退费金额合计：
			Cell cell4 = row4.createCell(6);//居民实收合计：

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("居民应收金额合计：" + Tools.getAmountByYuan(String.valueOf(jmys)));
			cell2.setCellValue("居民违约金额合计：" + Tools.getAmountByYuan(String.valueOf(jmwy)));
			cell3.setCellValue("居民退费金额合计：" + Tools.getAmountByYuan(String.valueOf(jmtt)));
			cell4.setCellValue("居民实收合计：" + Tools.getAmountByYuan(String.valueOf(jmss)));

			//汇总第二行
			//公建应收金额合计：    公建违约金额合计：    公建退费金额合计：    公建实收合计：
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//公建应收金额合计
			cell2 = row4.createCell(2);//公建违约金额合计：
			cell3 = row4.createCell(4);// 公建退费金额合计：
			cell4 = row4.createCell(6);//公建实收合计：

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("公建应收金额合计：" + Tools.getAmountByYuan(String.valueOf(gjys)));
			cell2.setCellValue("公建违约金额合计：" + Tools.getAmountByYuan(String.valueOf(gjwy)));
			cell3.setCellValue("公建退费金额合计：" + Tools.getAmountByYuan(String.valueOf(gjtt)));
			cell4.setCellValue("公建实收合计：" + Tools.getAmountByYuan(String.valueOf(gjss)));

			//汇总第三行
			//应收金额合计：        违约金额合计：       退费金额合计：        实收合计：
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//应收金额合计：
			cell2 = row4.createCell(2);// 违约金额合计：
			cell3 = row4.createCell(4);// 退费金额合计：：
			cell4 = row4.createCell(6);//实收合计：

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("应收金额合计：" + Tools.getAmountByYuan(String.valueOf(jmys + gjys)));
			cell2.setCellValue("违约金额合计：" + Tools.getAmountByYuan(String.valueOf(jmwy + gjwy)));
			cell3.setCellValue("退费金额合计：" + Tools.getAmountByYuan(String.valueOf(jmtt + gjtt)));
			cell4.setCellValue("实收合计：" + Tools.getAmountByYuan(String.valueOf(jmss + gjss)));

			//汇总第四行
			//居民户数合计：        公建户数合计：       总户数合计：
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//居民户数合计：
			cell2 = row4.createCell(2);//  公建户数合计：
			cell3 = row4.createCell(4);// 总户数合计：
			cell4 = row4.createCell(6);//null

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("居民户数合计：" + jmnum);
			cell2.setCellValue("公建户数合计：" + gjnum);
			cell3.setCellValue("总户数合计：" + (jmnum + gjnum));

			out = new FileOutputStream(ConfigReader.getAppConfValue("fileBasePath") + "/"
					+ com.dc.repo.Tools.translateString(args[3]));
			workBook.write(out);
			resBean.setFilename(com.dc.repo.Tools.translateString(args[3]));
			resBean.setNum(String.valueOf(list.size()));
		} catch (Exception e) {
			log.error(Tools.getStackTrace(e));
			throw e;
		} finally {
			String msgt = "";
			if (null != jc)
				try {
					jc.close();
				} catch (Exception e) {
					msgt = e.getMessage();
					log.error(Tools.getStackTrace(e));
				}
			if (out != null)
				try {
					out.close();
				} catch (Exception e) {
					msgt = e.getMessage();
					log.error(Tools.getStackTrace(e));
				}
			if (!msgt.isEmpty())
				throw new SQLException(msgt);
		}

	}

	public static Workbook getWorkbok(File file) throws IOException {
		Workbook wb = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			if (file.getName().endsWith(EXCEL_XLS)) { // Excel?0?22003
				wb = new HSSFWorkbook(in);
			} else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
				wb = new XSSFWorkbook(in);
			}
		} finally {
			if (null != in)
				in.close();
		}
		return wb;
	}
}
