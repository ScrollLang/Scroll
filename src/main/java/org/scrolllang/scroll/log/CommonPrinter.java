package org.scrolllang.scroll.log;

public abstract class CommonPrinter {

	protected static final String REPORT_URL = "https://github.com/SkriptLang/Scroll/issues";
	protected final String[] messages;

	public CommonPrinter(String... messages) {
		this.messages = messages;
	}

}
