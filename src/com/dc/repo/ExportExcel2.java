package com.dc.repo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
//import com.icbc.dlfh.util.ExcelWriter;
import com.icbc.dlfh.util.mail.SendMail;

/**
 * <pre>
 * 使用模板方式导出excel
 * 2020年五四青年节
 * 自己做的决定跪着也要走完！
 * </pre>
 *
 * @author dlfh-yuc02
 *
 */
public class ExportExcel2 {

	// private ExcelWriter excelWriter;
	private String mailConf;
	private String fileName;
	private String compress;
	private String title;
	private String scheduleRun;
	private List<String[]> sheetList;
	private final static Logger log = Logger.getLogger(ExportExcel2.class);
	@SuppressWarnings("unused")
	private boolean hideHeader;
	private int adjustnum;

	private String templateFileName;
	private ExcelWriter excelWriter;
	private WriteSheet writeSheet;

	/**
	 *
	 * @param mailConf    -邮件文件位置
	 * @param fileName    -excel文件名
	 * @param compress    -是否zip压缩
	 * @param title       -邮件附加主题
	 * @param scheduleRun -调度参数
	 * @param sheetList   -excelsheet参数
	 */
	public ExportExcel2(String mailConf, String fileName, String compress, String title, String scheduleRun,
			List<String[]> sheetList, String hideHeader, String templateFileName) {
		this.mailConf = mailConf;// 邮件发送配置
		this.fileName = Tools.translateString(fileName);// 生成的文件名
		this.compress = compress;// 是否zip压缩
		this.title = Tools.translateString(title);// 通知的邮件名
		this.scheduleRun = scheduleRun;// 调度参数
		this.sheetList = sheetList;// sheet
		// this.hideHeader = Boolean.parseBoolean(hideHeader);
		if (null == hideHeader || hideHeader.isEmpty()) {
			this.hideHeader = true;
			this.adjustnum = 0;
		} else {
			this.hideHeader = Boolean.parseBoolean(Tools.get_hideHeader_count(hideHeader)[0]);
			this.adjustnum = Integer.parseInt(Tools.get_hideHeader_count(hideHeader)[1]);
		}
		@SuppressWarnings("unused")
		boolean excel2003;
		if (fileName.endsWith(".xls"))
			excel2003 = true;
		else if (fileName.endsWith(".xlsx"))
			excel2003 = false;
		else {
			excel2003 = false;
			fileName += ".xlsx";
		}
		this.templateFileName = Tools.translateString(templateFileName);
		excelWriter = EasyExcel.write(ConfigReader.getAppConfValue("fileBasePath") + "/" + this.fileName)
				.withTemplate("./config/" + this.templateFileName).build();
		// writeSheet = EasyExcel.writerSheet().build();
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("EXCEL2")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			// 生成excel
			for (String[] elem : sheetList) {
				k += GenOneSheet(elem[0], elem[1], elem[2]);
			}
			excelWriter.finish();

			filename = Tools.ZipCompressing(ConfigReader.getAppConfValue("fileBasePath") + "/" + fileName,
					Boolean.parseBoolean(compress));
			// filename = Tools.getFileName(filename);
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
			writeSheet = EasyExcel.writerSheet(Tools.translateString(sheetName)).build();
			jc = new JConnection(dateSource);
			jc.setAutoCommit(false);
			int j = 0;
			int updsqlcnt = 0;
			statement = jc.getStatement();
			for (String elem : sqlArr) {
				if (!elem.trim().toLowerCase().startsWith("select")) {
					log.info("准备执行sql语句[修改]-->" + elem);
					statement.execute(elem);
				} else {
					log.info("准备执行sql语句[查询]-->" + elem);
					j += this.addResultSetToExcel(statement, elem, ++updsqlcnt);
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
	private int addResultSetToExcel(Statement statement, String sql, int sqlcnt) throws SQLException {
		int exportnum = 0;
		ResultSet rs = null;
		try {
			rs = statement.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			String[] strArr = new String[columnCount];
			int[] type = new int[columnCount];

			for (int i = 1; i <= columnCount; i++) {
				strArr[i - 1] = rsmd.getColumnName(i);
				type[i - 1] = rsmd.getColumnType(i);
			}

			FillConfig fillConfig;
			if (strArr[0].equalsIgnoreCase("unForceNewRow")) {
				fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
			} else if(strArr[0].equalsIgnoreCase("HORIZONTAL")){
				fillConfig =FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
			}else{
				fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
			}

			// 表数据,不补充表头信息
			List<Map<String, Object>> list = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					switch (type[i - 1]) {
					case Types.VARCHAR:
					case Types.CHAR:
					case Types.NCHAR:
					case Types.NVARCHAR:
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
						map.put(strArr[i - 1], com.dc.util.Tools.print(rs.getString(i)));
						break;
					case Types.INTEGER:
					case Types.BIGINT:
					case Types.SMALLINT:
					case Types.TINYINT:
						map.put(strArr[i - 1], rs.getLong(i));
						break;
					case Types.TIMESTAMP:
						// map.put(strArr[i - 1], rs.getDate(i));
						map.put(strArr[i - 1], com.dc.util.Tools.print(rs.getString(i)));
						break;
					case Types.DOUBLE:
						map.put(strArr[i - 1], rs.getDouble(i));
						break;
					case Types.FLOAT:
						map.put(strArr[i - 1], rs.getFloat(i));
						break;
					case Types.BLOB:
						map.put(strArr[i - 1], rs.getBlob(i));
						break;
					case Types.CLOB:
						map.put(strArr[i - 1], com.dc.util.Tools.print(rs.getString(i)));
						break;
					case Types.NUMERIC:
					case Types.DECIMAL:
						map.put(strArr[i - 1], rs.getBigDecimal(i));
						break;
					default:
						log.error("not match type for metadata[" + type[i - 1] + "]");
						map.put(strArr[i - 1], com.dc.util.Tools.print(rs.getString(i)));
					}
				} // 逐个属性获取
				list.add(map);
				if (list.size() >= 100) {
					excelWriter.fill(new FillWrapper("sql" + sqlcnt, list), fillConfig, writeSheet);
					// list.clear();
					list = new ArrayList<>();
				}
				// this.excelWriter.addRow(strArr);
				exportnum++;
			}
			if (list.size() > 0)
				excelWriter.fill(new FillWrapper("sql" + sqlcnt, list), fillConfig, writeSheet);
			excelWriter.writeContext();
			return exportnum;
		} finally {
			if (null != rs)
				rs.close();
		}
	}
}
