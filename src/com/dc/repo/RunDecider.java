package com.dc.repo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class RunDecider {

	private final static Logger log = Logger.getLogger(RunDecider.class);

	//test
	public static void main(String[] args) {
		System.out.println(decide("week@6,1,2"));
	}

	/**
	 * true-调起来 false-放弃调用
	 *
	 * @param inputStr
	 * @return
	 */
	public static boolean decide(String criStr) {
		log.info("判决参数[" + criStr + "]");
		boolean res = decide_inner(criStr);
		if (res) {
			log.info("判决结果[Run Right Now]");
		} else {
			log.info("判决结果[Give Up Running]");
		}
		return res;
	}

	public static boolean decide_inner(String criStr) {

		if (null == criStr)
			return false;
		criStr = criStr.trim();
		if ("true".equalsIgnoreCase(criStr)) {
			return true;
		}
		if ("false".equalsIgnoreCase(criStr))
			return false;
		String[] criArr = criStr.split("@");
		if (criArr.length != 2 || "".equalsIgnoreCase(criStr))
			return false;

		// day,week
		if ("day".equalsIgnoreCase(criArr[0]))
			return decide_day(criArr[1]);
		if ("week".equalsIgnoreCase(criArr[0]))
			return decide_week(criArr[1]);

		return false;
	}

	private static boolean decide_day(String instr) {
		String currday;
		if (instr.startsWith("00-")) {
			SimpleDateFormat df = new SimpleDateFormat("dd");
			currday = df.format(new Date());
			instr = instr.replace("00-", "");
		} else {
			SimpleDateFormat df = new SimpleDateFormat("MM-dd");
			currday = df.format(new Date());
		}

		String strarr[] = instr.split(",");
		for (String elem : strarr) {
			if (currday.equalsIgnoreCase(elem)) {
				return true;
			}
		}
		return false;
	}

	private static boolean decide_week(String instr) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		String strarr[] = instr.split(",");
		for (String elem : strarr) {
			if (Integer.toString(w).equalsIgnoreCase(elem)) {
				return true;
			}
		}
		return false;
	}
}
