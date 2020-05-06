package com.dc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.util.jdbc.MyBatisManager;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

/**
 * 通用数据库操作第二版
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
			log.debug(MapperName + ":开始查询");
			list = sqlsession.selectList(MapperName, example);
			retcode = list.size();
			for (T t : list) {
				reslist.add(t);
			}
			sqlsession.commit(true);
		} catch (Exception e) {
			retcode = -2;
			log.error("查询异常");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(11, e.getMessage());
		}
		log.debug(MapperName + ":查询结束，查询代码：" + retcode);
		return retcode;
	}

	public List<T> selectByExample(Object example) throws MySqlException {
		String exampleClassName = example.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.selectByExample";
		List<T> list = null;
		try {
			SqlSession sqlsession = getSqlSession();
			log.debug(MapperName + ":开始查询");
			list = sqlsession.selectList(MapperName, example);
			sqlsession.commit(true);
		} catch (Exception e) {
			log.error("查询异常");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(12, e.getMessage());
		}
		log.debug(MapperName + ":查询结束，查询条数:" + list.size());
		return list;
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-17 下午02:45:54
	 * @param example
	 * @return
	 * @throws MySqlException
	 */
	public List<T> selectByString(String str, String mappername) throws MySqlException {
		String MapperName = mappername;
		List<T> list = null;
		try {
			SqlSession sqlsession = getSqlSession();
			log.debug(MapperName + ":开始查询");
			list = sqlsession.selectList(mappername, "");
			sqlsession.commit(true);
		} catch (Exception e) {
			log.error("查询异常");
			log.error(MapperName + ":" + e.getMessage());
			e.printStackTrace();
			throw new MySqlException(13, e.getMessage());
		}
		log.debug(MapperName + ":查询结束，查询条数:" + list.size());
		return list;
	}

	public T selectByExample_SingleResult(Object example) throws MySqlException {
		List<T> reslist = new ArrayList<T>();
		int retcode = selectByExample(reslist, example);
		if (1 == retcode) {
			return reslist.get(0);
		} else if (retcode > 1 || 0 == retcode) {
			log.error(example.getClass().getName() + "期望查询[1]条记录，实际查询[" + retcode + "]条记录");
			throw new MySqlException("期望查询[1]条记录，实际查询[" + retcode + "]条记录");
		} else {
			log.error("查询出错，查询返回码" + retcode);
			return null;
		}
	}

	/**
	 * 通过主键更新db，此方法要求数据库表必须设有主键
	 *
	 * @author dlfh-yuc02
	 * @time 2015-9-8 上午08:34:45
	 * @param updobject
	 * @return
	 * @throws MySqlException
	 */
	public int updateByPrimaryKey(T updobject) throws MySqlException {
		int retcode = -9;
		log.debug(updobject.getClass().getName() + "开始更新");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.update(updobject.getClass().getName() + "Mapper.updateByPrimaryKeySelective",
					updobject);
			sqlsession.commit(true);// 提交
			log.debug(updobject.getClass().getName() + "成功更新");
		} catch (Exception e) {
			retcode = -1;
			log.error("更新失败，异常信息：" + e.getMessage());
			throw new MySqlException(21, e.getMessage());
		}
		return retcode;
	}

	/**
	 * @author dlfh-yuc02
	 * @time 2015-9-8 上午08:59:56
	 * @param updobject
	 * @return
	 * @throws MySqlException
	 */
	public int updateByExampleSelective(T updateValueObject, Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("record", updateValueObject);
		map.put("example", whereExampleClause);
		log.debug(updateValueObject.getClass().getName() + "开始更新");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession
					.update(updateValueObject.getClass().getName() + "Mapper.updateByExampleSelective", map);
			sqlsession.commit(true);// 提交
			log.debug(updateValueObject.getClass().getName() + "成功更新");
		} catch (Exception e) {
			retcode = -1;
			log.error("更新失败，异常信息：" + e.getMessage());
			throw new MySqlException(22, e.getMessage());
		}

		return retcode;
	}

	/**
	 * 调用自己添加的mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-20 上午09:31:08
	 * @return
	 * @throws MySqlException
	 */
	public int updateByString(String param, String mappername) throws MySqlException {
		int retcode = -9;
		log.debug(mappername + ":开始更新");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.update(mappername, param);
			sqlsession.commit(true);// 提交
			log.debug(mappername + ":成功更新");
		} catch (Exception e) {
			retcode = -1;
			throw new MySqlException(23, e.getMessage());
		}
		return retcode;
	}

	/**
	 * 更新一条记录
	 *
	 * @author dlfh-yuc02
	 * @time 2016-1-27 上午10:24:11
	 * @param updateValueObject
	 * @param whereExampleClause
	 * @return
	 * @throws MySqlException
	 */
	public void updateOneByExampleSelective(T updateValueObject, Object whereExampleClause) throws MySqlException {
		int ret = updateByExampleSelective(updateValueObject, whereExampleClause);
		if (1 != ret) {
			log.error("期望更新[1]条记录实际更新[" + ret + "]条记录");
			throw new MySqlException(24, "期望更新[1]条记录实际更新[" + ret + "]条记录");
		}
	}

	/**
	 * 指定example删除db数据
	 *
	 * @author dlfh-yuc02
	 * @time 2015-9-8 上午10:22:24
	 * @param whereExampleClause
	 * @return
	 * @throws MySqlException
	 */
	public int deleteByExample(Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		String exampleClassName = whereExampleClause.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.deleteByExample";
		log.debug(MapperName + "开始删除");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.delete(MapperName, whereExampleClause);
			sqlsession.commit(true);
			log.debug(MapperName + "成功删除");
		} catch (Exception e) {
			retcode = -1;
			log.error("删除失败，异常信息：" + e.getMessage());
			throw new MySqlException(31, e.getMessage());
		}
		return retcode;
	}

	public int countByExample(Object whereExampleClause) throws MySqlException {
		int retcode = -9;
		String exampleClassName = whereExampleClause.getClass().getName();
		String MapperName = exampleClassName.substring(0, exampleClassName.length() - 7) + "Mapper.countByExample";
		log.debug(MapperName + "开始查询");
		SqlSession sqlsession = getSqlSession();
		try {
			retcode = sqlsession.selectOne(MapperName, whereExampleClause);
			sqlsession.commit(true);
			log.debug(MapperName + "成功查询");
		} catch (Exception e) {
			retcode = -1;
			log.error("查询失败，异常信息：" + e.getMessage());
			throw new MySqlException(51, e.getMessage());
		}
		return retcode;
	}

	/**
	 * @author dlfh-yuc02
	 * @time 2015-9-17 上午09:50:07
	 * @param object
	 * @throws MySqlException
	 */
	public int insertSelective(T obj) throws MySqlException {
		int retcode = -9;
		SqlSession sqlsession = getSqlSession();
		try {
			String MapperName = obj.getClass().getName() + "Mapper.insertSelective";
			log.debug(MapperName + "开始插入");
			retcode = sqlsession.insert(MapperName, obj);
			sqlsession.commit(true);
			log.debug(MapperName + "成功插入");
		} catch (Exception e) {
			retcode = -1;
			log.error("插入失败，异常信息：" + e.getMessage());
			throw new MySqlException(41, e.getMessage());
		}
		return retcode;
	}

	public int insertOneSelective(T obj) throws MySqlException {
		int ret = insertSelective(obj);
		if (1 != ret) {
			log.error("期望插入[1]条记录实际插入[" + ret + "]条记录");
			throw new MySqlException("期望插入[1]条记录实际插入[" + ret + "]条记录");
		}
		return 0;
	}

	/**
	 * 调用自己写的mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-18 上午11:09:53
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
			log.debug(MapperName + "开始插入");
			retcode = sqlsession.insert(MapperName, str);
			sqlsession.commit(true);
			log.debug(MapperName + "成功插入");
		} catch (Exception e) {
			retcode = -1;
			log.error("插入失败，异常信息：" + e.getMessage());
			throw new MySqlException(42, e.getMessage());
		}
		return retcode;
	}

	/**
	 * 调用自己写的mapper
	 *
	 * @author dlfh-yuc02
	 * @time 2015-11-18 上午11:10:11
	 * @param mappername
	 * @return
	 * @throws MySqlException
	 */
	public int insertByNoParam(String mappername) throws MySqlException {
		int retcode = -9;
		SqlSession sqlsession = getSqlSession();
		try {
			String MapperName = mappername;
			log.debug(MapperName + "开始插入");
			retcode = sqlsession.insert(MapperName);
			sqlsession.commit(true);
			log.debug(MapperName + "成功插入");
		} catch (Exception e) {
			retcode = -1;
			log.error("插入失败，异常信息：" + e.getMessage());
			throw new MySqlException(43, e.getMessage());
		}
		return retcode;
	}

}// class
