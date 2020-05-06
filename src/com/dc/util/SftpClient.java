package com.dc.util;

import java.io.File;

import com.jcraft.jsch.*;

public class SftpClient {
	private String username;
	private String password;
	private String hostname;
	private int port;
	Session session;
	ChannelSftp c;

	//test
	public static void main(String[] args) {
		SftpClient sftp = new SftpClient("104.6.189.208", 22, "gtcg", "gtcg");
		try {
			sftp.connect();
			//sftp.put("C:/Users/dlfh-yuc02/Desktop/ftpTest.java", "dlcosp/ftpTest2.java");
			sftp.get("C:/Users/dlfh-yuc02/Desktop/ftpTest.java", "dlcosp2/ftpTest2.java");
		} catch (Exception e) {
			//String msg;
			if (null == e.getCause())
				System.out.println("==========\n" + e.getMessage() + "\n===========");
			else
				System.out.println("==========\n" + e.getCause().getMessage() + "\n===========");
			e.printStackTrace();
		} finally {
			sftp.close();
		}
	}

	public SftpClient(String hostname, int port, String username, String password) {
		this.username = username;
		this.password = password;
		this.hostname = hostname;
		this.port = port;
		session = null;
		c = null;
	}

	public void connect() throws JSchException {

		JSch jsch = new JSch();
		System.out.println("username=" + username + " hostname=" + hostname + " port=" + port);
		session = jsch.getSession(username, hostname, port);
		UserInfo ui = new MyUserInfo() {
			public String getPassword() {
				return password;
			}

			public boolean promptYesNo(String str) {
				return true;
			}

			public String getPassphrase() {
				return null;
			}

			public boolean promptPassphrase(String message) {
				return true;
			}

			public boolean promptPassword(String message) {
				System.out.println(message);
				return true;
			}

			public void showMessage(String message) {
				System.out.println(message);
			}

			@Override
			public String[] promptKeyboardInteractive(String arg0, String arg1, String arg2, String[] arg3,
					boolean[] arg4) {
				System.out.println(arg0);
				System.out.println(arg1);
				System.out.println(arg2);
				for (String s1 : arg3)
					System.out.println(s1);
				for (boolean s2 : arg4)
					System.out.println(s2);
				return new String[] { password };
			}
		};
		session.setUserInfo(ui);
		session.connect();
		Channel channel = session.openChannel("sftp");
		channel.connect();
		c = (ChannelSftp) channel;
		// System.out.println("当前sftp版本：" + c.version());
		System.out.println("sftp建立连接成功");
	}

	public void getFile(String remotepath, String localpath, String filename) throws SftpException {
		c.cd(remotepath);
		c.lcd(localpath);
		System.out.println("正在下载文件：" + filename + "...");
		try {
			c.get(filename, filename);
		} catch (SftpException e) {
			try {
				new File(filename).delete();
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
			throw e;
		}
		System.out.println("文件已经下载完成");
	}

	public void get(String local, String remote) throws SftpException {
		//c.cd(remotepath);
		//c.lcd(localpath);
		System.out.println("正在下载文件：" + remote + "-->" + local);
		try {
			c.get(remote, local);
		} catch (SftpException e) {
			try {
				new File(local).delete();
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
			throw e;
		}
		System.out.println("文件已经下载完成");
	}

	public void putFile(String remotepath, String localpath, String filename) throws SftpException {
		c.cd(remotepath);
		c.lcd(localpath);
		System.out.println("正在上传文件：" + filename + "...");
		try {
			c.put(filename, filename);
		} catch (SftpException e) {
			try {
				c.rm(filename);
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
			throw e;
		}
		System.out.println("文件已经上传完成");
	}

	public void put(String local, String remote) throws SftpException {
		//		c.cd(remotepath);
		//		c.lcd(localpath);
		System.out.println("正在上传文件：" + local + "-->" + remote);
		try {
			c.put(local, remote);
		} catch (SftpException e) {
			try {
				c.rm(remote);
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			}
			throw e;
		}
		System.out.println("文件已经上传完成");
	}

	public void close() {
		if (null != c)
			c.quit();
		if (null != session)
			session.disconnect();
		System.out.println("sftp连接关闭");
	}

	abstract class MyUserInfo implements UserInfo, UIKeyboardInteractive {
	}

}
