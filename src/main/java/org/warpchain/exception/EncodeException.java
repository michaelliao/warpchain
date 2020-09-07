package org.warpchain.exception;

public class EncodeException extends BaseException {

	public EncodeException() {
	}

	public EncodeException(String message) {
		super(message);
	}

	public EncodeException(Throwable cause) {
		super(cause);
	}

	public EncodeException(String message, Throwable cause) {
		super(message, cause);
	}
}
