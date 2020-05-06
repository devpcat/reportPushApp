package com.dc.util;

@SuppressWarnings("serial")
public class MyOtherException extends MyException {

	public MyOtherException(int ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MyOtherException(String ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MyOtherException(String ErrorMessagr) {
		super(ErrorMessagr);
	}
}
