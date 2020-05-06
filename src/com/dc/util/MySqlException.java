package com.dc.util;

@SuppressWarnings("serial")
public class MySqlException extends MyException {

	public MySqlException(int ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MySqlException(String ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MySqlException(String ErrorMessagr) {
		super(ErrorMessagr);
	}
}
