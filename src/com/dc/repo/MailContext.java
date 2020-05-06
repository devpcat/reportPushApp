package com.dc.repo;

import java.util.List;

import com.dc.util.ConsoleTable;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO �����������
 *
 * <pre>
 *
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2019-6-18
 *    fix->1.190618�޲�--��ֻ�в�ѯ�����ݺ��ڽ����ʼ�����
 *         2.191114�޲�--֧�ָ���SQL���
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
		String msg = "δ֪";
		try {
			k = GenQuery(dataSource, sqlString);

			log.info("����������\n" + mailcontext.toString());

			k += this.adjustnum;
			//�����ʼ�-[����1-�ʼ����ݣ�����2-�ʼ����⣬����3-�����ļ� ������4-����]
			if (k > 0)//����ļ�����������з���
				SendMail.main(new String[] { mailcontext.toString(), "", "./config/" + this.mailConf });
			else
				log.info("�����͵ļ�¼����Ϊ0��ȡ���ʼ�����...");
			msg = "�ɹ�";
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
			//log.debug("����SQL���:"+sql);
			String sqlArr[] = Tools.getSql(sql);
			jc = new JConnection(dateSource);
			int i = 0, j = 0;
			for (String elem : sqlArr) {
				//log.debug("����SQL��"+elem);
				//������ǲ�ѯ��䣬�Ǹ�����䣬����и���
				if (!elem.matches("^(?i)select.*")) {
					//���ǲ�ѯ���
					log.info("׼��ִ�и������:" + elem);
					jc.exeUpdate(elem);//Ĭ���Զ��ύ
					continue;
				}
				log.info("׼��ִ�в�ѯ���:" + elem);
				List<String[]> qryres = jc.queryAsList(elem);
				j = j + qryres.size() - 1;

				if (!this.hideHeader) {
					//�����--�����ر���ͷ
					if (null != cosoleTable)
						this.mailcontext.append("\n\n").append(cosoleTable.toString());
					cosoleTable = new ConsoleTable(qryres.get(0).length, true);
				} else {
					//������--���ر���ͷ
					if (i <= 0) {
						//��һ��
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
