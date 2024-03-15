package com.scrolllang.scroll.commands;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.script.Script;

public class CommandManager {

	private static CommandRegistrar<?> serverCommandRegistrar = new ServerCommandRegistrar();
	private static final Multimap<Script, Command> commands = HashMultimap.create();
	private static CommandRegistrar<?> clientCommandRegistrar;

	static void setClientCommandInitalizer(CommandRegistrar<?> clientCommandRegistrar) throws IllegalAccessException {
		if (clientCommandRegistrar != null)
			throw new IllegalAccessException(Scroll.languageFormat("scroll.commands.api.client.registrar"));
		CommandManager.clientCommandRegistrar = clientCommandRegistrar;
	}

	static void setServerCommandInitalizer(CommandRegistrar<?> serverCommandRegistrar) throws IllegalAccessException {
		if (serverCommandRegistrar != null)
			throw new IllegalAccessException(Scroll.languageFormat("scroll.commands.api.server.registrar"));
		CommandManager.serverCommandRegistrar = serverCommandRegistrar;
	}

	/**
	 * Returns all the commands registered under a script.
	 * 
	 * @param script The {@link Script} to lookup all commands registered from.
	 * @return A collection containing all the {@link Command}s registered under a {@link Script}.
	 */
	public Collection<Command> getScriptCommands(Script script) {
		return commands.get(script);
	}

	/**
	 * Validate if the name or an aliases exists already.
	 * 
	 * @param input The string with ignoreCase on to compare against.
	 * @return true if there was already a command or aliases under the input name.
	 */
	public static boolean contains(String input) {
		for (Command command : commands.values()) {
			if (command.getName().equalsIgnoreCase(input))
				return true;
			if (command.getAliases().stream().anyMatch(input::equalsIgnoreCase))
				return true;
		}
		return false;
	}

	/**
	 * Validate if the name or an aliases exists already.
	 * 
	 * @param input The string with ignoreCase on to compare against.
	 * @return true if there was already a command or aliases under the input name.
	 */
	public static boolean contains(Command command) {
		if (contains(command.getName()))
			return true;
		if (command.getAliases().stream().anyMatch(CommandManager::contains))
			return true;
		if (commands.values().contains(command))
			return true;
		return false;
	}

	/**
	 * Register a command to Brigadier under Scroll.
	 * 
	 * @param command The {@link Command} object to register.
	 * @return false if the command already exists, true if successful.
	 */
	public static boolean register(Script script, Command command) {
		if (script == null)
			return false;
		if (contains(command))
			return false;
		if (command.isClientSided()) {
			if (clientCommandRegistrar == null)
				return false;
			clientCommandRegistrar.register(command);
		} else {
			serverCommandRegistrar.register(command);
		}
		commands.put(script, command);
		return true;
	}

	public static void unregisterAll(Script script) {
		for (Command command : commands.get(script)) {
			if (command.isClientSided()) {
				if (clientCommandRegistrar == null)
					continue;
				clientCommandRegistrar.unregister(command);
			} else {
				serverCommandRegistrar.unregister(command);
			}
		}
		commands.removeAll(script);
	}

	public static void unregisterAll() {
		for (Command command : commands.values()) {
			if (command.isClientSided()) {
				if (clientCommandRegistrar == null)
					continue;
				clientCommandRegistrar.unregister(command);
			} else {
				serverCommandRegistrar.unregister(command);
			}
		}
		commands.clear();
	}

}
