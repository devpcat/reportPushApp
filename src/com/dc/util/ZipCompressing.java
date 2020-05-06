package com.dc.util;

import java.io.*;
import java.util.zip.*;

/**
 * ����ʵ����ZIPѹ����
 * <p>
 * ���¹��ܰ������˶�̬���ݹ��JAVA���ļ��������ԶԵ����ļ������⼶���ļ��н���ѹ���ͽ�ѹ�� ���ڴ������Զ���Դ����·����Ŀ�����·����
 * <p>
 * �ڱ��δ����У�ʵ�ֵ���ѹ������
 *
 */

public class ZipCompressing {
	//private int k = 1; // ����ݹ��������

	public ZipCompressing() {
		// TODO Auto-generated constructor stub
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		ZipCompressing book = new ZipCompressing();
//		try {
//			book.zip("C:\\Users\\ICBC\\Desktop\\test2.zip", new File("D:\\Programs\\³��ʦ"));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public static void zip(String zipFileName, File inputFile) throws IOException {
		System.out.println("ѹ����...");
		ZipOutputStream out = null;
		BufferedOutputStream bo = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			bo = new BufferedOutputStream(out);
			zip(out, inputFile, inputFile.getName(), bo);
			System.out.println("ѹ�����");
		} finally {
			bo.close();
			out.close(); // ������ر�
		}
	}

	private static void zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws IOException { // ��������
		if (f.isDirectory()) {
			File[] fl = f.listFiles();
			if (fl.length == 0) {
				out.putNextEntry(new ZipEntry(base + "/")); // ����zipѹ�������base
				System.out.println(base + "/");
			}
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + "/" + fl[i].getName(), bo); // �ݹ�������ļ���
			}
		} else {
			out.putNextEntry(new ZipEntry(base)); // ����zipѹ�������base
			System.out.println(base);
			BufferedInputStream bi = null;
			try {
				bi = new BufferedInputStream(new FileInputStream(f));
				int b;
				while ((b = bi.read()) != -1) {
					bo.write(b); // ���ֽ���д�뵱ǰzipĿ¼
				}
			} finally {
				if (null != bi)
					bi.close();
			}
		}
	}
}
