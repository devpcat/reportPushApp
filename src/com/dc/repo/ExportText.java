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
import org.apache.log4j.Logger;
import com.dc.util.jdbc.JConnection;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 查询结果生成TEXT纯文本文件，用于导出生成各种报表
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-20
 *    fix->1.
 *         2.
 * </pre>
 */
public class ExportText {

	private String mailConf;
	private String fileName;
	private String compress;
	private String title;
	private String dataSource;
	private String sql;
	private String scheduleRun;
	private String separator;//分隔符
	private boolean lastSeparator;
	private boolean hideHeader;
	private int adjustnum;
	private String encoding = "GBK";

	private final static Logger log = Logger.getLogger(ExportText.class);

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
	public ExportText(String mailConf, String fileName, String compress, String title, String dataSource, String sql,
			String scheduleRun, String separator, String lastSeparator, String hideHeader, String encoding) {
		super();
		this.mailConf = mailConf;
		this.fileName = ConfigReader.getAppConfValue("fileBasePath") + "/" + Tools.translateString(fileName);
		this.compress = compress;
		this.title = Tools.translateString(title);
		this.dataSource = dataSource;
		this.sql = sql;
		this.scheduleRun = scheduleRun;
		this.separator = null == separator ? "" : separator.replace("\\t", "\t").replace("\\n", "\n")
				.replace("\\r", "\r");
		if ("!block".equals(this.separator)) {
			this.separator = "";
		}
		this.lastSeparator = Boolean.parseBoolean(lastSeparator);
		//this.hideHeader = Boolean.parseBoolean(hideHeader);
		this.hideHeader = Boolean.parseBoolean(Tools.get_hideHeader_count(hideHeader)[0]);
		this.adjustnum = Integer.parseInt(Tools.get_hideHeader_count(hideHeader)[1]);
		this.encoding = encoding;
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("TEXT")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			BufferedWriter bw = null;
			JConnection jc = null;
			try {
				//新建输出流
				new File(this.fileName).delete();
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.fileName)),
						this.encoding));

				//获取sql数组
				String sqlArr[] = Tools.getSql(sql);

				jc = new JConnection(this.dataSource);
				jc.setAutoCommit(false);
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
					k += AddResultSetToFile(jc, elem, bw);
				}
				jc.commit();

			} catch (Exception e) {
				if (null != jc)
					jc.rollback();
				throw e;
			} finally {
				String msgt = "";
				if (null != jc)
					try {
						jc.close();
					} catch (Exception e) {
						msgt = e.getMessage();
						log.error(com.dc.util.Tools.getStackTrace(e));
					}
				if (bw != null)
					try {
						bw.close();
					} catch (Exception e) {
						msgt = e.getMessage();
						log.error(com.dc.util.Tools.getStackTrace(e));
					}
				if (!msgt.isEmpty())
					throw new SQLException(msgt);
			}

			//按条件压缩
			filename = Tools.ZipCompressing(this.fileName, Boolean.parseBoolean(compress));
			//filename = Tools.getFileName(filename);
			if (null != this.mailConf && !this.mailConf.trim().isEmpty())
				// 发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
				SendMail.main(new String[] { "", "", "./config/" + this.mailConf, filename });
			msg = "成功";
			k += this.adjustnum;
		} catch (Exception e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k < 0 ? 0 : k).appendColum(Tools.getFileName(filename))
					.appendColum(msg.replace("\n", ""));
		}
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-20 下午03:55:50
	 * @param jc
	 * @param sql
	 * @param bw
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private int AddResultSetToFile(JConnection jc, String sql, BufferedWriter bw) throws SQLException, IOException {
		int exportnum = 0;
		ResultSet rs = null;
		Statement statement = null;
		try {
			statement = jc.getStatement();
			rs = statement.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			if (!this.hideHeader) {
				// 表名
				for (int i = 1; i <= columnCount; i++) {
					bw.append(rsmd.getColumnName(i));
					if (i != columnCount)
						bw.append(this.separator);
					else if (this.lastSeparator)
						bw.append(this.separator);
				}
				bw.newLine();
			}

			// 表数据,不补充表头信息
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					bw.append(com.dc.util.Tools.print(rs.getString(i)));
					if (i != columnCount)
						bw.append(this.separator);
					else if (this.lastSeparator)
						bw.append(this.separator);
				}
				exportnum++;
				bw.newLine();
			}

			bw.flush();
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
		return exportnum;
	}
}
