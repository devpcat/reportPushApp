package com.dc.reflect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.dc.util.TCommSender;
import com.dc.util.Tools;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.dc.repo.ResultBean;

/**
 *
 * TODO �����������
 *
 * <pre>
 * �������ɷѶ����ļ�֪ͨ
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2019-9-24
 *    fix->1.
 *         2.
 * </pre>
 */
public class OMC_NotifyMethod {

	private final static Logger log = Logger.getLogger(OMC_NotifyMethod.class);

	public static void exec(String[] args, ResultBean resBean) throws Exception {
		String datasource = args[0];//����Դ
		String trxcode = args[1];//���״���
		String qrysql = args[2];//��ѯ���

		JConnection jc = null;
		PreparedStatement ps = null;
		try {
			jc = new JConnection(datasource);
			List<String[]> list = jc.queryAsList(qrysql);
			TCommSender sender = new TCommSender("./config/omcpMessage.xml");
			String[] elem1 = list.get(0);
			String[] elem2 = list.get(1);
			for (int i = 0; i < elem1.length; i++) {
				sender.setValue("/omcp/send/" + elem1[i], elem2[i]);
			}
			sender.setValue("/Public/TrxCode", trxcode);
			sender.send();
			resBean.setNum("1");
		} finally {
			String msg = "";
			if (null != ps)
				try {
					ps.close();
				} catch (SQLException e) {
					msg = e.getMessage();
					log.error(Tools.getStackTrace(e));
				}
			if (jc != null)
				try {
					jc.close();
				} catch (SQLException e) {
					msg = e.getMessage();
					log.error(Tools.getStackTrace(e));
				}

			//������ĸ�������true����Ա���
			if (args.length == 4 && "true".equalsIgnoreCase(args[3])) {
				msg = "";
			}

			if (!msg.isEmpty())
				throw new SQLException(msg);
		}
	}
}
