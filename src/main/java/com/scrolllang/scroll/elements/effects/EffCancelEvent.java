package com.scrolllang.scroll.elements.effects;

import java.util.Set;

import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.context.CancellableContext;
import com.scrolllang.scroll.documentation.annotations.Description;
import com.scrolllang.scroll.documentation.annotations.Examples;
import com.scrolllang.scroll.documentation.annotations.Name;
import com.scrolllang.scroll.documentation.annotations.Since;
import com.scrolllang.scroll.language.Languaged;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

@Name("Cancel Event")
@Description("Cancel an event. This means preventing an event from happening.")
@Examples({
	"on left click on a block:",
		"\tcancel the event"
})
@Since("1.0.0")
public class EffCancelEvent extends Effect implements Languaged {

	static {
		Scroll.getRegistration().addEffect(EffCancelEvent.class, "cancel [the] event", "uncancel [the] event");
	}

	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		cancel = matchedPattern == 0;
		Set<Class<? extends TriggerContext>> contexts = parseContext.getParserState().getCurrentContexts();
		if (contexts == null)
			return false;
		for (Class<? extends TriggerContext> context : contexts) {
			if (!CancellableContext.class.isAssignableFrom(context)) {
				error(parseContext, node("syntaxes.effcancelevent.cannot", getEventName(parseContext)));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void execute(TriggerContext context) {
		if (!(context instanceof CancellableContext))
			return;
		((CancellableContext) context).setCancelled(cancel);
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return (cancel ? "" : "un") + "cancel event";
	}

}
