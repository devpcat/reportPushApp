package com.dc.util;

@SuppressWarnings("serial")
public class MyFlowException extends MyException {

	public MyFlowException(int ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MyFlowException(String ErrorCode, String ErrorMessagr) {
		super(ErrorCode, ErrorMessagr);
	}

	public MyFlowException(String ErrorMessagr) {
		super(ErrorMessagr);
	}
}
