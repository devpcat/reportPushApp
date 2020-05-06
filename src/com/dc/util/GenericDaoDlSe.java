package com.dc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.util.jdbc.MyBatisManager;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

/**
 * ͨ�����ݿ�����ڶ���
 *
 * @author dlfh-yuc02
 * @time 2015-09-01 14:48
 */
public class GenericDaoDlSe<T> {

	private static final Logger log = Logger.getLogger(GenericDaoDlSe.class);

	protected SqlSession getSqlSession() {
		return MyBatisManager.getCurrentSession();
	}

	private int selectByExample(List<T> reslist, Object example) throws MySqlException {
		int retcode = -1;
		String exampleClassName = example.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.selectByExample";
		List<T> list = null;
		reslist.clear();
		try {
			SqlSession sqlsession = getSqlSession();
			log.debug(MapperName + ":��ʼ��ѯ");
			list = sqlsession.selectList(MapperName, example);
			retcode = list.size();
			for (T t : list) {
				reslist.add(t);
			}
			sqlsession.commit(true);
		} catch (Exception e) {
			retcode = -2;
			log.error("��ѯ�쳣");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(11, e.getMessage());
		}
		log.debug(MapperName + ":��ѯ��������ѯ���룺" + retcode);
		return retcode;
	}

	public List<T> selectByExample(Object example) throws MySqlException {
		String exampleClassName = example.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.selectByExample";
		List<T> list = null;
		try {
			SqlSession sqlsession = getSqlSession();
			log.debug(MapperName + ":��ʼ��ѯ");
			list = sqlsession.selectList(MapperName, example);
			sqlsession.commit(true);
		} catch (Exception e) {
			log.error("��ѯ�쳣");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(12, e.getMessage());
		}
		log.debug(MapperName + ":��ѯ��������ѯ����:" + list.size());
		return list;
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-17 ����02:45:54
	 * @param example
	 * @return
	 * @throws MySqlException
	 */
	public List<T> selectByString(String str, String mappername) throws MySqlException {
		String MapperName = mappername;
		List<T> list = null;
		try {
			SqlSession sqlsession = getSqlSession();
			log.debug(MapperName + ":��ʼ��ѯ");
			list = sqlsession.selectList(mappername, "");
			sqlsession.commit(true);
		} catch (Exception e) {
			log.error("��ѯ�쳣");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(13, e.getMessage());
		}
		log.debug(MapperName + ":��ѯ��������ѯ����:" + list.size());
		return list;
	}

	public T selectByExample_SingleResult(Object example) throws MySqlException {
		List<T> reslist = new ArrayList<T>();
		int retcode = selectByExample(reslist, example);
		if (1 == retcode) {
			return reslist.get(0);
		} else if (retcode > 1 || 0 == retcode) {
			log.error(example.getClass().getName() + "������ѯ[1]����¼��ʵ�ʲ�ѯ[" + retcode + "]����¼");
			throw new MySqlException("������ѯ[1]����¼��ʵ�ʲ�ѯ[" + retcode + "]����¼");
		} else {
			log.error("��ѯ������ѯ������" + retcode);
			return null;
		}
	}

	/**
	 * ͨ����������db���˷���Ҫ�����ݿ�������������
	 *
	 * @author dlfh-yuc02
	 * @time 2015-9-8 ����08:34:45
	 * @param updobject
	 * @return
	 * @throws MySqlException
	 */
	public int updateByPrimaryKey(T updobject) throws MySqlException {
		int retcode = -9;
		log.debug(updobject.getClass().getName() + "��ʼ����");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.update(updobject.getClass().getName() + "Mapper.updateByPrimaryKeySelective",
					updobject);
			sqlsession.commit(true);// �ύ
			log.debug(updobject.getClass().getName() + "�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			log.error("����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(21, e.getMessage());
		}
		return retcode;
	}

	/**
	 * @author dlfh-yuc02
	 * @time 2015-9-8 ����08:59:56
	 * @param updobject
	 * @return
	 * @throws MySqlException
	 */
	public int updateByExampleSelective(T updateValueObject, Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("record", updateValueObject);
		map.put("example", whereExampleClause);
		log.debug(updateValueObject.getClass().getName() + "��ʼ����");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession
					.update(updateValueObject.getClass().getName() + "Mapper.updateByExampleSelective", map);
			sqlsession.commit(true);// �ύ
			log.debug(updateValueObject.getClass().getName() + "�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			log.error("����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(22, e.getMessage());
		}

		return retcode;
	}

	/**
	 * �����Լ���ӵ�mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-20 ����09:31:08
	 * @return
	 * @throws MySqlException
	 */
	public int updateByString(String param, String mappername) throws MySqlException {
		int retcode = -9;
		log.debug(mappername + ":��ʼ����");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.update(mappername, param);
			sqlsession.commit(true);// �ύ
			log.debug(mappername + ":�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			throw new MySqlException(23, e.getMessage());
		}
		return retcode;
	}

	/**
	 * ����һ����¼
	 *
	 * @author dlfh-yuc02
	 * @time 2016-1-27 ����10:24:11
	 * @param updateValueObject
	 * @param whereExampleClause
	 * @return
	 * @throws MySqlException
	 */
	public void updateOneByExampleSelective(T updateValueObject, Object whereExampleClause) throws MySqlException {
		int ret = updateByExampleSelective(updateValueObject, whereExampleClause);
		if (1 != ret) {
			log.error("��������[1]����¼ʵ�ʸ���[" + ret + "]����¼");
			throw new MySqlException(24, "��������[1]����¼ʵ�ʸ���[" + ret + "]����¼");
		}
	}

	/**
	 * ָ��exampleɾ��db����
	 *
	 * @author dlfh-yuc02
	 * @time 2015-9-8 ����10:22:24
	 * @param whereExampleClause
	 * @return
	 * @throws MySqlException
	 */
	public int deleteByExample(Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		String exampleClassName = whereExampleClause.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.deleteByExample";
		log.debug(MapperName + "��ʼɾ��");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.delete(MapperName, whereExampleClause);
			sqlsession.commit(true);
			log.debug(MapperName + "�ɹ�ɾ��");
		} catch (Exception e) {
			retcode = -1;
			log.error("ɾ��ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(31, e.getMessage());
		}
		return retcode;
	}

	public int countByExample(Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		String exampleClassName = whereExampleClause.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.countByExample";
		log.debug(MapperName + "��ʼ��ѯ");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.selectOne(MapperName, whereExampleClause);
			sqlsession.commit(true);
			log.debug(MapperName + "�ɹ���ѯ");
		} catch (Exception e) {
			retcode = -1;
			log.error("��ѯʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(51, e.getMessage());
		}
		return retcode;
	}

	/**
	 * @author dlfh-yuc02
	 * @time 2015-9-17 ����09:50:07
	 * @param object
	 * @throws MySqlException
	 */
	public int insertSelective(T obj) throws MySqlException {
		int retcode = -9;
		SqlSession sqlsession = getSqlSession();
		try {
			String MapperName = obj.getClass().getName() + "Mapper.insertSelective";
			log.debug(MapperName + "��ʼ����");
			retcode = sqlsession.insert(MapperName, obj);
			sqlsession.commit(true);
			log.debug(MapperName + "�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			log.error("����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(41, e.getMessage());
		}
		return retcode;
	}

	public int insertOneSelective(T obj) throws MySqlException {
		int ret = insertSelective(obj);
		if (1 != ret) {
			log.error("��������[1]����¼ʵ�ʲ���[" + ret + "]����¼");
			throw new MySqlException("��������[1]����¼ʵ�ʲ���[" + ret + "]����¼");
		}
		return 0;
	}

	/**
	 * �����Լ�д��mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-18 ����11:09:53
	 * @param str
	 * @param mappername
	 * @return
	 * @throws MySqlException
	 */
	public int insertByString(String str, String mappername) throws MySqlException {
		int retcode = -9;
		SqlSession sqlsession = getSqlSession();
		try {
			String MapperName = mappername;
			log.debug(MapperName + "��ʼ����");
			retcode = sqlsession.insert(MapperName, str);
			sqlsession.commit(true);
			log.debug(MapperName + "�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			log.error("����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(42, e.getMessage());
		}
		return retcode;
	}

	/**
	 * �����Լ�д��mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-18 ����11:10:11
	 * @param mappername
	 * @return
	 * @throws MySqlException
	 */
	public int insertByNoParam(String mappername) throws MySqlException {
		int retcode = -9;
		SqlSession sqlsession = getSqlSession();
		try {
			String MapperName = mappername;
			log.debug(MapperName + "��ʼ����");
			retcode = sqlsession.insert(MapperName);
			sqlsession.commit(true);
			log.debug(MapperName + "�ɹ�����");
		} catch (Exception e) {
			retcode = -1;
			log.error("����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
			throw new MySqlException(43, e.getMessage());
		}
		return retcode;
	}

}// class
