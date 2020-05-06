package com.dc.repo;

import java.net.InetAddress;

import com.dc.util.ConsoleTable;
import com.dc.util.Tools;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO 类的描述：。
 * <pre>
 *
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-2
 *    fix->1.20191009 邮件通知支持文件配置
 *         2.
 * </pre>
 */
public class ExecMain {

	/**
	 * 程序入口
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-6 下午06:08:25
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (2 == args.length && args[0].equalsIgnoreCase("-config")) {
			com.dc.repo.Tools.configPath = args[1];
		}

		PropertyConfigurator.configure("./config" + "/log4j.properties");
		Logger log = Logger.getLogger(ExecMain.class);

		{
			//准入控制
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();
			String ipt = ip;
			//log.info("当前IP地址[" + ip + "]");
			ip = new String(Tools.encrypt(ip.getBytes(), com.dc.repo.Tools.pwd));
			if (null == ConfigReader.getAppConfValue("accessControl")
					|| !ConfigReader.getAppConfValue("accessControl").contains(ip)) {
				log.error("当前IP地址[" + ipt + "]未注册,请联系系统管理员!");
				return;
			}
		}

		log.info("APP启动@" + Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime());

		String msg = "";
		try {
			com.dc.repo.Tools.consoleTable = new ConsoleTable(8, true);
			com.dc.repo.Tools.consoleTable.appendRow();
			com.dc.repo.Tools.consoleTable.appendColum("序号").appendColum("类型").appendColum("开始时间")
					.appendColum("结束时间").appendColum("描述").appendColum("数量").appendColum("文件名").appendColum("返回信息");

			ConfigReader.schedule();

		} catch (Throwable e) {
			msg = Tools.getStackTrace(e);
			log.error(msg);
		} finally {
			String resString = com.dc.repo.Tools.consoleTable.toString();
			log.error("本次调度执行结果:\n" + resString);
			if (com.dc.repo.Tools.succ && msg.isEmpty()
					&& "false".equalsIgnoreCase(ConfigReader.getAppConfValue("sendMail"))) {
			} else {
				// 发送邮件-[参数1-邮件内容，参数2-邮件主题，参数3-配置文件 ，参数4-附件]
				//				SendMail.main(new String[] { Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime() + "\n" + msg
				//						+ "\n" + resString });

				//发送调度通知邮件-展现本次调度的运行状况
				try {
					SendMail sm = new SendMail();
					sm.setXmlConfigFileName(ConfigReader.getAppConfValue("sendMailConfig"));
					sm.setValuebyXmlConfig();
					if (!msg.isEmpty() || !com.dc.repo.Tools.succ)
						sm.setSubject("[异常]" + sm.getSubject());
					sm.setSendContext(Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime() + "\n" + msg + "\n"
							+ resString);
					sm.send();
				} catch (Throwable e) {
					log.error("报告发送失败:" + e.getMessage());
				}
			}
		}
		log.info("APP关闭@" + Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime());
	}
}
