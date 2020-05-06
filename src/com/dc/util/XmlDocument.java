package com.dc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * XML解析器，致力于通过xpath方式实现xml的读和写
 * 极大程度简化直接操作jdom的复杂繁琐
 * 依赖jdom
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-5-3
 *    fix->1.支持数组（多包）读和写
 *         2.2017-06-15 支持命名空间
 *         3.2017-06-20 新增方法
 *         4.2018-06-08 setDocument方法实现通过xml字符串声明自动设置字符集
 *         5.2018-06-08 增加clear清空文档的方法
 * </pre>
 */
public class XmlDocument {
	protected Document document;
	protected String encoding = "GBK";
	private SAXBuilder saxBuilder = new SAXBuilder(false);
	private Document freezeDocument;
	private Namespace namespace;

	public XmlDocument() {
		document = new Document();
		Element root = new Element("root");
		document.addContent(root);
	}

	public XmlDocument(Document document) {
		this.document = (Document) document.clone();
	}

	public XmlDocument(Element element) {
		this.document = new Document((Element) element.clone());
	}

	public XmlDocument(String xmlFilePath) throws JDOMException, IOException {
		document = saxBuilder.build(xmlFilePath);
	}

	private class NameIndex {
		protected int index;
		protected String name;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Index=" + index + " Name=" + name;
		}

		private NameIndex(String str) {
			// NameIndex ni = new NameIndex();
			int a = str.indexOf("[");
			if (a < 0) {
				this.index = -1;
				this.name = str;
			} else {
				int b = str.indexOf("]");
				this.index = Integer.parseInt(str.substring(a + 1, b));
				this.name = str.substring(0, a);
			}
			// return ni;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Element> getElementArray(String path, boolean createFlag) {
		int index = path.lastIndexOf("/");
		String str1 = path.substring(0, index);
		String str2 = path.substring(index + 1);
		// System.out.println(str1);System.out.println(str2);
		return null == namespace ? getElement(str1, createFlag).getChildren(str2)
				: getElement(str1, createFlag).getChildren(str2, namespace);
	}

	public List<Element> getElementArray(String path) {
		return getElementArray(path, false);
	}

	public int getElementArraySize(String path) {
		return getElementArray(path).size();
	}

	public int getElementArraySize(String path, boolean createFlag) {
		return getElementArray(path, createFlag).size();
	}

	public Element getElement(String path) {
		return getElement(path, false);
	}

	public Element getElement(String path, boolean createFlag) {
		String e_name[] = path.split("/");
		Element currentElement = this.document.getRootElement();
		Element previousElement = null;

		int i = 0;
		// 去除路径中的根
		if (e_name.length > 0 && e_name[0].equals(document.getRootElement().getName())) {
			i++;
		}

		// 开始遍历
		for (; i < e_name.length; i++) {
			if (e_name[i].trim().isEmpty())
				continue;
			NameIndex ni = new NameIndex(e_name[i]);
			previousElement = currentElement;
			// System.out.println(ni);
			if (ni.index < 0) {
				// 只是单包
				if (null != namespace) {
					currentElement = currentElement.getChild(ni.name, namespace);
				} else {
					currentElement = currentElement.getChild(ni.name);
				}

				// 不存在这个节点
				if (null == currentElement) {
					if (!createFlag) {
						throw new RuntimeException("value can not be got from [" + path + "]");
					} else {
						Element newElement;
						if (null != namespace) {
							newElement = new Element(ni.name, namespace);
						} else {
							newElement = new Element(ni.name);
						}
						previousElement.addContent(newElement);
						currentElement = newElement;
					}
				}
			} else {
				// 涉及多包
				@SuppressWarnings("rawtypes")
				List currlist = null == namespace ? currentElement.getChildren(ni.name)
						: currentElement.getChildren(ni.name, namespace);

				if (null == currlist || currlist.size() - 1 < ni.index) {// 不存在这个节点
																			// 或者
																			// 存在但是数量不够
					if (!createFlag) {
						throw new RuntimeException("value can not be got from [" + path + "]");
					} else {

						if (currlist.size() - 1 + 1 < ni.index)
							throw new RuntimeException("array index must be added in order");

						Element newElement = null == namespace ? new Element(ni.name) : new Element(ni.name, namespace);
						previousElement.addContent(newElement);
						currentElement = newElement;
					}
				} else {
					currentElement = (Element) currlist.get(ni.index);
				}
			}
		} // for
		return currentElement;
	}

	public void write(String filename) throws IOException {
		// int retcode = 0;
		// 将doc对象输出到文件
		OutputStreamWriter osw = null;
		try {
			// 创建xml文件输出流
			XMLOutputter xmlopt = new XMLOutputter();
			// 创建文件输出流
			// FileWriter writer = new FileWriter(filename + ".xml");
			osw = new OutputStreamWriter(new FileOutputStream(filename), encoding);
			// 指定文档格式
			Format fm = Format.getPrettyFormat();
			fm.setEncoding(encoding);
			xmlopt.setFormat(fm);
			// 将doc写入到指定的文件中
			xmlopt.output(document, osw);
			fm = null;
			xmlopt = null;
		} finally {
			if (null != osw)
				osw.close();
		}
	}

	public void setValue(String path, String value) {
		// value = null == value ? "" : value;
		// Element e = getElement(path, true);
		// e.setText(value);
		setValue(path, value, true);
	}

	public void setValue(String path, String value, boolean createFlag) {
		value = null == value ? "" : value;
		Element e = getElement(path, createFlag);
		e.setText(value);
	}

	/**
	 * 属性赋值
	 *
	 * @author dlfh-yuc02
	 * @time 2017-2-27 上午10:37:17
	 * @param node
	 *            -路径
	 * @param name
	 *            -属性名
	 * @param value
	 *            -属性值
	 */
	public void setAttribute(String path, String name, String value) {
		// value = null == value ? "" : value;
		// Element e = getElement(path, true);
		// e.setAttribute(name, value);
		setAttribute(path, name, value, true);
	}

	public void setAttribute(String path, String name, String value, boolean createFlag) {
		value = null == value ? "" : value;
		Element e = getElement(path, createFlag);
		if (null == namespace) {
			e.setAttribute(name, value);
		} else {
			e.setAttribute(name, value, namespace);
		}
	}

	public String getValue(String path) {
		// Element e = getElement(path, false);
		// return e.getTextTrim();
		return getValue(path, false);
	}

	public String getValue(String path, boolean createFlag) {
		Element e = getElement(path, createFlag);
		return e.getTextTrim();
	}

	/**
	 * 返回执行指定节点属性值
	 *
	 * @author dlfh-yuc02
	 * @time 2017-2-27 上午10:37:45
	 * @param node节点路径
	 * @param name节点名
	 * @return
	 */
	public String getAttribute(String path, String name) {
		// Element e = getElement(path, false);
		// String str = e.getAttributeValue(name);
		// if (null == str)
		// throw new RuntimeException("attribute can not be got from [" + path +
		// "+@+" + name + "]");
		// return str.trim();
		return getAttribute(path, name, false);
	}

	public String getAttribute(String path, String name, boolean createFlag) {
		Element e = getElement(path, createFlag);
		String str = null == namespace ? e.getAttributeValue(name) : e.getAttributeValue(name, namespace);
		if (null == str)
			throw new RuntimeException("attribute can not be got from [" + path + "+@+" + name + "]");
		return str.trim();
	}

	public String getXmlStr() throws IOException {
		ByteArrayOutputStream bos = null;
		String res;
		try {
			Format format = Format.getCompactFormat();
			format.setEncoding(this.encoding);
			XMLOutputter outp = new XMLOutputter(format);
			bos = new ByteArrayOutputStream();
			outp.output(document, bos);
			res = new String(bos.toByteArray(), this.encoding);
			format = null;
			outp = null;
		} finally {
			if (null != bos) {
				bos.close();
				bos = null;
			}
		}
		return res;
	}

	public void setRootName(String name) {
		document.getRootElement().setName(name);
	}

	/**
	 *
	 * <pre>
	 *
	 * </pre>
	 *
	 * @author dlfh-yuc02
	 * @time 2018年6月8日 下午4:21:49
	 * @param str
	 * @return
	 */
	private String getEncodingFromXmlStr(String str) {
		Matcher matcher = Pattern.compile("(?i)encoding=\".+\"\\?>").matcher(str);
		if (matcher.find()) {
			String tmp = str.substring(matcher.start(), matcher.end());
			short status = 0;
			StringBuilder sbBuilder = new StringBuilder();
			for (int i = 0; i < tmp.length(); i++) {
				if ('"' == tmp.charAt(i)) {
					status++;
				}
				if (1 == status)
					sbBuilder.append('"' == tmp.charAt(i) ? "" : tmp.charAt(i));
				if (status > 1)
					break;
			} // for
			return sbBuilder.toString();
		} else {
			throw new RuntimeException("no encoding declaration found");
		}
	}

	/**
	 *
	 * <pre>
	 * 实现通过xml声明自动识别字符集
	 * </pre>
	 *
	 * @author dlfh-yuc02
	 * @time 2018年6月8日 下午4:25:17
	 * @param str
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void setDocument(String str) throws JDOMException, IOException {

		try {
			this.setEncoding(getEncodingFromXmlStr(str));
		} catch (Exception e) {
		}
		;

		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(str.getBytes(this.encoding));
			document = saxBuilder.build(bis);
		} finally {
			if (null != bis) {
				bis.close();
				bis = null;
			}
		}
	}

	public void setDocument(byte[] byteArr) throws JDOMException, IOException {
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(byteArr);
			document = saxBuilder.build(bis);
		} finally {
			if (null != bis) {
				bis.close();
				bis = null;
			}
		}
	}

	/**
	 * 冻结当前XML文档结构
	 *
	 * @author dlfh-yuc02
	 * @time 2017-5-5 上午10:56:20
	 */
	public void freezeDocument() {
		this.freezeDocument = (Document) this.document.clone();
	}

	/**
	 * 恢复XML文档结构至冻结时的结构
	 *
	 * @author dlfh-yuc02
	 * @time 2017-5-5 上午10:56:37
	 */
	public void resetDocument() {
		if (null == this.freezeDocument)
			throw new RuntimeException("there is not a freeze document for resetting");
		this.document = (Document) this.freezeDocument.clone();
	}

	@Override
	public String toString() {
		try {
			return getXmlStr();
		} catch (IOException e) {
			e.printStackTrace();
			return "get string format error: " + e.getMessage();
		}
	}

	/**
	 *
	 * <pre>
	 * 清空文档
	 * </pre>
	 *
	 * @author dlfh-yuc02
	 * @time 2018年6月8日 下午5:18:27
	 */
	public void clear() {
		document = new Document();
		Element root = new Element("root");
		document.addContent(root);
	}

	/**
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @param document
	 *            The document to set.
	 */
	public void setDocument(Document document) {
		this.document = (Document) document.clone();
	}

	/**
	 * @return Returns the encoding.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            The encoding to set.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}

	public void setNamespace(String prefix, String uri) {
		this.namespace = Namespace.getNamespace(prefix, uri);
	}
}
