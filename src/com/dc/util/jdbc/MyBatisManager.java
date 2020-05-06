package com.dc.util.jdbc;

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

/**
 * MyBatis管理, 单例对象.
 *
 * @author kfzx-zhaowei
 * @date 2014-4-11下午05:46:47
 */
public class MyBatisManager {

	private static final Logger log = Logger.getLogger(MyBatisManager.class);
	private static SqlSessionFactory sqlSessionFactory = null;

	private String mapfilePath = null;

	public String getMapfilePath() {
		return mapfilePath;
	}

	public void setMapfilePath(String mapfilePath) {
		this.mapfilePath = mapfilePath;
	}

	public MyBatisManager() throws Exception {
		setMapfilePath("./config" + "/sql-map-config.xml");
		if (sqlSessionFactory == null) {
			InputStream resourceAsStream = new FileInputStream(mapfilePath);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
			log.info("build sqlSessionFactory");
		}

	}

	public void initSqlSessionFactory() throws Exception {
		if (sqlSessionFactory == null) {
			InputStream resourceAsStream = new FileInputStream(mapfilePath);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
		}
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public static final ThreadLocal<SqlSession> session = new ThreadLocal<SqlSession>();

	public static SqlSession getCurrentSession() {
		try {
			SqlSession s = session.get();
			if (s == null) {
				// s = ((MyBatisManager)
				// FileSystemXmlApplicationContext.get("MyBatisManager")).getSqlSessionFactory().openSession();
				s = new MyBatisManager().getSqlSessionFactory().openSession();
				session.set(s);
				log.info("open session");
			}
			s.clearCache();
			return s;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// public static void initCurrentSession() throws Exception {
	// SqlSession s = ((MyBatisManager)
	// FileSystemXmlApplicationContext.get("MyBatisManager")).getSqlSessionFactory()
	// .openSession();
	// session.set(s);
	// }

	public static void clearSession() {
		SqlSession s = session.get();
		if (s != null) {
			session.set(null);
			s.close();
			log.info("close session");
		}
	}

}
