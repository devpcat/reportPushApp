package com.dc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class Tools {

	private final static Logger log = Logger.getLogger(Tools.class);
	private static Properties SYS_PROPERTIES = new Properties();

	public static String getSysPropertiesString(String name) {
		return SYS_PROPERTIES.getProperty(name);
	}

	public static void setSysProperties(String PropertiesAbosuluteFilePath) throws FileNotFoundException, IOException {
		SYS_PROPERTIES.load(new FileReader(new File(PropertiesAbosuluteFilePath)));
	}

	public static Properties getSysProperties() {
		return SYS_PROPERTIES;
	}

	public static String getStackTrace(Throwable e) {
		Writer w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		try {
			w.close();
		} catch (IOException e1) {
			log.error(e1.toString());
			e1.printStackTrace();
		}
		return w.toString();
	}

	public static String getYesterday() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.roll(java.util.Calendar.DAY_OF_YEAR, -1);
		String _yesterday = sdf.format(calendar.getTime());
		return _yesterday;
	}

	// public synchronized static String getSerno() {
	// String val = null;
	// try {
	// val =
	// MyBatisManager.getCurrentSession().selectOne("com.icbc.jcy.entity.SequenceMapper.getSeqNextVal");
	// MyBatisManager.getCurrentSession().commit(true);
	// // log.debug("��ȡ���ݿ����У�" + val);
	// } catch (Exception e) {
	// log.error("��ȡ���ݿ�����ʧ�ܣ��쳣��Ϣ��" + e.getMessage());
	// return null;
	// }
	// return (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + val;
	// }

	public static String getCurrentWorkDate() {
		return (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
	}

	public static String getCurrentWorkTime() {
		return (new SimpleDateFormat("HH:mm:ss")).format(new Date());
	}

	public static String getAmountByCent(String yuanAmount) {
		// 2015-10-19 �޸�
		BigDecimal bd = new BigDecimal(yuanAmount);
		bd = bd.movePointRight(2);
		return bd.toPlainString();
	}

	public static String getAmountByYuan(String fenAmount) {
		// 2015-10-19�޸�
		BigDecimal bd = new BigDecimal(fenAmount);
		bd = bd.movePointLeft(2);
		return bd.toPlainString();
	}

	// public synchronized static String getChannelCode() {
	// String chantype = "403";// (String)
	// // ctx.getValueAt("xml:Channel/CHANTYPE");
	// String serno = Tools.getSerno().substring(11);// (String)
	// // ctx.getValueAt("xml:substring(Channel/CospSerno,11,9)");
	// String chnserno_tmp = "9" + StringUtils.leftPad(chantype, 3, '0') +
	// "3400" + julianDate() + "82314"
	// + StringUtils.leftPad(serno, 9, '0');
	// String res = chnserno_tmp + check2121(chnserno_tmp);
	// // log.info("�����¼����:"+res);
	// return res;
	// }

	// private static int check2121(String value) {
	// if (value == null) {
	// // throw new NullPointerException("�����У������ݲ���Ϊ��");
	// throw new
	// NullPointerException(ResourceUtil.getMessage("cospn-eventsernoutil-001"));
	// }
	// if (value.length() != 26) {
	// // throw new IllegalArgumentException("�����У�������[" + value +
	// // "]���Ȳ�Ϊ26");
	// throw new
	// IllegalArgumentException(ResourceUtil.getMessage("cospn-eventsernoutil-002",
	// new String[] { value }));
	// }
	// int result = 0;
	// for (int i = 0; i < 26; i++) {
	// int v = value.charAt(i) - '0';
	// if (v > 9 || v < 0) {
	// /*
	// * throw new IllegalArgumentException("�����У�������[" + value +
	// * "]�а����������ַ�");
	// */
	// throw new
	// IllegalArgumentException(ResourceUtil.getMessage("cospn-eventsernoutil-003",
	// new String[] { value }));
	// }
	// result += (i % 2 == 0) ? v : (v * 2 / 10 + v * 2 % 10);
	// }
	// String ret = String.valueOf(result);
	// int r = ret.charAt(ret.length() - 1) - '0';
	// return r == 0 ? 0 : 10 - r;
	// }

	@SuppressWarnings("unused")
	private static String julianDate() {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int days = now.get(Calendar.DAY_OF_YEAR);
		String result = String.valueOf(String.valueOf(year).charAt(3));
		int x = ((String.valueOf(year).charAt(2) - '0') % 2) * 500 + days;
		if (x < 10) {
			result += "00" + x;
		} else if (x < 100) {
			result += "0" + x;
		} else {
			result += x;
		}
		return result;
	}

	public static String print(String str) {
		return null == str ? "" : str;
	}

	public static String DateTimeParse(String inputDateType, String outputDateType, String inputdate)
			throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat(inputDateType);
		SimpleDateFormat sdf2 = new SimpleDateFormat(outputDateType);
		String output = null;
		try {
			output = sdf2.format(sdf1.parse(inputdate));
		} catch (ParseException e) {
			log.error(e.getMessage());
			throw e;
		}
		return output;
	}

	/**
	 * ��ȡMD5ֵ
	 *
	 * @author dlfh-yuc02
	 * @time 2016-3-21 ����03:58:42
	 * @param filename
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String getMD5(String filename) throws NoSuchAlgorithmException, IOException {
		String value = null;
		File file = new File(filename);
		FileInputStream in = new FileInputStream(file);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} finally {
			if (null != in)
				in.close();
		}
		int length = value.length();
		for (int i = 0; i < 32 - length; i++) {
			value = "0" + value;
		}
		return value.toUpperCase();
	}

	public static String getYYYYMMDDHHMMSS() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
		String s = sdf.format(new Date());
		return s;
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-22 ����10:20:31
	 * @param pattern
	 *            -yyyyMMddHHmmssSSS
	 * @return
	 */
	public static String getPatternTime(String pattern) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
		String s = sdf.format(new Date());
		return s;
	}

	/**
	 * DES����
	 *
	 * @param datasource
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static byte[] encrypt(byte[] datasource, String password) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException,
			BadPaddingException {
		SecureRandom random = new SecureRandom();
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		// ����һ���ܳ׹�����Ȼ��������DESKeySpecת����
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(desKey);
		// Cipher����ʵ����ɼ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");
		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
		// ���ڣ���ȡ���ݲ�����
		// ��ʽִ�м��ܲ���
		return Base64.encodeBase64(cipher.doFinal(datasource));
	}

	/**
	 * DES����
	 *
	 * @param src
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] src, String password) throws Exception {
		src = Base64.decodeBase64(src);
		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom random = new SecureRandom();
		// ����һ��DESKeySpec����
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		// ����һ���ܳ׹���
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// ��DESKeySpec����ת����SecretKey����
		SecretKey securekey = keyFactory.generateSecret(desKey);
		// Cipher����ʵ����ɽ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");
		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		// ������ʼ���ܲ���
		return cipher.doFinal(src);
	}

}// class tools
