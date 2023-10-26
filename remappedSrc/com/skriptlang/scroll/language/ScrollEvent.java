package com.skriptlang.scroll.language;

import org.jetbrains.annotations.NotNull;

import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

public abstract class ScrollEvent extends SkriptEvent {

	/**
	 * Every ScrollEvent must return a defined ScrollTriggerList object.
	 * 
	 * @return ScrollTriggerList which was defined in the implementing ScrollEvent.
	 */
	// This method mainly exists to force the implementing class to keep a record of triggers properly.
	@NotNull
    public abstract ScrollTriggerList getTriggers();

	/**
	 * Shortcut for executing a context to all triggers of the ScrollEvent.
	 * @param <C> A context class that extends TriggerContext.
	 * 
	 * @param triggers The ScrollTriggerList belonging to the ScrollEvent.
	 * @param context The TriggerContext to apply to the triggers.
	 */
	public static <C extends TriggerContext> void runTriggers(ScrollTriggerList triggers, C context) {
		for (Trigger trigger : triggers.getTriggers())
			Statement.runAll(trigger, context);
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return context.getName();
	}

}
