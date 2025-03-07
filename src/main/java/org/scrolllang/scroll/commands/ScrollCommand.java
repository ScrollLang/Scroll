package org.scrolllang.scroll.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.ScrollLoader;
import org.scrolllang.scroll.language.Languaged;
import org.scrolllang.scroll.script.Script;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;

/**
 * The main command class for handling the /scroll command.
 */
public class ScrollCommand implements Languaged {

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
						.requires(source -> environment != RegistrationEnvironment.DEDICATED || source.hasPermissionLevel(PERMISSION_LEVEL) || !source.isExecutedByPlayer())
						.executes(ScrollCommand::noArguments)
						.then(literal("reload")
							.then(argument("file", word())
								.suggests(RELOAD_SUGGESTS)
								.executes(context -> {
									String argument = context.getArgument("file", String.class);
									switch (argument) {
										case "configuration":
											if (Scroll.CONFIGURATION.reload()) {
												context.getSource().sendMessage(Scroll.adventure("configuration.reload.success"));
											} else {
												context.getSource().sendMessage(Scroll.adventure("scroll.reload.failed", "configuration.toml"));
											}
											break;
										case "languages":
											if (Scroll.LANGUAGE.reload()) {
												context.getSource().sendMessage(Scroll.adventure("language.reload.success"));
											} else {
												context.getSource().sendMessage(Scroll.adventure("scroll.reload.failed", Scroll.LANGUAGE.getLanguage() + ".properties"));
											}
											break;
										default:
											Optional<Script> script = ScrollLoader.getScriptByName(argument);
											try {
												if (!script.isPresent()) {
													context.getSource().sendMessage(Scroll.adventure("scripts.doesnt.exist", argument));
												} else if (ScrollLoader.reloadScript(script.get()).isPresent()) {
													context.getSource().sendMessage(Scroll.adventure("scripts.reload.success", argument));
												} else {
													context.getSource().sendMessage(Scroll.adventure("scripts.reload.failed", argument));
												}
											} catch (Exception e) {
												Scroll.LOGGER.info("There was an error");
												Scroll.getInstance().printException(e);
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
									Optional<Script> script = ScrollLoader.getScriptByName(argument);
									if (!script.isPresent()) {
										context.getSource().sendMessage(Scroll.adventure("scripts.doesnt.exist", argument));
									} else {
										ScrollLoader.disableScript(script.get());
										context.getSource().sendMessage(Scroll.adventure("scripts.disable.success", argument));
									}
									return 0;
								})))
						.then(literal("enable")
								.then(argument("file", word())
									.suggests(ENABLE_SUGGESTS)
									.executes(context -> {
										String argument = context.getArgument("file", String.class);
										if (!argument.startsWith(ScrollLoader.DISABLED_PREFIX))
											argument = ScrollLoader.DISABLED_PREFIX + argument;
										if (!argument.endsWith(ScrollLoader.EXTENSION))
											argument = argument + ScrollLoader.EXTENSION;
										if (!ScrollLoader.enableScriptAt(ScrollLoader.getScriptsFolder().resolve(argument)).isPresent()) {
											context.getSource().sendMessage(Scroll.adventure("scripts.doesnt.exist", argument));
										} else {
											context.getSource().sendMessage(Scroll.adventure("scripts.enable.success", argument));
										}
										return 0;
									})))
						);
				dispatcher.register(literal("scroll").executes(ScrollCommand::noArguments).redirect(scroll));
			});
		}
	}

	/**
	 * Method that is called when no arguments are given.
	 * This exists because the redirect does no respect no arguments of the redirected.
	 * 
	 * @param context The context used from the command.
	 * @return error code
	 */
	private static int noArguments(CommandContext<ServerCommandSource> context) {
		context.getSource().sendMessage(Scroll.adventure("scroll.command.no.arguments", Scroll.MOD_CONTAINER.getMetadata().getVersion()));
		return 0;
	}

	private static Stream<String> collectDisabledScripts() {
		Stream<String> stream = ScrollLoader.collectScriptsAt(ScrollLoader.getScriptsFolder(), path -> path.getFileName().toString().startsWith(ScrollLoader.DISABLED_PREFIX))
				.map(Path::getFileName)
				.map(Path::toString);
		if (REMOVE_DISABLED_PREFIX)
			stream = stream
					.filter(name -> name.startsWith(ScrollLoader.DISABLED_PREFIX))
					.map(name -> name.substring(0, name.lastIndexOf(ScrollLoader.DISABLED_PREFIX)));
		if (HIDE_EXTENSIONS)
			return stream.map(name -> name.substring(0, name.lastIndexOf(ScrollLoader.EXTENSION)));
		return stream;
	}

	private static Stream<String> collectScripts() {
		if (HIDE_EXTENSIONS)
			return ScrollLoader.getLoadedScripts().stream().map(Script::getSimpleName);
		return ScrollLoader.getLoadedScripts().stream().map(Script::getFileName);
	}

	private static Stream<String> collectReload() {
		return Stream.concat(collectScripts(), Stream.of("configuration", "languages"));
	}

}
