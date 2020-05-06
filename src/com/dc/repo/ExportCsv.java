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
 * TODO �����������
 *
 * <pre>
 * ��ѯ�������csv���ı��ļ����������ڴ���������
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
	 *            -�ʼ������ļ���Ϊ��
	 * @param fileName
	 *            -���ɵ��ļ���
	 * @param compress
	 *            -�Ƿ�zipѹ��
	 * @param title
	 *            -����
	 * @param dataSource
	 *            -����Դ
	 * @param sql
	 *            -sql����sql�ļ�·��
	 * @param scheduleRun
	 *            -��������
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
		String msg = "δ֪";
		try {
			List<String> fileList = new ArrayList<String>();

			//��ȡsql����
			String sqlArr[] = Tools.getSql(sql);
			JConnection jc = null;
			try {
				jc = new JConnection(this.dataSource);
				jc.setAutoCommit(false);
				//�����ļ�
				int i = 0;
				for (String elem : sqlArr) {

					//2017-03-23 ����ִ���޸����
					if (!elem.trim().toLowerCase().startsWith("select")) {
						//���ǲ�ѯ���
						Statement sup = null;
						try {
							sup = jc.getStatement();
							log.info("׼��ִ��sql���-->" + elem);
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
				//������ѹ��
				Tools.zip(this.fileName + ".zip", fileList);

				for (String elem : fileList) {
					new File(elem).delete();
				}
				filename = this.fileName + ".zip";
			} else {
				//������ѹ��
				filename = Tools.ZipCompressing(this.fileName + ".csv", Boolean.parseBoolean(compress));
			}
			//filename = Tools.getFileName(filename);
			if (null != this.mailConf && !this.mailConf.trim().isEmpty())
				// �����ʼ�-[����1-�ʼ����ݣ�����2-�ʼ����⣬����3-�����ļ� ������4-����]
				SendMail.main(new String[] { "", "", "./config/" + this.mailConf, filename });
			msg = "�ɹ�";
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
	 * һ��sql����һ��csv�ļ�
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-9 ����02:37:32
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
				// ����
				for (int i = 1; i <= columnCount; i++) {
					bw.append(rsmd.getColumnName(i));
					if (i != columnCount)
						bw.append(",");
				}
				bw.newLine();
				// ������
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
		log.info("�ɹ�����CSV�ļ�-->�ļ���[" + filename + "] ����[" + exportnum + "]");
		return exportnum;
	}
}
