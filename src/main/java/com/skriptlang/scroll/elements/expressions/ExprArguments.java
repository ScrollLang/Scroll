package com.skriptlang.scroll.elements.expressions;

import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.commands.Command;
import com.skriptlang.scroll.commands.ScriptCommand;
import com.skriptlang.scroll.commands.ScriptCommand.ScrollCommandContext;
import com.skriptlang.scroll.commands.arguments.CommandArgument;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Examples;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.language.Languaged;
import com.skriptlang.scroll.utils.StringUtils;
import com.skriptlang.scroll.utils.collections.CollectionUtils;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;

@Name("Argument")
@Description({
	"Usable in script commands and command events. Holds the value of an argument given to the command, " +
	"e.g. if the command \"/tell &lt;player&gt; &lt;text&gt;\" is used like \"/tell Njol Hello Njol!\" argument 1 is the player named \"Njol\" and argument 2 is \"Hello Njol!\".",
	"One can also use the type of the argument instead of its index to address the argument, e.g. in the above example 'player-argument' is the same as 'argument 1'.",
	"Using the String identifier allows you to collect the argument with an identifier. &lt;example:player&gt; would be 'argument \"example\"'"
})
@Examples({
	"give the item-argument to the player-argument",
	"damage the player-argument by the number-argument",
	"give a diamond pickaxe to the argument",
	"add argument 1 to argument 2",
	"heal the last argument"
})
@Since("1.0.0")
public class ExprArguments implements Expression<Object>, Languaged {

	static {
		Scroll.getRegistration().addExpression(ExprArguments.class, Object.class, true,
				"[the] last arg[ument]", // LAST
				"[the] arg[ument](-| )<(\\d+)>", // ORDINAL
				"[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument][s]", // ORDINAL
				"[(all [[of] the]|the)] arg[ument][(1:s)]", // SINGLE OR ALL
				"[the] %=type%( |-)arg[ument][( |-)<\\d+>]", // TYPE
				"[the] arg[ument]( |-)%=type%[( |-)<\\d+>]", // TYPE
				"[the] arg[ument][s] [with id[entifier][s]] %strings%" // IDENTIFIER
		);
	}

	private static final int LAST = 0, ORDINAL = 1, SINGLE = 2, ALL = 3, TYPE = 4,  IDENTIFIER = 5;
	private Expression<String> identifier;
	private Expression<Type<?>> type;
	private int selected, ordinal;
	private Command command;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		Optional<Command> command = Optional.ofNullable(parseContext.getParserState().getCurrentSections().getLast())
				.filter(Trigger.class::isInstance)
				.map(Trigger.class::cast)
				.map(Trigger::getEvent)
				.filter(ScriptCommand.class::isInstance)
				.map(ScriptCommand.class::cast)
				.map(ScriptCommand::getCommand);
		if (!command.isPresent()) {
			error(parseContext, node("shared.context.incorrect", "arguments", "command"));
			return false;
		}

		this.command = command.get();
		if (this.command.getParameters().isEmpty()) {
			error(parseContext, node("syntaxes.exprarguments.empty", this.command.getName()));
			return false;
		}

		switch (matchedPattern) {
			case 0:
				selected = LAST;
				break;
			case 1:
			case 2:
				selected = ORDINAL;
				break;
			case 3:
				selected = parseContext.getNumericMark() == 1 ? ALL : SINGLE;
				break;
			case 4:
			case 5:
				selected = TYPE;
				type = (Expression<Type<?>>) expressions[0];
				break;
			case 6:
				selected = IDENTIFIER;
				identifier = (Expression<String>) expressions[0];
				break;
			default:
				assert false;
		}

		if ((selected == ORDINAL || selected == TYPE) && parseContext.getMatches().size() > 0) {
			MatchResult regex = parseContext.getMatches().get(0);
			String argMatch = null;
			for (int i = 1; i <= 4; i++) {
				argMatch = regex.group(i);
				if (argMatch != null)
					break; // Found format
			}
			assert argMatch != null;
			try {
				ordinal = Integer.parseInt(argMatch);
			} catch (NumberFormatException exception) {}
			int size = this.command.getParameters().size();
			if (ordinal > size) {
				error(parseContext, node("syntaxes.exprarguments.not.many", ordinal, size));
				return false;
			}
		}
		return true;
	}

	@Override
	public Object[] getValues(TriggerContext context) {
		if (!(context instanceof ScrollCommandContext))
			return new Object[0];
		List<CommandArgument<?>> arguments = command.getArguments();
		CommandArgument<?> argument = null;
		switch (selected) {
			case LAST:
				argument = arguments.get(arguments.size() - 1);
				break;
			case ORDINAL:
				argument = arguments.get(ordinal);
				break;
			case SINGLE:
				argument = arguments.get(0);
				break;
			case ALL:
				return arguments.stream()
						.map(CommandArgument::get)
						.toArray(Object[]::new);
			case TYPE:
				Optional<Class<?>> type = this.type.getSingle(context).map(Type::getTypeClass);
				if (!type.isPresent())
					return new Object[0];
				List<CommandArgument<?>> typeArguments = arguments.stream()
						.filter(arg -> arg.getTypeClass().equals(type.get()))
						.collect(Collectors.toList());
				argument = typeArguments.get(ordinal < 0 ? 0 : ordinal);
				break;
			case IDENTIFIER:
				Optional<? extends String> identifier = this.identifier.getSingle(context);
				if (!identifier.isPresent())
					return new Object[0];
				argument = arguments.stream()
						.filter(arg -> arg.getParameterInfo().getIdentifier().equalsIgnoreCase(identifier.get()))
						.findFirst()
						.orElse(null);
				break;
		}

		if (argument == null || !argument.isPresent())
			return new Object[0];
		return CollectionUtils.array(argument.get());
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		switch (selected) {
			case LAST:
				return "the last argument";
			case ORDINAL:
				return "the " + StringUtils.fancyOrderNumber(ordinal) + " argument";
			case SINGLE:
				return "the argument";
			case ALL:
				return "the arguments";
			case TYPE:
				Optional<? extends Type<?>> type = this.type.getSingle(context);
				if (!type.isPresent())
					return "type argument";
				return "the " + type.get().getBaseName() + " argument " + (ordinal != -1 ? ordinal : "");
			default:
				return "argument";
		}
	}

}
