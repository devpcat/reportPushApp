package com.dc.repo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 查询结果生成csv纯文本文件，可以用于处理超大数据
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-9
 *    fix->1.
 *         2.
 * </pre>
 */
public class ExportCsv {

	private String mailConf;
	private String fileName;
	private String compress;
	private String title;
	private String dataSource;
	private String sql;
	private String scheduleRun;

	private final static Logger log = Logger.getLogger(ExportCsv.class);

	/**
	 *
	 * @param mailConf
	 *            -邮件配置文件可为空
	 * @param fileName
	 *            -生成的文件名
	 * @param compress
	 *            -是否zip压缩
	 * @param title
	 *            -标题
	 * @param dataSource
	 *            -数据源
	 * @param sql
	 *            -sql语句或sql文件路径
	 * @param scheduleRun
	 *            -触发条件
	 */
	public ExportCsv(String mailConf, String fileName, String compress, String title, String dataSource, String sql,
			String scheduleRun) {
		super();
		this.mailConf = mailConf;
		this.fileName = ConfigReader.getAppConfValue("fileBasePath") + "/" + Tools.translateString(fileName);
		this.compress = compress;
		this.title = Tools.translateString(title);
		this.dataSource = dataSource;
		this.sql = sql;
		this.scheduleRun = scheduleRun;
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("CSV")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			List<String> fileList = new ArrayList<String>();

			//获取sql数组
			String sqlArr[] = Tools.getSql(sql);
			JConnection jc = null;
			try {
				jc = new JConnection(this.dataSource);
				jc.setAutoCommit(false);
				//生成文件
				int i = 0;
				for (String elem : sqlArr) {

					//2017-03-23 可以执行修改语句
					if (!elem.trim().toLowerCase().startsWith("select")) {
						//不是查询语句
						Statement sup = null;
						try {
							sup = jc.getStatement();
							log.info("准备执行sql语句-->" + elem);
							sup.execute(elem);
						} finally {
							if (null != sup)
								sup.close();
						}
						continue;
					}

					i++;
					String filename0;
					if (sqlArr.length > 1) {
						filename0 = this.fileName + "_" + i + ".csv";
					} else {
						filename0 = this.fileName + ".csv";
					}
					k += GenOneCSV(jc, elem, filename0);
					fileList.add(filename0);
				}
				jc.commit();
			} catch (Exception e) {
				if (null != jc)
					jc.rollback();
				throw e;
			} finally {
				if (null != jc)
					jc.close();
			}

			if (sqlArr.length > 1) {
				//无条件压缩
				Tools.zip(this.fileName + ".zip", fileList);

				for (String elem : fileList) {
					new File(elem).delete();
				}
				filename = this.fileName + ".zip";
			} else {
				//按条件压缩
				filename = Tools.ZipCompressing(this.fileName + ".csv", Boolean.parseBoolean(compress));
			}
			//filename = Tools.getFileName(filename);
			if (null != this.mailConf && !this.mailConf.trim().isEmpty())
				// 发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
				SendMail.main(new String[] { "", "", "./config/" + this.mailConf, filename });
			msg = "成功";
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
	 * 一条sql生成一个csv文件
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-9 下午02:37:32
	 * @param jc
	 * @param sql
	 * @param filename
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private int GenOneCSV(JConnection jc, String sql, String filename) throws IOException, SQLException {
		int exportnum = 0;
		ResultSet rs = null;
		Statement statement = null;
		try {
			statement = jc.getStatement();
			rs = statement.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)), "GBK"));
				// 表名
				for (int i = 1; i <= columnCount; i++) {
					bw.append(rsmd.getColumnName(i));
					if (i != columnCount)
						bw.append(",");
				}
				bw.newLine();
				// 表数据
				while (rs.next()) {
					for (int i = 1; i <= columnCount; i++) {
						bw.append(com.dc.util.Tools.print(rs.getString(i)));
						if (i != columnCount)
							bw.append(",");
					}
					exportnum++;
					bw.newLine();
				}
			} finally {
				if (null != bw)
					bw.close();
			}
		} finally {
			String msg = "";
			if (null != rs)
				try {
					rs.close();
				} catch (SQLException e) {
					msg = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					msg = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (!msg.isEmpty())
				throw new SQLException(msg);
		}
		log.info("成功生成CSV文件-->文件名[" + filename + "] 条数[" + exportnum + "]");
		return exportnum;
	}
}
