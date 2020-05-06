package com.dc.repo;

import java.io.File;

import org.apache.log4j.Logger;
import com.icbc.dlfh.util.mail.SendMail;

public class MailFile {

	private String localFile;
	private String mailConf;
	private String title;
	private String scheduleRun;
	private final static Logger log = Logger.getLogger(MailFile.class);

	public MailFile(String localFile, String mailConf, String title, String scheduleRun) {
		super();
		this.localFile = Tools.translateString(localFile);
		this.mailConf = mailConf;
		this.title = Tools.translateString(title);
		this.scheduleRun = scheduleRun;
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("MFILE")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		String msg = "未知";
		try {
			File file = new File(this.localFile);
			if (!file.exists()) {
				throw new Exception("文件[" + this.localFile + "]不存在");
			}

			if (!file.isFile()) {
				throw new Exception("路径[" + this.localFile + "]指向非文件");
			}

			// 发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
			SendMail.main(new String[] { "", "", "./config/" + this.mailConf, this.localFile });
			filename = Tools.getFileName(this.localFile);
			msg = "成功";
		} catch (Throwable e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(1).appendColum(filename).appendColum(msg);
		}
	}

}
