package com.dc.repo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.dc.util.MyFlowException;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;

public class LoadSeparatorFile {

	private String fileName;//待导入文件
	private String title;//备注--xml
	private String dataSource;//数据源--xml
	private String sql1;//导入前sql
	private String sql2;//导入后sql
	private String scheduleRun;//调度控制--xml
	private String EncodingCharsetName;//文件编码
	private final static Logger log = Logger.getLogger(LoadSeparatorFile.class);
	List<position> positionList;//文件自己生成
	private String tablename;//表名
	private String batchcolumn = "";//批次号列
	//批次号
	private String batnum;
	private Properties CUS_PROPERTIES;
	private String propertyPath;//配置文件路径 --xml
	private String separator;
	private boolean ignoreError;
	private int checkout;
	private int skipline;
	private boolean delLoadedFile;

	public LoadSeparatorFile(String title, String dataSource, String propertyPath, String scheduleRun) {
		super();
		this.title = title;
		this.dataSource = dataSource;
		this.scheduleRun = scheduleRun;
		this.propertyPath = propertyPath;
	}

	private void initProperties() throws IOException {

		FileReader resourceFile = null;
		try {

			log.info("加载控制文件-->" + "./config/" + propertyPath);

			resourceFile = new FileReader(new File("./config/" + propertyPath));
			CUS_PROPERTIES = new Properties();
			CUS_PROPERTIES.load(resourceFile);

			//初始化基础杂项
			this.fileName = ConfigReader.getAppConfValue("fileBasePath") + "/"
					+ Tools.translateString(CUS_PROPERTIES.getProperty("filename"));
			this.EncodingCharsetName = CUS_PROPERTIES.getProperty("encodingCharset");
			this.sql1 = CUS_PROPERTIES.getProperty("sql1");
			this.sql2 = CUS_PROPERTIES.getProperty("sql2");
			this.tablename = CUS_PROPERTIES.getProperty("tablename");
			this.batchcolumn = CUS_PROPERTIES.getProperty("batchcolumn");
			this.batnum = com.dc.util.Tools.getPatternTime("yyyyMMddHHmmss") + "_" + Tools.getFileName(fileName);
			this.separator = CUS_PROPERTIES.getProperty("separator");
			this.separator = null == separator ? "" : separator.replace("\\t", "\t").replace("\\n", "\n")
					.replace("\\r", "\r");
			this.ignoreError = Boolean.parseBoolean(CUS_PROPERTIES.getProperty("ignoreError"));
			this.delLoadedFile = Boolean.parseBoolean(CUS_PROPERTIES.getProperty("delOriFile"));

			try {
				this.checkout = Integer.parseInt(CUS_PROPERTIES.getProperty("checkout"));
			} catch (NumberFormatException e) {
				this.checkout = -1;
			}

			try {
				this.skipline = Integer.parseInt(CUS_PROPERTIES.getProperty("skipline"));
			} catch (NumberFormatException e) {
				this.skipline = 0;
			}

			@SuppressWarnings("rawtypes")
			java.util.Enumeration enumeration = CUS_PROPERTIES.propertyNames();

			//初始化数据库配置列
			positionList = new ArrayList<LoadSeparatorFile.position>();
			LoadSeparatorFile.position currpostion;

			while (enumeration.hasMoreElements()) {
				Object object = (Object) enumeration.nextElement();
				if (Character.isLowerCase(object.toString().charAt(0))) {
					//小写字母
					continue;
				}
				String value = CUS_PROPERTIES.getProperty(object.toString());
				currpostion = new position(object.toString(), Integer.parseInt(value));
				positionList.add(currpostion);
			}
		} finally {
			if (null != resourceFile)
				resourceFile.close();
		}
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("LOADS")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		JConnection jc = null;
		try {

			initProperties();

			try {
				jc = new JConnection(dataSource);
				jc.setAutoCommit(false);

				//第一个sql
				Tools.execUpdateSql(jc, sql1);
				k += load(jc);
				Tools.execUpdateSql(jc, sql2);

			} finally {
				if (null != jc)
					jc.close();
			}
			if (this.delLoadedFile) {
				if (!new File(this.fileName).delete()) {
					throw new MyFlowException("文件[" + this.fileName + "]删除失败");
				}
			}
			msg = "成功";
		} catch (Throwable e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k).appendColum(Tools.getFileName(this.fileName)).appendColum(msg.replace("\n", ""));
		}
	}

	private int load(JConnection jc) throws Exception {

		//数据库声明
		PreparedStatement ps = null;

		//文件声明
		BufferedReader br = null;
		String line = null;
		int j = 0;
		String splitArr[];

		try {
			ps = jc.getPreparedStatement(getSql());
			log.info("正在处理文件-->" + fileName);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), EncodingCharsetName));

			for (int sk = 0; sk < this.skipline; sk++) {
				log.info("跳过文件记录[" + (sk + 1) + "]-->" + br.readLine());
			}

			while (null != (line = br.readLine())) {

				try {
					if (line.trim().isEmpty())
						continue;
					splitArr = line.split(this.separator, -1);

					//进行字段项检查
					if (this.checkout > 0 && splitArr.length != this.checkout) {
						throw new MyFlowException("行记录字段值检查失败-->期望checkout=" + this.checkout + " 实际checkout="
								+ splitArr.length);
					}

					int k = 0;
					//如果存在批次号的情况
					if (null != this.batchcolumn && !this.batchcolumn.isEmpty()) {
						ps.setObject(1, batnum);
						k++;
					}

					for (int i = 0; i < positionList.size(); i++) {
						position currposition = positionList.get(i);
						String currfield = splitArr[currposition.getIndex()];
						ps.setObject(i + 1 + k, currfield.trim());
					}//for

					ps.addBatch();
					j++;
				} catch (Exception e) {
					log.error("\n行记录识别失败:" + e.getMessage() + "\n" + line + "\n"
							+ com.dc.util.Tools.getStackTrace(e));
					if (!this.ignoreError)
						throw e;
				}//内部catch块

				//每500行提交一次
				if (j % 500 == 0) {
					log.debug("reach commit point - 500 * " + j / 500);
					ps.executeBatch();
					jc.commit();
					ps.clearBatch();
				}
			}//while
			ps.executeBatch();
			jc.commit();
			log.info("文件[" + fileName + "]导入完毕，导入条数[" + j + "]");
			return j;
		} catch (Exception e) {
			log.error(com.dc.util.Tools.getStackTrace(e));
			if (null != jc)
				jc.rollback();

			//如果存在批次号的情况
			if (null != this.batchcolumn && !this.batchcolumn.isEmpty()) {
				//进行回退操作
				delBatnum(jc);
			}

			throw e;
		} finally {
			String msg = "";
			if (null != br)
				try {
					br.close();
				} catch (Exception e) {
					msg = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (ps != null)
				try {
					ps.close();
				} catch (Exception e) {
					msg = e.getMessage();
					log.error(com.dc.util.Tools.getStackTrace(e));
				}
			if (!msg.isEmpty())
				throw new Exception(msg);
		}
	}

	private void delBatnum(JConnection jConnection) throws SQLException {
		Statement statement = null;
		try {
			statement = jConnection.getStatement();
			String sqldel = "delete from " + this.tablename + " where " + this.batchcolumn + "='" + this.batnum + "'";
			log.info("准备执行sql语句-->" + sqldel);
			int num = statement.executeUpdate(sqldel);
			log.info("事务回滚，删除记录数-->" + num);
		} finally {
			if (null != statement)
				statement.close();
		}
	}

	private String getSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("insert into ").append(this.tablename).append("(");
		int k = 0;
		if (null != this.batchcolumn && !this.batchcolumn.isEmpty()) {
			sb.append(this.batchcolumn + ",");
			k = 1;
		}
		for (int i = 0; i < positionList.size(); i++) {
			position currposition = positionList.get(i);
			sb.append(currposition.getColumn() + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") values(");
		for (int i = 0; i < positionList.size() + k; i++) {
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length() - 1).append(")");
		log.info("生成导入sql-->" + sb.toString());
		return sb.toString();
	}

	class position {

		private String column;
		private int index;

		public position(String column, int index) {
			super();
			this.column = column;
			this.index = index;
		}

		/**
		 * @return Returns the column.
		 */
		public String getColumn() {
			return column;
		}

		/**
		 * @param column
		 *            The column to set.
		 */
		public void setColumn(String column) {
			this.column = column;
		}

		/**
		 * @return Returns the index.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @param index
		 *            The index to set.
		 */
		public void setIndex(int index) {
			this.index = index;
		}
	}

}
