package org.scrolllang.scroll.log;

public abstract class CommonPrinter {

	protected final String[] messages;

	public CommonPrinter(String... messages) {
		this.messages = messages;
	}

}
