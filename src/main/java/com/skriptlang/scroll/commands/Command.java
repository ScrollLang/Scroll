package com.skriptlang.scroll.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.context.CommandContext;
import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.commands.ScriptCommand.ScrollCommandContext;
import com.skriptlang.scroll.commands.arguments.CommandArgument;
import com.skriptlang.scroll.commands.arguments.CommandParameter;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class Command {

	private static final Text DEFAULT_PERMISSION_MESSAGE = Scroll.getAdventure().toNative(MiniMessage.miniMessage().deserialize(Scroll.language("scripts.commands.no.permissions")));

	private final List<CommandParameter<?>> parameters = new ArrayList<>();
	private final List<CommandArgument<?>> arguments = new ArrayList<>();
	private final List<String> aliases = new ArrayList<>();

	private final Text permissionMessage;
	private final boolean client;
	private final int permission;
	private final String name;
	private final Text usage;

	private boolean filled;

	public Command(String name, List<CommandParameter<?>> parameters, List<String> aliases, boolean client, int permission, @Nullable Text permissionMessage, @Nullable Text usage) {
		this.permissionMessage = permissionMessage;
		this.parameters.addAll(parameters);
		this.permission = permission;
		this.aliases.addAll(aliases);
		this.client = client;
		this.usage = usage;
		this.name = name;
	}

	/**
	 * @return The {@link CommandParameter}s of this command.
	 */
	@NotNull
	public List<CommandParameter<?>> getParameters() {
		return parameters;
	}

	/**
	 * Arguments are only set when this command has been ran, and this command object is updated.
	 * Use {@link isFilled} to verify if the arguments has been filled for this command.
	 * 
	 * @return The arguments if this method is called during runtime, aka the arguments are set.
	 */
	@NotNull
	public List<CommandArgument<?>> getArguments() {
		return arguments;
	}

	/**
	 * @return The aliases for this command.
	 */
	@NotNull
	public List<String> getAliases() {
		return aliases;
	}

	/**
	 * Return the message to be sent to the command source when they do not pass the permission requirements.
	 * Will default to a language configuration option if no permission message was defined.
	 * 
	 * @return The message to be sent to the command source.
	 */
	@NotNull
	public Text getPermissionMessage() {
		return Optional.ofNullable(permissionMessage).orElse(DEFAULT_PERMISSION_MESSAGE);
	}

	public boolean isClientSided() {
		return client;
	}

	/**
	 * @return the permission level of this command.
	 */
	public int getPermission() {
		return permission;
	}

	/**
	 * @return true if the arguments of this command have been filled.
	 */
	public boolean isFilled() {
		return filled;
	}

	@Nullable
	public Text getUsage() {
		return usage;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<T extends CommandSource> void fill(ScrollCommandContext<T> context) {
		CommandContext<T> commandContext = context.getCommandContext();
		for (CommandParameter<?> parameter : parameters) {
			Text value = null;
			try {
				value = commandContext.getArgument(parameter.getIdentifier(), Text.class);
			} catch (Exception ignored) {}
			arguments.add(new CommandArgument(parameter, value));
		}
		this.filled = true;
	}

	@Override
	public String toString() {
		return "command /" + name;
	}

	public String toString(ScrollCommandContext<?> context, boolean debug) {
		// TODO
		return toString();
	}

}
