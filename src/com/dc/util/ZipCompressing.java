package com.dc.util;

import java.io.*;
import java.util.zip.*;

/**
 * 程序实现了ZIP压缩。
 * <p>
 * 大致功能包括用了多态，递归等JAVA核心技术，可以对单个文件和任意级联文件夹进行压缩和解压。 需在代码中自定义源输入路径和目标输出路径。
 * <p>
 * 在本段代码中，实现的是压缩部分
 *
 */

public class ZipCompressing {
	//private int k = 1; // 定义递归次数变量

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
//			book.zip("C:\\Users\\ICBC\\Desktop\\test2.zip", new File("D:\\Programs\\鲁大师"));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public static void zip(String zipFileName, File inputFile) throws IOException {
		System.out.println("压缩中...");
		ZipOutputStream out = null;
		BufferedOutputStream bo = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			bo = new BufferedOutputStream(out);
			zip(out, inputFile, inputFile.getName(), bo);
			System.out.println("压缩完成");
		} finally {
			bo.close();
			out.close(); // 输出流关闭
		}
	}

	private static void zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws IOException { // 方法重载
		if (f.isDirectory()) {
			File[] fl = f.listFiles();
			if (fl.length == 0) {
				out.putNextEntry(new ZipEntry(base + "/")); // 创建zip压缩进入点base
				System.out.println(base + "/");
			}
			for (int i = 0; i < fl.length; i++) {
				zip(out, fl[i], base + "/" + fl[i].getName(), bo); // 递归遍历子文件夹
			}
		} else {
			out.putNextEntry(new ZipEntry(base)); // 创建zip压缩进入点base
			System.out.println(base);
			BufferedInputStream bi = null;
			try {
				bi = new BufferedInputStream(new FileInputStream(f));
				int b;
				while ((b = bi.read()) != -1) {
					bo.write(b); // 将字节流写入当前zip目录
				}
			} finally {
				if (null != bi)
					bi.close();
			}
		}
	}
}
