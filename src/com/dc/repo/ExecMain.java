package com.dc.repo;

import java.net.InetAddress;

import com.dc.util.ConsoleTable;
import com.dc.util.Tools;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.icbc.dlfh.util.mail.SendMail;

/**
 *
 * TODO �����������
 * <pre>
 *
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-2
 *    fix->1.20191009 �ʼ�֪֧ͨ���ļ�����
 *         2.
 * </pre>
 */
public class ExecMain {

	/**
	 * �������
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-6 ����06:08:25
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
			//׼�����
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();
			String ipt = ip;
			//log.info("��ǰIP��ַ[" + ip + "]");
			ip = new String(Tools.encrypt(ip.getBytes(), com.dc.repo.Tools.pwd));
			if (null == ConfigReader.getAppConfValue("accessControl")
					|| !ConfigReader.getAppConfValue("accessControl").contains(ip)) {
				log.error("��ǰIP��ַ[" + ipt + "]δע��,����ϵϵͳ����Ա!");
				return;
			}
		}

		log.info("APP����@" + Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime());

		String msg = "";
		try {
			com.dc.repo.Tools.consoleTable = new ConsoleTable(8, true);
			com.dc.repo.Tools.consoleTable.appendRow();
			com.dc.repo.Tools.consoleTable.appendColum("���").appendColum("����").appendColum("��ʼʱ��")
					.appendColum("����ʱ��").appendColum("����").appendColum("����").appendColum("�ļ���").appendColum("������Ϣ");

			ConfigReader.schedule();

		} catch (Throwable e) {
			msg = Tools.getStackTrace(e);
			log.error(msg);
		} finally {
			String resString = com.dc.repo.Tools.consoleTable.toString();
			log.error("���ε���ִ�н��:\n" + resString);
			if (com.dc.repo.Tools.succ && msg.isEmpty()
					&& "false".equalsIgnoreCase(ConfigReader.getAppConfValue("sendMail"))) {
			} else {
				// �����ʼ�-[����1-�ʼ����ݣ�����2-�ʼ����⣬����3-�����ļ� ������4-����]
				//				SendMail.main(new String[] { Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime() + "\n" + msg
				//						+ "\n" + resString });

				//���͵���֪ͨ�ʼ�-չ�ֱ��ε��ȵ�����״��
				try {
					SendMail sm = new SendMail();
					sm.setXmlConfigFileName(ConfigReader.getAppConfValue("sendMailConfig"));
					sm.setValuebyXmlConfig();
					if (!msg.isEmpty() || !com.dc.repo.Tools.succ)
						sm.setSubject("[�쳣]" + sm.getSubject());
					sm.setSendContext(Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime() + "\n" + msg + "\n"
							+ resString);
					sm.send();
				} catch (Throwable e) {
					log.error("���淢��ʧ��:" + e.getMessage());
				}
			}
		}
		log.info("APP�ر�@" + Tools.getCurrentWorkDate() + " " + Tools.getCurrentWorkTime());
	}
}
