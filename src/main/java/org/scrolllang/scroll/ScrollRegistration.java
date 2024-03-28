package org.scrolllang.scroll;

import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class ScrollRegistration extends SkriptRegistration {

	private final ScrollAddon registerer;

	public ScrollRegistration(ScrollAddon registerer) {
		super(Scroll.getInstance());
		this.registerer = registerer;
	}

	@Override
	public SkriptAddon getRegisterer() {
		throw new UnsupportedOperationException();
	}

	public ScrollAddon getAddonRegisterer() {
		return registerer;
	}

	/**
	 * Must use {@link Scroll#newEvent(String, Class, Class, String...)}
	 */
	@Override
	public <E extends SkriptEvent> EventRegistrar<E> newEvent(Class<E> c, String... patterns) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Must use {@link Scroll#newEvent(String, Class, Class, String...)}
	 */
	@Override
	public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, String... patterns) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Must use {@link Scroll#newEvent(String, Class, Class, String...)}
	 */
	@Override
	public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, int priority, String... patterns) {
		throw new UnsupportedOperationException();
	}

}
