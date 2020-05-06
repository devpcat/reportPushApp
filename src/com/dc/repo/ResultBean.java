package com.dc.repo;

public class ResultBean {
	//序号 | 类型    | 开始时间 | 结束时间 | 描述                                    | 数量  | 文件名
	private String seqno;
	private String type;
	private String startTime;
	private String endTime;
	private String describtion;
	private String effectnum;
	private String filename;
	private String retmsg;

	/**
	 * @return Returns the seqno.
	 */
	public String getSeqno() {
		return seqno;
	}

	/**
	 * @param seqno
	 *            The seqno to set.
	 */
	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the startTime.
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            The startTime to set.
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return Returns the endTime.
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            The endTime to set.
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return Returns the describtion.
	 */
	public String getDescribtion() {
		return describtion;
	}

	/**
	 * @param describtion
	 *            The describtion to set.
	 */
	public void setDescribtion(String describtion) {
		this.describtion = describtion;
	}

	/**
	 * @return Returns the num.
	 */
	public String getNum() {
		return effectnum;
	}

	/**
	 * @param num
	 *            The num to set.
	 */
	public void setNum(String num) {
		this.effectnum = num;
	}

	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return Returns the retmsg.
	 */
	public String getRetmsg() {
		return retmsg;
	}

	/**
	 * @param retmsg
	 *            The retmsg to set.
	 */
	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}

}
