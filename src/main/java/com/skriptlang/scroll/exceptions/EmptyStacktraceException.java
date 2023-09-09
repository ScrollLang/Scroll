package com.skriptlang.scroll.exceptions;

public class EmptyStacktraceException extends RuntimeException {

	private static final long serialVersionUID = -3191784010923143433L;

	public EmptyStacktraceException() {
		super(null, null, true, false);
	}

}
