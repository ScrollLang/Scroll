package org.scrolllang.scroll.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * Represents an event that comes from Fabric.
 */
public class ScrollContext implements TriggerContext {

	private final String name;

	public ScrollContext(String name) {
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}

}
