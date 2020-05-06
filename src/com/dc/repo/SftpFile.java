package com.dc.repo;

import java.util.List;

import com.dc.util.SftpClient;
import org.apache.log4j.Logger;

public class SftpFile {

	private String host;
	private int port;
	private String username;
	private String password;
	private List<String> cmdList;
	private String scheduleRun;
	private String title;

	private final static Logger log = Logger.getLogger(SftpFile.class);

	public SftpFile(String title, String server, String username, String password, List<String> cmdList,
			String scheduleRun) {
		String tmpArr[];
		tmpArr = server.split(":");
		this.host = tmpArr[0];
		if (tmpArr.length == 2) {
			this.port = Integer.parseInt(tmpArr[1]);
		} else {
			this.port = 22;
		}
		this.username = username;
		this.password = password;
		this.cmdList = cmdList;
		this.scheduleRun = scheduleRun;
		this.title = Tools.translateString(title);
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("SFTP")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		String msg = "未知";
		String msgtmp = "";
		int i = 0;
		try {

			String passwd;
			if ("false".equalsIgnoreCase(ConfigReader.getAppConfValue("passwdEncrypt")))
				passwd = password;
			else
				passwd = new String(com.dc.util.Tools.decrypt(password.getBytes(), "34095580"));

			SftpClient sftp = new SftpClient(host, port, username, passwd);
			try {
				sftp.connect();
				for (String elem : cmdList) {
					String tmpArr[] = elem.split(";");
					try {
						if ("put".equalsIgnoreCase(tmpArr[0])) {
							sftp.put(Tools.translateString(tmpArr[1]), Tools.translateString(tmpArr[2]));
						} else if ("get".equalsIgnoreCase(tmpArr[0])) {
							sftp.get(Tools.translateString(tmpArr[1]), Tools.translateString(tmpArr[2]));
						}
						i++;
						filename += (Tools.getFileName(Tools.translateString(tmpArr[2])) + " ");
					} catch (Exception e) {
						log.error("文件传输失败-->" + e.getMessage());
						if (!"true".equalsIgnoreCase(tmpArr[3]))
							throw e;
						else {
							if (null == e.getCause())
								msgtmp += e.getMessage() + "[忽略] ";
							else
								msgtmp += e.getCause().getMessage() + "[忽略] ";
							log.error(com.dc.util.Tools.getStackTrace(e));
						}
					}
				}
				if (filename.getBytes().length >= 45) {
					filename = "...";
				}
				if (msgtmp.isEmpty())
					msg = "成功";
				else
					msg = msgtmp;
			} finally {
				sftp.close();
			}
		} catch (Throwable e) {
			if (null == e.getCause())
				msg = e.getMessage();
			else
				msg = e.getCause().getMessage();
			msg.replace("\n", "").replace("\r", "");
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(i).appendColum(filename).appendColum(msg);
		}
	}
}
