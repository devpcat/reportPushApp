package com.dc.repo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dc.util.ConsoleTable;
import com.dc.util.jdbc.JConnection;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

public class Tools {

	public static ConsoleTable consoleTable;
	public static int rownum = 0;
	public static boolean succ = true;
	public static String configPath = "reportConfig.xml";
	private final static Logger log = Logger.getLogger(Tools.class);
	public static final String pwd = "3400955804118373";

	/**
	 * 输入sql自动判定是文件还是单条sql并反馈sql数组
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午06:10:27
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public static String[] getSql(String sql) throws IOException {

		ArrayList<String> sqlArrayList = new ArrayList<String>();
		if (sql.endsWith(".sql") || sql.endsWith(".SQL")) {
			// 判定为sql文件
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./config/" + sql)), "GBK"));
				// 读取文件
				String line;
				StringBuilder onesql = new StringBuilder();
				// int i = 0;
				while (null != (line = br.readLine())) {
					line = line.replace("\n", "").replace("\r", "").trim();
					if (line.length() == 0 || line.startsWith("--") || line.startsWith("#"))
						continue;

					if (line.contains(";")) {
						// 包含；一条sql结束了
						String arr[] = line.split(";", -1);
						onesql.append(arr[0] + " ");
						sqlArrayList.add(onesql.toString().trim());

						// sql初始化
						onesql = new StringBuilder();
						onesql.append(null == arr[1] ? "" : arr[1]);
					} else {
						onesql.append(line + " ");
					}
				}
			} finally {
				if (null != br)
					br.close();
			}
		} else {
			// 单独一条sql
			String tmparr[] = sql.trim().split(";");
			for (String elem : tmparr)
				sqlArrayList.add(elem);
		}
		return sqlArrayList.toArray(new String[sqlArrayList.size()]);
	}

	/**
	 * 翻译特殊字符
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午07:05:22
	 * @param str
	 * @return
	 */
	public static String translateString(String str) {
		if (str.contains("${")) {
			int b = 2 + str.indexOf("${");
			int e = str.indexOf("}");
			String cmd = str.substring(b, e);
			String insertString = "";
			if (cmd.startsWith("to_date")) {
				int b2 = 8 + cmd.indexOf("to_date(");
				int e2 = cmd.indexOf(")");
				String cmdArr[] = cmd.substring(b2, e2).split(",");
				insertString = getRollDay(cmdArr[0], cmdArr[1]);
			} else {
			}
			return str.substring(0, b - 2) + insertString + str.substring(e + 1);
		} else
			return str;
	}

	/**
	 * 取出输入字符串中方括号内的数字
	 *
	 * @author dlfh-yuc02
	 * @time 2017-5-5 下午01:57:53
	 * @param str
	 * @return
	 */
	public static int getIndexFromSquareBracket(String str) {
		int index;
		int a = str.indexOf("[");
		if (a < 0) {
			index = -1;
		} else {
			int b = str.indexOf("]");
			index = Integer.parseInt(str.substring(a + 1, b));
		}
		return index;
	}

	/**
	 * 将字符串拆分获取隐藏标题头和调整数量
	 *
	 * @author dlfh-yuc02
	 * @time 2017-5-5 下午02:07:07
	 * @param str
	 * @return
	 */
	public static String[] get_hideHeader_count(String str) {
		String[] strarr = str.split("#");
		String[] result = new String[2];
		if (strarr.length == 1) {
			result[0] = strarr[0];
			result[1] = "0";
		} else if (strarr.length == 2) {
			result[0] = strarr[0];
			result[1] = strarr[1];
		} else {

		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(getRollDay("yyyy-MM-dd", "-30"));
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-3 下午01:17:24
	 * @param format
	 * @param rollString
	 * @return
	 */
	private static String getRollDay(String format, String rollString) {
		Date as = new Date(new Date().getTime() + Integer.parseInt(rollString) * 24 * 60 * 60 * 1000l);
		SimpleDateFormat matter1 = new SimpleDateFormat(format);
		String time = matter1.format(as);
		return time;
	}

	public static String ZipCompressing(String infilepath, boolean compress) throws IOException {
		if (compress) {
			try {
				File file = new File(infilepath);
				String outfile;
				if (-1 == infilepath.lastIndexOf("."))
					outfile = infilepath + ".zip";
				else
					outfile = infilepath.substring(0, infilepath.lastIndexOf(".")) + ".zip";
				zip(outfile, infilepath);
				file.delete();
				return outfile;
			} catch (Exception e) {
				log.error(com.dc.util.Tools.getStackTrace(e));
				return infilepath;
			}
		} else {
			return infilepath;
		}
	}

	public static void zip(String desfil, String srcfile) {
		Project prj = new Project();
		Zip zip = new Zip();
		zip.setProject(prj);
		zip.setDestFile(new File(desfil));
		FileSet fileSet = new FileSet();
		fileSet.setProject(prj);
		//fileSet.setDir(new File("C:\\Users\\ICBC\\Desktop\\apache-ant-1.9.9\\lib\\ant.jar"));
		fileSet.setFile(new File(Tools.translateString(srcfile)));
		zip.addFileset(fileSet);
		zip.execute();
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-9 下午03:31:50
	 * @param desfil
	 * @param srcfileArr
	 */
	public static void zip(String desfil, List<String> srcfileList) {
		Project prj = new Project();
		Zip zip = new Zip();
		zip.setProject(prj);
		zip.setDestFile(new File(desfil));
		FileSet fileSet = new FileSet();
		fileSet.setProject(prj);
		//fileSet.setDir(new File("C:\\Users\\ICBC\\Desktop\\apache-ant-1.9.9\\lib\\ant.jar"));
		//fileSet.setFile(new File(srcfile));
		for (String elem : srcfileList)
			fileSet.setFile(new File(elem));
		zip.addFileset(fileSet);
		zip.execute();
	}

	public static String getFileName(String filePath) {
		// 拆分文件名
		String filearr[] = filePath.split("\\\\");
		if (filearr.length == 1)
			filearr = filePath.split("/");
		return filearr[filearr.length - 1];
	}

	/**
	 * 反馈0-文件名+1-扩展名
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-20 上午11:31:20
	 * @param filePath
	 * @return
	 */
	public static String[] getFileExtensionName(String filePath) {
		int index = filePath.lastIndexOf(".");
		if (-1 == index) {
			return new String[] { filePath, "" };
		} else {
			return new String[] { filePath.substring(0, index), filePath.substring(index) };
		}
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-21 下午02:23:07
	 * @param src
	 * @param srcPos
	 * @param length
	 * @return
	 */
	public static String subString(byte[] src, int srcPos, int length) {
		byte[] byRet = new byte[length];
		System.arraycopy(src, srcPos, byRet, 0, length);
		return new String(byRet).trim();
	}

	/**
	 * 接收sqlfile的输入，返回更新条数 如果成功则提交，如果失败则回滚
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-23 下午05:35:47
	 * @param jc
	 * @param sql
	 * @throws Exception
	 */
	public static synchronized int execUpdateSql(JConnection jc, String sql) throws Exception {

		if (null == sql || sql.isEmpty())
			return 0;

		int num = 0;
		//获取sql数组
		String sqlArr[] = Tools.getSql(sql);
		Statement statement = null;
		try {
			statement = jc.getStatement();
			for (String elem : sqlArr) {
				if (elem.trim().toLowerCase().startsWith("select"))
					continue;
				log.info("准备执行sql-->" + elem);
				num += statement.executeUpdate(elem);
			}
			jc.commit();
			return num;
		} catch (Exception e) {
			jc.rollback();
			throw e;
		} finally {
			if (null != statement)
				statement.close();
		}
	}
}
