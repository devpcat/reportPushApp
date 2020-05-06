package com.dc.util.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dc.repo.Tools;
import org.apache.log4j.Logger;
import com.dc.repo.ConfigReader;

public class JConnection {
	private Connection ct;
	//private boolean autoCommit;
	private final static Logger log = Logger.getLogger(JConnection.class);
	private int counter;
	private static int num = 0;
	private static Map<String, Connection> connectionMap = new HashMap<String, Connection>();

	//jvm����ʱ�ر����ݿ�����
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Set<String> keyset = connectionMap.keySet();
				for (String elem : keyset) {
					Connection conn = connectionMap.get(elem);
					try {
						if (!conn.isClosed()) {
							log.debug("׼���ر�JDBC����-->" + conn);
							conn.close();
						}
					} catch (SQLException e) {
						log.error("���ݿ�ر��쳣[����]\n" + com.dc.util.Tools.getStackTrace(e));
					}
				}
			}

		});
	}

	// ���캯��
	public JConnection(String dateSource) throws Exception {
		Connection conn = connectionMap.get(dateSource);
		if (null == conn) {
			conn = getConnection(dateSource);
			connectionMap.put(dateSource, conn);
			log.debug("[" + counter + "]��ȡ���ݿ�����[��]-->" + conn);
		} else {
			counter = ++num;
			log.debug("[" + counter + "]��ȡ���ݿ�����[��]-->" + conn);
		}
		conn.clearWarnings();
		conn.setAutoCommit(true);
		this.ct = conn;
	}

	private Connection getConnection(String dateSource) throws Exception {

		String datesourceConf[] = ConfigReader.getDataSource(dateSource);
		counter = ++num;
		String passwd, url, user, driver;
		if ("false".equalsIgnoreCase(ConfigReader.getAppConfValue("passwdEncrypt"))) {
			passwd = datesourceConf[3];
			url = datesourceConf[1];
			user = datesourceConf[2];
			driver = datesourceConf[0];
		} else {
			passwd = new String(com.dc.util.Tools.decrypt(datesourceConf[3].getBytes(), Tools.pwd));
			url = new String(com.dc.util.Tools.decrypt(datesourceConf[1].getBytes(), Tools.pwd));
			user = new String(com.dc.util.Tools.decrypt(datesourceConf[2].getBytes(), Tools.pwd));
			driver = new String(com.dc.util.Tools.decrypt(datesourceConf[0].getBytes(), Tools.pwd));
		}
		Class.forName(driver);

		log.info("[" + counter + "]" + "����JDBC����[" + "driver=" + datesourceConf[0] + " url=" + datesourceConf[1]
				+ " username=" + datesourceConf[2] + " password=" + datesourceConf[3] + "]");
		return DriverManager.getConnection(url, user, passwd);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		ct.setAutoCommit(autoCommit);
		//this.autoCommit = autoCommit;
	}

	public void commit() throws SQLException {
		if (false == ct.getAutoCommit()) {
			ct.commit();
			log.info("[" + counter + "]" + "�����ύ");
		}
	}

	public void rollback() throws SQLException {
		if (false == ct.getAutoCommit()) {
			ct.rollback();
			log.info("[" + counter + "]" + "����ع�");
		}
	}

	public void close() throws SQLException {
		//		log.info("[" + counter + "]" + "�ر�JDBC����\n");
		//		if (null != ct)
		//			ct.close();
	}

	public Statement getStatement() throws SQLException {
		return ct.createStatement();
	}

	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		return ct.prepareStatement(sql);
	}

	/**
	 * ����list�������һ��Ϊ��ͷ���ڶ�������Ϊ����
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 ����05:05:32
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public List<String[]> queryAsList(String sql) throws SQLException {
		Statement statement = null;
		ResultSet rs = null;
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			statement = getStatement();
			log.info("׼��ִ��sql���:" + sql);
			rs = statement.executeQuery(sql);
			// ��ȡ��ͷ
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			String[] strarr = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				strarr[i] = rsmd.getColumnName(1 + i);
			}
			list.add(strarr);

			// ��ȡ����
			while (rs.next()) {
				strarr = new String[columnCount];
				for (int i = 0; i < columnCount; i++) {
					strarr[i] = rs.getString(1 + i);
				}
				list.add(strarr);

				//				String tmp="";
				//				for(String elem:strarr){
				//					tmp+=elem+" ";
				//				}
				//				log.debug(tmp);
			}
			// ���ؽ��
			return list;
		} finally {
			String msg = "";
			if (null != rs)
				try {
					rs.close();
				} catch (SQLException e) {
					log.error(com.dc.util.Tools.getStackTrace(e));
					msg += e.getMessage();
				}
			if (null != statement)
				try {
					statement.close();
				} catch (SQLException e) {
					log.error(com.dc.util.Tools.getStackTrace(e));
					msg += e.getMessage();
				}
			if (!msg.isEmpty())
				throw new SQLException(msg);
		}
	}

	//	/**
	//	 *
	//	 * @author dlfh-yuc02
	//	 * @time 2017-3-9 ����02:16:10
	//	 * @param sql
	//	 * @return
	//	 * @throws SQLException
	//	 */
	//	public ResultSet queryAsResultSet(String sql) throws SQLException {
	//		Statement statement = null;
	//		ResultSet rs = null;
	//		try {
	//			statement = getStatement();
	//			log.info("׼��ִ��sql���:" + sql);
	//			rs = statement.executeQuery(sql);
	//			return rs;
	//		} finally {
	//			if (null != statement)
	//				statement.close();
	//		}
	//
	//	}

	/**
	 * ���ݿ���ɾ��- PreparedStatementע�η�ʽ
	 *
	 * @author dlfh-yuc02
	 * @time 2016-5-3 ����10:36:35
	 * @param sql
	 * @param paras
	 * @return
	 * @throws SQLException
	 */
	public int exeUpdate(String sql, String... paras) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = ct.prepareStatement(sql);
			// ��sql�Ĳ�����ֵ
			for (int i = 0; i < paras.length; i++) {
				ps.setString(i + 1, paras[i]);
			}
			return ps.executeUpdate();
		} finally {
			if (null != ps)
				ps.close();
		}
	}
	//
	//	/**
	//	 * ���ݿ��ѯ- PreparedStatementע�η�ʽ
	//	 *
	//	 * @author dlfh-yuc02
	//	 * @time 2016-5-3 ����10:37:07
	//	 * @param sql
	//	 * @param paras
	//	 * @return
	//	 * @throws SQLException
	//	 */
	//	public ResultSet query(String sql, String[] paras) throws SQLException {
	//		PreparedStatement ps = null;
	//		try {
	//			ps = ct.prepareStatement(sql);
	//			// ��sql�Ĳ�����ֵ
	//			for (int i = 0; i < paras.length; i++) {
	//				ps.setString(i + 1, paras[i]);
	//			}
	//			// ִ�в�ѯ
	//			return ps.executeQuery();
	//		} finally {
	//			if (null != ps)
	//				ps.close();
	//		}
	//	}
}
