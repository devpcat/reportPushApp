package com.dc.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.dc.util.MyFlowException;

public class ZipFiles {

	private String outputfile;
	private List<String> inputfileList;
	private String scheduleRun;
	private String title;
	private final static Logger log = Logger.getLogger(ZipFiles.class);

	public ZipFiles(String title, String outputfile, String scheduleRun, List<String> inputfileList) {
		super();
		this.outputfile = Tools.translateString(outputfile) + ".zip";
		this.inputfileList = inputfileList;
		this.scheduleRun = scheduleRun;
		this.title = Tools.translateString(title);
	}

	public void schedule() {
		if (!RunDecider.decide(scheduleRun))
			return;
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("ZIPFL")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		int k = 0;
		String msg = "未知";
		try {
			List<String> fileslist = new ArrayList<String>();
			for (String elem : inputfileList) {
				String tmparr[] = elem.split(";");
				if (!new File(tmparr[0]).isFile()) {
					if (!Boolean.parseBoolean(tmparr[1]))
						throw new MyFlowException("文件[" + tmparr[0] + "]不存在 ");
				} else {
					fileslist.add(tmparr[0]);
				}
			}
			Tools.zip(outputfile, fileslist);
			k += fileslist.size();
			msg = "成功";
		} catch (Exception e) {
			msg = e.getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(k).appendColum(Tools.getFileName(outputfile)).appendColum(msg.replace("\n", ""));
		}
	}
}
