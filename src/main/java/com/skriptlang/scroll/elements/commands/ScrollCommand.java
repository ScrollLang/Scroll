package com.skriptlang.scroll.elements.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.ScrollScriptLoader;
import com.skriptlang.scroll.script.Script;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * The main command class for handling the /scroll command.
 */
public class ScrollCommand {

	private static final boolean REMOVE_DISABLED_PREFIX = Scroll.CONFIGURATION.getCommandSection().getBoolean("scroll.commands.remove-disabled-prefix", () -> false);
	private static final boolean HIDE_EXTENSIONS = Scroll.CONFIGURATION.getCommandSection().getBoolean("scroll.commands.hide-extensions", () -> true);
	private static final int PERMISSION_LEVEL = (int) Scroll.CONFIGURATION.getCommandSection().getLong("scroll.commands.permission-level", () -> 4);

	private static final SuggestionProvider<ServerCommandSource> RELOAD_SUGGESTS = new SuggestionProvider<ServerCommandSource>() {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return CommandSource.suggestMatching(collectReload(), builder);
		}
	};

	private static final SuggestionProvider<ServerCommandSource> DISABLED_SUGGESTS = new SuggestionProvider<ServerCommandSource>() {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return CommandSource.suggestMatching(collectScripts(), builder);
		}
	};

	private static final SuggestionProvider<ServerCommandSource> ENABLE_SUGGESTS = new SuggestionProvider<ServerCommandSource>() {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			return CommandSource.suggestMatching(collectDisabledScripts(), builder);
		}
	};

	static {
		if (Scroll.CONFIGURATION.getCommandSection().getBoolean("scroll.commands.enabled", () -> true)) {
			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				final LiteralCommandNode<ServerCommandSource> scroll = dispatcher.register(literal("sc")
						.requires(source -> !environment.dedicated || source.hasPermissionLevel(PERMISSION_LEVEL))
						.then(literal("reload")
							.then(argument("file", word())
								.suggests(RELOAD_SUGGESTS)
								.executes(context -> {
									String argument = context.getArgument("file", String.class);
									switch (argument) {
										case "configuration":
											if (Scroll.CONFIGURATION.reload()) {
												context.getSource().sendMessage(Text.literal(Scroll.language("configuration.reload.success")));
											} else {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scroll.reload.failed", "configuration.toml")));
											}
											break;
										case "languages":
											if (Scroll.LANGUAGE.reload()) {
												context.getSource().sendMessage(Text.literal(Scroll.language("language.reload.success")));
											} else {
												context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scroll.reload.failed", Scroll.LANGUAGE.getLanguage() + ".properties")));
											}
											break;
										default:
											Optional<Script> script = ScrollScriptLoader.getScriptByName(argument);
											try {
												if (!script.isPresent()) {
													context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.doesnt.exist", argument)));
												} else if (ScrollScriptLoader.reloadScript(script.get()).isPresent()) {
													context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.reload.success", argument)));
												} else {
													context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.reload.failed", argument)));
												}
											} catch (Exception e) {
												Scroll.LOGGER.info("There was an error");
												Scroll.printException(e, "wat");
											}
											break;
									}
									return 0;
								})))
						.then(literal("disable")
							.then(argument("file", word())
								.suggests(DISABLED_SUGGESTS)
								.executes(context -> {
									String argument = context.getArgument("file", String.class);
									Optional<Script> script = ScrollScriptLoader.getScriptByName(argument);
									if (!script.isPresent()) {
										context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.doesnt.exist", argument)));
									} else {
										ScrollScriptLoader.disableScript(script.get());
										context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.disable.success", argument)));
									}
									return 0;
								})))
						.then(literal("enable")
								.then(argument("file", word())
									.suggests(ENABLE_SUGGESTS)
									.executes(context -> {
										String argument = context.getArgument("file", String.class);
										if (!argument.startsWith(ScrollScriptLoader.DISABLED_PREFIX))
											argument = ScrollScriptLoader.DISABLED_PREFIX + argument;
										if (!argument.endsWith(ScrollScriptLoader.EXTENSION))
											argument = argument + ScrollScriptLoader.EXTENSION;
										if (!ScrollScriptLoader.enableScriptAt(ScrollScriptLoader.getScriptsFolder().resolve(argument)).isPresent()) {
											context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.doesnt.exist", argument)));
										} else {
											context.getSource().sendMessage(Text.literal(Scroll.languageFormat("scripts.enable.success", argument)));
										}
										return 0;
									})))
						.executes(context -> { // No arguments
							context.getSource().sendMessage(Text.literal("No arguments TODO"));
							return 0;
						}));
				dispatcher.register(literal("scroll").redirect(scroll));
			});
		}
	}

	private static Stream<String> collectDisabledScripts() {
		Stream<String> stream = ScrollScriptLoader.collectScriptsAt(ScrollScriptLoader.getScriptsFolder(), true)
				.map(Path::getFileName)
				.map(Path::toString);
		if (REMOVE_DISABLED_PREFIX)
			stream = stream
					.filter(name -> name.startsWith(ScrollScriptLoader.DISABLED_PREFIX))
					.map(name -> name.substring(0, name.lastIndexOf(ScrollScriptLoader.DISABLED_PREFIX)));
		if (HIDE_EXTENSIONS)
			return stream.map(name -> name.substring(0, name.lastIndexOf(ScrollScriptLoader.EXTENSION)));
		return stream;
	}

	private static Stream<String> collectScripts() {
		if (HIDE_EXTENSIONS)
			return ScrollScriptLoader.getLoadedScripts().stream().map(Script::getSimpleName);
		return ScrollScriptLoader.getLoadedScripts().stream().map(Script::getFileName);
	}

	private static Stream<String> collectReload() {
		return Stream.concat(collectScripts(), Stream.of("configuration", "languages"));
	}

}
