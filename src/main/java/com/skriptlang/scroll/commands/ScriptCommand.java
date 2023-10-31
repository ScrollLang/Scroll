package com.skriptlang.scroll.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.ScrollScriptLoader;
import com.skriptlang.scroll.context.ScrollContext;
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
import net.minecraft.command.CommandSource;
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
		Scroll.addEvent("command", ScriptCommand.class, CommandContext.class, "*command [/]<.+>");
	}

	public static class CommandContext extends ScrollContext {

		private final CommandSource source;
		private int returnCode = 0;

		public CommandContext(CommandSource source) {
			super("command");
			this.source = source;
		}

		public CommandSource getCommandSource() {
			return source;
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
			.addOptionalList("aliases")
			.addOptionalKey("usage")
			.addSection("trigger")
			.build();

	private final List<String> aliases = new ArrayList<>();
	private Text permissionMessage;
	private boolean client;
	private int permission;

	@Override
	public List<Statement> loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		configuration.loadConfiguration(null, section, parserState, logger);
		Optional.ofNullable(configuration.getStringList("aliases")).ifPresent(list -> Arrays.stream(list).forEach(aliases -> this.aliases.add(aliases)));
		Optional.ofNullable(configuration.getValue("permission message", Text.class)).ifPresent(permissionMessage -> this.permissionMessage = permissionMessage);
		Optional.ofNullable(configuration.getValue("permission", Number.class)).ifPresent(permission -> this.permission = permission.intValue());
		Optional.ofNullable(configuration.getValue("client", Boolean.class)).ifPresent(client -> this.client = client);
		return configuration.getSection("trigger").getItems();
	}

	private Command command;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		assert parseContext.getMatches().size() > 0;
		String[] split = parseContext.getMatches().get(0).group().split(" ", 2);
		String commandName = split[0];
		if (CommandManager.contains(commandName)) {
			error(parseContext, node("scripts.commands.register.exists", commandName));
			return false;
		}
		if (split.length > 1 && !parseArguments(split[1])) {
			// TODO Error per argument
			error(parseContext, node("scripts.commands.register.failed", split[1]));
			return false;
		}
		this.command = new Command(commandName, aliases, client, permission, permissionMessage);
		if (!CommandManager.register(ScrollScriptLoader.getCurrentlyLoadingScript(), command)) {
			error(parseContext, node("scripts.commands.register.failed", commandName));
			return false;
		}
		return true;
	}

	private boolean parseArguments(String argument) {
		//TODO 
		return true;
	}

	@Override
	public boolean check(TriggerContext context) {
		if (!(context instanceof CommandContext))
			return false;
		CommandContext commandContext = (CommandContext) context;
		return commandContext.getReturnCode() >= 0;
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		if (!(context instanceof CommandContext))
			return command.toString();
		return command.toString((CommandContext) context, debug);
	}

}
