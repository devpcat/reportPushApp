package com.dc.util;

import org.apache.log4j.Logger;

/**
 * 控制台显示进度条
 *
 * @author YC
 *
 */
public class ConsoleProgressBar {

	private static final Logger log = Logger.getLogger(ConsoleProgressBar.class);

	public static void main(String args[]) throws InterruptedException {
		ConsoleProgressBar cpBar = new ConsoleProgressBar(200, 5, "test-test");
		for (double i = 0; i <= 1.1;) {
			// cpBar.setCurrentProgressNum(i);
			cpBar.show(i);
			i = i + 0.01;
			System.out.print(i);
			Thread.sleep(100);
		}
	}

	int totalProgressNum;
	int showPerNum;
	String progressName;

	public ConsoleProgressBar(int totalProgressNum, int showPerNum, String progressName) {
		this.totalProgressNum = totalProgressNum;
		this.showPerNum = showPerNum;
		this.progressName = progressName;
		System.out.println("\n" + getTime() + "  [" + this.progressName + "] 任务开始");
		log.debug(getTime() + "  [" + this.progressName + "] 任务开始");
		show(0);
	}

	public ConsoleProgressBar(int showPerNum, String progressName) {
		this(0, showPerNum, progressName);
	}

	public ConsoleProgressBar(String progressName) {
		this(0, 0, progressName);
	}

	public ConsoleProgressBar() {
		this("unknown");
	}

	public void setCurrentProgressNum(int currentProgressNum) {
		if (0 == currentProgressNum % showPerNum || 0 == currentProgressNum || totalProgressNum == currentProgressNum) {
			this.show(currentProgressNum / (0.0 + totalProgressNum));
		}
	}

	public void show(double progeress) {
		java.math.BigDecimal bigDecimal = new java.math.BigDecimal(String.valueOf(progeress));
		bigDecimal = bigDecimal.movePointRight(2);
		int i = bigDecimal.setScale(0, java.math.RoundingMode.FLOOR).intValue();
		if (i < 0 || i > 100) {
			System.out.println("进度值不合法:progeress=" + progeress + " i=" + i);
			return;
		}
		System.out.print("\r");
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int j = 0; j < 50; j++) {
			sb.append(j < i / 2 ? "=" : (i / 2 == j ? ">" : " "));
		}
		System.out.print(sb.toString() + "]| " + bigDecimal.setScale(2, java.math.RoundingMode.FLOOR).toPlainString()
				+ "%   ");
		if (100 == i) {
			System.out.println("\n" + getTime() + "  [" + this.progressName + "] 任务完成");
			log.debug(getTime() + "  [" + this.progressName + "] 任务完成");
		}
	}

	private String getTime() {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		return df.format(new java.util.Date());
	}

	/**
	 * @return Returns the totalProgressNum.
	 */
	public int getTotalProgressNum() {
		return totalProgressNum;
	}

	/**
	 * @param totalProgressNum
	 *            The totalProgressNum to set.
	 */
	public void setTotalProgressNum(int totalProgressNum) {
		this.totalProgressNum = totalProgressNum;
	}

	/**
	 * @return Returns the showPerNum.
	 */
	public int getShowPerNum() {
		return showPerNum;
	}

	/**
	 * @param showPerNum
	 *            The showPerNum to set.
	 */
	public void setShowPerNum(int showPerNum) {
		this.showPerNum = showPerNum;
	}

	/**
	 * @return Returns the progressName.
	 */
	public String getProgressName() {
		return progressName;
	}

	/**
	 * @param progressName
	 *            The progressName to set.
	 */
	public void setProgressName(String progressName) {
		this.progressName = progressName;
	}
}
