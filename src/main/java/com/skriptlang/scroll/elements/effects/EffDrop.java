package com.skriptlang.scroll.elements.effects;

import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Examples;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

@Name("Drop")
@Description("Drops one or more items.")
@Examples({
	"on death of creeper:",
		"\tdrop 1 TNT"
})
@Since("1.0.0")
public class EffDrop extends Effect {

	static {
		//if (Scroll.isServerEnvironment())
			//Scroll.getRegistration().addEffect(EffDrop.class, "drop %itemtypes/experiences% [%directions% %locations%] [(1¦without velocity)]");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		return false;
	}

	@Override
	protected void execute(TriggerContext context) {
		
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return null;
	}

}
