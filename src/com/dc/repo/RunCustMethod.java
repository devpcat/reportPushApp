package com.dc.repo;

import org.apache.log4j.Logger;

public class RunCustMethod {

	private String title;
	private String method;
	private String scheduleRun;
	private String paramString;

	private final static Logger log = Logger.getLogger(RunCustMethod.class);

	public RunCustMethod(String title, String method, String paramString, String scheduleRun) {
		super();
		this.title = title;
		this.method = method;
		this.scheduleRun = scheduleRun;
		this.paramString = paramString;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void schedule() {
		if (!RunDecider.decide(scheduleRun))
			return;
		Tools.consoleTable.appendRow();
		Tools.consoleTable.appendColum(++Tools.rownum).appendColum("CLASS")
				.appendColum(com.dc.util.Tools.getCurrentWorkTime());
		//int k = 0;
		String msg = "未知";
		ResultBean resBean = new ResultBean();
		try {
			int index = method.lastIndexOf(".");
			String cls = method.substring(0, index);
			String mtd = method.substring(index + 1);
			log.debug("准备反射调用-->类名:" + cls + " 方法：" + mtd);
			Class cl = Class.forName(cls);
			cl.getMethod(mtd, String[].class, ResultBean.class).invoke(null,
					new Object[] { paramString.split("#"), resBean });
			msg = "成功";
		} catch (Exception e) {
			if (null == e.getCause())
				msg = e.getMessage();
			else
				msg = e.getCause().getMessage();
			msg = (null == msg || msg.isEmpty()) ? "NULL" : msg;
			Tools.succ = false;
			log.error(com.dc.util.Tools.getStackTrace(e));
		} finally {
			// 序号 | 类型    | 开始时间 | 结束时间 | 描述                                    | 数量  | 文件名                                | 返回信息
			Tools.consoleTable.appendColum(com.dc.util.Tools.getCurrentWorkTime()).appendColum(title)
					.appendColum(resBean.getNum()).appendColum(resBean.getFilename())
					.appendColum(msg.replace("\n", ""));
		}
	}
}
