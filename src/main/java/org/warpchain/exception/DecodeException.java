package org.warpchain.exception;

public class DecodeException extends BaseException {

	public DecodeException() {
	}

	public DecodeException(String message) {
		super(message);
	}

	public DecodeException(Throwable cause) {
		super(cause);
	}

	public DecodeException(String message, Throwable cause) {
		super(message, cause);
	}
}
