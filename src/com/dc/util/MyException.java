package com.dc.util;

@SuppressWarnings("serial")
public class MyException extends Exception {

	private String message;
	private int code;

	public MyException(int ErrorCode, String ErrorMessagr) {
		code = ErrorCode;
		message = ErrorMessagr;
	}

	public MyException(String ErrorCode, String ErrorMessagr) {
		try {
			code = Integer.parseInt(ErrorCode);
		} catch (Exception e) {
			code = Integer.MAX_VALUE;
		}
		message = ErrorMessagr;
	}

	public MyException(String ErrorMessagr) {
		this(Integer.MIN_VALUE, ErrorMessagr);
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		if (Integer.MIN_VALUE != code && code != Integer.MAX_VALUE) {
			return code + "#" + message;
		} else {
			return message;
		}
	}
}
