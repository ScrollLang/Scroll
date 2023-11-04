package com.skriptlang.scroll.commands;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.ScrollScriptLoader;
import com.skriptlang.scroll.commands.arguments.CommandParameter;
import com.skriptlang.scroll.context.ScrollContext;
import com.skriptlang.scroll.exceptions.ScrollAPIException;
import com.skriptlang.scroll.language.Languaged;
import com.skriptlang.scroll.language.ScrollEvent;
import com.skriptlang.scroll.language.ScrollTriggerList;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.LiteralLoader;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ScriptCommand extends ScrollEvent implements Languaged {

	// Required Context triggers methods. Start.
	private final static ScrollTriggerList triggers = new ScrollTriggerList();

	public static List<Trigger> getTriggersList() {
		return triggers.getTriggers();
	}

	@Override
	public @NotNull ScrollTriggerList getTriggers() {
		return triggers;
	}
	// Required Context triggers methods. End.

	static {
		Scroll.addEvent("command", ScriptCommand.class, ScrollCommandContext.class, "*command [/]<.+>");
	}

	/**
	 * Represents the command at runtime. The raw CommandSource from the command library.
	 * 
	 * @param <T> Generic to be filled with either FabricClientCommandSource for client or ServerCommandSource for the correct environment.
	 */
	public static class ScrollCommandContext<T extends CommandSource> extends ScrollContext {

		private final CommandContext<T> context;
		private int returnCode = 0;

		public ScrollCommandContext(CommandContext<T> context) {
			super("command");
			this.context = context;
		}

		public CommandContext<T> getCommandContext() {
			return context;
		}

		/**
		 * Returns a code representing the result of the command.
		 * In Minecraft, the result can correspond to the power of a redstone comparator feeding from a command block
		 * or the value that will be passed to the chain command block the command block is facing.
		 * <p>
		 * Typically negative values mean a command has failed and will do nothing.
		 * Positive values mean the command was successful and did something.
		 * A result of 0 means the command has passed.
		 * 
		 * @param returnCode The return code of the command.
		 */
		public void setReturnCode(int returnCode) {
			this.returnCode = returnCode;
		}

		public int getReturnCode() {
			return returnCode;
		}

	}

	private final SectionConfiguration configuration = new SectionConfiguration.Builder()
			.addLoader(new LiteralLoader<Text>("permission message", Text.class, false, true))
			.addLoader(new LiteralLoader<Number>("permission", Number.class, false, true))
			.addLoader(new LiteralLoader<Boolean>("client", Boolean.class, false, true))
			.addLoader(new LiteralLoader<Text>("usage", Text.class, false, true))
			.addOptionalList("aliases")
			.addOptionalKey("usage")
			.addSection("trigger")
			.build();

	private static final Pattern ARGUMENTS_PATTERN = Pattern.compile("\\[?<.*?>]?");
	private final List<String> aliases = new ArrayList<>();
	private Text permissionMessage, usage;
	private boolean client;
	private int permission;

	@Override
	public List<Statement> loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		configuration.loadConfiguration(null, section, parserState, logger);
		Optional.ofNullable(configuration.getStringList("aliases")).ifPresent(list -> Arrays.stream(list).forEach(aliases -> this.aliases.add(aliases)));
		Optional.ofNullable(configuration.getValue("permission message", Text.class)).ifPresent(permissionMessage -> this.permissionMessage = permissionMessage);
		Optional.ofNullable(configuration.getValue("permission", Number.class)).ifPresent(permission -> this.permission = permission.intValue());
		Optional.ofNullable(configuration.getValue("client", Boolean.class)).ifPresent(client -> this.client = client);
		Optional.ofNullable(configuration.getValue("usage", Text.class)).ifPresent(usage -> this.usage = usage);
		return configuration.getSection("trigger").getItems();
	}

	private Command command;

	public Command getCommand() {
		return command;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		assert parseContext.getMatches().size() > 0;
		String[] split = parseContext.getMatches().get(0).group().split(" ", 2);
		String commandName = split[0];
		if (CommandManager.contains(commandName)) {
			error(parseContext, node("scripts.commands.register.exists", commandName));
			return false;
		}
		if (client && (permissionMessage != null || permission != -1)) {
			warning(parseContext, node("scripts.commands.register.client.permission", commandName));
			return false;
		}
		List<CommandParameter<?>> parameters = new ArrayList<>();
		if (split.length > 1) {
			String string = split[1];
			int leftCount = 0;
			int rightCount = 0;
			for (int i = 0; i < string.length(); i++) {
				if (string.charAt(0) == '<') {
					leftCount++;
				} else if (string.charAt(0) == '>') {
					rightCount++;
				}
			}
			if (leftCount != rightCount) {
				error(parseContext, node("scripts.commands.register.parameters.incorrect", string));
				return false;
			}
			try {
				Map<Type, Integer> occurances = new HashMap<>();
				ARGUMENTS_PATTERN.matcher(string).results()
						.map(MatchResult::group)
						.map(CommandParameter::parse)
						.forEach(parameter -> {
							parameters.stream()
									.map(CommandParameter::getPatternType)
									.filter(type -> {
										Class<?> typeClass = type.getType().getClass();
										occurances.put(typeClass, occurances.getOrDefault(typeClass, 0) + 1);
										// Negated
										if (!Lists.newArrayList(String.class, Text.class).contains(typeClass))
											return true;
										if (type.isSingle())
											return true;
										// We want to throw for String/Text of plural if there are more parameters to add after this.
										return false;
									})
									.findFirst()
									.orElseThrow(() -> new ScrollAPIException(Scroll.languageFormat("scripts.commands.register.parameters.strings.not.last", parameter.getPatternType().getType().getBaseName())));
							parameters.add(parameter);
						});
				// Apply an index for the type. Example making the identifier be string-1
				Map<Type, Integer> indices = new HashMap<>();
				for (CommandParameter<?> parameter : parameters) {
					if (parameter.getIdentifier() != null)
						continue;
					Class<?> typeClass = parameter.getPatternType().getType().getTypeClass();
					if (occurances.getOrDefault(typeClass, 0) <= 1) // Don't apply an index.
						continue;
					for (CommandParameter<?> parameter2 : parameters) {
						if (typeClass.equals(parameter2.getPatternType().getType().getTypeClass())) {
							int index = indices.getOrDefault(typeClass, -1) + 1;
							indices.put(typeClass, index);
							parameter.setIndex(index);
							break;
						}
					}
				}
			} catch (ScrollAPIException error) {
				error(parseContext, error.getMessage());
				return false;
			}
		}
		this.command = new Command(commandName, parameters, aliases, client, permission, permissionMessage, usage);
		if (!CommandManager.register(ScrollScriptLoader.getCurrentlyLoadingScript(), command)) {
			error(parseContext, node("scripts.commands.register.failed", commandName));
			return false;
		}
		return true;
	}

	@Override
	public boolean check(TriggerContext context) {
		if (!(context instanceof ScrollCommandContext))
			return false;
		ScrollCommandContext<?> scrollCommandContext = (ScrollCommandContext<?>) context;
		CommandContext<?> commandContext = scrollCommandContext.getCommandContext();
		for (CommandParameter<?> argument : command.getParameters()) {
			if (!argument.isOptional()) { // Check for required arguments.
				try {
					commandContext.getArgument(argument.getIdentifier(), Text.class);
				} catch (IllegalArgumentException incorrect) {
					Object source = commandContext.getSource();
					if (source instanceof FabricClientCommandSource clientSource) {
						clientSource.sendFeedback(command.getUsage());
						return false;
					} else if (source instanceof ServerCommandSource serverSource) {
						serverSource.sendFeedback(() -> command.getUsage(), false);
						return false;
					}
				}
			}
		}
		return scrollCommandContext.getReturnCode() >= 0;
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		if (!(context instanceof ScrollCommandContext))
			return command.toString();
		return command.toString((ScrollCommandContext<?>) context, debug);
	}

}
