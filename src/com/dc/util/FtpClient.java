package com.dc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * FTP简单客户端，实现get、put命令
 * 依赖commons-net-3.6.jar包
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2017-3-8
 *    fix->1.
 *         2.
 * </pre>
 */
public class FtpClient {
	private FTPClient ftp = new FTPClient();
	private String username;
	private String password;
	private String host;
	private int port;
	private String controlEncoding;
	private boolean transMode_port_pasv;//true-port,false-pasv
	private boolean transMode_asc_bin;//true-asc,false-bin

	//test
	public static void main(String[] args) throws IOException {
		FtpClient ftp = new FtpClient("104.6.189.201", 21, "gtcg", "gtcg", "GBK", false, false);
		try {
			ftp.connect();
			ftp.put("C:/Users/dlfh-yuc02/Desktop/ftpTest.java", "dlcosp3/ftpTest2.java");
			ftp.get("C:/Users/dlfh-yuc02/Desktop/ftpTest.java", "dlcosp/ftpTest2.java");
		} finally {
			ftp.close();
		}
	}

	/**
	 *
	 * @param username
	 * @param password
	 * @param host
	 * @param port
	 * @param controlEncoding
	 * @param transMode_port_pasv
	 *            -true-port,false-pasv
	 * @param transMode_asc_bin
	 *            -true-asc,false-bin
	 */
	public FtpClient(String host, int port, String username, String password, String controlEncoding,
			boolean transMode_port_pasv, boolean transMode_asc_bin) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.controlEncoding = controlEncoding;
		this.transMode_port_pasv = transMode_port_pasv;
		this.transMode_asc_bin = transMode_asc_bin;
	}

	public FtpClient(String host, int port, String username, String password) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.controlEncoding = "GBK";
		this.transMode_port_pasv = false;
		this.transMode_asc_bin = false;
	}

	/**
	 * 建立ftp连接
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-8 下午01:33:10
	 * @throws SocketException
	 * @throws IOException
	 */
	public void connect() throws SocketException, IOException {
		if (null != this.controlEncoding)
			ftp.setControlEncoding(this.controlEncoding);
		//连接
		ftp.connect(this.host, this.port);
		judgeReturnCode();
		//登陆
		ftp.login(this.username, this.password);
		judgeReturnCode();
		//asc-bin
		if (this.transMode_asc_bin)
			ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
		else
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
		judgeReturnCode();
		//主被动
		if (this.transMode_port_pasv)
			ftp.enterLocalActiveMode();
		else
			ftp.enterLocalPassiveMode();
		//judgeReturnCode(ftp);
		System.out.println("FTP建立连接成功");
	}

	/**
	 * 关闭ftp连接
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-8 下午01:38:31
	 * @throws IOException
	 */
	public void close() {
		if (null == ftp)
			return;
		try {
			try {
				ftp.logout();
			} catch (Exception e) {
				// TODO: handle exception
			}
			//judgeReturnCode();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// do nothing
				}
			}
		}
		System.out.println("FTP连接关闭");
	}

	/**
	 * put
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-8 下午01:46:25
	 * @param local
	 * @param remote
	 * @throws IOException
	 */
	public void put(String local, String remote) throws IOException {
		System.out.println("正在上传文件：" + local + "-->" + remote);
		InputStream input = null;
		try {
			input = new FileInputStream(new File(local));
			ftp.storeFile(remote, input);
			judgeReturnCode();
		} catch (IOException e) {
			try {
				ftp.dele(remote);
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
			throw e;
		} finally {
			if (null != input)
				input.close();
		}
		System.out.println("文件已经上传完成");
	}

	/**
	 * get
	 *
	 * @author dlfh-yuc02
	 * @time 2017-3-8 下午01:46:40
	 * @param local
	 * @param remote
	 * @throws IOException
	 */
	public void get(String local, String remote) throws IOException {
		System.out.println("正在下载文件：" + remote + "-->" + local);
		OutputStream output = null;
		boolean succ = false;
		try {
			output = new FileOutputStream(new File(local));
			ftp.retrieveFile(remote, output);
			judgeReturnCode();
			succ = true;
		} finally {
			if (null != output)
				output.close();
			try {
				if (!succ)
					new File(local).delete();
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
		}
		System.out.println("文件已经下载完成");
	}

	private void judgeReturnCode() throws IOException {
		int replyCode = ftp.getReplyCode();
		String replyString = ftp.getReplyString();
		System.out.println(replyString);
		if (!FTPReply.isPositiveCompletion(replyCode)) {
			//ftp.disconnect();
			//ftp = null;
			//System.out.println("断开FTP连接");
			throw new IOException(replyString);
		}
	}

}
