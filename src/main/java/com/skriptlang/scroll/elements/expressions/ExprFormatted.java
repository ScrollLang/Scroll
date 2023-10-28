package com.skriptlang.scroll.elements.expressions;

import java.util.Objects;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Examples;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.language.Languaged;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.Text;

@Name("Formatted String")
@Description("Format a string to a Minecraft Text value.")
@Examples("broadcast formatted \"<lime green>Hello!</lime green>\"")
@Since("1.0.0")
public class ExprFormatted implements Expression<Text>, Languaged {

	static {
		Scroll.getRegistration().addExpression(ExprFormatted.class, Text.class, false, "formatted", "strings");
	}

	private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private final FabricAudiences ADVENTURE = Scroll.getAdventure();
	private Expression<String> strings;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		strings = (Expression<String>) expressions[0];
		return true;
	}

	@Override
	public Text[] getValues(TriggerContext context) {
		return strings.stream(context)
				.filter(Objects::nonNull)
				.map(MINI_MESSAGE::deserialize)
				.map(ADVENTURE::toNative)
				.toArray(Text[]::new);
	}

	@Override
	public boolean isSingle() {
		return strings.isSingle();
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return "formatted " + strings.toString(context, debug);
	}

}
