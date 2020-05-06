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
 * TODO ÀàµÄÃèÊö£º¡£
 *
 * <pre>
 * ²éÑ¯½á¹ûÉú³ÉTEXT´¿ÎÄ±¾ÎÄ¼ş£¬ÓÃÓÚµ¼³öÉú³É¸÷ÖÖ±¨±í
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
	private String separator;//·Ö¸ô·û
	private boolean lastSeparator;
	private boolean hideHeader;
	private int adjustnum;
	private String encoding = "GBK";

	private final static Logger log = Logger.getLogger(ExportText.class);

	/**
	 *
	 * @param mailConf
	 *            -ÓÊ¼şÅäÖÃÎÄ¼ş¿ÉÎª¿Õ
	 * @param fileName
	 *            -Éú³ÉµÄÎÄ¼şÃû
	 * @param compress
	 *            -ÊÇ·ñzipÑ¹Ëõ
	 * @param title
	 *            -±êÌâ
	 * @param dataSource
	 *            -Êı¾İÔ´
	 * @param sql
	 *            -sqlÓï¾ä»òsqlÎÄ¼şÂ·¾¶
	 * @param scheduleRun
	 *            -´¥·¢Ìõ¼ş
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
		String msg = "Î´Öª";
		try {
			BufferedWriter bw = null;
			JConnection jc = null;
			try {
				//ĞÂ½¨Êä³öÁ÷
				new File(this.fileName).delete();
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.fileName)),
						this.encoding));

				//»ñÈ¡sqlÊı×é
				String sqlArr[] = Tools.getSql(sql);

				jc = new JConnection(this.dataSource);
				jc.setAutoCommit(false);
				for (String elem : sqlArr) {

					//2017-03-23 ¿ÉÒÔÖ´ĞĞĞŞ¸ÄÓï¾ä
					if (!elem.trim().toLowerCase().startsWith("select")) {
						//²»ÊÇ²éÑ¯Óï¾ä
						Statement sup = null;
						try {
							sup = jc.getStatement();
							log.info("×¼±¸Ö´ĞĞsqlÓï¾ä-->" + elem);
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

			//°´Ìõ¼şÑ¹Ëõ
			filename = Tools.ZipCompressing(this.fileName, Boolean.parseBoolean(compress));
			//filename = Tools.getFileName(filename);
			if (null != this.mailConf && !this.mailConf.trim().isEmpty())
				// ·¢ËÍÓÊ¼ş-[²ÎÊı1-ÓÊ¼şÄÚÈİ£¬²ÎÊı2-ÓÊ¼şÖ÷Ìâ£¬²ÎÊı3-ÅäÖÃÎÄ¼ş £¬²ÎÊı4-¸½¼ş]
				SendMail.main(new String[] { "", "", "./config/" + this.mailConf, filename });
			msg = "³É¹¦";
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
	 * @time 2017-3-20 ÏÂÎç03:55:50
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
				// ±íÃû
				for (int i = 1; i <= columnCount; i++) {
					bw.append(rsmd.getColumnName(i));
					if (i != columnCount)
						bw.append(this.separator);
					else if (this.lastSeparator)
						bw.append(this.separator);
				}
				bw.newLine();
			}

			// ±íÊı¾İ,²»²¹³ä±íÍ·ĞÅÏ¢
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
