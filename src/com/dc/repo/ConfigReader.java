package com.dc.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.util.Tools;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 配置文件读取
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-2
 *    fix->1.
 *         2.
 * </pre>
 */
@SuppressWarnings("unchecked")
public class ConfigReader {

	private static Document document;
	private static Element rootElement;
	private static Map<String, String[]> dataSource;
	private static Map<String, String> appConf;// app系统配置
	private final static Logger log = Logger.getLogger(ConfigReader.class);

	static {
		SAXBuilder sb = new SAXBuilder();
		try {
			document = sb.build("./config/" + com.dc.repo.Tools.configPath);
			rootElement = document.getRootElement();
			dataSource = getConnectionConf();
			appConf = getAppConf();
		} catch (Exception e) {
			log.error(Tools.getStackTrace(e));
		}
	}

	public static Document getDocument() {
		return document;
	}

	public static String[] getDataSource(String id) {
		return dataSource.get(id.toLowerCase());
	}

	public static String getAppConfValue(String key) {
		return appConf.get(key.toLowerCase());
	}

	/**
	 * 读取节点root/appconf节点
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午03:23:41
	 * @return
	 */
	private static Map<String, String> getAppConf() {
		List<Element> listElement = rootElement.getChild("appconf").getChildren("property");
		appConf = new HashMap<String, String>();
		for (Element propertyElement : listElement) {
			String name = propertyElement.getAttributeValue("name");
			String value = propertyElement.getAttributeValue("value");
			appConf.put(name.toLowerCase(), value);
			log.info("加载appconf配置[" + "name=" + name + " value=" + value + "]");
		}
		return appConf;
	}

	/**
	 * 获取连接配置信息
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-2 下午03:25:33
	 * @return
	 */
	private static Map<String, String[]> getConnectionConf() {
		List<Element> listElement = rootElement.getChild("environment").getChildren("dataSource");
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (Element dateSourcElement : listElement) {
			List<Element> listProperty = dateSourcElement.getChildren("property");
			String propertyArr[] = new String[4];
			for (Element propertyElement : listProperty) {
				switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
				case driver:
					propertyArr[0] = propertyElement.getAttributeValue("value");
					break;
				case url:
					propertyArr[1] = propertyElement.getAttributeValue("value");
					break;
				case username:
					propertyArr[2] = propertyElement.getAttributeValue("value");
					break;
				case password:
					propertyArr[3] = propertyElement.getAttributeValue("value");
					break;
				}
			}// inner-for
			map.put(dateSourcElement.getAttributeValue("id").toLowerCase(), propertyArr);
			log.info("加载数据源配置[" + "driver=" + propertyArr[0] + " url=" + propertyArr[1] + " username=" + propertyArr[2]
					+ " password=" + propertyArr[3] + "]");
		}// outer-for
		return map;
	}

	protected static enum properties {
		driver, url, username, password, exportexcel, mailconf, filename, //--
		compress, title, schedulerun, sheet, datasource, sql, mailcontext, //--
		//mutititle,
		maillocalfile, localfile, ftpfile, server, controlencoding, //--
		transmode, put, get, sftpfile, exportcsv, exporttext, separator, //--
		lastseparator, loadfixedlenfile, propertyfile, loadseparatorfile, //--
		customizedsql, customizedmethod, method, param, hideheader, zipfile, //--
		outfile, addfile, encoding,//
		exportexcel2,templatefilename;

		public static properties getproperty(String type) {
			return valueOf(type.toLowerCase());
		}
	}

	/**
	 * 按照配置文件顺序调度各个任务
	 *
	 * @author dlfh-yuc02
	 * @throws Exception
	 * @time 2016-5-26 上午08:51:47
	 */
	public static void schedule() {
		log.info("总控调度开始--->");
		List<Element> listElement = rootElement.getChild("statconf").getChildren("opertype");
		// 边读取配置边调用
		for (Element opertypeElement : listElement) {
			List<Element> listProperty = opertypeElement.getChildren("property");
			switch (properties.getproperty(opertypeElement.getAttributeValue("id"))) {
			// 发送excel
			case exportexcel: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				ArrayList<String[]> sheet = new ArrayList<String[]>(5);
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case mailconf:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case filename:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case compress:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case hideheader:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					case sheet:// sheet元素下可能包含子元素
					{
						String sheetArr[] = new String[3];
						sheetArr[0] = propertyElement.getAttributeValue("value");
						List<Element> sheetList = propertyElement.getChildren("property");
						for (Element sheetElem : sheetList) {
							switch (properties.getproperty(sheetElem.getAttributeValue("name"))) {
							case datasource:
								sheetArr[1] = sheetElem.getAttributeValue("value");
								break;
							case sql:
								sheetArr[2] = sheetElem.getAttributeValue("value");
								break;

							}
						}
						sheet.add(sheetArr);
						log.info(String.format("加载mailExcel-sheet配置[sheet=%s dataSource=%s sql=%s]", sheetArr[0],
								sheetArr[1], sheetArr[2]));
						break;
					}
					}
				}// inner-for
				log.info(String.format(
						"加载ExportExcel配置[mailConf=%s fileName=%s compress=%s title=%s hideheader=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[5], propertyArr[4]));
				// 发起调度命令
				new ExportExcel(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4], sheet,
						propertyArr[5]).schedule();
				break;
			}// switch-case-1
			case exportexcel2: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "","" };
				ArrayList<String[]> sheet = new ArrayList<String[]>(1);
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case mailconf:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case filename:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case compress:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case hideheader:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					case sheet:// sheet元素下可能包含子元素
					{
						String sheetArr[] = new String[3];
						sheetArr[0] = propertyElement.getAttributeValue("value");
						List<Element> sheetList = propertyElement.getChildren("property");
						for (Element sheetElem : sheetList) {
							switch (properties.getproperty(sheetElem.getAttributeValue("name"))) {
							case datasource:
								sheetArr[1] = sheetElem.getAttributeValue("value");
								break;
							case sql:
								sheetArr[2] = sheetElem.getAttributeValue("value");
								break;

							}
						}
						sheet.add(sheetArr);
						log.info(String.format("加载mailExcel-sheet配置[sheet=%s dataSource=%s sql=%s]", sheetArr[0],
								sheetArr[1], sheetArr[2]));
						break;
					}
					case templatefilename:
						propertyArr[6] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String.format(
						"加载ExportExcel2配置[mailConf=%s fileName=%s compress=%s title=%s hideheader=%s scheduleRun=%s templateFileName=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[5], propertyArr[4],propertyArr[6]));
				// 发起调度命令
				new ExportExcel2(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4], sheet,
						propertyArr[5],propertyArr[6]).schedule();
				break;
			}// switch-case-1-1
			case mailcontext: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case datasource:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case sql:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case hideheader:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case mailconf:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String.format(
						"加载mailContext配置[dataSource=%s sql=%s mutiTitle=%s mailConf=%s title=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4], propertyArr[5]));
				// 发起调度命令
				new MailContext(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4],
						propertyArr[5]).schedule();
				break;
			}// switch-case-2
			case maillocalfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case localfile:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case mailconf:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String.format("加载mailLocalFile配置[localFile=%s mailConf=%s title=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]));
				// 发起调度命令
				new MailFile(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]).schedule();
				break;

			}// switch-case-3

			case ftpfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				ArrayList<String> cmdList = new ArrayList<String>(5);
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case server:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case username:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case password:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case controlencoding:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case transmode:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[6] = propertyElement.getAttributeValue("value");
						break;
					case put:// put元素下可能包含子元素
					{
						String str1 = propertyElement.getAttributeValue("local");
						String str2 = propertyElement.getAttributeValue("remote");
						{
							//修改可以执行相对路径,但原绝对路径还不受影响 20191009
							if (str1.charAt(0) != '/' && str1.charAt(1) != ':') {
								str1 = getAppConfValue("fileBasePath") + str1;
							}
							/*
							 * if (str2.charAt(0) != '/' && str2.charAt(1) !=
							 * ':') { str2 = getAppConfValue("fileBasePath") +
							 * str2; }
							 */
						}
						String str3 = propertyElement.getAttributeValue("ignore");
						log.info("加载put参数配置[local=" + str1 + " remote=" + str2 + " ignore=" + str3);
						cmdList.add("put;" + str1 + ";" + str2 + ";" + str3);
						break;
					}
					case get:// get元素下可能包含子元素
					{
						String str1 = propertyElement.getAttributeValue("local");
						String str2 = propertyElement.getAttributeValue("remote");
						{
							//修改可以执行相对路径,但原绝对路径还不受影响 20191009
							if (str1.charAt(0) != '/' && str1.charAt(1) != ':') {
								str1 = getAppConfValue("fileBasePath") + str1;
							}
							/*
							 * if (str2.charAt(0) != '/' && str2.charAt(1) !=
							 * ':') { str2 = getAppConfValue("fileBasePath") +
							 * str2; }
							 */
						}
						String str3 = propertyElement.getAttributeValue("ignore");
						log.info("加载get参数配置[local=" + str1 + " remote=" + str2 + " ignore=" + str3);
						cmdList.add("get;" + str1 + ";" + str2 + ";" + str3);
						break;
					}
					}
				}// inner-for
				log.info(String
						.format("加载ftpFile配置[title=%s server=%s username=%s password=%s controlEncoding=%s transMode=%s scheduleRun=%s]",
								propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4],
								propertyArr[5], propertyArr[6]));
				// 发起调度命令
				new FtpFile(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4],
						propertyArr[5], cmdList, propertyArr[6]).schedule();
				break;
			}//switch-case-4
			case sftpfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				ArrayList<String> cmdList = new ArrayList<String>(5);
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case server:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case username:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case password:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case put:// put元素下可能包含子元素
					{
						String str1 = propertyElement.getAttributeValue("local");
						String str2 = propertyElement.getAttributeValue("remote");
						{
							//修改可以执行相对路径,但原绝对路径还不受影响 20191009
							if (str1.charAt(0) != '/' && str1.charAt(1) != ':') {
								str1 = getAppConfValue("fileBasePath") + str1;
							}
							/*
							 * if (str2.charAt(0) != '/' && str2.charAt(1) !=
							 * ':') { str2 = getAppConfValue("fileBasePath") +
							 * str2; }
							 */
						}
						String str3 = propertyElement.getAttributeValue("ignore");
						log.info("加载put参数配置[local=" + str1 + " remote=" + str2 + " ignore=" + str3);
						cmdList.add("put;" + str1 + ";" + str2 + ";" + str3);
						break;
					}
					case get:// get元素下可能包含子元素
					{
						String str1 = propertyElement.getAttributeValue("local");
						String str2 = propertyElement.getAttributeValue("remote");
						{
							//修改可以执行相对路径,但原绝对路径还不受影响 20191009
							if (str1.charAt(0) != '/' && str1.charAt(1) != ':') {
								str1 = getAppConfValue("fileBasePath") + str1;
							}
							/*
							 * if (str2.charAt(0) != '/' && str2.charAt(1) !=
							 * ':') { str2 = getAppConfValue("fileBasePath") +
							 * str2; }
							 */
						}
						String str3 = propertyElement.getAttributeValue("ignore");
						log.info("加载get参数配置[local=" + str1 + " remote=" + str2 + " ignore=" + str3);
						cmdList.add("get;" + str1 + ";" + str2 + ";" + str3);
						break;
					}
					}
				}// inner-for
				log.info(String.format("加载sftpFile配置[title=%s server=%s username=%s password=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4]));
				// 发起调度命令
				new SftpFile(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], cmdList, propertyArr[4])
						.schedule();
				break;
			}//switch-case-5

			case exportcsv: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case mailconf:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case filename:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case compress:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[6] = propertyElement.getAttributeValue("value");
						break;
					case datasource:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case sql:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String
						.format("加载ExportCsv配置[mailConf=%s fileName=%s compress=%s title=%s scheduleRun=%s dataSource=%s sql=%s]",
								propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[6],
								propertyArr[4], propertyArr[5]));
				// 发起调度命令(String0 mailConf, String 1fileName, String 2compress, String 3title, String 4dataSource, String 5sql, String 6scheduleRun)
				new ExportCsv(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4],
						propertyArr[5], propertyArr[6]).schedule();
				break;
			}// switch-case-6

			case exporttext: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "", "GBK" };//new String[10];
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case mailconf:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case filename:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case compress:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case title:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[6] = propertyElement.getAttributeValue("value");
						break;
					case datasource:
						propertyArr[4] = propertyElement.getAttributeValue("value");
						break;
					case sql:
						propertyArr[5] = propertyElement.getAttributeValue("value");
						break;
					case separator:
						propertyArr[7] = propertyElement.getAttributeValue("value");
						break;
					case lastseparator:
						propertyArr[8] = propertyElement.getAttributeValue("value");
						break;
					case hideheader:
						propertyArr[9] = propertyElement.getAttributeValue("value");
						break;
					case encoding:
						propertyArr[10] = propertyElement.getAttributeValue("value");
					}
				}// inner-for
				log.info(String
						.format("加载ExportCsv配置[mailConf=%s fileName=%s compress=%s title=%s scheduleRun=%s dataSource=%s sql=%s separator=%s lastSeparator=%s encoding=%s]",
								propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[6],
								propertyArr[4], propertyArr[5], propertyArr[7], propertyArr[8], propertyArr[10]));
				// 发起调度命令(String0 mailConf, String 1fileName, String 2compress, String 3title, String 4dataSource, String 5sql, String 6scheduleRun)
				new ExportText(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3], propertyArr[4],
						propertyArr[5], propertyArr[6], propertyArr[7], propertyArr[8], propertyArr[9], propertyArr[10])
						.schedule();
				break;
			}// switch-case-7

			case loadfixedlenfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };//new String[10];
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case datasource:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case propertyfile:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String.format("加载loadFixedLenFile配置[title=%s dataSource=%s propertyPath=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]));
				new LoadFixedLenFile(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]).schedule();
				break;
			}// switch-case-8

			case loadseparatorfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };//new String[10];
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case datasource:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case propertyfile:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[3] = propertyElement.getAttributeValue("value");
						break;
					}
				}// inner-for
				log.info(String.format("加载LoadSeparatorFile配置[title=%s dataSource=%s propertyPath=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]));
				new LoadSeparatorFile(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]).schedule();
				break;
			}// switch-case-9

			case customizedsql: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };//new String[10];
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case datasource:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case sql:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[3] = propertyElement.getAttributeValue("value");
					}
				}// inner-for
				log.info(String.format("加载CustomizedSql配置[title=%s dataSource=%s sql=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]));
				new RunCustSql(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]).schedule();
				break;
			}// switch-case-10

			case customizedmethod: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };//new String[10];
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case method:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case param:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[3] = propertyElement.getAttributeValue("value");
					}
				}// inner-for
				log.info(String.format("加载CustomizedNethod配置[title=%s method=%s param=%s scheduleRun=%s]",
						propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]));
				new RunCustMethod(propertyArr[0], propertyArr[1], propertyArr[2], propertyArr[3]).schedule();
				break;
			}// switch-case-11

			case zipfile: {
				String propertyArr[] = { "", "", "", "", "", "", "", "", "", "" };
				ArrayList<String> cmdList = new ArrayList<String>(5);
				for (Element propertyElement : listProperty) {
					switch (properties.getproperty(propertyElement.getAttributeValue("name"))) {
					case title:
						propertyArr[0] = propertyElement.getAttributeValue("value");
						break;
					case outfile:
						propertyArr[1] = propertyElement.getAttributeValue("value");
						break;
					case schedulerun:
						propertyArr[2] = propertyElement.getAttributeValue("value");
						break;
					case addfile:// put元素下可能包含子元素
					{
						String str1 = propertyElement.getAttributeValue("value");
						String str2 = propertyElement.getAttributeValue("ignore");
						cmdList.add(str1 + ";" + str2);
						break;
					}
					}
				}// inner-for
				log.info(String.format("加载zipfile配置[title=%s outfile=%s scheduleRun=%s]", propertyArr[0],
						propertyArr[1], propertyArr[2]));
				// 发起调度命令
				new ZipFiles(propertyArr[0], propertyArr[1], propertyArr[2], cmdList).schedule();
				break;
			}//switch-case-12

			}// outer-switch
		}// outer-for
		log.info("总控调度结束<---");
	}
}
