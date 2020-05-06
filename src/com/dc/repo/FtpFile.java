package com.dc.repo;

import java.util.List;

import com.dc.util.FtpClient;
import org.apache.log4j.Logger;

public class FtpFile {

	private String host;
	private int port;
	private String username;
	private String password;
	private String controlEncoding;
	private boolean transMode_port_pasv;
	private boolean transMode_asc_bin;
	private List<String> cmdList;
	private String scheduleRun;
	private String title;

	private final static Logger log = Logger.getLogger(FtpFile.class);

	public FtpFile(String title, String server, String username, String password, String controlEncoding,
			String transMode, List<String> cmdList, String scheduleRun) {
		String tmpArr[];
		tmpArr = server.split(":");
		this.host = tmpArr[0];
		if (tmpArr.length == 2) {
			this.port = Integer.parseInt(tmpArr[1]);
		} else {
			this.port = 21;
		}
		this.username = username;
		this.password = password;
		if (null == controlEncoding || controlEncoding.trim().isEmpty())
			this.controlEncoding = "GBK";
		else
			this.controlEncoding = controlEncoding;

		if (null == transMode || transMode.trim().isEmpty()) {
			this.transMode_port_pasv = false;
			this.transMode_asc_bin = false;
		} else {
			tmpArr = transMode.split(";");
			if ("port".equalsIgnoreCase(tmpArr[0]))
				this.transMode_port_pasv = true;
			else
				this.transMode_port_pasv = false;
			if ("asc".equalsIgnoreCase(tmpArr[1]))
				this.transMode_asc_bin = true;
			else
				this.transMode_asc_bin = false;
		}
		this.cmdList = cmdList;
		this.scheduleRun = scheduleRun;
		this.title = Tools.translateString(title);
	}

	public void schedule() {

		if (!RunDecider.decide(scheduleRun))
			return;

		String filename = "";
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("FTP")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		String msg = "δ֪";
		String msgtmp = "";
		int i = 0;
		try {

			String passwd;
			if ("false".equalsIgnoreCase(ConfigReader.getAppConfValue("passwdEncrypt")))
				passwd = password;
			else
				passwd = new String(com.dc.util.Tools.decrypt(password.getBytes(), "34095580"));

			FtpClient ftp = new FtpClient(host, port, username, passwd, controlEncoding, transMode_port_pasv,
					transMode_asc_bin);
			try {
				ftp.connect();
				for (String elem : cmdList) {
					String tmpArr[] = elem.split(";");

					try {
						if ("put".equalsIgnoreCase(tmpArr[0])) {
							ftp.put(Tools.translateString(tmpArr[1]), Tools.translateString(tmpArr[2]));
						} else if ("get".equalsIgnoreCase(tmpArr[0])) {
							ftp.get(Tools.translateString(tmpArr[1]), Tools.translateString(tmpArr[2]));
						}
						i++;
						filename += (Tools.getFileName(Tools.translateString(tmpArr[2])) + " ");
					} catch (Exception e) {
						log.error("�ļ�����ʧ��-->" + e.getMessage());
						if (!"true".equalsIgnoreCase(tmpArr[3]))
							throw e;
						else {
							msgtmp += e.getMessage() + "[����] ";
							log.error(com.dc.util.Tools.getStackTrace(e));
						}
					}
				}
				if (filename.getBytes().length >= 45) {
					filename = "...";
				}
				if (msgtmp.isEmpty())
					msg = "�ɹ�";
				else
					msg = msgtmp;
			} finally {
				ftp.close();
			}
		} catch (Throwable e) {
			msg = e.getMessage().replace("\n", "").replace("\r", "");
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(i).appendColum(filename).appendColum(msg);
		}
	}
}
