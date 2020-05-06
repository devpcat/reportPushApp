package com.dc.repo;

import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 *
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-24
 *    fix->1.
 *         2.
 * </pre>
 */
public class RunCustSql {

	private String title;
	private String dataSource;
	private String sql;
	private String scheduleRun;

	private final static Logger log = Logger.getLogger(RunCustSql.class);

	/**
	 *
	 * @param title
	 * @param dataSource
	 * @param sql
	 * @param scheduleRun
	 */
	public RunCustSql(String title, String dataSource, String sql, String scheduleRun) {
		super();
		this.title = title;
		this.dataSource = dataSource;
		this.sql = sql;
		this.scheduleRun = scheduleRun;
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-24 上午07:34:53
	 */
	public void schedule() {
		if (!RunDecider.decide(scheduleRun))
			return;
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("CSQL")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			JConnection jc = null;
			try {
				//获取sql数组
				String sqlArr[] = Tools.getSql(sql);

				jc = new JConnection(this.dataSource);
				jc.setAutoCommit(false);
				for (String elem : sqlArr) {
					k += Tools.execUpdateSql(jc, elem);
				}
				jc.commit();
			} catch (Exception e) {
				if (null != jc)
					jc.rollback();
				throw e;
			} finally {
				jc.close();
			}
			msg = "成功";
		} catch (Exception e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k).appendColum("").appendColum(msg.replace("\n", ""));
		}
	}
}
