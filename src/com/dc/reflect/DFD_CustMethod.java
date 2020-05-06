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
 * TODO �����������
 *
 * <pre>
 * �󷢵�ȡů�Ѷ�����ϸ��
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
	 * 0-���� 1-����Դ 2-ģ��·�� 3-���·��
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-28 ����11:34:28
	 * @param args
	 * @throws Exception
	 */
	public static void CreateExcel(String[] args, ResultBean resBean) throws Exception {
		OutputStream out = null;
		JConnection jc = null;

		String workdate = com.dc.repo.Tools.translateString(args[0]);

		try {
			jc = new JConnection(args[1]);
			//��ȡģ��
			Workbook workBook = getWorkbok(new File("./config/" + args[2]));
			Sheet sheet = workBook.getSheetAt(0);
			Row row1 = sheet.getRow(1);
			//�޸Ľ�������
			row1.getCell(0).setCellValue("�������ڣ�" + workdate);
			CellStyle cellStyle2A = row1.getCell(0).getCellStyle();

			//��cellstyble��ȡ���洢
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

			//ִ�в�ѯsql
			List<String[]> list = jc.queryAsList("select t.khh,t.nd,t.jfje,t.wyj,t.tfje,t.amount,t.yhlx "
					+ "from dldfahf_trxinfos t " + "where t.dzflag='0' and t.TRXMODE='0' and t.workdate='" + workdate + "'");
			list.remove(0);//ɾ������ͷ
			//д��excel����
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
				//0-�û��� 1-��� 2-�ɷѽ�Ӧ�ս� 3-ΥԼ�� 4-�˷ѽ�� 5-ʵ�ս�� 6-�û�����
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
				//0-�û��� 1-��� 2-�ɷѽ�Ӧ�ս� 3-ΥԼ�� 4-�˷ѽ�� 5-ʵ�ս�� 6-�û�����
				if ("����".equals(elem[6].trim())) {
					//����
					jmnum++;
					jmys += Integer.parseInt(elem[2]);
					jmwy += Integer.parseInt(elem[3]);
					jmtt += Integer.parseInt(elem[4]);
					jmss += Integer.parseInt(elem[5]);
				} else {
					//����
					gjnum++;
					gjys += Integer.parseInt(elem[2]);
					gjwy += Integer.parseInt(elem[3]);
					gjtt += Integer.parseInt(elem[4]);
					gjss += Integer.parseInt(elem[5]);
				}
			}//for

			if (3 == i)
				i++;

			//���ܵ�һ��
			//����Ӧ�ս��ϼƣ�    ����ΥԼ���ϼƣ�    �����˷ѽ��ϼƣ�    ����ʵ�պϼƣ�
			Row row4 = sheet.createRow(i++);
			Cell cell1 = row4.createCell(0);//����Ӧ�ս��ϼƣ�
			Cell cell2 = row4.createCell(2);//����ΥԼ���ϼƣ�
			Cell cell3 = row4.createCell(4);// �����˷ѽ��ϼƣ�
			Cell cell4 = row4.createCell(6);//����ʵ�պϼƣ�

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("����Ӧ�ս��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmys)));
			cell2.setCellValue("����ΥԼ���ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmwy)));
			cell3.setCellValue("�����˷ѽ��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmtt)));
			cell4.setCellValue("����ʵ�պϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmss)));

			//���ܵڶ���
			//����Ӧ�ս��ϼƣ�    ����ΥԼ���ϼƣ�    �����˷ѽ��ϼƣ�    ����ʵ�պϼƣ�
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//����Ӧ�ս��ϼ�
			cell2 = row4.createCell(2);//����ΥԼ���ϼƣ�
			cell3 = row4.createCell(4);// �����˷ѽ��ϼƣ�
			cell4 = row4.createCell(6);//����ʵ�պϼƣ�

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("����Ӧ�ս��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(gjys)));
			cell2.setCellValue("����ΥԼ���ϼƣ�" + Tools.getAmountByYuan(String.valueOf(gjwy)));
			cell3.setCellValue("�����˷ѽ��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(gjtt)));
			cell4.setCellValue("����ʵ�պϼƣ�" + Tools.getAmountByYuan(String.valueOf(gjss)));

			//���ܵ�����
			//Ӧ�ս��ϼƣ�        ΥԼ���ϼƣ�       �˷ѽ��ϼƣ�        ʵ�պϼƣ�
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//Ӧ�ս��ϼƣ�
			cell2 = row4.createCell(2);// ΥԼ���ϼƣ�
			cell3 = row4.createCell(4);// �˷ѽ��ϼƣ���
			cell4 = row4.createCell(6);//ʵ�պϼƣ�

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("Ӧ�ս��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmys + gjys)));
			cell2.setCellValue("ΥԼ���ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmwy + gjwy)));
			cell3.setCellValue("�˷ѽ��ϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmtt + gjtt)));
			cell4.setCellValue("ʵ�պϼƣ�" + Tools.getAmountByYuan(String.valueOf(jmss + gjss)));

			//���ܵ�����
			//�������ϼƣ�        ���������ϼƣ�       �ܻ����ϼƣ�
			row4 = sheet.createRow(i++);
			cell1 = row4.createCell(0);//�������ϼƣ�
			cell2 = row4.createCell(2);//  ���������ϼƣ�
			cell3 = row4.createCell(4);// �ܻ����ϼƣ�
			cell4 = row4.createCell(6);//null

			cell1.setCellStyle(cellStyle2A);
			cell2.setCellStyle(cellStyle2A);
			cell3.setCellStyle(cellStyle2A);
			cell4.setCellStyle(cellStyle2A);

			cell1.setCellValue("�������ϼƣ�" + jmnum);
			cell2.setCellValue("���������ϼƣ�" + gjnum);
			cell3.setCellValue("�ܻ����ϼƣ�" + (jmnum + gjnum));

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
