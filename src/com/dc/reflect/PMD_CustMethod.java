package com.dc.reflect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.dc.util.Tools;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import com.dc.repo.ResultBean;

/**
 *
 * TODO �����������
 *
 * <pre>
 * �Զ��巽��������sqlʵ���޷������Ӧ��
 * �����еķ�����ȫ��Ϊ��̬�ģ����������
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-24
 *    fix->1.
 *         2.
 * </pre>
 */
public class PMD_CustMethod {

	private final static Logger log = Logger.getLogger(PMD_CustMethod.class);

	/**
	 * ����item���
	 *
	 * @author dlfh-yuc02
	 * @throws Exception
	 * @time 2017-3-24 ����06:25:06
	 */
	public static void pmd_split_item(String[] args, ResultBean resBean) throws Exception {
		String datasource = args[0];
		String querysql = "select t.chan_serno,t.query_item from pmd_trxinfos t where t.deal_flag is null";
		String updsql = "update pmd_trxinfos t set t.item1=?,t.item2=?,t.item45=?,t.deal_flag='0' where t.chan_serno=?";
		JConnection jc = null;
		PreparedStatement ps = null;
		try {
			jc = new JConnection(datasource);
			//jc.setAutoCommit(false);
			List<String[]> list = jc.queryAsList(querysql);
			ps = jc.getPreparedStatement(updsql);
			list.remove(0);//ȥ��������
			log.debug("׼����������-->" + list.size() + " ִ��sql���-->" + updsql);
			for (String[] elem : list) {
				ps.setString(4, elem[0]);
				String tmpArr[] = elem[1].split("\\$\\|\\$", -1);
				ps.setString(1, tmpArr[15]);//16item1
				ps.setString(2, tmpArr[16]);//17item2
				ps.setString(3, tmpArr[59]);//60item45
				ps.executeUpdate();
			}
			resBean.setNum(String.valueOf(list.size()));
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
			if (!msg.isEmpty())
				throw new SQLException(msg);
		}
	}
}
