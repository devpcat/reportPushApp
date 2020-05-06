package com.dc.repo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.dc.util.ExcelWriter;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.icbc.dlfh.util.mail.SendMail;

public class ExportExcel {

	private ExcelWriter excelWriter;
	private String mailConf;
	private String fileName;
	private String compress;
	private String title;
	private String scheduleRun;
	private List<String[]> sheetList;
	private final static Logger log = Logger.getLogger(ExportExcel.class);
	private boolean hideHeader;
	private int adjustnum;

	/**
	 *
	 * @param mailConf
	 *            -邮件文件位置
	 * @param fileName
	 *            -excel文件名
	 * @param compress
	 *            -是否zip压缩
	 * @param title
	 *            -邮件附加主题
	 * @param scheduleRun
	 *            -调度参数
	 * @param sheetList
	 *            -excelsheet参数
	 */
	public ExportExcel(String mailConf, String fileName, String compress, String title, String scheduleRun,
			List<String[]> sheetList, String hideHeader) {
		this.mailConf = mailConf;
		this.fileName = Tools.translateString(fileName);
		this.compress = compress;
		this.title = Tools.translateString(title);
		this.scheduleRun = scheduleRun;
		this.sheetList = sheetList;
		//this.hideHeader = Boolean.parseBoolean(hideHeader);
		this.hideHeader = Boolean.parseBoolean(Tools.get_hideHeader_count(hideHeader)[0]);
		this.adjustnum = Integer.parseInt(Tools.get_hideHeader_count(hideHeader)[1]);
		boolean excel2003;
		if (fileName.endsWith(ExcelWriter.EXCEL_2003))
			excel2003 = true;
		else if (fileName.endsWith(ExcelWriter.EXCEL_2007))
			excel2003 = false;
		else {
			excel2003 = false;
			fileName += ".xlsx";
		}
		excelWriter = new ExcelWriter(excel2003);
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("EXCEL")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			// 生成excel
			for (String[] elem : sheetList) {
				k += GenOneSheet(elem[0], elem[1], elem[2]);
			}
			excelWriter.write(ConfigReader.getAppConfValue("fileBasePath") + "/" + fileName);

			filename = Tools.ZipCompressing(ConfigReader.getAppConfValue("fileBasePath") + "/" + fileName,
					Boolean.parseBoolean(compress));
			//filename = Tools.getFileName(filename);
			if (null != this.mailConf && !this.mailConf.trim().isEmpty())
				// 发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
				SendMail.main(new String[] { "", "", "./config/" + this.mailConf, filename });
			msg = "成功";
			k += this.adjustnum;
		} catch (Throwable e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k).appendColum(Tools.getFileName(filename)).appendColum(msg.replace("\n", ""));
		}

	}

	/**
	 * 创建一个sheet
	 *
	 * @author dlfh-yuc02
	 * @throws Exception
	 * @time 2017-3-2 下午04:21:01 2017-03-29再次修改，直接使用resultset
	 */
	private int GenOneSheet(String sheetName, String dateSource, String sql) throws Exception {
		JConnection jc = null;
		Statement statement = null;
		try {
			String sqlArr[] = Tools.getSql(sql);
			excelWriter.addSheet(Tools.translateString(sheetName));
			jc = new JConnection(dateSource);
			jc.setAutoCommit(false);
			int j = 0;

			statement = jc.getStatement();
			for (String elem : sqlArr) {
				if (!elem.trim().toLowerCase().startsWith("select")) {
					log.info("准备执行sql语句[修改]-->" + elem);
					statement.execute(elem);
				} else {
					log.info("准备执行sql语句[查询]-->" + elem);
					j += this.addResultSetToExcel(statement, elem);
				}
			}
			jc.commit();
			return j;
		} catch (Exception e) {
			if (null != jc)
				jc.rollback();
			throw e;
		} finally {
			String msgt = "";
			if (null != statement)
				try {
					statement.close();
				} catch (Exception e) {
					msgt = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (null != jc)
				try {
					jc.close();
				} catch (Exception e) {
					msgt = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (!msgt.isEmpty())
				throw new SQLException(msgt);
		}
	}

	/**
	 * 通过resultset直接输出到excel，减少内存消耗
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-29 下午05:52:30
	 * @param statement
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	private int addResultSetToExcel(Statement statement, String sql) throws SQLException {
		int exportnum = 0;
		ResultSet rs = null;
		try {
			rs = statement.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			String[] strArr = new String[columnCount];

			if (!this.hideHeader) {
				// 表名
				for (int i = 1; i <= columnCount; i++) {
					strArr[i - 1] = rsmd.getColumnName(i);
				}
				this.excelWriter.addRow(strArr);
			}

			// 表数据,不补充表头信息
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					strArr[i - 1] = com.dc.util.Tools.print(rs.getString(i));
				}
				this.excelWriter.addRow(strArr);
				exportnum++;
			}

			return exportnum;
		} finally {
			if (null != rs)
				rs.close();
		}
	}
}
