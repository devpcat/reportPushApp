package com.dc.repo;

import java.util.List;

import com.dc.util.ConsoleTable;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 *
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2019-6-18
 *    fix->1.190618修补--当只有查询出内容后在进行邮件发送
 *         2.191114修补--支持更新SQL语句
 * </pre>
 */
public class MailContext {

	private String dataSource;
	private String sqlString;
	private boolean hideHeader;
	private int adjustnum;
	private String mailConf;
	private String title;
	private String scheduleRun;
	private StringBuilder mailcontext = new StringBuilder();
	private ConsoleTable cosoleTable;
	private final static Logger log = Logger.getLogger(MailContext.class);

	public MailContext(String dataSource, String sqlString, String mutiTitle, String mailConf, String title,
			String scheduleRun) {
		super();
		this.dataSource = dataSource;
		this.sqlString = sqlString;

		this.hideHeader = Boolean.parseBoolean(Tools.get_hideHeader_count(mutiTitle)[0]);
		this.adjustnum = Integer.parseInt(Tools.get_hideHeader_count(mutiTitle)[1]);

		this.mailConf = mailConf;
		this.title = Tools.translateString(title);
		this.scheduleRun = scheduleRun;
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("MAIL")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			k = GenQuery(dataSource, sqlString);

			log.info("待发送内容\n" + mailcontext.toString());

			k += this.adjustnum;
			//发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
			if (k > 0)//如果文件有数量则进行发送
				SendMail.main(new String[] { mailcontext.toString(), "", "./config/" + this.mailConf });
			else
				log.info("待发送的记录条数为0，取消邮件发送...");
			msg = "成功";
		} catch (Throwable e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k).appendColum("").appendColum(msg.replace("\n", ""));
		}
	}

	private int GenQuery(String dateSource, String sql) throws Exception {
		JConnection jc = null;
		try {
			//log.debug("加载SQL语句:"+sql);
			String sqlArr[] = Tools.getSql(sql);
			jc = new JConnection(dateSource);
			int i = 0, j = 0;
			for (String elem : sqlArr) {
				//log.debug("加载SQL："+elem);
				//如果不是查询语句，是更新语句，则进行更新
				if (!elem.matches("^(?i)select.*")) {
					//不是查询语句
					log.info("准备执行更新语句:" + elem);
					jc.exeUpdate(elem);//默认自动提交
					continue;
				}
				log.info("准备执行查询语句:" + elem);
				List<String[]> qryres = jc.queryAsList(elem);
				j = j + qryres.size() - 1;

				if (!this.hideHeader) {
					//多标题--不隐藏标题头
					if (null != cosoleTable)
						this.mailcontext.append("\n\n").append(cosoleTable.toString());
					cosoleTable = new ConsoleTable(qryres.get(0).length, true);
				} else {
					//单标题--隐藏标题头
					if (i <= 0) {
						//第一次
						cosoleTable = new ConsoleTable(qryres.get(0).length, true);
						//j--;
					}
					qryres.remove(0);
				}
				addListToCosoleTable(qryres);
				i++;
			}//for
			this.mailcontext.append("\n\n").append(cosoleTable.toString());
			return j;
		} finally {
			if (null != jc)
				jc.close();
		}
	}

	private void addListToCosoleTable(List<String[]> list) {
		for (String[] elem : list) {
			this.cosoleTable.appendRow();
			for (String elem2 : elem) {
				this.cosoleTable.appendColum(elem2);
			}
		}
	}

}
