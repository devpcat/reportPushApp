package com.dc.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import cn.com.icbc.tools.StringTool;
import cn.com.icbc.tools.dsr.Crc32_Agree;

/**
 *
 * TODO 类的描述：。
 *
 * <pre>
 * 与外联前置第三方通讯组件通讯
 * 需要引用Gtcg.jar包
 * </pre>
 *
 * <pre>
 * modify by dlfh-yuc02 on 2016-11-18
 *    fix->1.完美解决与外联前置"第三方通讯组件"通讯的问题，现在可以像使用COSP、MAPS一样发报文啦！
 *         2.继承XmlDocument，xml解析能力更强大/2017-06-01
 * </pre>
 */
public class TCommSender extends XmlDocument {

	private String hostName;
	private int portNum;
	private int timeOut = 30 * 1000;
	private Logger log = Logger.getLogger(TCommSender.class);
	private static long num = 0;

	//private EnvProperties env = EnvProperties.getInstance();

	/**
	 * 通过CTP的ENV配置文件配置相应的接口技术信息
	 *
	 * @author dlfh-yuc02
	 * @time 2017年7月13日 上午9:13:08
	 * @param envPrefix
	 */
	/*
	 * public void setConfigByEnv(String envPrefix) { this.hostName =
	 * env.get(envPrefix + ".hostName"); this.portNum =
	 * Integer.parseInt(env.get(envPrefix + ".portNum")); this.timeOut =
	 * Integer.parseInt(env.get(envPrefix + ".timeOut")); }
	 */

	public TCommSender() {
	}

	public TCommSender(String xmlFilePath) throws JDOMException, IOException {
		super(xmlFilePath);
		try {
			this.hostName = getValue("/PRIVATE/HOSTNAME");
			this.portNum = Integer.parseInt(getValue("/PRIVATE/PORTNUM"));
			this.timeOut = Integer.parseInt(getValue("/PRIVATE/TIMEOUT"));
		} catch (Exception e) {
			// 如果报错则全部置为默认
			this.hostName = null;
			this.portNum = 0;
			this.timeOut = 30 * 1000;
		}
	}

	public TCommSender(String hostIp, int portNum, int timeOut, String xmlFilePath) throws Exception {
		super(xmlFilePath);
		this.hostName = hostIp;
		this.portNum = portNum;
		this.timeOut = timeOut;
	}

	/**
	 *
	 * @param hostIp
	 * @param portNum
	 * @param timeOut
	 *            单位毫秒
	 * @throws Exception
	 */
	public TCommSender(String hostIp, int portNum, int timeOut) throws Exception {
		super();
		this.hostName = hostIp;
		this.portNum = portNum;
		this.timeOut = timeOut;
	}

	private synchronized long getserno() {
		long s = ++num;
		if (num >= Integer.MAX_VALUE)
			num = 0;
		return s;
	}

	public void send() throws IOException, JDOMException {
		long serno = getserno();
		String getXmlStr = getXmlStr();
		log.info("GTCG_TC[" + serno + "]发送报文-->\n" + getXmlStr);
		byte[] bytesRcv = sendMessage(getXmlStr);
		log.info("GTCG_TC[" + serno + "]返回报文-->\n" + new String(bytesRcv, this.encoding) + "\n");
		setDocument(bytesRcv);
	}

	/**
	 *
	 * @author dlfh-yuc02
	 * @time 2016-11-18 下午05:09:47
	 * @return
	 * @throws IOException
	 */
	private Socket CreatSocket() throws IOException {
		Socket s = null;
		s = new Socket();
		s.connect(new InetSocketAddress(hostName, portNum), timeOut);
		s.setSoTimeout(timeOut);
		return s;
	}

	public byte[] sendMessage(String strMessage) throws IOException {
		Socket socket = null;
		DataTransfer2 transfer;
		try {
			socket = CreatSocket();
			transfer = new DataTransfer2(socket);
			byte[] bytesToSend = strMessage.getBytes(this.encoding);
			transfer.REQSendDatas(bytesToSend);
			byte[] bytesRcv = transfer.natpRecv();
			return bytesRcv;
			// return new String(bytesRcv);
		} finally {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
	}

	// 内部类---参考COSP源码--
	class DataTransfer2 {
		private OutputStream _outputStream;
		private InputStream _inputStream;
		private Socket _sock;
		private byte[] recvDatas = new byte[4096000];
		private int dataLen = 0;
		private String _tradeCode;
		private String _businessType;
		private String _agentNo;
		private String _areaNo;
		private String _bankNo;
		private String _tellerNo;
		private int _protoclVersion;
		private String _fileServerName;

		public DataTransfer2(Socket sock) {
			this._sock = sock;
			try {
				setInputStream(this._sock.getInputStream());
				setOutputStream(this._sock.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public byte[] natpRecv() throws IOException {
			DataInputStream din = new DataInputStream(getInputStream());
			int totalLength = din.readInt();
			byte[] allDatas = new byte[totalLength];

			readFully(din, allDatas);

			if (Crc32_Agree.crc32_2(allDatas, 0, totalLength - 4) != getCrc(allDatas)) {
				throw new IOException("新NATP组件接收数据后crc校验出错");
			}
			byte protoclVersion = allDatas[0];
			byte natpVersion = allDatas[6];
			setProtoclVersion(protoclVersion);

			if (protoclVersion == 1) {
				byte[] datas = (byte[]) null;
				if (natpVersion == 3) {
					datas = natpRecvREQData(allDatas, din);
				} else {
					datas = natpRecvData(allDatas, din);
				}
				return handleDatas(datas);
			}

			if (protoclVersion == 2) {
				return natpRecvFile(allDatas, din);
			}
			throw new IOException("新NATP组件接收数据出错：无此协议版本号" + protoclVersion);
		}

		public void natpSendDatas(byte[] datas) throws IOException {
			DataOutputStream dout = new DataOutputStream(getOutputStream());
			DataInputStream din = new DataInputStream(getInputStream());
			byte continueFlag = 0;
			short packNum = 1;
			int sendLength = datas.length + 47;
			int index = datas.length;
			if (sendLength > 4096) {
				sendLength = 4092;
				continueFlag = 1;
				index = sendLength - 43;
			} else {
				sendLength = datas.length + 43;
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(bout);

			dataOut.writeInt(sendLength);
			dataOut.write(1);
			dataOut.write(0);
			dataOut.write(continueFlag);
			dataOut.write(0);
			dataOut.writeShort(packNum);
			dataOut.write(1);

			dataOut.write(StringTool.fill(getTradeCode(), ' ', 5, false).getBytes());
			dataOut.write(StringTool.fill(getBusinessType(), ' ', 3, false).getBytes());
			dataOut.write(StringTool.fill(getAgentNo(), ' ', 9, false).getBytes());
			dataOut.write(StringTool.fill(getAreaNo(), ' ', 5, false).getBytes());
			dataOut.write(StringTool.fill(getBankNo(), ' ', 5, false).getBytes());
			dataOut.write(StringTool.fill(getTellerNo(), ' ', 5, false).getBytes());
			dataOut.write(datas, 0, index);
			byte[] pureDatas = bout.toByteArray();

			int crc = Crc32_Agree.crc32_2(pureDatas, 4, pureDatas.length - 4);

			int length = pureDatas.length;
			byte[] temp = new byte[length + 4];
			System.arraycopy(pureDatas, 0, temp, 0, length);
			System.arraycopy(intToByte(crc), 0, temp, length, 4);
			dout.write(temp);

			dout.flush();
			while (continueFlag == 1) {
				recvProtocolPack(din);
				if (datas.length <= index + 4096 - 14) {
					continueFlag = 0;
					sendLength = datas.length - index;
				} else {
					continueFlag = 1;
					sendLength = 4082;
				}
				bout.reset();

				dataOut.writeInt(sendLength + 10);
				dataOut.write(1);
				dataOut.write(0);
				dataOut.write(continueFlag);
				dataOut.write(0);
				dataOut.writeShort(packNum = (short) (packNum + 1));
				dataOut.write(datas, index, sendLength);
				pureDatas = bout.toByteArray();

				int len = pureDatas.length;
				int crc2 = Crc32_Agree.crc32_2(pureDatas, 4, len - 4);
				byte[] temp2 = new byte[len + 4];
				System.arraycopy(pureDatas, 0, temp2, 0, len);
				System.arraycopy(intToByte(crc2), 0, temp2, len, 4);
				dout.write(temp2);
				dout.flush();
				index += sendLength;
			}
		}

		private void recvProtocolPack(DataInputStream din) throws IOException {
			int len = din.readInt();
			byte[] protocol = new byte[len];
			din.read(protocol);
		}

		private byte[] handleDatas(byte[] datas) throws IOException {
			int natpVersion = datas[0];
			if ((natpVersion == 1) || (natpVersion == 2)) {
				byte[] tradeCode = { datas[1], datas[2], datas[3], datas[4], datas[5] };

				byte[] businessType = { datas[6], datas[7], datas[8] };

				byte[] agentNo = { datas[9], datas[10], datas[11], datas[12], datas[13], datas[14], datas[15],
						datas[16], datas[17] };

				byte[] areaNo = { datas[18], datas[19], datas[20], datas[21], datas[22] };

				byte[] bankNo = { datas[23], datas[24], datas[25], datas[26], datas[27] };

				byte[] tellerNo = { datas[28], datas[29], datas[30], datas[31], datas[32] };
				setTradeCode(new String(tradeCode).trim());
				setAgentNo(new String(agentNo).trim());
				setBusinessType(new String(businessType).trim());
				setAreaNo(new String(areaNo).trim());
				setBankNo(new String(bankNo).trim());
				setTellerNo(new String(tellerNo).trim());
				byte[] realDatas = new byte[datas.length - 33];
				System.arraycopy(datas, 33, realDatas, 0, realDatas.length);
				return realDatas;
			}
			if (natpVersion == 3) {
				byte[] realDatas = new byte[datas.length - 1];
				System.arraycopy(datas, 1, realDatas, 0, realDatas.length);
				return realDatas;
			}

			throw new IOException("新NATP通讯组件接收数据后无此NATP版本类型" + natpVersion);
		}

		private void readFully(InputStream din, byte[] rec) throws IOException {
			int readed = din.read(rec);
			while (readed < rec.length) {
				try {
					int next = din.read(rec, readed, rec.length - readed);
					readed += next;
				} catch (IOException e1) {
					throw new IOException("新NATP通讯组件readFully函数异常终止 ");
				}
			}
		}

		private byte[] natpRecvFile(byte[] allDatas, DataInputStream din) throws UnknownHostException, IOException {
			byte[] hostName = new byte[15];
			System.arraycopy(allDatas, 7, hostName, 0, hostName.length);
			String host = new String(hostName).trim();
			setFileServerName(host);
			return allDatas;
		}

		public void fileTransfer(byte[] firstDatas, InputStream _hostInput, OutputStream _hostOutput)
				throws IOException {
			DataInputStream hostInput = new DataInputStream(_hostInput);
			DataOutputStream hostOuput = new DataOutputStream(_hostOutput);
			DataOutputStream clientOutput = new DataOutputStream(getOutputStream());
			DataInputStream clientInput = new DataInputStream(getInputStream());
			hostOuput.writeInt(firstDatas.length);
			hostOuput.write(firstDatas);
			hostOuput.flush();
			while (true) {
				try {
					int len = hostInput.readInt();
					byte[] recv = new byte[len];
					readFully(hostInput, recv);
					clientOutput.writeInt(len);
					clientOutput.write(recv);
					clientOutput.flush();
					len = clientInput.readInt();
					recv = new byte[len];
					readFully(clientInput, recv);
					hostOuput.writeInt(len);
					hostOuput.write(recv);
					hostOuput.flush();
				} catch (IOException e) {
					if (e.toString().equals("java.io.EOFException")) {
						return;
					}

					if (e.toString().equals("java.net.SocketException: Software caused connection abort: recv failed")) {
						return;
					}

					throw e;
				}
			}
		}

		@SuppressWarnings("unused")
		private byte[] natpRecvREQData(byte[] allDatas, DataInputStream din) throws IOException {
			int packType = allDatas[1];
			int continueFlag = allDatas[2];
			int compressFlag = allDatas[3];
			short packNum = bytesToShort(allDatas, 4);
			appendBytes(allDatas, 6, allDatas.length - 10);
			DataOutputStream dout = new DataOutputStream(getOutputStream());
			while ((continueFlag == 1) || (continueFlag == 49)) {
				natpSendProtocol(dout, (short) (packNum + 1));
				int len = din.readInt();
				byte[] datas = new byte[len];
				readFully(din, datas);
				packType = datas[1];
				continueFlag = datas[2];
				compressFlag = datas[3];
				packNum = bytesToShort(datas, 4);
				if (Crc32_Agree.crc32_2(datas, 0, datas.length - 4) != getCrc(datas))
					throw new IOException("新NATP通讯组件发送多包数据后接收协议数据crc校验不符");
				appendBytes(datas, 7, datas.length - 11);
			}
			return getAllDatas();
		}

		@SuppressWarnings("unused")
		private byte[] natpRecvData(byte[] allDatas, DataInputStream din) throws IOException {
			int packType = allDatas[1];
			int continueFlag = allDatas[2];
			int compressFlag = allDatas[3];
			short packNum = bytesToShort(allDatas, 4);
			appendBytes(allDatas, 6, allDatas.length - 10);
			DataOutputStream dout = new DataOutputStream(getOutputStream());
			while ((continueFlag == 1) || (continueFlag == 49)) {
				natpSendProtocol(dout, (short) (packNum + 1));
				int len = din.readInt();
				byte[] datas = new byte[len];
				readFully(din, datas);
				packType = datas[1];
				continueFlag = datas[2];
				compressFlag = datas[3];
				packNum = bytesToShort(datas, 4);
				if (Crc32_Agree.crc32_2(datas, 0, datas.length - 4) != getCrc(datas))
					throw new IOException("新NATP通讯组件发送多包数据后接收协议数据crc校验不符");
				appendBytes(datas, 6, datas.length - 10);
			}
			return getAllDatas();
		}

		private void natpSendProtocol(DataOutputStream dout, short packNum) throws IOException {
			byte[] datas = new byte[10];
			datas[0] = 1;
			datas[1] = 1;
			datas[2] = 0;
			datas[3] = 0;
			datas[4] = (byte) (packNum >>> 8 & 0xFF);
			datas[5] = (byte) (packNum >>> 0 & 0xFF);
			datas[6] = 0;
			dout.writeInt(14);
			dout.write(datas);
			dout.writeInt(Crc32_Agree.crc32_2(datas, 0, datas.length));
			dout.flush();
		}

		private void appendBytes(byte[] allDatas, int i, int j) {
			// modify by kfzx-zhaowei, 扩容
			if (this.recvDatas.length <= this.dataLen + j) {
				byte[] bytes = new byte[this.recvDatas.length + 4096000];
				System.arraycopy(this.recvDatas, 0, bytes, 0, this.dataLen);
				this.recvDatas = bytes;
				bytes = null;
			}
			// end modify

			System.arraycopy(allDatas, i, this.recvDatas, this.dataLen, j);
			this.dataLen += j;
		}

		private byte[] getAllDatas() {
			byte[] ret = new byte[this.dataLen];
			System.arraycopy(this.recvDatas, 0, ret, 0, this.dataLen);
			for (int i = 0; i < this.dataLen; ++i)
				this.recvDatas[i] = 0;
			this.dataLen = 0;
			return ret;
		}

		private short bytesToShort(byte[] allDatas, int i) {
			int i1 = allDatas[i] & 0xFF;
			int i2 = allDatas[(i + 1)] & 0xFF;
			return (short) ((i1 << 8) + (i2 << 0));
		}

		private int getCrc(byte[] allDatas) throws IOException {
			if (allDatas.length < 4)
				throw new IOException("crc校验时数据长度太小");
			int i1 = allDatas[(allDatas.length - 4)] & 0xFF;
			int i2 = allDatas[(allDatas.length - 3)] & 0xFF;
			int i3 = allDatas[(allDatas.length - 2)] & 0xFF;
			int i4 = allDatas[(allDatas.length - 1)] & 0xFF;
			return (i1 << 24) + (i2 << 16) + (i3 << 8) + (i4 << 0);
		}

		public void setOutputStream(OutputStream stream) {
			this._outputStream = stream;
		}

		public void setInputStream(InputStream stream) {
			this._inputStream = stream;
		}

		public InputStream getInputStream() {
			return this._inputStream;
		}

		public OutputStream getOutputStream() {
			return this._outputStream;
		}

		public DataTransfer2() {
		}

		public String getAgentNo() {
			return this._agentNo;
		}

		public String getAreaNo() {
			return this._areaNo;
		}

		public String getBankNo() {
			return this._bankNo;
		}

		public String getBusinessType() {
			return this._businessType;
		}

		public String getTellerNo() {
			return this._tellerNo;
		}

		public String getTradeCode() {
			return this._tradeCode;
		}

		public void setAgentNo(String string) {
			this._agentNo = string;
		}

		public void setAreaNo(String string) {
			this._areaNo = string;
		}

		public void setBankNo(String string) {
			this._bankNo = string;
		}

		public void setBusinessType(String string) {
			this._businessType = string;
		}

		public void setTellerNo(String string) {
			this._tellerNo = string;
		}

		public void setTradeCode(String string) {
			this._tradeCode = string;
		}

		public int getProtoclVersion() {
			return this._protoclVersion;
		}

		public void setProtoclVersion(int i) {
			this._protoclVersion = i;
		}

		public String getFileServerName() {
			return this._fileServerName;
		}

		public void setFileServerName(String string) {
			this._fileServerName = string;
		}

		public byte[] intToByte(int pInt) {
			byte[] bi = { 0, 0, 6, 5 };
			bi[0] = (byte) ((pInt & 0xFF000000) >>> 24);
			bi[1] = (byte) ((pInt & 0xFF0000) >>> 16);
			bi[2] = (byte) ((pInt & 0xFF00) >>> 8);
			bi[3] = (byte) (pInt & 0xFF);
			return bi;
		}

		public void REQSendDatas(byte[] datas) throws IOException {
			DataOutputStream dout = new DataOutputStream(getOutputStream());
			DataInputStream din = new DataInputStream(getInputStream());
			byte continueFlag = 0;
			short packNum = 1;
			int sendLength = datas.length + 15;
			int index = datas.length;
			if (sendLength > 4096) {
				sendLength = 4092;
				continueFlag = 1;
				index = sendLength - 11;
			} else {
				sendLength = datas.length + 11;
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(bout);

			dataOut.writeInt(sendLength);
			dataOut.write(1);
			dataOut.write(0);
			dataOut.write(continueFlag);
			dataOut.write(0);
			dataOut.writeShort(packNum);
			dataOut.write(3);
			dataOut.write(datas, 0, index);
			byte[] pureDatas = bout.toByteArray();

			int crc = Crc32_Agree.crc32_2(pureDatas, 4, pureDatas.length - 4);
			int length = pureDatas.length;
			byte[] temp = new byte[length + 4];
			System.arraycopy(pureDatas, 0, temp, 0, length);
			System.arraycopy(intToByte(crc), 0, temp, length, 4);
			dout.write(temp);
			dout.flush();
			while (continueFlag == 1) {
				recvProtocolPack(din);
				if (datas.length <= index + 4096 - 14) {
					continueFlag = 0;
					sendLength = datas.length - index;
				} else {
					continueFlag = 1;
					sendLength = 4081;
				}
				bout.reset();

				dataOut.writeInt(sendLength + 11);
				dataOut.write(1);
				dataOut.write(0);
				dataOut.write(continueFlag);
				dataOut.write(0);
				dataOut.writeShort(packNum = (short) (packNum + 1));
				dataOut.write(3);
				dataOut.write(datas, index, sendLength);
				pureDatas = bout.toByteArray();

				int len = pureDatas.length;
				int crc2 = Crc32_Agree.crc32_2(pureDatas, 4, len - 4);
				byte[] temp2 = new byte[len + 4];
				System.arraycopy(pureDatas, 0, temp2, 0, len);
				System.arraycopy(intToByte(crc2), 0, temp2, len, 4);
				dout.write(temp2);
				dout.flush();
				index += sendLength;
			}
		}
	}

	/**
	 * @return Returns the hostName.
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName
	 *            The hostName to set.
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return Returns the portNum.
	 */
	public int getPortNum() {
		return portNum;
	}

	/**
	 * @param portNum
	 *            The portNum to set.
	 */
	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	/**
	 * @return Returns the timeOut.
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * @param timeOut
	 *            The timeOut to set.
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}
}
